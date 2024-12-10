package dev.wateralt.mc.weapontroll.asm.amagus;

import dev.wateralt.mc.weapontroll.asm.Program;
import dev.wateralt.mc.weapontroll.energy.EnergyUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class AmagusProgramState implements Program.State {
  private AmagusProgram program;
  private ServerWorld world;
  private LivingEntity user;
  @Nullable
  private LivingEntity target;
  private Vec3d targetPos;
  @Nullable
  private ServerPlayerEntity manaSource;
  
  public AmagusProgramState(AmagusProgram program, ServerWorld world, LivingEntity user, @Nullable LivingEntity target, Vec3d targetPos) {
    this.program = program;
    this.world = world;
    this.user = user;
    this.target = target;
    this.targetPos = targetPos;
    if(user instanceof ServerPlayerEntity entity) {
      this.manaSource = entity;
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
  
  @Override
  public boolean isFinished() {
    return false;
  }

  @Override
  public int run() {
    throw new RuntimeException("todo");
  }
}
