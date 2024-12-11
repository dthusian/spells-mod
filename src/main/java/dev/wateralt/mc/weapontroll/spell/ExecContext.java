package dev.wateralt.mc.weapontroll.spell;

import dev.wateralt.mc.weapontroll.asm.std20.EnergyCosts;
import dev.wateralt.mc.weapontroll.energy.EnergyUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ExecContext {
  // user, target != null: User hit target (with melee or projectile)
  // user == target: User casted on self (shift-click)
  // target == null: Fired from projectile and didn't hit anything
  
  private ServerWorld world;
  private LivingEntity user;
  private LivingEntity target;
  private Vec3d targetPos;
  private Vec3d direction;
  @Nullable
  private ServerPlayerEntity manaSource;
  
  public ExecContext(ServerWorld world, LivingEntity user, LivingEntity target, Vec3d targetPos, Vec3d direction) {
    this.world = world;
    this.user = user;
    this.target = target;
    if(user instanceof ServerPlayerEntity spe) {
      manaSource = spe;
    }
    this.targetPos = targetPos;
    this.direction = direction;
  }

  public ServerWorld world() {
    return world;
  }

  public LivingEntity user() {
    return user;
  }

  public LivingEntity target() {
    return target;
  }

  public Vec3d targetPos() {
    return targetPos;
  }

  public ServerPlayerEntity manaSource() {
    return manaSource;
  }

  public void setTarget(LivingEntity target) {
    this.target = target;
    this.targetPos = target.getPos();
  }
  
  public void useEnergy(double energy) {
    if(manaSource != null) {
      EnergyUtil.useEnergy(manaSource, energy);
    }
  }

  public void useEnergyAt(Vec3d pos, double amount) {
    double dist = targetPos.distanceTo(pos);
    double factor;
    if(dist <= EnergyCosts.LOCAL_RADIUS) {
      factor = 1;
    } else {
      factor = 1 + 0.001 * Math.pow(dist - EnergyCosts.LOCAL_RADIUS, 2);
    }
    useEnergy(factor * amount);
  }
}
