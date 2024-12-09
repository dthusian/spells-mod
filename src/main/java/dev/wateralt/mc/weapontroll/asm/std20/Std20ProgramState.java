package dev.wateralt.mc.weapontroll.asm.std20;

import com.google.common.collect.Streams;
import dev.wateralt.mc.weapontroll.Weapontroll;
import dev.wateralt.mc.weapontroll.asm.AsmError;
import dev.wateralt.mc.weapontroll.asm.EnergyCosts;
import dev.wateralt.mc.weapontroll.asm.Program;
import dev.wateralt.mc.weapontroll.playertracker.TrackedPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class Std20ProgramState implements Program.State {
  Std20Program program;
  Vec3d origin;
  ServerWorld world;
  LivingEntity user;
  @Nullable
  LivingEntity target;
  @Nullable
  ServerPlayerEntity manaSource;
  int instructionsLeft;
  int pc;
  Object[] slots;

  public Std20ProgramState(Std20Program program, Vec3d origin, ServerWorld world, LivingEntity user, @Nullable LivingEntity target, int maxSlots, int instructionLimit) {
    this.program = program;
    this.origin = origin;
    this.world = world;
    this.user = user;
    this.target = target;
    if(user instanceof ServerPlayerEntity spe) {
      manaSource = spe;
    }
    this.instructionsLeft = instructionLimit;
    this.pc = 0;
    this.slots = new Object[maxSlots];
  }

  // getters
  public Vec3d origin() {
    return origin;
  }
  public ServerWorld world() {
    return world;
  }
  public LivingEntity user() { return user; }
  public LivingEntity target() { return target; }
  public ServerPlayerEntity manaSource() {
    return manaSource;
  }
  public int getInstructionsLeft() { return instructionsLeft; }
  
  // mutators
  public void useEnergy(double amount) {
    if(manaSource != null) {
      boolean stop = false;
      TrackedPlayer pl = Weapontroll.PLAYER_TRACKER.get(manaSource);
      int newEnergy = pl.getEnergy() - (int)Math.ceil(amount);
      if(newEnergy < 0) {
        int deficit = -newEnergy;
        newEnergy = 0;
        double damage = deficit / 50.0;
        if(damage > manaSource.getHealth()) {
          manaSource.kill(world);
          stop = true;
        } else {
          manaSource.setHealth((float) (manaSource.getHealth() - damage));
          manaSource.markHealthDirty();
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
    if(dist <= EnergyCosts.LOCAL_RADIUS) {
      factor = 1;
    } else {
      factor = 1 + 0.001 * Math.pow(dist - EnergyCosts.LOCAL_RADIUS, 2);
    }
    useEnergy(factor * amount);
  }

  public void jumpTo(String label) {
    pc = this.program.getLabels().get(label);
  }
  
  // run
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
    if(obj == null && clazz != Object.class) {
      return false;
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
    while(instructionsLeft > 0) {
      if(pc >= program.getInstrs().size()) {
        instructionsLeft = 0;
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
        instructionsLeft--;
      } catch(AsmError err) {
        throw new AsmError("at pc %d (`%s`): %s".formatted(pc, instr.toString(), err.getMessage()));
      }
    }
    return 0;
  }

  @Override
  public boolean isFinished() {
    return this.instructionsLeft == 0;
  }
}
