package dev.wateralt.mc.weapontroll.asm;

import dev.wateralt.mc.weapontroll.Weapontroll;
import dev.wateralt.mc.weapontroll.playertracker.TrackedPlayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.function.Consumer;

public class Executor {
  public static class ExecutionContext {
    private Vec3d origin;
    private ServerWorld world;
    @Nullable
    private ServerPlayerEntity manaVessel;
    private int manaDeficit;
    
    public ExecutionContext(Vec3d origin, ServerWorld world, @Nullable ServerPlayerEntity manaVessel) {
      this.origin = origin;
      this.world = world;
      this.manaVessel = manaVessel;
      this.manaDeficit = 0;
    }
    
    public ServerWorld world() {
      return world;
    }
    
    public void useEnergy(double amount) {
      if(manaVessel != null) {
        boolean stop = false;
        TrackedPlayer pl = Weapontroll.PLAYER_TRACKER.get(manaVessel);
        int newEnergy = pl.getEnergy() - (int)Math.ceil(amount);
        if(newEnergy < 0) {
          manaDeficit += -newEnergy;
          newEnergy = 0;
          if(manaDeficit * EnergyCosts.HP_PER_ENERGY_DEPLETED >= manaVessel.getMaxHealth() && !manaVessel.isCreative()) {
            manaVessel.kill(world);
            stop = true;
          }
        }
        pl.setEnergy(newEnergy);
        if(stop) {
          throw new AsmError("Out of energy!");
        }
      }
    }
    
    public void useEnergyAt(Vec3d pos, double amount) {
      double dist = origin.distanceTo(pos);
      double factor;
      if(dist <= EnergyCosts.FREE_RADIUS) {
        factor = 1;
      } else {
        factor = 1 + 0.001 * Math.pow(dist - EnergyCosts.FREE_RADIUS, 2);
      }
      useEnergy(factor * amount);
    }
    
    public Vec3d origin() {
      return origin;
    }
    
    public ServerPlayerEntity playerUser() {
      return manaVessel;
    }
  }
  
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
  
  private static boolean iHateJava(Class<?> a, Object b) {
    if(b == null) {
      return false;
    }
    Class<?> cb = b.getClass();
    if(a.isAssignableFrom(cb)) {
      return true;
    }
    if(a.isPrimitive() && a.getSimpleName().equalsIgnoreCase(cb.getSimpleName())) {
      return true;
    }
    return false;
  }
  
  public static Object[] execute(Program program, @Nullable LivingEntity user, @Nullable LivingEntity target, ServerWorld world, int instructionLimit) {
    int pc = 0;
    List<Instructions.Instr> instrs = program.getInstrs();
    HashMap<String, Integer> labels = program.getLabels();
    Object[] slots = new Object[program.getMaxSlots()];
    slots[0] = user;
    slots[1] = target;
    ExecutionContext ctx;
    if(user instanceof ServerPlayerEntity spl) {
      ctx = new ExecutionContext(user.getPos(), world, spl);
    } else if(user != null) {
      ctx = new ExecutionContext(user.getPos(), world, null);
    } else {
      ctx = new ExecutionContext(Vec3d.ZERO, world, null);
    }
    
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
          if(Objects.equals(a, b)) {
            pc = labels.get(x.label());
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
          // duck-type the instruction
          int nParams;
          int paramOffset;
          boolean isReturn = false;
          boolean isNoReturn = false;
          boolean isJump = false;
          if(exec.getReturnType() == void.class) {
            // no return
            isNoReturn = true;
            nParams = components.length;
            paramOffset = 0;
          } else if(exec.getReturnType() == boolean.class) {
            // jump
            isJump = true;
            nParams = components.length - 1;
            paramOffset = 1;
          } else {
            // returns
            isReturn = true;
            nParams = components.length - 1;
            paramOffset = 1;
          }
          Object[] execParams = new Object[nParams + 1];
          execParams[0] = ctx;
          
          Class<?>[] execTypes = exec.getParameterTypes();
          assert execTypes[0] == ExecutionContext.class;

          // typecheck and load args into execParams
          boolean wrongTypes = false; 
          for(int j = 0; j < nParams; j++) {
            if(componentValues[j + paramOffset].getClass() == Short.class) {
              execParams[j + 1] = slots[(Short)componentValues[j + paramOffset]];
            } else {
              execParams[j + 1] = componentValues[j + paramOffset];
            }
            if(!iHateJava(execTypes[j + 1], execParams[j + 1])) {
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
            if(err.getCause() instanceof AsmError asmErr) {
              throw asmErr;
            } else {
              throw new RuntimeException(err);
            }
          }
          if(isReturn) {
            slots[(Short)componentValues[0]] = ret;
          } else if(isJump && (boolean)ret) {
            pc = labels.get(componentValues[0]);
          }
        }
      }
      
      pc++;
    }
    
    // process mana deficit damage
    if(ctx.manaDeficit > 0 && ctx.manaVessel != null && !ctx.manaVessel.isCreative()) {
      ctx.manaVessel.damage(world, world.getDamageSources().magic(), (float) (ctx.manaDeficit * EnergyCosts.HP_PER_ENERGY_DEPLETED));
    }
    
    return slots;
  }
}
