package dev.wateralt.mc.weapontroll.mixin;

import com.mojang.brigadier.ParseResults;
import dev.wateralt.mc.weapontroll.Weapontroll;
import dev.wateralt.mc.weapontroll.editor.EditorCmd;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
  @Inject(method = "<init>", at = @At("RETURN"))
  public void init(CommandManager.RegistrationEnvironment environment, CommandRegistryAccess commandRegistryAccess, CallbackInfo ci) {
    CommandManager that = (CommandManager) (Object) this;
    EditorCmd.register(that.getDispatcher());
  }
  
  @Inject(method = "execute", at = @At("RETURN"))
  public void execute(ParseResults<ServerCommandSource> parseResults, String command, CallbackInfo ci) {
    Weapontroll.LOGGER.info("Command: " + command);
  }
}
