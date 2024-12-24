package dev.wateralt.mc.weapontroll.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
  /// Allows entities to ride players. Players are usually marked non-saveable, and by default you can't
  /// ride a non-saveable entity.
  @Redirect(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityType;isSaveable()Z"))
  private boolean isSaveable(EntityType<?> instance) {
    return true;
  }
  
  /// Makes spell fireballs not collide with each other.
  @Inject(method = "canBeHitByProjectile", at = @At("HEAD"), cancellable = true)
  private void canBeHitByProjectile(CallbackInfoReturnable<Boolean> cir) {
    Object that = this;
    if(that instanceof FireballEntity) {
      cir.setReturnValue(false);
      cir.cancel();
    }
  }
  
  /// Fix to make it impossible to build 2-dimension guardian farms (for balancing)
  @Inject(method = "teleportCrossDimension", at = @At("HEAD"), cancellable = true)
  private void teleportCrossDimension(ServerWorld world, TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> cir) {
    Object that = this;
    if(that instanceof GuardianEntity thatEnt) {
      thatEnt.setPosition(new Vec3d(0, -100, 0));
      cir.setReturnValue((Entity) that);
      cir.cancel();
    }
  }
  
  /// Transform guardians
  @Inject(method = "onStruckByLightning", at = @At("HEAD"))
  private void onStruckByLightning(ServerWorld world, LightningEntity lightning, CallbackInfo ci) {
    
  }
}
