package dev.wateralt.mc.weapontroll;

import dev.wateralt.mc.weapontroll.playertracker.PlayerTracker;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Weapontroll implements ModInitializer {
  public static final Logger LOGGER = LoggerFactory.getLogger(Weapontroll.class);
  public static final PlayerTracker PLAYER_TRACKER = new PlayerTracker();
  @Override
  public void onInitialize() {
  }
}
