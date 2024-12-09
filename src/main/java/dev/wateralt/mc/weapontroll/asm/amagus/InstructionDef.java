package dev.wateralt.mc.weapontroll.asm.amagus;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public interface InstructionDef {
  void execute(@Nullable ServerPlayerEntity caster, ServerWorld world, int amp, Vec3d parsed, @Nullable LivingEntity target);
}
