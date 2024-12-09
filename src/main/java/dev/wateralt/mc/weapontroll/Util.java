package dev.wateralt.mc.weapontroll;

import dev.wateralt.mc.weapontroll.asm.AsmError;
import dev.wateralt.mc.weapontroll.asm.Language;
import dev.wateralt.mc.weapontroll.asm.Languages;
import dev.wateralt.mc.weapontroll.asm.std20.Std20Program;
import dev.wateralt.mc.weapontroll.asm.std20.Std20ProgramState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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
      Language lang = Languages.identify(source);
      if(lang != null) {
        Std20Program prog = new Std20Program(source, 16);
        Std20ProgramState state = prog.prepareRun(sw, attacker.getPos(), attacker, target);
        state.run();
      }
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
  
  public static BlockPos vecToPos(Vec3d vec) {
    return new BlockPos((int)Math.floor(vec.getX()), (int)Math.floor(vec.getY()), (int)Math.floor(vec.getZ()));
  }
}
