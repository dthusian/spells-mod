package dev.wateralt.mc.weapontroll.mixin;

import dev.wateralt.mc.weapontroll.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
  @Inject(method = "swingHand", at = @At("HEAD"))
  private void swingHand(Hand hand, CallbackInfo ci) {
    ServerPlayerEntity that = (ServerPlayerEntity) (Object) this;
    ItemStack stack = that.getStackInHand(hand);
    if(stack != null && stack.getItem().equals(Items.WRITABLE_BOOK) && that.isSneaking()) {
      Util.executeProgram(stack, null, that, that.getServerWorld());
    }
  }
}
