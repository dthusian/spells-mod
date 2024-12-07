package dev.wateralt.mc.weapontroll.asm;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.function.Consumer;

public class Executor {
  
  private static void incorrectTypes(Consumer<String> throwErr, Object... args) {
    StringBuilder sb = new StringBuilder();
    sb.append("Incorrect type [");
    for(int i = 0; i < args.length; i++) {
      if(i != 0) {
        sb.append(", ");
      }
      if(Objects.isNull(args[i])) {
        sb.append("<null>");
      } else {
        sb.append(args[i].getClass().getSimpleName());
      }
    }
    sb.append("]");
    throwErr.accept(sb.toString());
  }
  
  private static boolean iHateJava(Class<?> a, Class<?> b) {
    if(a.isAssignableFrom(b)) {
      return true;
    }
    if(a.isPrimitive() && a.getSimpleName().equalsIgnoreCase(b.getSimpleName())) {
      return true;
    }
    return false;
  }
  
  public static Object[] execute(Program program, LivingEntity user, LivingEntity target, ServerWorld world, int instructionLimit) {
    int pc = 0;
    List<Instructions.Instr> instrs = program.getInstrs();
    HashMap<String, Integer> labels = program.getLabels();
    Object[] slots = new Object[program.getMaxSlots()];
    slots[0] = user;
    slots[1] = target;
    
    for(int i = 0; i < instructionLimit; i++) {
      if(pc == instrs.size()) {
        return slots;
      }
      int finalPc = pc;
      Consumer<String> throwErr = s -> { 
        throw new AsmError("Runtime error at pc %d: %s".formatted(finalPc, s));
      };
      
      Instructions.Instr instr = program.getInstrs().get(pc);
      
      switch(instr) {
        case Instructions.JumpIfEqual x -> {
          Object a = slots[x.slotA()];
          Object b = slots[x.slotB()];
          if(a instanceof Double da && b instanceof Double db) {
            if(da.equals(db)) {
              pc = labels.get(x.label());
            }
          } else {
            Executor.incorrectTypes(throwErr, a, b);
          }
        }
        case Instructions.JumpIfLess x -> {
          Object a = slots[x.slotA()];
          Object b = slots[x.slotB()];
          if(a instanceof Double da && b instanceof Double db) {
            if(da < db) {
              pc = labels.get(x.label());
            }
          } else {
            Executor.incorrectTypes(throwErr, a, b);
          }
        }
        case Instructions.Label x -> {}
        case Instructions.SplitVec x -> {
          Object o = slots[x.slotSrc()];
          if(o instanceof Vec3d vo) {
            slots[x.slotX()] = vo.getX();
            slots[x.slotY()] = vo.getY();
            slots[x.slotZ()] = vo.getZ();
          } else {
            Executor.incorrectTypes(throwErr, o);
          }
        }
        
        default -> {
          // read record components
          RecordComponent[] components = instr.getClass().getRecordComponents();
          Object[] componentValues = Arrays.stream(components).map(v -> {
            try {
              return v.getAccessor().invoke(instr);
            } catch (Exception err) {
              throw new RuntimeException(err);
            }
          }).toArray();
          
          // read exec parameters
          Method exec = Arrays.stream(instr.getClass().getDeclaredMethods())
            .filter(v -> v.getName().equals("exec"))
            .findFirst()
            .get();
          int nParams;
          int paramOffset;
          if(exec.getReturnType() == void.class) {
            nParams = components.length;
            paramOffset = 0;
          } else {
            nParams = components.length - 1;
            paramOffset = 1;
          }
          Object[] execParams = new Object[nParams + 1];
          execParams[0] = world;
          
          Class<?>[] execTypes = exec.getParameterTypes();
          assert execTypes[0] == ServerWorld.class;

          // typecheck and load args into execParams
          boolean wrongTypes = false; 
          for(int j = 0; j < nParams; j++) {
            if(componentValues[j + paramOffset].getClass() == Short.class) {
              execParams[j + 1] = slots[(Short)componentValues[j + paramOffset]];
            } else {
              execParams[j + 1] = componentValues[j + paramOffset];
            }
            if(!iHateJava(execTypes[j + 1], execParams[j + 1].getClass())) {
              wrongTypes = true;
            }
          }
          if(wrongTypes) {
            Executor.incorrectTypes(throwErr, Arrays.copyOfRange(execParams, 1, execParams.length));
          }
          
          // execute and writeback
          Object ret;
          try {
            ret = exec.invoke(instr, execParams);
          } catch(Exception err) {
            throw new RuntimeException(err);
          }
          if(exec.getReturnType() != void.class) {
            slots[(Short)componentValues[0]] = ret;
          }
        }
      }
      
      pc++;
    }
    
    return slots;
  }
}
