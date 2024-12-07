package dev.wateralt.mc.weapontroll.playertracker;

import net.minecraft.server.network.ServerPlayerEntity;

public class TrackedPlayer {
  ServerPlayerEntity entity;
  int energy;
  int maxEnergy;
  
  public TrackedPlayer(ServerPlayerEntity entity, int energy, int maxEnergy) {
    
  }
  
  public ServerPlayerEntity getEntity() {
    return entity;
  }
  
  public int getEnergy() {
    return energy;
  }
  
  public int getMaxEnergy() {
    return maxEnergy;
  }
  
  public void setEnergy(int newEnergy) {
    energy = newEnergy;
  }
  
  public void addEnergy(int amount) {
    energy += amount;
    if(energy < 0) energy = 0;
    if(energy > maxEnergy) energy = maxEnergy;
  }
}
