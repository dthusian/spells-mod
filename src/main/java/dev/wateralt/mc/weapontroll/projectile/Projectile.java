package dev.wateralt.mc.weapontroll.projectile;

import dev.wateralt.mc.weapontroll.Util;
import dev.wateralt.mc.weapontroll.asm.ExecContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Projectile {
  public static FireballEntity create(ServerWorld world, LivingEntity caster, Vec3d pos, Vec3d dir, String spell) {
    Vec3d vel = dir.normalize().multiply(0.1);
    FireballEntity ent = new FireballEntity(EntityType.FIREBALL, world);
    ent.setOwner(caster);
    ent.setPosition(pos.add(dir.normalize()));
    ent.setVelocity(vel);
    ItemStack stack = new ItemStack(Items.NETHER_STAR, 1);
    NbtCompound nbt = new NbtCompound();
    nbt.putString("weapontroll_spell", spell);
    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    ent.setItem(stack);
    ent.setBoundingBox(new Box(pos.add(0.5), pos.subtract(0.5)));
    world.spawnEntity(ent);
    return ent;
  }
  
  public static void execute(FireballEntity ent, LivingEntity target) {
    NbtComponent component = ent.getStack().get(DataComponentTypes.CUSTOM_DATA);
    if(component == null) return;
    AtomicReference<String> src = new AtomicReference<>();
    component.apply(v -> src.set(v.getString("weapontroll_spell")));
    String srcO = src.get();
    if(srcO.isEmpty()) return;
    if(ent.getWorld() instanceof ServerWorld sw && ent.getOwner() instanceof LivingEntity owner) {
      Util.executeString(List.of(srcO), new ExecContext(sw, owner, target, ent.getPos(), ent.getVelocity()));
    }
  }
}
