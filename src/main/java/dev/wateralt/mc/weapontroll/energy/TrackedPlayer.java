package dev.wateralt.mc.weapontroll.energy;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TrackedPlayer {
  private final MinecraftServer server;
  private final UUID uuid;
  @Nullable
  private CommandBossBar energyBar;
  private int displayEnergyBarTicks;
  
  public TrackedPlayer(MinecraftServer server, UUID uuid) {
    this.server = server;
    this.uuid = uuid;
    this.energyBar = null;
    this.displayEnergyBarTicks = 0;
  }
  
  public ServerPlayerEntity getEntity() {
    return server.getPlayerManager().getPlayer(this.uuid);
  }
  
  public void periodic() {
    ServerPlayerEntity spl = this.server.getPlayerManager().getPlayer(this.uuid);
    
    if(spl != null && !spl.isDisconnected()) {
      ItemStack mainHand = spl.getMainHandStack();
      ItemStack offHand = spl.getOffHandStack();
      boolean shouldDisplayEnergy = false;
      if(mainHand != null && mainHand.getItem().equals(Items.WRITABLE_BOOK)) {
        shouldDisplayEnergy = true;
      }
      if(offHand != null && offHand.getItem().equals(Items.WRITABLE_BOOK)) {
        shouldDisplayEnergy = true;
      }
      if(shouldDisplayEnergy) {
        displayEnergyBarTicks = 100;
      }
    }
    
    if(spl != null && !spl.isDisconnected() && displayEnergyBarTicks > 0) {
      int energy = EnergyUtil.getEnergy(spl);
      int maxEnergy = EnergyUtil.computeMaxEnergy(spl);
      Text energyStr = Text.of(formatEnergy(energy, maxEnergy));
      if(energyBar == null) {
        BossBarManager bbm = server.getBossBarManager();
        CommandBossBar bar = bbm.add(Identifier.of("weapontroll:weapontroll_energybar." + uuid.toString()), energyStr);
        bar.setColor(BossBar.Color.BLUE);
        bar.addPlayer(spl);
        this.energyBar = bar;
      }
      energyBar.setMaxValue(maxEnergy);
      energyBar.setValue(energy);
      energyBar.setName(energyStr);
      energyBar.setVisible(true);
    } else if(energyBar != null) {
      energyBar.setVisible(false);
    }
    
    if(displayEnergyBarTicks > 0) {
      displayEnergyBarTicks -= PlayerTracker.TRACK_INTERVAL;
    }
  }
  
  public String formatEnergy(int energy, int maxEnergy) {
    return "Energy %d / %d".formatted(energy, maxEnergy);
  }
}
