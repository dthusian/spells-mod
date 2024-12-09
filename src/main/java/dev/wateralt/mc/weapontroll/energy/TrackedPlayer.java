package dev.wateralt.mc.weapontroll.energy;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TrackedPlayer {
  private MinecraftServer server;
  private UUID uuid;
  private int energy;
  private int maxEnergy;
  @Nullable
  private CommandBossBar energyBar;
  
  public TrackedPlayer(MinecraftServer server, UUID uuid, int energy, int maxEnergy) {
    this.server = server;
    this.uuid = uuid;
    this.energy = energy;
    this.maxEnergy = maxEnergy;
    this.energyBar = null;
  }
  
  public ServerPlayerEntity getEntity() {
    return server.getPlayerManager().getPlayer(this.uuid);
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
  
  public void updateAndShowEnergyBar() {
    if(energyBar == null) {
      BossBarManager bbm = server.getBossBarManager();
      CommandBossBar bar = bbm.add(Identifier.of("weapontroll_energybar." + uuid.toString()), Text.of(formatEnergy()));
      bar.setMaxValue(maxEnergy);
      bar.setValue(energy);
      bar.setColor(BossBar.Color.BLUE);
      bar.addPlayer(this.server.getPlayerManager().getPlayer(this.uuid));
      this.energyBar = bar;
    }
    energyBar.setValue(energy);
    energyBar.setMaxValue(maxEnergy);
    energyBar.setName(Text.of(formatEnergy()));
    energyBar.setVisible(true);
  }
  
  public void hideEnergyBar() {
    if(energyBar != null) energyBar.setVisible(false);
  }
  
  public String formatEnergy() {
    return "Energy %d / %d".formatted(energy, maxEnergy);
  }
}
