package dev.wateralt.mc.weapontroll.mixin;

import dev.wateralt.mc.weapontroll.Weapontroll;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
  @Inject(method = "tick", at = @At("HEAD"))
  private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
    ServerWorld that = (ServerWorld) (Object) this;
    if(that.getTime() % 20 == 0) {
      Weapontroll.PLAYER_TRACKER.periodic();
    }
  }
}
