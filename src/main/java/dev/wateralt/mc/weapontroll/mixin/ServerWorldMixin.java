package dev.wateralt.mc.weapontroll.mixin;

import dev.wateralt.mc.weapontroll.Weapontroll;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
  @Inject(method = "<init>", at = @At("RETURN"))
  private void init(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List spawners, boolean shouldTickTime, RandomSequencesState randomSequencesState, CallbackInfo ci) {
    BossBarManager bbm = server.getBossBarManager();
    List<Identifier> bossbarIds = new ArrayList<>(bbm.getIds());
    bossbarIds.forEach(v -> {
      if(v.toString().startsWith("weapontroll_energybar.")) {
        bbm.remove(bbm.get(v));
      }
    });
  }
  
  @Inject(method = "tick", at = @At("HEAD"))
  private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
    ServerWorld that = (ServerWorld) (Object) this;
    if(that.getTime() % 20 == 0) {
      Weapontroll.PLAYER_TRACKER.periodic();
    }
  }
}
