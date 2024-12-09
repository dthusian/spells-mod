package dev.wateralt.mc.weapontroll.asm.std20;

import dev.wateralt.mc.weapontroll.Util;
import dev.wateralt.mc.weapontroll.asm.AsmError;
import dev.wateralt.mc.weapontroll.asm.EnergyCosts;
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
  
  // Number manip instrs
  public static Object mov(Std20ProgramState ctx, Object val) {
    return val;
  }
  public static double add(Std20ProgramState ctx, double a, double b) {
    return a + b;
  }
  public static double sub(Std20ProgramState ctx, double a, double b) {
    return a - b;
  }
  public static double mul(Std20ProgramState ctx, double a, double b) {
    return a * b;
  }
  
  public static double div(Std20ProgramState ctx, double a, double b) {
    return a / b;
  }
  public static double round(Std20ProgramState ctx, double a) {
    return Math.round(a);
  }
  public static double sqrt(Std20ProgramState ctx, double a) {
    return Math.sqrt(a);
  }
  public static double sin(Std20ProgramState ctx, double a) {
    return Math.sin(a);
  }
  public static double cos(Std20ProgramState ctx, double a) {
    return Math.cos(a);
  }
  
  // Branching instrs
  public static void label(Std20ProgramState ctx, String label) { }
  public static void jmpl(Std20ProgramState ctx, String label, double a, double b) {
    if(a < b) ctx.jumpTo(label);
  }

  public static void jmple(Std20ProgramState ctx, String label, double a, double b) {
    if(a <= b) ctx.jumpTo(label);
  }

  public static void jmpg(Std20ProgramState ctx, String label, double a, double b) {
    if(a > b) ctx.jumpTo(label);
  }

  public static void jmpge(Std20ProgramState ctx, String label, double a, double b) {
    if(a > b) ctx.jumpTo(label);
  }
  public static void jmpe(Std20ProgramState ctx, String label, Object a, Object b) {
    if(Objects.equals(a, b)) ctx.jumpTo(label);
  }

  public static void jmpne(Std20ProgramState ctx, String label, Object a, Object b) {
    if(!Objects.equals(a, b)) ctx.jumpTo(label);
  }
  
  // Vector manip instrs
  public static Vec3d makevec(Std20ProgramState ctx, double x, double y, double z) {
    return new Vec3d(x, y, z);
  }
  public static double vx(Std20ProgramState ctx, Vec3d v) {
    return v.getX();
  }
  public static double vy(Std20ProgramState ctx, Vec3d v) {
    return v.getY();
  }
  public static double vz(Std20ProgramState ctx, Vec3d v) {
    return v.getZ();
  }
  public static Vec3d vadd(Std20ProgramState ctx, Vec3d a, Vec3d b) {
    return a.add(b);
  }
  public static Vec3d vsub(Std20ProgramState ctx, Vec3d a, Vec3d b) {
    return a.subtract(b);
  }
  public static Vec3d vmul(Std20ProgramState ctx, Vec3d a, double b) {
    return a.multiply(b);
  }
  public static Vec3d vdiv(Std20ProgramState ctx, Vec3d a, double b) {
    return a.multiply(1 / b);
  }
  public static double vdist(Std20ProgramState ctx, Vec3d a) {
    return a.length();
  }
  public static Vec3d vnorm(Std20ProgramState ctx, Vec3d a) {
    return a.normalize();
  }
  public static double vdot(Std20ProgramState ctx, Vec3d a, Vec3d b) {
    return a.dotProduct(b);
  }
  public static Vec3d vcross(Std20ProgramState ctx, Vec3d a, Vec3d b) {
    return a.crossProduct(b);
  }
  
  // World query instrs
  public static Entity findent(Std20ProgramState ctx, Vec3d pos, double slot) {
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
  public static Vec3d entpos(Std20ProgramState ctx, Entity ent) {
    return ent.getPos();
  }
  public static Vec3d entvel(Std20ProgramState ctx, Entity ent) {
    return ent.getVelocity();
  }
  public static Vec3d entfacing(Std20ProgramState ctx, Entity ent) {
    return ent.getRotationVector();
  }
  public static double checkblcok(Std20ProgramState ctx, Vec3d pos, String block) {
    if(pos.distanceTo(ctx.origin()) > EnergyCosts.LOCAL_RADIUS) {
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
  
  // In-world effects
  public static void accelent(Std20ProgramState ctx, Entity ent, Vec3d vel) {
    ctx.useEnergyAt(ent.getPos(), vel.distanceTo(Vec3d.ZERO) * EnergyCosts.ACCEL_COST_FACTOR);
    ent.addVelocity(vel);
    ent.velocityModified = true;
  }
  public static void damageent(Std20ProgramState ctx, Entity ent, double dmg) {
    ctx.useEnergyAt(ent.getPos(), Math.pow(dmg, EnergyCosts.DAMAGE_COST_POWER));
    ent.damage(ctx.world(), ctx.world().getDamageSources().magic(), (float)dmg);
  }
  public static void mountent(Std20ProgramState ctx, Entity bottom, Entity top) {
    top.dismountVehicle();
    top.startRiding(bottom, true);
  }
  public static void placeblock(Std20ProgramState ctx, Vec3d pos, String block) {
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
    ServerPlayerEntity user = ctx.manaSource();
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
      ctx.useEnergyAt(pos, EnergyCosts.PLACE_COST_FACTOR * Math.max(Math.ceil(bi.getBlock().getHardness()), 1.0));
    }
  }
  public static void destroyblock(Std20ProgramState ctx, Vec3d pos) {
    BlockPos blockPos = Util.vecToPos(pos);
    BlockState state = ctx.world().getBlockState(blockPos);
    ctx.useEnergyAt(pos, EnergyCosts.DESTROY_COST_FACTOR * Math.max(Math.ceil(state.getBlock().getHardness()), 1.0));
    ctx.world().breakBlock(blockPos, true, ctx.user());
  }
  public static void explode(Std20ProgramState ctx, Vec3d pos, double power) {
    power = Math.clamp(power, 0.0, 6.0);
    ctx.useEnergyAt(pos, EnergyCosts.EXPLODE_COST_FACTOR * Math.pow(EnergyCosts.EXPLODE_COST_BASE, power));
    ctx.world().createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), (float)power, World.ExplosionSourceType.MOB);
  }
  public static void lightning(Std20ProgramState ctx, Vec3d pos) {
    ctx.useEnergyAt(pos, EnergyCosts.LIGHTNING_COST);
    LightningEntity ent = new LightningEntity(EntityType.LIGHTNING_BOLT, ctx.world());
    ent.setPosition(pos);
    ctx.world().spawnEntity(ent);
  }
  public static Entity summon(Std20ProgramState ctx, Vec3d pos, String entity) {
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
