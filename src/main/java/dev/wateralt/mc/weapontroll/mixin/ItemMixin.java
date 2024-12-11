package dev.wateralt.mc.weapontroll.mixin;

import dev.wateralt.mc.weapontroll.Util;
import dev.wateralt.mc.weapontroll.asm.ExecContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {
  @Inject(method = "postHit", at = @At("HEAD"))
  private void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
    if(stack.getItem() == Items.WRITABLE_BOOK) {
      if(attacker.getWorld() instanceof ServerWorld sw) {
        Util.executeBook(stack, new ExecContext(sw, attacker, target, target.getPos(), attacker.getRotationVector()));
      }
    }
  }
}
