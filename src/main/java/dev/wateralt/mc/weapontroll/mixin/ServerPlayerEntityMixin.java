package dev.wateralt.mc.weapontroll.mixin;

import dev.wateralt.mc.weapontroll.Util;
import dev.wateralt.mc.weapontroll.asm.ExecContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
  @Shadow @Final public MinecraftServer server;
  
  @Unique
  private Entity lastVehicle = null;

  @Inject(method = "swingHand", at = @At("HEAD"))
  private void swingHand(Hand hand, CallbackInfo ci) {
    ServerPlayerEntity that = (ServerPlayerEntity) (Object) this;
    if(that.isSneaking()) {
      ItemStack stack1 = that.getMainHandStack();
      ExecContext ctx = new ExecContext(that.getServerWorld(), that, that, that.getEyePos(), that.getRotationVector());
      if(stack1 != null && stack1.getItem().equals(Items.WRITABLE_BOOK)) {
        Util.executeBook(stack1, ctx);
      }
    }
  }

  @Inject(method = "stopRiding", at = @At("HEAD"))
  private void stopRidingHead(CallbackInfo ci) {
    ServerPlayerEntity that = (ServerPlayerEntity)(Object) this;
    lastVehicle = that.getVehicle();
  }
  
  @Inject(method = "stopRiding", at = @At("RETURN"))
  private void stopRidingTail(CallbackInfo ci) {
    if(lastVehicle != null) {
      server.getPlayerManager().getPlayerList().forEach(v -> {
        v.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(lastVehicle));
      });
    }
  }
}
