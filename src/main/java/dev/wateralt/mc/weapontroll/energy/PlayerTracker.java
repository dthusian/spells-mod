package dev.wateralt.mc.weapontroll.energy;

import dev.wateralt.mc.weapontroll.Weapontroll;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.UUID;

public class PlayerTracker {
  public static final int TRACK_INTERVAL = 5;
  private final HashMap<UUID, TrackedPlayer> players = new HashMap<>();
  
  public PlayerTracker() {}
  
  public TrackedPlayer track(ServerPlayerEntity entity) {
    TrackedPlayer pl = new TrackedPlayer(entity.getServer(), entity.getUuid());
    players.put(entity.getUuid(), pl);
    return pl;
  }
  
  public void periodic() {
    Weapontroll.SERVER.getPlayerManager()
      .getPlayerList()
      .stream()
      .filter(v -> !players.containsKey(v.getUuid()))
      .forEach(this::track);
    this.players.forEach((k, v) -> {
      v.periodic();
    });
  }
}
