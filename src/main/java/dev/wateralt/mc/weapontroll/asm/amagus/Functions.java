package dev.wateralt.mc.weapontroll.asm.amagus;

import dev.wateralt.mc.weapontroll.Util;
import dev.wateralt.mc.weapontroll.energy.EnergyUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class Functions {
  public static void setFireTo(@Nullable ServerPlayerEntity caster, ServerWorld world, int amp, Vec3d targetPos, @Nullable LivingEntity target) {
    EnergyUtil.useEnergy(caster, EnergyCosts.SET_FIRE);
    if(target == null) {
      BlockPos blockPos = Util.vecToPos(targetPos);
      if(world.canPlace(Blocks.FIRE.getDefaultState(), blockPos, ShapeContext.absent())) {
        world.setBlockState(blockPos, Blocks.FIRE.getDefaultState(), Block.NOTIFY_ALL);
      }
    } else {
      target.setOnFireFor(5);
    }
  }
}
