package dev.wateralt.mc.weapontroll.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.FireballEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
  /// Allows entities to ride players. Players are usually marked non-saveable, and by default you can't
  /// ride a non-saveable entity.
  @Redirect(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityType;isSaveable()Z"))
  private boolean isSaveable(EntityType<?> instance) {
    return true;
  }
  
  @Inject(method = "canBeHitByProjectile", at = @At("HEAD"), cancellable = true)
  private void canBeHitByProjectile(CallbackInfoReturnable<Boolean> cir) {
    Object that = this;
    if(that instanceof FireballEntity) {
      cir.setReturnValue(false);
      cir.cancel();
    }
  }
}
