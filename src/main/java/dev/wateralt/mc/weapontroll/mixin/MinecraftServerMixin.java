package dev.wateralt.mc.weapontroll.mixin;

import com.mojang.datafixers.DataFixer;
import dev.wateralt.mc.weapontroll.Weapontroll;
import dev.wateralt.mc.weapontroll.energy.PlayerTracker;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Identifier;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
  @Inject(method = "<init>", at = @At("RETURN"))
  private void init(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, ApiServices apiServices, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
    MinecraftServer that = (MinecraftServer) (Object) this;
    Weapontroll.SERVER = that;
    BossBarManager bbm = that.getBossBarManager();
    List<Identifier> bossbarIds = new ArrayList<>(bbm.getIds());
    bossbarIds.forEach(v -> {
      if(v.getPath().startsWith("weapontroll_energybar.")) {
        bbm.remove(bbm.get(v));
      }
    });
  }
  
  @Inject(method = "tick", at = @At("HEAD"))
  private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
    try {
      MinecraftServer that = (MinecraftServer) (Object) this;
      if(that.getTicks() % PlayerTracker.TRACK_INTERVAL == 0) {
        Weapontroll.PLAYER_TRACKER.periodic();
      }
      Weapontroll.PROGRAM_TRACKER.tick();
    } catch(Exception err) {
      err.printStackTrace();
    }
  }
}
