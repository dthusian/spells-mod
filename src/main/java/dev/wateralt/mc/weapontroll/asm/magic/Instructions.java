package dev.wateralt.mc.weapontroll.asm.magic;

import dev.wateralt.mc.weapontroll.Util;
import dev.wateralt.mc.weapontroll.asm.ExecContext;
import static dev.wateralt.mc.weapontroll.asm.magic.InstructionStatus.DEFAULT;

import dev.wateralt.mc.weapontroll.projectile.Projectile;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

public class Instructions {
  public static InstructionStatus self(MagicProgramState state) {
    state.ctx().setTarget(state.ctx().user());
    return DEFAULT;
  }
  
  public static InstructionStatus fire(MagicProgramState state) {
    ExecContext ctx = state.ctx();
    ctx.useEnergy(EnergyCosts.FIRE);
    if(ctx.target() != null) {
      ctx.target().setOnFireFor(5);
    } else {
      BlockPos blockPos = Util.vecToPos(ctx.targetPos());
      if(ctx.world().getBlockState(blockPos).isAir() && ctx.world().getBlockState(blockPos.down()).isOpaqueFullCube()) {
        ctx.world().setBlockState(blockPos, Blocks.FIRE.getDefaultState());
      }
    }
    return DEFAULT;
  }
  
  public static InstructionStatus heal(MagicProgramState state) {
    if(state.ctx().target() != null) {
      state.ctx().useEnergy(EnergyCosts.HEAL);
      state.ctx().target().heal(4.0f);
    }
    return DEFAULT;
  }

  public static InstructionStatus explode(MagicProgramState state) {
    Vec3d pos = state.ctx().targetPos();
    state.ctx().useEnergy(EnergyCosts.EXPLODE);
    state.ctx().world().createExplosion(state.ctx().user(), pos.getX(), pos.getY(), pos.getZ(), 4.0f, World.ExplosionSourceType.MOB);
    return DEFAULT;
  }
  
  public static InstructionStatus projectile(MagicProgramState state) {
    ExecContext ctx = state.ctx();
    String[] instructions = state.program().getInstructions();
    String source = "#lang magic\n" + String.join("\n", Arrays.copyOfRange(instructions, state.pc() + 1, instructions.length));
    Projectile.create(ctx.world(), ctx.user(), ctx.targetPos(), ctx.direction(), source);
    return InstructionStatus.HALT;
  }
  
  public static final HashMap<String, Function<MagicProgramState, InstructionStatus>> INSTRUCTIONS = new HashMap<>();
  
  static {
    INSTRUCTIONS.put("self", Instructions::self);
    INSTRUCTIONS.put("fire", Instructions::fire);
    INSTRUCTIONS.put("heal", Instructions::heal);
    INSTRUCTIONS.put("explode", Instructions::explode);
    INSTRUCTIONS.put("projectile", Instructions::projectile);
  }
}
