package dev.wateralt.mc.weapontroll.asm.std20;

import dev.wateralt.mc.weapontroll.Util;
import dev.wateralt.mc.weapontroll.asm.AsmError;
import dev.wateralt.mc.weapontroll.asm.ExecContext;
import dev.wateralt.mc.weapontroll.mixin.FireballEntityAccessor;
import dev.wateralt.mc.weapontroll.mixin.ServerPlayerEntityAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Instructions {

  // General instrs
  public static Object mov(Std20ProgramState state, Object val) {
    return val;
  }
  public static double isnull(Std20ProgramState state, Object val) { 
    if(val == null) return 1.0;
    else return 0.0;
  }
  public static void print(Std20ProgramState state, Object val) {
    if(state.getContext().user() instanceof ServerPlayerEntity spe) {
      String str = sifyd(state, val);
      spe.sendMessage(Text.of("program: " + str));
    }
  }
  
  // Number manip instrs
  public static double add(Std20ProgramState state, double a, double b) {
    return a + b;
  }
  public static double sub(Std20ProgramState state, double a, double b) {
    return a - b;
  }
  public static double mul(Std20ProgramState state, double a, double b) {
    return a * b;
  }
  
  public static double div(Std20ProgramState state, double a, double b) {
    return a / b;
  }
  public static double round(Std20ProgramState state, double a) {
    return Math.round(a);
  }
  public static double sqrt(Std20ProgramState state, double a) {
    return Math.sqrt(a);
  }
  public static double floor(Std20ProgramState state, double a) {
    return Math.floor(a);
  }
  public static double ceil(Std20ProgramState state, double a) {
    return Math.ceil(a);
  }
  public static double sin(Std20ProgramState state, double a) {
    return Math.sin(a);
  }
  public static double cos(Std20ProgramState state, double a) {
    return Math.cos(a);
  }
  
  // Branching instrs
  public static void label(Std20ProgramState state, String label) { }
  public static void jmpl(Std20ProgramState state, String label, double a, double b) {
    if(a < b) state.jumpTo(label);
  }

  public static void jmple(Std20ProgramState state, String label, double a, double b) {
    if(a <= b) state.jumpTo(label);
  }

  public static void jmpg(Std20ProgramState state, String label, double a, double b) {
    if(a > b) state.jumpTo(label);
  }

  public static void jmpge(Std20ProgramState state, String label, double a, double b) {
    if(a >= b) state.jumpTo(label);
  }
  public static void jmpe(Std20ProgramState state, String label, Object a, Object b) {
    if(Objects.equals(a, b)) state.jumpTo(label);
  }

  public static void jmpne(Std20ProgramState state, String label, Object a, Object b) {
    if(!Objects.equals(a, b)) state.jumpTo(label);
  }
  
  // Vector manip instrs
  public static Vec3d makevec(Std20ProgramState state, double x, double y, double z) {
    return new Vec3d(x, y, z);
  }
  public static double vx(Std20ProgramState state, Vec3d v) {
    return v.getX();
  }
  public static double vy(Std20ProgramState state, Vec3d v) {
    return v.getY();
  }
  public static double vz(Std20ProgramState state, Vec3d v) {
    return v.getZ();
  }
  public static Vec3d vadd(Std20ProgramState state, Vec3d a, Vec3d b) {
    return a.add(b);
  }
  public static Vec3d vsub(Std20ProgramState state, Vec3d a, Vec3d b) {
    return a.subtract(b);
  }
  public static Vec3d vmul(Std20ProgramState state, Vec3d a, double b) {
    return a.multiply(b);
  }
  public static Vec3d vdiv(Std20ProgramState state, Vec3d a, double b) {
    return a.multiply(1 / b);
  }
  public static double vdist(Std20ProgramState state, Vec3d a) {
    return a.length();
  }
  public static Vec3d vnorm(Std20ProgramState state, Vec3d a) {
    return a.normalize();
  }
  public static double vdot(Std20ProgramState state, Vec3d a, Vec3d b) {
    return a.dotProduct(b);
  }
  public static Vec3d vcross(Std20ProgramState state, Vec3d a, Vec3d b) {
    return a.crossProduct(b);
  }
  
  // String manip instrs
  public static double slength(Std20ProgramState state, String a) {
    return a.length();
  }
  public static String scharat(Std20ProgramState state, String a, double x) {
    int idx = (int)Math.floor(x);
    if(idx < 0 || idx >= a.length()) {
      throw new AsmError("String index out of bounds");
    }
    return a.substring(idx, idx + 1);
  }
  
  public static double scodeat(Std20ProgramState state, String a, double x) {
    int idx = (int)Math.floor(x);
    if(idx < 0 || idx >= a.length()) {
      throw new AsmError("String index out of bounds");
    }
    return a.codePointAt(idx);
  }
  
  public static String ssubstr(Std20ProgramState state, String s, double start, double end) {
    int si = (int)Math.floor(start);
    int ei = (int)Math.floor(end);
    if(si < 0 || si >= s.length() || ei < 0 || ei >= s.length()) {
      throw new AsmError("String index out of bounds");
    }
    if(si > ei) {
      throw new AsmError("Start index must be less than end index");
    }
    return s.substring(si, ei);
  }
  
  public static String sconcat(Std20ProgramState state, String a, String b) {
    return a + b;
  }
  
  public static double ssearch(Std20ProgramState state, String a, String b) {
    return a.indexOf(b);
  }
  
  public static double scmp(Std20ProgramState state, String a, String b) {
    return Math.clamp(a.compareTo(b), -1, 1);
  }
  
  public static String sify(Std20ProgramState state, Object val) {
    String str;
    if(val == null) {
      str = "null";
    } else if(val instanceof Entity ent) {
      str = ent.getName().getString();
    } else if(val instanceof Vec3d vec) {
      str = "[%f, %f, %f]".formatted(vec.getX(), vec.getY(), vec.getZ());
    } else if(val instanceof Double dbl) {
      str = Double.toString(dbl);
    } else if(val instanceof String s) {
      str = s;
    } else {
      str = "object";
    }
    return str;
  }
  
  public static String sifyd(Std20ProgramState state, Object val) {
    String str;
    if(val == null) {
      str = "<null>";
    } else if(val instanceof Entity ent) {
      str = "Entity(%s)".formatted(ent.getName().getString());
    } else if(val instanceof Vec3d vec) {
      str = "Vector" + vec;
    } else if(val instanceof Double dbl) {
      str = "Double(%f)".formatted(dbl);
    } else if(val instanceof String s) {
      str = s;
    } else {
      str = "<unknown>";
    }
    return str;
  }
  
  // World query instrs
  public static Entity findent(Std20ProgramState state, Vec3d pos, double slot) {
    ExecContext ctx = state.getContext();
    Box box = new Box(pos.add(-4, -4, -4), pos.add(4, 4, 4));
    List<Entity> list = ctx.world().getEntitiesByType(TypeFilter.instanceOf(Entity.class), box, EntityPredicates.VALID_ENTITY);
    list.sort((a, b) -> (int) Math.signum(a.getPos().distanceTo(pos) - b.getPos().distanceTo(pos)));

    int slotInt = (int)slot;
    System.out.println("findent idx: " + slotInt);
    if(slotInt < list.size()) {
      return list.get((int)slot);
    } else {
      return null;
    }
  }
  public static Vec3d entpos(Std20ProgramState state, Entity ent) {
    return ent.getPos();
  }
  public static Vec3d entvel(Std20ProgramState state, Entity ent) {
    return ent.getVelocity();
  }
  public static Vec3d entfacing(Std20ProgramState state, Entity ent) {
    return ent.getRotationVector();
  }
  public static double checkblock(Std20ProgramState state, Vec3d pos, String block) {
    ExecContext ctx = state.getContext();
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
  public static void accelent(Std20ProgramState state, Entity ent, Vec3d vel) {
    ExecContext ctx = state.getContext();
    ctx.useEnergyAt(ent.getPos(), vel.distanceTo(Vec3d.ZERO) * EnergyCosts.ACCEL_COST_FACTOR);
    ent.addVelocity(vel);
    ent.velocityModified = true;
  }
  public static void damageent(Std20ProgramState state, Entity ent, double dmg) {
    ExecContext ctx = state.getContext();
    ctx.useEnergyAt(ent.getPos(), Math.pow(dmg, EnergyCosts.DAMAGE_COST_POWER));
    ent.damage(ctx.world(), ctx.world().getDamageSources().magic(), (float)dmg);
  }
  public static void mountent(Std20ProgramState state, Entity bottom, Entity top) {
    if(bottom == top) return;
    AtomicBoolean stop = new AtomicBoolean(false);
    top.getPassengersDeep().iterator().forEachRemaining(v -> {
      if(v == bottom) {
        stop.set(true);
      }
    });
    if(stop.get()) return;
    state.getContext().useEnergy(EnergyCosts.MOUNT_DIST_COST_FACTOR * bottom.getPos().distanceTo(top.getPos()));
    top.startRiding(bottom, true);
    if(bottom instanceof ServerPlayerEntity spe) {
      ServerPlayNetworkHandler handler = ((ServerPlayerEntityAccessor) spe).getNetworkHandler();
      handler.sendPacket(new EntityPassengersSetS2CPacket(spe));
    }
  }
  public static void placeblock(Std20ProgramState state, Vec3d pos, String block) {
    ExecContext ctx = state.getContext();
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
      ctx.useEnergyAt(pos, EnergyCosts.PLACE_COST_FACTOR * Math.max(Math.ceil(bi.getBlock().getHardness()), 1.0));
      ctx.world().setBlockState(blockPos, bi.getBlock().getDefaultState(), Block.NOTIFY_ALL);
      if(!creative) {
        for(int i = 0; i < inv.size(); i++) {
          if(inv.getStack(i).getItem().equals(bi)) {
            inv.removeStack(i, 1);
            break;
          }
        }
      }
    }
  }
  public static void destroyblock(Std20ProgramState state, Vec3d pos) {
    ExecContext ctx = state.getContext();
    BlockPos blockPos = Util.vecToPos(pos);
    BlockState blockState = ctx.world().getBlockState(blockPos);
    float hardness = blockState.getBlock().getHardness();
    if(hardness < 0) return;
    ctx.useEnergyAt(pos, EnergyCosts.DESTROY_COST_FACTOR * Math.max(Math.ceil(hardness), 1.0));
    ctx.world().breakBlock(blockPos, true, ctx.user());
  }
  public static void explode(Std20ProgramState state, Vec3d pos, double power) {
    ExecContext ctx = state.getContext();
    power = Math.clamp(power, 0.0, 6.0);
    ctx.useEnergyAt(pos, EnergyCosts.EXPLODE_COST_FACTOR * Math.pow(EnergyCosts.EXPLODE_COST_BASE, power));
    ctx.world().createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), (float)power, World.ExplosionSourceType.MOB);
  }
  public static void fireballpwr(Std20ProgramState state, Entity ent, double power) {
    if(ent instanceof FireballEntity fireball) {
      state.getContext().useEnergyAt(ent.getPos(), EnergyCosts.EXPLODE_COST_BASE * Math.pow(EnergyCosts.EXPLODE_COST_BASE, power));
      power = Math.clamp(power, 0.0, 6.0);
      ((FireballEntityAccessor) fireball).setExplosionPower((int)Math.round(power));
    }
  }
  public static void lightning(Std20ProgramState state, Vec3d pos) {
    ExecContext ctx = state.getContext();
    ctx.useEnergyAt(pos, EnergyCosts.LIGHTNING_COST);
    LightningEntity ent = new LightningEntity(EntityType.LIGHTNING_BOLT, ctx.world());
    ent.setPosition(pos);
    ctx.world().spawnEntity(ent);
  }
  public static Entity summon(Std20ProgramState state, Vec3d pos, String entity) {
    ExecContext ctx = state.getContext();
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
      case "fireball" -> summonedEntity = new FireballEntity(ctx.world(), ctx.user(), Vec3d.ZERO, 0);
      default -> throw new RuntimeException("unreachable");
    }
    summonedEntity.setPosition(pos);
    ctx.world().spawnEntity(summonedEntity);
    return summonedEntity;
  }
  public static void wait(Std20ProgramState state, double ticks) {
    state.setWait((int)ticks);
  }
}
