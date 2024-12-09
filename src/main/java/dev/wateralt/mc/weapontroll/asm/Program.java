package dev.wateralt.mc.weapontroll.asm;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public interface Program {
  interface State {
    int run();
    boolean isFinished();
  }
  State prepareRun(ServerWorld world, Vec3d origin, LivingEntity user, LivingEntity target);
}
