package dev.wateralt.mc.weapontroll.mixin;

import net.minecraft.entity.projectile.FireballEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FireballEntity.class)
public interface FireballEntityAccessor {
  @Accessor("explosionPower")
  void setExplosionPower(int x);
}
