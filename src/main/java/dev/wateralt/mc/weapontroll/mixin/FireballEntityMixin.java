package dev.wateralt.mc.weapontroll.mixin;

import dev.wateralt.mc.weapontroll.asm.phys.Projectile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireballEntity.class)
public abstract class FireballEntityMixin {
  @Inject(method = "onCollision", at = @At("HEAD"), cancellable = true)
  private void onCollision(HitResult hitResult, CallbackInfo ci) {
    FireballEntity that = (FireballEntity) (Object) this;
    if(that.getStack().getItem().equals(Items.NETHER_STAR)) {
      ci.cancel();
      LivingEntity hit = null;
      if(hitResult instanceof EntityHitResult res && res.getEntity() instanceof LivingEntity livingHit) {
        hit = livingHit;
      }
      Projectile.execute(that, hit);
      that.discard();
    }
  }
}
