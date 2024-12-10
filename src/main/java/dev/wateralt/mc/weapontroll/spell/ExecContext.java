package dev.wateralt.mc.weapontroll.spell;

import dev.wateralt.mc.weapontroll.asm.std20.EnergyCosts;
import dev.wateralt.mc.weapontroll.energy.EnergyUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ExecContext {
  private ServerWorld world;
  private LivingEntity user;
  @Nullable
  private LivingEntity target;
  private Vec3d targetPos;
  @Nullable
  private ServerPlayerEntity manaSource;
  
  public ExecContext(ServerWorld world, LivingEntity user, @Nullable LivingEntity target, Vec3d targetPos) {
    this.world = world;
    this.user = user;
    this.target = target;
    if(user instanceof ServerPlayerEntity spe) {
      manaSource = spe;
    }
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
