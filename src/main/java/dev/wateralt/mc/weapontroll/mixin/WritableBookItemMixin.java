package dev.wateralt.mc.weapontroll.mixin;

import dev.wateralt.mc.weapontroll.Util;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WritableBookItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WritableBookItem.class)
public abstract class WritableBookItemMixin {
  @Inject(method = "use", at = @At("HEAD"), cancellable = true)
  private void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
    ItemStack stack = user.getStackInHand(hand);
    if(stack != null && stack.getItem() == Items.WRITABLE_BOOK && !user.isSneaking()) {
      if(user.getWorld() instanceof ServerWorld sw) {
        Util.executeProgram(stack, null, user, sw);
        cir.setReturnValue(ActionResult.SUCCESS);
        cir.cancel();
      }
    }
  }
}
