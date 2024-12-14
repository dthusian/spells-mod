package dev.wateralt.mc.weapontroll.asm.phys;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class InfuseUtil {
  public static void infuseFirst(ServerWorld world, Inventory inv, Vec3d position, Vec3d vector, boolean dontConsume) {
    for(int i = 0; i < inv.size(); i++) {
      ItemStack stack = inv.getStack(i);
      if(!stack.isEmpty() && stack.contains(DataComponentTypes.POTION_CONTENTS)) {
        infuse(world, stack, position, vector);
        if(!dontConsume) {
          stack.decrement(1);
        }
      }
    }
  }
  
  public static void infuse(ServerWorld world, ItemStack stack, Vec3d position, Vec3d vector) {
    PotionContentsComponent component = stack.get(DataComponentTypes.POTION_CONTENTS);
    if(component != null) {
      PotionEntity ent = new PotionEntity(EntityType.POTION, world);
      ent.setPosition(position.subtract(vector.normalize().multiply(0.15)));
      ent.setVelocity(vector.normalize().multiply(0.1));
      ent.velocityModified = true;
      ent.setItem(stack.copy());
      world.spawnEntity(ent);
    }
  }
}
