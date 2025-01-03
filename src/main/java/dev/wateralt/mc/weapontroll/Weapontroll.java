package dev.wateralt.mc.weapontroll;

import dev.wateralt.mc.weapontroll.asm.ProgramTracker;
import dev.wateralt.mc.weapontroll.energy.PlayerTracker;
import dev.wateralt.mc.weapontroll.ritual.RitualTracker;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Weapontroll implements ModInitializer {
  public static final Logger LOGGER = LoggerFactory.getLogger(Weapontroll.class);
  public static final PlayerTracker PLAYER_TRACKER = new PlayerTracker();
  public static MinecraftServer SERVER;
  public static final ProgramTracker PROGRAM_TRACKER = new ProgramTracker();
  public static final RitualTracker RITUAL_TRACKER = new RitualTracker();
  @Override
  public void onInitialize() {}
}
