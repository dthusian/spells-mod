package dev.wateralt.mc.weapontroll;

import dev.wateralt.mc.weapontroll.asm.AsmError;
import dev.wateralt.mc.weapontroll.asm.Language;
import dev.wateralt.mc.weapontroll.asm.Languages;
import dev.wateralt.mc.weapontroll.asm.Program;
import dev.wateralt.mc.weapontroll.asm.ExecContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class Util {
  public static void executeString(List<String> src, ExecContext ctx) {
    if(!ctx.user().isAlive()) return;
    try {
      if(src.size() == 0) {
        return;
      }
      Language lang = Languages.identify(src.get(0));
      if(lang != null) {
        Program prog = lang.compile(src);
        Program.State state = prog.prepareRun(ctx);
        state.run();
      }
    } catch(AsmError err) {
      if(ctx.user() instanceof ServerPlayerEntity spe) {
        spe.sendMessage(Text.of("Program failed: " + err.getMessage()));
      }
    } catch(Exception err) {
      if(ctx.user() instanceof ServerPlayerEntity spe) {
        spe.sendMessage(Text.of("Program failed with an unknown error"));
      }
      Weapontroll.LOGGER.warn("Exception occurred while executing program: " + err);
      err.printStackTrace();
    }
  }
  
  public static void executeBook(ItemStack stack, ExecContext ctx) {
    WritableBookContentComponent content = stack.get(DataComponentTypes.WRITABLE_BOOK_CONTENT);
    if(content == null) return;
    List<String> pages = content.pages().stream().map(RawFilteredPair::raw).toList();
    executeString(pages, ctx);
  }
  
  public static BlockPos vecToPos(Vec3d vec) {
    return new BlockPos((int)Math.floor(vec.getX()), (int)Math.floor(vec.getY()), (int)Math.floor(vec.getZ()));
  }
  
  public static double normal(double x) {
    return Math.sqrt(2) * Math.tan(2 * x - 1);
  }
  
  public static RegistryEntry<Potion> MANA_POTION = null;
}
