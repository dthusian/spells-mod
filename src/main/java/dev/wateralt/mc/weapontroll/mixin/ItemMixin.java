package dev.wateralt.mc.weapontroll.mixin;

import dev.wateralt.mc.weapontroll.Weapontroll;
import dev.wateralt.mc.weapontroll.asm.AsmError;
import dev.wateralt.mc.weapontroll.asm.Executor;
import dev.wateralt.mc.weapontroll.asm.Program;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Item.class)
public abstract class ItemMixin {
  @Inject(method = "postHit", at = @At("HEAD"))
  private void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
    if(stack.getItem() == Items.WRITABLE_BOOK) {
      if(attacker.getWorld() instanceof ServerWorld sw) {
        WritableBookContentComponent content = stack.get(DataComponentTypes.WRITABLE_BOOK_CONTENT);
        if(content == null) return;
        StringBuilder sb = new StringBuilder();
        List<String> pages = content.pages().stream().map(RawFilteredPair::raw).toList();
        pages.forEach(sb::append);
        String source = sb.toString();
        try {
          Program prog = new Program(source, 8);
          Executor.execute(prog, attacker, target, sw, 128);
        } catch(AsmError err) {
          if(attacker instanceof ServerPlayerEntity spe) {
            spe.sendMessage(Text.of("Program failed: " + err.getMessage()));
          }
        } catch(Exception err) {
          Weapontroll.LOGGER.warn("Exception occurred while executing program: " + err);
        }
      }
    }
  }
}
