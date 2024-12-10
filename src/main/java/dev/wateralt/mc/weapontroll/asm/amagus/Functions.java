package dev.wateralt.mc.weapontroll.asm.amagus;

import dev.wateralt.mc.weapontroll.Util;
import dev.wateralt.mc.weapontroll.spell.ExecContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Functions {
  public record Def(String name, AmagusFunc metadata, Method method) { }

  @AmagusFunc(format = "set %e on fire")
  public static void setFire(AmagusProgramState state, List<Entity> entities) {
    ExecContext ctx = state.getContext();
    entities.forEach(v -> {
      ctx.useEnergy(EnergyCosts.SET_FIRE);
      v.setOnFireFor(5);
    });
  }
  
  @AmagusFunc(format = "fire projectile with effects %xs")
  public static void makeProjectile(AmagusProgramState state, Object... effects) {}
  
  @AmagusFunc(format = "in front of %e", returns = Type.POSITION_LIST)
  public static void front(AmagusProgramState state, List<Entity> entities) {}
  
  @AmagusFunc(format = "lightning at %p")
  public static void lightningPos(AmagusProgramState state, List<Vec3d> positions) {
    positions.forEach(v -> {
      state.getContext().useEnergy(EnergyCosts.LIGHTNING);
      LightningEntity ent = new LightningEntity(EntityType.LIGHTNING_BOLT, state.getContext().world());
      ent.setPosition(v);
      state.getContext().world().spawnEntity(ent);
    });
  }
  
  @AmagusFunc(format = "lightning at %p")
  public static void lightningEntity(AmagusProgramState state, List<Entity> entities) {
    lightningPos(state, AmagusUtil.entityToPos(entities));
  }
  
  public static final HashMap<String, Def> FUNCTIONS = new HashMap<>();
  
  static {
    Arrays.stream(Functions.class.getMethods()).forEach(v -> FUNCTIONS.put(v.getName(), new Def(v.getName(), v.getAnnotation(AmagusFunc.class), v)));
  }
}
