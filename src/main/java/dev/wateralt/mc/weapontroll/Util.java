package dev.wateralt.mc.weapontroll;

import dev.wateralt.mc.weapontroll.asm.AsmError;
import dev.wateralt.mc.weapontroll.asm.Executor;
import dev.wateralt.mc.weapontroll.asm.Program;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;

import java.util.List;

public class Util {
  public static void executeProgram(ItemStack stack, LivingEntity target, LivingEntity attacker, ServerWorld sw) {
    WritableBookContentComponent content = stack.get(DataComponentTypes.WRITABLE_BOOK_CONTENT);
    if(content == null) return;
    StringBuilder sb = new StringBuilder();
    List<String> pages = content.pages().stream().map(RawFilteredPair::raw).toList();
    pages.forEach(sb::append);
    String source = sb.toString();
    try {
      Program prog = new Program(source, 8);
      Executor.execute(prog, attacker, target, sw, 1024);
    } catch(AsmError err) {
      if(attacker instanceof ServerPlayerEntity spe) {
        spe.sendMessage(Text.of("Program failed: " + err.getMessage()));
      }
    } catch(Exception err) {
      if(attacker instanceof ServerPlayerEntity spe) {
        spe.sendMessage(Text.of("Program failed with an unknown error"));
      }
      Weapontroll.LOGGER.warn("Exception occurred while executing program: " + err);
      err.printStackTrace();
    }
  }
}
