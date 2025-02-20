package dev.wateralt.mc.weapontroll.mixin;

import dev.wateralt.mc.weapontroll.Util;
import dev.wateralt.mc.weapontroll.asm.ExecContext;
import dev.wateralt.mc.weapontroll.energy.EnergySources;
import dev.wateralt.mc.weapontroll.energy.EnergyUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
  @Shadow @Final public MinecraftServer server;

  @Shadow public abstract ServerWorld getServerWorld();

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
  
  @Inject(method = "tick", at = @At("HEAD"))
  private void tick(CallbackInfo ci) {
    ServerPlayerEntity that = (ServerPlayerEntity) (Object) this;
    ServerWorld sw = getServerWorld();
    if(sw.getTime() % 20 == 0 && that.isSleeping()) {
      BlockPos sleepPos = that.getSleepingPosition().get();
      // mana transfer
      sw
        .getPlayers()
        .stream()
        .filter(v -> v.getBlockPos().equals(sleepPos))
        .filter(v -> v.isCrawling())
        .forEach(src -> {
          int srcEnergy = EnergyUtil.getEnergy(src);
          int transfer = srcEnergy * EnergySources.TRANSFER_PERCENT_PER_S / 100;
          EnergyUtil.addEnergy(src, -transfer);
          EnergyUtil.addEnergy(that, transfer);
        });
      // mana stealing
      sw
        .getPlayers()
        .stream()
        .filter(v -> v.getBlockPos().equals(sleepPos))
        .filter(v -> v.hasVehicle())
        .forEach(dst -> {
          int srcEnergy = EnergyUtil.getEnergy(that);
          int transfer = srcEnergy * EnergySources.STEAL_PERCENT_PER_S / 100;
          EnergyUtil.addEnergy(dst, transfer);
          EnergyUtil.addEnergy(that, -transfer);
        });
      
    }
  }
}
