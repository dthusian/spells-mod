package dev.wateralt.mc.weapontroll.asm.magic;

import dev.wateralt.mc.weapontroll.Util;
import dev.wateralt.mc.weapontroll.asm.ExecContext;
import static dev.wateralt.mc.weapontroll.asm.magic.InstructionStatus.DEFAULT;

import dev.wateralt.mc.weapontroll.asm.phys.InfuseUtil;
import dev.wateralt.mc.weapontroll.asm.phys.Projectile;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
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
  
  public static InstructionStatus mine(MagicProgramState state) {
    ExecContext ctx = state.ctx();
    Vec3d start = ctx.targetPos();
    Vec3d end = ctx.targetPos().add(ctx.direction().normalize().multiply(4.0));
    BlockHitResult res = ctx.world().raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, ShapeContext.absent()));
    if(res.getType() == HitResult.Type.BLOCK) {
      Block targetBlock = ctx.world().getBlockState(res.getBlockPos()).getBlock();
      if(targetBlock.getHardness() < 1000) {
        ctx.useEnergy(EnergyCosts.BREAK_BLOCK_COEFF * Math.ceil(targetBlock.getHardness()));
        ctx.world().breakBlock(res.getBlockPos(), true, ctx.user());
      }
    }
    return DEFAULT;
  }

  public static InstructionStatus explode(MagicProgramState state) {
    Vec3d pos = state.ctx().targetPos();
    state.ctx().useEnergy(EnergyCosts.EXPLODE);
    state.ctx().world().createExplosion(state.ctx().user(), pos.getX(), pos.getY(), pos.getZ(), 4.0f, World.ExplosionSourceType.MOB);
    return DEFAULT;
  }
  
  public static InstructionStatus pull(MagicProgramState state) {
    ExecContext ctx = state.ctx();
    if(ctx.target() != null) {
      ctx.useEnergy(EnergyCosts.PULL);
      ctx.target().addVelocity(ctx.user().getEyePos().subtract(ctx.target().getPos()).multiply(0.05));
    }
    return DEFAULT;
  }

  public static InstructionStatus push(MagicProgramState state) {
    ExecContext ctx = state.ctx();
    if(ctx.target() != null) {
      ctx.useEnergy(EnergyCosts.PUSH);
      ctx.target().addVelocity(ctx.direction().normalize().multiply(1));
    }
    return DEFAULT;
  }
  
  public static InstructionStatus lift(MagicProgramState state) {
    ExecContext ctx = state.ctx();
    if(ctx.target() != null) {
      ctx.useEnergy(EnergyCosts.LIFT);
      ctx.target().addVelocity(new Vec3d(0, 0.5, 0));
    }
    return DEFAULT;
  }
  
  public static InstructionStatus lightning(MagicProgramState state) {
    ExecContext ctx = state.ctx();
    LightningEntity entity = new LightningEntity(EntityType.LIGHTNING_BOLT, ctx.world());
    entity.setPosition(ctx.targetPos());
    ctx.world().spawnEntity(entity);
    return DEFAULT;
  }
  
  public static InstructionStatus milk(MagicProgramState state) {
    ExecContext ctx = state.ctx();
    if(ctx.target() != null) {
      ctx.target().clearStatusEffects();
    }
    return DEFAULT;
  }
  
  public static InstructionStatus teleport(MagicProgramState state) {
    ExecContext ctx = state.ctx();
    ctx.useEnergy(EnergyCosts.TELEPORT);
    ctx.user().teleport(ctx.world(), ctx.targetPos().getX(), ctx.targetPos().getY(), ctx.targetPos().getZ(), Set.of(), 0.0f, 0.0f, false);
    return DEFAULT;
  }
  
  public static InstructionStatus teleswap(MagicProgramState state) {
    ExecContext ctx = state.ctx();
    if(ctx.target() != null) {
      ctx.useEnergy(EnergyCosts.TELESWAP);
      double x1 = ctx.user().getX();
      double y1 = ctx.user().getY();
      double z1 = ctx.user().getZ();
      double x2 = ctx.target().getX();
      double y2 = ctx.target().getY();
      double z2 = ctx.target().getZ();
      ctx.target().teleport(ctx.world(), x1, y1, z1, Set.of(), 0.0f, 0.0f, false);
      ctx.user().teleport(ctx.world(), x2, y2, z2, Set.of(), 0.0f, 0.0f, false);
    }
    return DEFAULT;
  }

  public static InstructionStatus infuse(MagicProgramState state) {
    ExecContext ctx = state.ctx();
    if(ctx.user() instanceof ServerPlayerEntity spe) {
      state.ctx().useEnergy(EnergyCosts.INFUSE);
      InfuseUtil.infuseFirst(ctx.world(), spe.getInventory(), ctx.targetPos(), ctx.direction(), ctx.user().isInCreativeMode());
    }
    return DEFAULT;
  }

  public static InstructionStatus projectile(MagicProgramState state) {
    ExecContext ctx = state.ctx();
    String[] instructions = state.program().getInstructions();
    String source = "#lang magic\n" + String.join("\n", Arrays.copyOfRange(instructions, state.pc() + 1, instructions.length));
    Projectile.create(ctx.world(), ctx.user(), ctx.targetPos(), ctx.direction(), source, 0.1);
    return InstructionStatus.HALT;
  }

  public static InstructionStatus multiprojectile(MagicProgramState state) {
    ExecContext ctx = state.ctx();
    String[] instructions = state.program().getInstructions();
    String source = "#lang magic\n" + String.join("\n", Arrays.copyOfRange(instructions, state.pc() + 1, instructions.length));
    
    Vec3d dir = state.ctx().direction();
    Random rng = new Random(Double.doubleToLongBits(dir.getX()));
    Vec3d orth1 = dir.crossProduct(dir.rotateX(0.727f)).normalize();
    Vec3d orth2 = dir.crossProduct(orth1).normalize();
    for(int i = 0; i < 9; i++) {
      double xDefl = Util.normal(rng.nextDouble()) * 0.2;
      double yDefl = Util.normal(rng.nextDouble()) * 0.2;
      Vec3d v = dir.add(orth1.multiply(xDefl)).add(orth2.multiply(yDefl)).normalize();
      Projectile.create(ctx.world(), ctx.user(), ctx.targetPos(), v, source, 0.1);
    }
    return InstructionStatus.HALT;
  }


  public static InstructionStatus wait(MagicProgramState state) {
    return new InstructionStatus(10, false);
  }
  
  public static final HashMap<String, Function<MagicProgramState, InstructionStatus>> INSTRS = new HashMap<>();
  
  static {
    INSTRS.put("self", Instructions::self);
    
    INSTRS.put("fire", Instructions::fire);
    INSTRS.put("heal", Instructions::heal);
    INSTRS.put("mine", Instructions::mine);
    INSTRS.put("explode", Instructions::explode);
    INSTRS.put("pull", Instructions::pull);
    INSTRS.put("push", Instructions::push);
    INSTRS.put("lift", Instructions::lift);
    INSTRS.put("lightning", Instructions::lightning);
    INSTRS.put("milk", Instructions::milk);
    INSTRS.put("infuse", Instructions::infuse);
    INSTRS.put("teleport", Instructions::teleport);
    INSTRS.put("teleswap", Instructions::teleswap);
    
    INSTRS.put("projectile", Instructions::projectile);
    INSTRS.put("multiprojectile", Instructions::multiprojectile);
    INSTRS.put("wait", Instructions::wait);
  }
}
