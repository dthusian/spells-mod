package dev.wateralt.mc.weapontroll.mixin;

import dev.wateralt.mc.weapontroll.energy.EnergySources;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin {
  @Inject(method = "craft", at = @At("RETURN"))
  private static void craft(World world, BlockPos pos, DefaultedList<ItemStack> slots, CallbackInfo ci) {
    for(int i = 0; i < 3; i++) {
      if(slots.get(i).isEmpty()) continue;
      PotionContentsComponent contents = slots.get(i).get(DataComponentTypes.POTION_CONTENTS);
      if(contents.potion().isPresent() && contents.potion().get().equals(Potions.INVISIBILITY)) {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("weapontroll_mana", EnergySources.INVIS_POT);
        NbtComponent comp = NbtComponent.of(nbt);
        slots.get(i).set(DataComponentTypes.CUSTOM_DATA, comp);
      }
    }
  }
}
