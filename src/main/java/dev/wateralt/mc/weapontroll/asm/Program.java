package dev.wateralt.mc.weapontroll.asm;

import dev.wateralt.mc.weapontroll.spell.ExecContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public interface Program {
  interface State {
    /// Runs the program until the next wait point,
    /// and return the number of ticks to wait.
    /// If isFinished would return true, must not do anything.
    int run();
    /// Whether the function is finished executing.
    boolean isFinished();
  }
  State prepareRun(ExecContext context);
}
