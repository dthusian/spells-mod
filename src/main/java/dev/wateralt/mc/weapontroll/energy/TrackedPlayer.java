package dev.wateralt.mc.weapontroll.energy;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
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
  private int displayEnergyBarTicks;
  
  public TrackedPlayer(MinecraftServer server, UUID uuid) {
    this.server = server;
    this.uuid = uuid;
    this.displayEnergyBarTicks = 0;
  }
  
  public ServerPlayerEntity getEntity() {
    return server.getPlayerManager().getPlayer(this.uuid);
  }
  
  private static boolean shouldDisplayEnergy(ItemStack handItem) {
    if(handItem == null) return false;
    if(handItem.getItem().equals(Items.WRITABLE_BOOK)) return true;

    NbtComponent comp = handItem.get(DataComponentTypes.CUSTOM_DATA);
    if(comp != null && comp.contains("weapontroll_mana")) {
      return true;
    }
    
    return false;
  }
  
  private CommandBossBar getOrCreateBar(ServerPlayerEntity spl, Text energyStr, int energy, int maxEnergy) {
    BossBarManager bbm = server.getBossBarManager();
    Identifier id = Identifier.of("weapontroll:weapontroll_energybar." + uuid.toString());
    CommandBossBar bar = bbm.get(id);
    if(bar == null) {
      bar = bbm.add(id, energyStr);
      bar.setColor(BossBar.Color.BLUE);
      bar.addPlayer(spl);
    }
    bar.setMaxValue(maxEnergy);
    bar.setValue(energy);
    bar.setName(energyStr);
    bar.setVisible(true);
    return bar;
  }
  
  private void hideBarIfExist(ServerPlayerEntity spl) {
    BossBarManager bbm = server.getBossBarManager();
    Identifier id = Identifier.of("weapontroll:weapontroll_energybar." + uuid.toString());
    CommandBossBar bar = bbm.get(id);
    if(bar != null) {
      bar.removePlayer(spl);
      bbm.remove(bar);
    }
  }
  
  public void periodic() {
    ServerPlayerEntity spl = this.server.getPlayerManager().getPlayer(this.uuid);
    
    if(spl != null && !spl.isDisconnected()) {
      ItemStack mainHand = spl.getMainHandStack();
      ItemStack offHand = spl.getOffHandStack();
      if(shouldDisplayEnergy(mainHand) || shouldDisplayEnergy(offHand)) {
        displayEnergyBarTicks = 100;
      }
    }
    
    if(spl != null && !spl.isDisconnected() && displayEnergyBarTicks > 0) {
      int energy = EnergyUtil.getEnergy(spl);
      int maxEnergy = EnergyUtil.computeMaxEnergy(spl);
      Text energyStr = Text.of(formatEnergy(energy, maxEnergy));
      getOrCreateBar(spl, energyStr, energy, maxEnergy);
    } else if(spl != null) {
      hideBarIfExist(spl);
    }
    
    if(displayEnergyBarTicks > 0) {
      displayEnergyBarTicks -= PlayerTracker.TRACK_INTERVAL;
    }
  }
  
  public String formatEnergy(int energy, int maxEnergy) {
    return "Energy %d / %d".formatted(energy, maxEnergy);
  }
}
