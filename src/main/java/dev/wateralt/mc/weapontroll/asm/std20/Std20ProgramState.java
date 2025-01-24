package dev.wateralt.mc.weapontroll.asm.std20;

import com.google.common.collect.Streams;
import dev.wateralt.mc.weapontroll.asm.AsmError;
import dev.wateralt.mc.weapontroll.asm.Program;
import dev.wateralt.mc.weapontroll.asm.ExecContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class Std20ProgramState implements Program.State {
  private static final int INSTRUCTIONS_PER_TICK = 100;
  private static final long MAX_RUNTIME = 20 * 60 * 60;
  long startTick;
  Std20Program program;
  ExecContext ctx;
  int pc;
  Object[] slots;
  Optional<Integer> wait;
  boolean finished;

  public Std20ProgramState(Std20Program program, ExecContext ctx, int maxSlots) {
    this.program = program;
    this.startTick = ctx.world().getTime();
    this.ctx = ctx;
    this.pc = 0;
    this.slots = new Object[maxSlots];
    this.slots[0] = ctx.user();
    this.slots[1] = ctx.target();
  }

  public Std20Program getProgram() { return program; }
  public ExecContext getContext() { return ctx; }

  public void jumpTo(String label) {
    pc = this.program.getLabels().get(label);
  }
  
  private String printType(Object obj) {
    if(obj != null) return printType(obj.getClass());
    else return "<null>";
  }
  private String printType(Class<?> clazz) {
    if(clazz == Double.class || clazz == double.class) {
      return "Number";
    } else if(clazz == String.class) {
      return "String";
    } else if(clazz == Vec3d.class) {
      return "Vector";
    } else if(Entity.class.isAssignableFrom(clazz)) {
      return "Entity";
    } else {
      return "Unknown";
    }
  }
  
  private boolean typesCompatible(Object obj, Class<?> clazz) {
    if(obj == null) {
      return clazz == Object.class;
    }
    Class<?> cb = obj.getClass();
    if(clazz.isAssignableFrom(cb)) {
      return true;
    }
    if(clazz.isPrimitive() && clazz.getSimpleName().equalsIgnoreCase(cb.getSimpleName())) {
      return true;
    }
    return false;
  }
  
  private void typeCheck(Object[] args, Class<?>[] argTypes) {
    if(args.length != argTypes.length) {
      throw new AsmError("Expected %d arguments, found %d".formatted(args.length - 1, argTypes.length - 1));
    }
    for(int i = 1; i < args.length; i++) {
      if(!typesCompatible(args[i], argTypes[i])) {
        throw new AsmError("In argument %d: Expected type %s, found %s".formatted(i, printType(argTypes[i]), printType(args[i])));
      }
    }
  }

  @Override
  public int run() {
    if(startTick + MAX_RUNTIME < ctx.world().getTime()) {
      finished = true;
      return 0;
    }
    
    wait = Optional.empty();
    int i;
    for(i = 0; i < INSTRUCTIONS_PER_TICK; i++) {
      if(pc >= program.getInstrs().size()) {
        finished = true;
        break;
      }
      Instruction instr = program.getInstrs().get(pc);
      
      try {
        Optional<Method> optMethod = Arrays.stream(Instructions.class.getMethods()).filter(v -> v.getName().equals(instr.function())).findFirst();
        if(optMethod.isEmpty()) {
          throw new AsmError("Invalid function: %s".formatted(instr.function()));
        }
        Method method = optMethod.get();

        Stream<Object> stream1 = Stream.of(this);
        Stream<Object> stream2 = instr.args().stream().map(v -> {
          switch(v) {
            case Instruction.NumArg numArg -> {
              return numArg.number();
            }
            case Instruction.SlotArg slotArg -> {
              return slots[slotArg.slot()];
            }
            case Instruction.StringArg stringArg -> {
              return stringArg.str();
            }
          }
        });
        Object[] args = Streams.concat(stream1, stream2).toArray();
        Class<?>[] argTypes = method.getParameterTypes();
        typeCheck(args, argTypes);
        
        Object ret;
        try {
          ret = method.invoke(null, args);
        } catch(InvocationTargetException err) {
          if(err.getCause() instanceof AsmError err2) {
            throw err2;
          } else {
            throw new RuntimeException(err);
          }
        } catch(Exception err) {
          throw new RuntimeException(err);
        }
        if(instr.returnInto().isPresent()) {
          slots[instr.returnInto().get()] = ret;
        }

        pc++;
        
        if(wait.isPresent()) {
          return wait.get();
        }
      } catch(AsmError err) {
        throw new AsmError("at pc %d (`%s`): %s".formatted(pc, instr.toString(), err.getMessage()));
      }
    }
    return 1;
  }

  @Override
  public boolean isFinished() {
    return finished;
  }
  
  public void setWait(int x) {
    this.wait = Optional.of(x);
  }
}
