package dev.wateralt.mc.weapontroll.asm;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Executor {
  private static void incorrectTypes(Consumer<String> throwErr, Object... args) {
    StringBuilder sb = new StringBuilder();
    sb.append("Incorrect type [");
    for(int i = 0; i < args.length; i++) {
      if(i != 0) {
        sb.append(", ");
      }
      if(Objects.isNull(args[i])) {
        sb.append("<null>");
      } else {
        sb.append(args[i].getClass().getSimpleName());
      }
    }
    sb.append("]");
    throwErr.accept(sb.toString());
  }
  
  public static void execute(Program program, LivingEntity user, LivingEntity target, ServerWorld world, int instructionLimit) {
    int pc = 0;
    List<Instructions.Instr> instrs = program.getInstrs();
    HashMap<String, Integer> labels = program.getLabels();
    Object[] slots = new Object[program.getMaxSlots()];
    slots[0] = user;
    slots[1] = target;
    
    for(int i = 0; i < instructionLimit; i++) {
      if(pc == instrs.size()) {
        return;
      }
      int finalPc = pc;
      Consumer<String> throwErr = s -> { 
        throw new AsmError("Runtime error at pc %d: %s".formatted(finalPc, s));
      };
      
      Instructions.Instr instr = program.getInstrs().get(pc);
      switch(instr) {
        case Instructions.LoadNum x -> {
          slots[x.slotDst()] = x.val();
        }
        case Instructions.Add x -> {
          Object a = slots[x.slotA()];
          Object b = slots[x.slotB()];
          if(a instanceof Double da && b instanceof Double db) {
            slots[x.slotDst()] = da + db;
          } else if(a instanceof Vec3d va && b instanceof Vec3d vb) {
            slots[x.slotDst()] = va.add(vb);
          } else {
            Executor.incorrectTypes(throwErr, a, b);
          }
        }
        case Instructions.Sub x -> {
          Object a = slots[x.slotA()];
          Object b = slots[x.slotB()];
          if(a instanceof Double da && b instanceof Double db) {
            slots[x.slotDst()] = da - db;
          } else if(a instanceof Vec3d va && b instanceof Vec3d vb) {
            slots[x.slotDst()] = va.subtract(vb);
          } else {
            Executor.incorrectTypes(throwErr, a, b);
          }
        }
        case Instructions.Mul x -> {
          Object a = slots[x.slotA()];
          Object b = slots[x.slotB()];
          if(a instanceof Double da && b instanceof Double db) {
            slots[x.slotDst()] = da * db;
          } else if(a instanceof Vec3d va && b instanceof Double db) {
            slots[x.slotDst()] = va.multiply(db);
          } else {
            Executor.incorrectTypes(throwErr, a, b);
          }
        }
        case Instructions.Div x -> {
          Object a = slots[x.slotA()];
          Object b = slots[x.slotB()];
          if(a instanceof Double da && b instanceof Double db) {
            slots[x.slotDst()] = da * db;
          } else if(a instanceof Vec3d va && b instanceof Double db) {
            slots[x.slotDst()] = va.multiply(1 / db);
          } else {
            Executor.incorrectTypes(throwErr, a, b);
          }
        }
        case Instructions.Round x -> {
          Object a = slots[x.slotSrc()];
          if(a instanceof Double da) {
            slots[x.slotDst()] = Math.round(da);
          } else {
            Executor.incorrectTypes(throwErr, a);
          }
        }
        case Instructions.Copy x -> {
          slots[x.slotDst()] = slots[x.slotSrc()];
        }

        case Instructions.LoadVec x -> {
          slots[x.slotDst()] = new Vec3d(x.x(), x.y(), x.z());
        }
        case Instructions.MakeVec x -> {
          Object ox = slots[x.slotX()];
          Object oy = slots[x.slotY()];
          Object oz = slots[x.slotZ()];
          if(ox instanceof Double dx && oy instanceof Double dy && oz instanceof Double dz) {
            slots[x.slotDst()] = new Vec3d(dx, dy, dz);
          } else {
            Executor.incorrectTypes(throwErr, ox, oy, oz);
          }
        }
        case Instructions.SplitVec x -> {
          Object o = slots[x.slotSrc()];
          if(o instanceof Vec3d vo) {
            slots[x.slotX()] = vo.getX();
            slots[x.slotY()] = vo.getY();
            slots[x.slotZ()] = vo.getZ();
          } else {
            Executor.incorrectTypes(throwErr, o);
          }
        }

        case Instructions.JumpIfEqual x -> {
          Object a = slots[x.slotA()];
          Object b = slots[x.slotB()];
          if(a instanceof Double da && b instanceof Double db) {
            if(da.equals(db)) {
              pc = labels.get(x.label());
            }
          } else {
            Executor.incorrectTypes(throwErr, a, b);
          }
        }
        case Instructions.JumpIfLess x -> {
          Object a = slots[x.slotA()];
          Object b = slots[x.slotB()];
          if(a instanceof Double da && b instanceof Double db) {
            if(da < db) {
              pc = labels.get(x.label());
            }
          } else {
            Executor.incorrectTypes(throwErr, a, b);
          }
        }
        case Instructions.Label x -> {
          // no op
        }

        case Instructions.AccelEntity x -> {
          Object a = slots[x.slotEntity()];
          Object b = slots[x.slotVel()];
          if(a instanceof Entity ea && b instanceof Vec3d vb) {
            ea.addVelocity(vb);
          } else {
            Executor.incorrectTypes(throwErr, a, b);
          }
        }
        case Instructions.DamageEntity x -> {
          Object a = slots[x.slotEntity()];
          Object b = slots[x.slotDmg()];
          if(a instanceof Entity ea && b instanceof Double db) {
            ea.damage(world, world.getDamageSources().magic(), db.floatValue());
          } else {
            Executor.incorrectTypes(throwErr, a, b);
          }
        }
        case Instructions.EntityFacing x -> {
          Object a = slots[x.slotEntity()];
          if(a instanceof Entity ea) {
            slots[x.slotFacing()] = ea.getRotationVector();
          } else {
            Executor.incorrectTypes(throwErr, a);
          }
        }
        case Instructions.EntityPos x -> {
          Object a = slots[x.slotEntity()];
          if(a instanceof Entity ea) {
            slots[x.slotPos()] = ea.getPos();
          } else {
            Executor.incorrectTypes(throwErr, a);
          }
        }
        case Instructions.EntityVel x -> {
          Object a = slots[x.slotEntity()];
          if(a instanceof Entity ea) {
            slots[x.slotVel()] = ea.getVelocity();
          } else {
            Executor.incorrectTypes(throwErr, a);
          }
        }
        case Instructions.Explode x -> {
          Object a = slots[x.slotPos()];
          Object b = slots[x.slotPower()];
          if(a instanceof Vec3d va && b instanceof Double db) {
            world.createExplosion(null, va.getX(), va.getY(), va.getZ(), db.floatValue(), World.ExplosionSourceType.MOB);
          } else {
            Executor.incorrectTypes(throwErr, a, b);
          }
        }
        case Instructions.NearestEntity x -> {
          Object a = slots[x.slotPos()];
          Object b = slots[x.slotIndex()];
          if(a instanceof Vec3d va && b instanceof Integer ib) {
            Box box = new Box(va.add(-4, -4, -4), va.add(4, 4, 4));
            List<Entity> list = world.getEntitiesByType(TypeFilter.instanceOf(Entity.class), box, EntityPredicates.VALID_LIVING_ENTITY);
            slots[x.slotEntity()] = list.get(ib);
          } else {
            Executor.incorrectTypes(throwErr, a, b);
          }
        }
        case Instructions.PlaceBlock x -> {
          //todo
        }
        case Instructions.SummonFireball x -> {
          Object a = slots[x.slotPos()];
          if(a instanceof Vec3d va) {
            FireballEntity ent = new FireballEntity(world, user, new Vec3d(0, 0, 0), 2);
            ent.setPosition(va);
            world.spawnEntity(ent);
            slots[x.slotFireball()] = ent;
          } else {
            Executor.incorrectTypes(throwErr, a);
          }
        }
        case Instructions.SummonLightning x -> {
          Object a = slots[x.slotPos()];
          if(a instanceof Vec3d va) {
            LightningEntity ent = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
            ent.setPosition(va);
            world.spawnEntity(ent);
          } else {
            Executor.incorrectTypes(throwErr, a);
          }
        }
      }
      
      pc++;
    }
  }
}
