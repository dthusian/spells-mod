package dev.wateralt.mc.weapontroll.mixin;

import dev.wateralt.mc.weapontroll.Util;
import dev.wateralt.mc.weapontroll.asm.ExecContext;
import dev.wateralt.mc.weapontroll.energy.EnergySources;
import dev.wateralt.mc.weapontroll.energy.EnergyUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
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
        Util.executeBook(stack, new ExecContext(sw, attacker, target, target.getEyePos(), attacker.getRotationVector()));
      }
    }
  }
  
  @Inject(method = "finishUsing", at = @At("HEAD"), cancellable = true)
  private void finishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
    if(user instanceof ServerPlayerEntity spe) {
      if(stack.getItem().equals(Items.SWEET_BERRIES)) {
        EnergyUtil.addEnergy(spe, EnergySources.SWEET_BERRIES);
      } else {
        NbtComponent comp = stack.get(DataComponentTypes.CUSTOM_DATA);
        if(comp != null && comp.contains("weapontroll_mana")) {
          EnergyUtil.addEnergy(spe, comp.getNbt().getInt("weapontroll_mana"));
          if(spe.isCreative()) {
            cir.setReturnValue(stack);
          } else if(stack.getItem().equals(Items.POTION)) {
            cir.setReturnValue(new ItemStack(Items.GLASS_BOTTLE, 1));
          } else {
            cir.setReturnValue(ItemStack.EMPTY);
          }
          cir.cancel();
        }
      }
    }
  }
}
