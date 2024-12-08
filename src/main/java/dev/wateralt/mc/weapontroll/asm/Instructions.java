package dev.wateralt.mc.weapontroll.asm;

import dev.wateralt.mc.weapontroll.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;

public class Instructions {

  public sealed interface Instr { }
  
  // Number manip instrs
  public record LoadNum(short slotDst, double val) implements Instr {
    public double exec(Executor.ExecutionContext ctx, double val) {
      return val;
    }
  }
  public record Add(short slotDst, short slotA, short slotB) implements Instr {
    public double exec(Executor.ExecutionContext ctx, double a, double b) {
      return a + b;
    }
  }
  public record Sub(short slotDst, short slotA, short slotB) implements Instr {
    public double exec(Executor.ExecutionContext ctx, double a, double b) {
      return a - b;
    }
  }
  public record Mul(short slotDst, short slotA, short slotB) implements Instr {
    public double exec(Executor.ExecutionContext ctx, double a, double b) {
      return a * b;
    }
  }
  public record Div(short slotDst, short slotA, short slotB) implements Instr {
    public double exec(Executor.ExecutionContext ctx, double a, double b) {
      return a / b;
    }
  }
  public record Round(short slotDst, short slotSrc) implements Instr {
    public double exec(Executor.ExecutionContext ctx, double a) {
      return Math.round(a);
    }
  }
  public record Sqrt(short slotDst, short slotSrc) implements Instr {
    public double exec(Executor.ExecutionContext ctx, double a) {
      return Math.sqrt(a);
    }
  }
  public record Sin(short slotDst, short slotSrc) implements Instr {
    public double exec(Executor.ExecutionContext ctx, double a) {
      return Math.sin(a);
    }
  }
  public record Cos(short slotDst, short slotSrc) implements Instr {
    public double exec(Executor.ExecutionContext ctx, double a) {
      return Math.cos(a);
    }
  }
  
  // Branching instrs
  public record Label(String label) implements Instr { }
  public record JumpIfLess(String label, short slotA, short slotB) implements Instr {
    public boolean exec(Executor.ExecutionContext ctx, double a, double b) {
      return a < b;
    }
  }

  public record JumpIfLessEqual(String label, short slotA, short slotB) implements Instr {
    public boolean exec(Executor.ExecutionContext ctx, double a, double b) {
      return a <= b;
    }
  }

  public record JumpIfGreater(String label, short slotA, short slotB) implements Instr {
    public boolean exec(Executor.ExecutionContext ctx, double a, double b) {
      return a > b;
    }
  }

  public record JumpIfGreaterEqual(String label, short slotA, short slotB) implements Instr {
    public boolean exec(Executor.ExecutionContext ctx, double a, double b) {
      return a >= b;
    }
  }
  public record JumpIfEqual(String label, short slotA, short slotB) implements Instr {
    public boolean exec(Executor.ExecutionContext ctx, Object a, Object b) {
      return Objects.equals(a, b);
    }
  }

  public record JumpIfNotEqual(String label, short slotA, short slotB) implements Instr {
    public boolean exec(Executor.ExecutionContext ctx, Object a, Object b) {
      return !Objects.equals(a, b);
    }
  }
  
  // Vector manip instrs
  public record LoadVec(short slotDst, double x, double y, double z) implements Instr {
    public Vec3d exec(Executor.ExecutionContext ctx, double x, double y, double z) {
      return new Vec3d(x, y, z);
    }
  }
  public record MakeVec(short slotDst, short slotX, short slotY, short slotZ) implements Instr {
    public Vec3d exec(Executor.ExecutionContext ctx, double x, double y, double z) {
      return new Vec3d(x, y, z);
    }
  }
  public record SplitVec(short slotX, short slotY, short slotZ, short slotSrc) implements Instr { }
  public record VAdd(short slotDst, short slotA, short slotB) implements Instr {
    public Vec3d exec(Executor.ExecutionContext ctx, Vec3d a, Vec3d b) {
      return a.add(b);
    }
  }
  public record VSub(short slotDst, short slotA, short slotB) implements Instr {
    public Vec3d exec(Executor.ExecutionContext ctx, Vec3d a, Vec3d b) {
      return a.subtract(b);
    }
  }
  public record VMul(short slotDst, short slotA, short slotB) implements Instr {
    public Vec3d exec(Executor.ExecutionContext ctx, Vec3d a, double b) {
      return a.multiply(b);
    }
  }
  public record VDiv(short slotDst, short slotA, short slotB) implements Instr {
    public Vec3d exec(Executor.ExecutionContext ctx, Vec3d a, double b) {
      return a.multiply(1 / b);
    }
  }
  public record VDist(short slotDst, short slotA) implements Instr {
    public double exec(Executor.ExecutionContext ctx, Vec3d a) {
      return a.distanceTo(Vec3d.ZERO);
    }
  }
  public record VNorm(short slotDst, short slotA) implements Instr {
    public Vec3d exec(Executor.ExecutionContext ctx, Vec3d a) {
      return a.normalize();
    }
  }
  public record VDot(short slotDst, short slotA, short slotB) implements Instr {
    public double exec(Executor.ExecutionContext ctx, Vec3d a, Vec3d b) {
      return a.dotProduct(b);
    }
  }
  public record VCross(short slotDst, short slotA, short slotB) implements Instr {
    public Vec3d exec(Executor.ExecutionContext ctx, Vec3d a, Vec3d b) {
      return a.crossProduct(b);
    }
  }
  
  // World query instrs
  public record NearestEntity(short slotEntity, short slotPos, short slotIndex) implements Instr {
    public Entity exec(Executor.ExecutionContext ctx, Vec3d pos, double slot) {
      Box box = new Box(pos.add(-4, -4, -4), pos.add(4, 4, 4));
      List<Entity> list = ctx.world().getEntitiesByType(TypeFilter.instanceOf(Entity.class), box, EntityPredicates.VALID_LIVING_ENTITY);
      list.sort((a, b) -> (int) Math.signum(a.getPos().distanceTo(pos) - b.getPos().distanceTo(pos)));
      
      int slotInt = (int)slot;
      if(slotInt < list.size()) {
        return list.get((int)slot);
      } else {
        return null;
      }
    }
  }
  public record EntityPos(short slotPos, short slotEntity) implements Instr {
    public Vec3d exec(Executor.ExecutionContext ctx, Entity ent) {
      return ent.getPos();
    }
  }
  public record EntityVel(short slotVel, short slotEntity) implements Instr {
    public Vec3d exec(Executor.ExecutionContext ctx, Entity ent) {
      return ent.getVelocity();
    }
  }
  public record EntityFacing(short slotFacing, short slotEntity) implements Instr {
    public Vec3d exec(Executor.ExecutionContext ctx, Entity ent) {
      return ent.getRotationVector();
    }
  }
  public record CheckBlock(short slotRes, short slotPos, String block) implements Instr {
    public double exec(Executor.ExecutionContext ctx, Vec3d pos, String block) {
      if(pos.distanceTo(ctx.origin()) > EnergyCosts.FREE_RADIUS) {
        ctx.useEnergyAt(pos, 1);
      }
      BlockPos blockPos = Util.vecToPos(pos);
      if(ctx.world()
        .getBlockState(blockPos)
        .getRegistryEntry()
        .getKey()
        .get()
        .getValue()
        .equals(Identifier.of("minecraft", block))
      ) {
        return 1.0;
      } else {
        return 0.0;
      }
    }
  }
  
  // In-world effects
  public record AccelEntity(short slotEntity, short slotVel) implements Instr {
    public void exec(Executor.ExecutionContext ctx, Entity ent, Vec3d vel) {
      ctx.useEnergyAt(ent.getPos(), vel.distanceTo(Vec3d.ZERO) * EnergyCosts.ACCEL_COST_FACTOR);
      ent.addVelocity(vel);
      ent.velocityModified = true;
    }
  }
  public record DamageEntity(short slotEntity, short slotDmg) implements Instr {
    public void exec(Executor.ExecutionContext ctx, Entity ent, double dmg) {
      ctx.useEnergyAt(ent.getPos(), Math.pow(dmg, EnergyCosts.DAMAGE_COST_POWER));
      ent.damage(ctx.world(), ctx.world().getDamageSources().magic(), (float)dmg);
    }
  }
  public record MountEntity(short slotBottom, short slotTop) implements Instr {
    public void exec(Executor.ExecutionContext ctx, Entity bottom, Entity top) {
      top.dismountVehicle();
      top.startRiding(bottom, true);
    }
  }
  public record PlaceBlock(short slotPos, String block) implements Instr {
    public void exec(Executor.ExecutionContext ctx, Vec3d pos, String block) {
      // load item and check it's a blockitem
      Item item = Registries.ITEM.get(Identifier.of("minecraft", block));
      if(item == null) {
        throw new AsmError("Invalid block");
      }
      BlockItem blockItem;
      if(item instanceof BlockItem bi) {
        blockItem = bi;
      } else {
        throw new AsmError("The block is not representible by an item");
      }
      
      // check it's in your inventory
      ServerPlayerEntity user = ctx.playerUser();
      if(user == null) {
        return;
      }
      boolean creative = user.isCreative();
      Inventory inv = user.getInventory();
      if(!creative && !inv.containsAny(v -> v.getItem().equals(bi))) {
        throw new AsmError("No such item in your inventory");
      }
      
      // place the block
      BlockPos blockPos = Util.vecToPos(pos);
      BlockState target = ctx.world().getBlockState(blockPos);
      if(target.isReplaceable()) {
        // actually do the stuff
        ctx.world().setBlockState(blockPos, bi.getBlock().getDefaultState(), Block.NOTIFY_ALL);
        if(!creative) {
          for(int i = 0; i < inv.size(); i++) {
            if(inv.getStack(i).getItem().equals(bi)) {
              inv.removeStack(i, 1);
              break;
            }
          }
        }
        ctx.useEnergyAt(pos, Math.max(Math.ceil(bi.getBlock().getHardness()), 1.0));
      }
    }
  }
  public record DestroyBlock(short slotPos) implements Instr {
    public void exec(Executor.ExecutionContext ctx, Vec3d pos) {
      BlockPos blockPos = Util.vecToPos(pos);
      BlockState state = ctx.world().getBlockState(blockPos);
      ctx.useEnergyAt(pos, Math.max(Math.ceil(state.getBlock().getHardness()), 1.0));
      ctx.world().breakBlock(blockPos, true, ctx.playerUser());
    }
  }
  public record Explode(short slotPos, short slotPower) implements Instr {
    public void exec(Executor.ExecutionContext ctx, Vec3d pos, double power) {
      power = Math.clamp(power, 0.0, 6.0);
      ctx.useEnergyAt(pos, EnergyCosts.EXPLODE_COST_FACTOR * Math.pow(EnergyCosts.EXPLODE_COST_BASE, power));
      ctx.world().createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), (float)power, World.ExplosionSourceType.MOB);
    }
  }
  public record SummonLightning(short slotPos) implements Instr {
    public void exec(Executor.ExecutionContext ctx, Vec3d pos) {
      ctx.useEnergyAt(pos, EnergyCosts.LIGHTNING_COST);
      LightningEntity ent = new LightningEntity(EntityType.LIGHTNING_BOLT, ctx.world());
      ent.setPosition(pos);
      ctx.world().spawnEntity(ent);
    }
  }
  public record SummonMob(short slotEnt, short slotPos, String entity) implements Instr {
    public Entity exec(Executor.ExecutionContext ctx, Vec3d pos, String entity) {
      Integer energyCost = EnergyCosts.SUMMON_ENTITY_COSTS.get(entity);
      if(energyCost == null) {
        throw new AsmError("Invalid entity");
      }
      ctx.useEnergyAt(pos, energyCost);
      Entity summonedEntity;
      switch(entity) {
        case "pig" -> summonedEntity = new PigEntity(EntityType.PIG, ctx.world());
        case "chicken" -> summonedEntity = new ChickenEntity(EntityType.CHICKEN, ctx.world());
        case "zombie" -> summonedEntity = new ZombieEntity(EntityType.ZOMBIE, ctx.world());
        case "skeleton" -> summonedEntity = new SkeletonEntity(EntityType.SKELETON, ctx.world());
        case "arrow" -> summonedEntity = new ArrowEntity(EntityType.ARROW, ctx.world());
        case "snowball" -> summonedEntity = new SnowballEntity(EntityType.SNOWBALL, ctx.world());
        case "fireball" -> summonedEntity = new FireballEntity(EntityType.FIREBALL, ctx.world());
        default -> throw new RuntimeException("unreachable");
      }
      summonedEntity.setPosition(pos);
      ctx.world().spawnEntity(summonedEntity);
      return summonedEntity;
    }
  }
  
  // Misc
  public record Copy(short slotDst, short slotSrc) implements Instr {
    public Object exec(Executor.ExecutionContext ctx, Object o) {
      return o;
    }
  }
}
