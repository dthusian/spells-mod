package dev.wateralt.mc.weapontroll.playertracker;

import dev.wateralt.mc.weapontroll.asm.EnergyCosts;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.UUID;

public class PlayerTracker {
  private final HashMap<UUID, TrackedPlayer> players = new HashMap<>();
  
  public PlayerTracker() {}
  
  public TrackedPlayer track(ServerPlayerEntity entity) {
    TrackedPlayer pl = new TrackedPlayer(entity.getServer(), entity.getUuid(), 1000, 1000);
    players.put(entity.getUuid(), pl);
    return pl;
  }
  
  public TrackedPlayer get(ServerPlayerEntity entity) {
    TrackedPlayer pl = players.get(entity.getUuid());
    if(pl != null) return pl;
    else return this.track(entity);
  }
  
  public void periodic() {
    this.players.forEach((k, v) -> {
      v.addEnergy(EnergyCosts.ENERGY_REGEN);
      if(v.getEnergy() != v.getMaxEnergy() && !v.getEntity().isDisconnected()) {
        v.updateAndShowEnergyBar();
      } else {
        v.hideEnergyBar();
      }
    });
  }
}
