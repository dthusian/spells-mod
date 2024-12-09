package dev.wateralt.mc.weapontroll;

import dev.wateralt.mc.weapontroll.energy.PlayerTracker;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Weapontroll implements ModInitializer {
  public static final Logger LOGGER = LoggerFactory.getLogger(Weapontroll.class);
  public static final PlayerTracker PLAYER_TRACKER = new PlayerTracker();
  public static MinecraftServer SERVER;
  @Override
  public void onInitialize() {
    
  }
}
