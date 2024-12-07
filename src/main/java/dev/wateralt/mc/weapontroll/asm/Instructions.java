package dev.wateralt.mc.weapontroll.asm;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Instructions {

  public sealed interface Instr { }
  
  // Number manip instrs
  public record LoadNum(short slotDst, double val) implements Instr {
    public double exec(ServerWorld world, double val) {
      return val;
    }
  }
  public record Add(short slotDst, short slotA, short slotB) implements Instr {
    public double exec(ServerWorld world, double a, double b) {
      return a + b;
    }
  }
  public record Sub(short slotDst, short slotA, short slotB) implements Instr {
    public double exec(ServerWorld world, double a, double b) {
      return a - b;
    }
  }
  public record Mul(short slotDst, short slotA, short slotB) implements Instr {
    public double exec(ServerWorld world, double a, double b) {
      return a * b;
    }
  }
  public record Div(short slotDst, short slotA, short slotB) implements Instr {
    public double exec(ServerWorld world, double a, double b) {
      return a / b;
    }
  }
  public record Round(short slotDst, short slotSrc) implements Instr {
    public double exec(ServerWorld world, double a) {
      return Math.round(a);
    }
  }
  
  // Branching instrs
  public record Label(String label) implements Instr { }
  public record JumpIfLess(String label, short slotA, short slotB) implements Instr { }
  public record JumpIfEqual(String label, short slotA, short slotB) implements Instr { }
  
  // Vector manip instrs
  public record LoadVec(short slotDst, double x, double y, double z) implements Instr {
    public Vec3d exec(ServerWorld world, double x, double y, double z) {
      return new Vec3d(x, y, z);
    }
  }
  public record MakeVec(short slotDst, short slotX, short slotY, short slotZ) implements Instr {
    public Vec3d exec(ServerWorld world, double x, double y, double z) {
      return new Vec3d(x, y, z);
    }
  }
  public record SplitVec(short slotX, short slotY, short slotZ, short slotSrc) implements Instr { }
  
  // Entity manip instrs
  public record NearestEntity(short slotEntity, short slotPos, short slotIndex) implements Instr {
    public Entity exec(ServerWorld world, Vec3d pos, double slot) {
      Box box = new Box(pos.add(-4, -4, -4), pos.add(4, 4, 4));
      List<Entity> list = world.getEntitiesByType(TypeFilter.instanceOf(Entity.class), box, EntityPredicates.VALID_LIVING_ENTITY);
      list.sort((a, b) -> (int) Math.signum(a.getPos().distanceTo(pos) - b.getPos().distanceTo(pos)));
      
      int slotInt = (int)slot;
      if(list.size() >= slotInt) {
        return null;
      } else {
        return list.get((int)slot);
      }
    }
  }
  public record EntityPos(short slotPos, short slotEntity) implements Instr {
    public Vec3d exec(ServerWorld world, Entity ent) {
      return ent.getPos();
    }
  }
  public record EntityVel(short slotVel, short slotEntity) implements Instr {
    public Vec3d exec(ServerWorld world, Entity ent) {
      return ent.getVelocity();
    }
  }
  public record EntityFacing(short slotFacing, short slotEntity) implements Instr {
    public Vec3d exec(ServerWorld world, Entity ent) {
      return ent.getRotationVector();
    }
  }
  
  // In-world effects
  public record AccelEntity(short slotEntity, short slotVel) implements Instr {
    public void exec(ServerWorld world, Entity ent, Vec3d vel) {
      ent.addVelocity(vel);
    }
  }
  public record DamageEntity(short slotEntity, short slotDmg) implements Instr {
    public void exec(ServerWorld world, Entity ent, Vec3d vel) {
      ent.addVelocity(vel);
    }
  }
  public record PlaceBlock(short slotPos, String block) implements Instr {
    public void exec(ServerWorld world, Vec3d pos, String block) {
      throw new AsmError("Not implemented");
    }
  }
  public record Explode(short slotPos, short slotPower) implements Instr {
    public void exec(ServerWorld world, Vec3d pos, double power) {
      world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), (float)power, World.ExplosionSourceType.MOB);
    }
  }
  public record SummonLightning(short slotPos) implements Instr {
    public void exec(ServerWorld world, Vec3d pos) {
      LightningEntity ent = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
      ent.setPosition(pos);
      world.spawnEntity(ent);
    }
  }
  public record SummonFireball(short slotFireball, short slotPos) implements Instr {
    public Entity exec(ServerWorld world, Vec3d pos) {
      LightningEntity ent = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
      ent.setPosition(pos);
      world.spawnEntity(ent);
      return ent;
    }
  }
  
  // Misc
  public record Copy(short slotDst, short slotSrc) implements Instr {
    public Object exec(ServerWorld world, Object o) {
      return o;
    }
  }
  
  private static final HashMap<String, Class<? extends Instr>> instrs = new HashMap<>();
  
  static {
    instrs.put("loadnum", LoadNum.class);
    instrs.put("add", Add.class);
    instrs.put("sub", Sub.class);
    instrs.put("mul", Mul.class);
    instrs.put("div", Div.class);
    instrs.put("round", Round.class);
    instrs.put("copy", Copy.class);
    
    instrs.put("label", Label.class);
    instrs.put("jmpl", JumpIfLess.class);
    instrs.put("jmpe", JumpIfEqual.class);
    
    instrs.put("loadvec", LoadVec.class);
    instrs.put("makevec", MakeVec.class);
    instrs.put("splitvec", SplitVec.class);
    
    instrs.put("nearestent", NearestEntity.class);
    instrs.put("entpos", EntityPos.class);
    instrs.put("entvel", EntityVel.class);
    instrs.put("entfacing", EntityFacing.class);
    
    instrs.put("accelent", AccelEntity.class);
    instrs.put("damageent", DamageEntity.class);
    instrs.put("explode", Explode.class);
    instrs.put("placeblock", PlaceBlock.class);
    instrs.put("lightning", SummonLightning.class);
    instrs.put("fireball", SummonFireball.class);
  }
  
  private static short parseSlot(String s) {
    return Short.parseShort(s);
  }
  
  private static double parseDouble(String s) {
    return Double.parseDouble(s);
  }
  
  public static Instr parse(String x) {
    String[] parts = x.split(" ");
    String op = parts[0];
    Class<? extends Instr> cls = instrs.get(op);
    if(cls == null) {
      throw new AsmError("Invalid instruction: %s".formatted(op));
    }
    Constructor<?> ctor = cls.getConstructors()[0];
    Class<?>[] paramTypes = ctor.getParameterTypes();
    if(paramTypes.length != parts.length - 1) {
      throw new AsmError("Wrong number of params: expected %d, found %d".formatted(paramTypes.length, parts.length - 1));
    }
    Object[] params = new Object[paramTypes.length];
    for(int i = 0; i < paramTypes.length; i++) {
      if(paramTypes[i] == String.class) {
        params[i] = parts[i + 1];
      } else if(paramTypes[i] == Short.class) {
        params[i] = parseSlot(parts[i + 1]);
      } else if(paramTypes[i] == Double.class) {
        params[i] = parseDouble(parts[i + 1]);
      } else {
        throw new RuntimeException("Invalid type in instruction ctor");
      }
    }
    try {
      return (Instr) ctor.newInstance(params);
    } catch(Exception err) {
      throw new RuntimeException(err);
    }
  }
}
