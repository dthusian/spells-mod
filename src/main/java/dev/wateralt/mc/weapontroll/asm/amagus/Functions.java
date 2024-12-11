package dev.wateralt.mc.weapontroll.asm.amagus;

import dev.wateralt.mc.weapontroll.spell.ExecContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Functions {
  public record Def(String name, AmagusFunc metadata, Method method) {
    public List<Type> getArgTypes() {
      return Arrays.stream(metadata.format().split(" ")).filter(v -> v.startsWith("%")).map(Type::parse).toList();
    }
    
    public Object[] createDefault() {
      if(method.getParameters()[1].isVarArgs()) {
        return new Object[] { name };
      } else {
        Object[] obj = new Object[getArgTypes().size()];
        obj[0] = name;
        return obj;
      }
    }
  }

  @AmagusFunc(format = "set %e on fire", returns = Type.EFFECT)
  public static void setFire(AmagusProgramState state, List<Entity> entities) {
    ExecContext ctx = state.getContext();
    entities.forEach(v -> {
      ctx.useEnergy(EnergyCosts.SET_FIRE);
      v.setOnFireFor(5);
    });
  }
  
  @AmagusFunc(format = "fire projectile with effects %x", returns = Type.EFFECT)
  public static void makeProjectile(AmagusProgramState state, Object... effects) {}
  
  @AmagusFunc(format = "lightning at %p", returns = Type.EFFECT)
  public static void lightningPos(AmagusProgramState state, List<Vec3d> positions) {
    positions.forEach(v -> {
      state.getContext().useEnergy(EnergyCosts.LIGHTNING);
      LightningEntity ent = new LightningEntity(EntityType.LIGHTNING_BOLT, state.getContext().world());
      ent.setPosition(v);
      state.getContext().world().spawnEntity(ent);
    });
  }
  
  @AmagusFunc(format = "lightning at %p", returns = Type.EFFECT)
  public static void lightningEntity(AmagusProgramState state, List<Entity> entities) {
    lightningPos(state, AmagusUtil.entityToPos(entities));
  }

  @AmagusFunc(format = "in front of %e", returns = Type.POSITION)
  public static void front(AmagusProgramState state, List<Entity> entities) {}
  
  public static final HashMap<String, Def> FUNCTIONS = new HashMap<>();
  
  static {
    Arrays.stream(Functions.class.getMethods()).forEach(v -> FUNCTIONS.put(v.getName(), new Def(v.getName(), v.getAnnotation(AmagusFunc.class), v)));
  }
}
