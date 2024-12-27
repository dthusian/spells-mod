package dev.wateralt.mc.weapontroll.ritual;

import dev.wateralt.mc.weapontroll.Util;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.List;

public class RitualTracker {
  private static class State {
    RitualDef def;
    ServerWorld world;
    BlockPos pos;
    int errorCooldown;
    int chaos;
    CommandBossBar progressBar;
    CommandBossBar chaosBar;
    int tick;
    ItemEntity ritualTarget;
      
    State(int id, ServerWorld world, BlockPos pos, ItemEntity ritualTarget, RitualDef def) {
      chaos = def.initialChaos();
      errorCooldown = 0;
      this.world = world;
      this.pos = pos;
      tick = 0;
      
      ritualTarget.setInvulnerable(true);
      ritualTarget.setNeverDespawn();
      
      BossBarManager bbm = world.getServer().getBossBarManager();
      progressBar = bbm.add(
        Identifier.of("weapontroll", "ritualbar" + id),
        Text.literal("Ritual")
      );
      progressBar.setMaxValue(def.time());
      progressBar.setVisible(true);
      progressBar.setDarkenSky(true);
      progressBar.setColor(BossBar.Color.PURPLE);
      
      chaosBar = bbm.add(
        Identifier.of("weapontroll", "chaosbar" + id),
        Text.literal("Chaos")
      );
      chaosBar.setMaxValue(def.maxChaos());
      chaosBar.setVisible(true);
      chaosBar.setColor(BossBar.Color.RED);
      
      updateProgressBars();
    }
    
    void updateProgressBars() {
      // update bars
      progressBar.setValue(tick);
      chaosBar.setValue(chaos);
      
      // set players
      List<ServerPlayerEntity> entities = world.getPlayers(v -> v.getPos().distanceTo(Util.posToVec(pos)) < 16);
      HashSet<ServerPlayerEntity> entitiesSetTarget = new HashSet<>(entities);
      HashSet<ServerPlayerEntity> entitiesSetCurrentUnion = new HashSet<>(progressBar.getPlayers());
      entitiesSetCurrentUnion.addAll(chaosBar.getPlayers());
      HashSet<ServerPlayerEntity> entitiesSetCurrentIntersection = new HashSet<>(progressBar.getPlayers());
      entitiesSetCurrentUnion.retainAll(chaosBar.getPlayers());

      // add entities that are supposed to be in but aren't
      for(ServerPlayerEntity ent : entitiesSetTarget) {
        if(!entitiesSetCurrentIntersection.contains(ent)) {
          progressBar.addPlayer(ent);
          chaosBar.addPlayer(ent);
        }
      }
      // remove entities that aren't supposed to be in but are
      for(ServerPlayerEntity ent : entitiesSetCurrentUnion) {
        if(entitiesSetTarget.contains(ent)) {
          progressBar.removePlayer(ent);
          chaosBar.removePlayer(ent);
        }
      }
    }
    
    // Return true to stop the ritual
    boolean tick() {
      if(tick % 4 == 0) {
        updateProgressBars();
      }
      if(tick >= def.time()) {
        onStopRitual(true);
        return true;
      }
      if(chaos >= def.maxChaos()) {
        onStopRitual(false);
        return true;
      }
      
      tick++;
      return false;
    }
    
    void onStopRitual(boolean success) {
      progressBar.clearPlayers();
      chaosBar.clearPlayers();
      world.getServer().getBossBarManager().remove(progressBar);
      world.getServer().getBossBarManager().remove(chaosBar);
      
      if(!success) {
        
      }
    }
  }
  
  private int nextId = 0;
  private final HashSet<State> rituals = new HashSet<>();
  
  public RitualTracker() {}
  
  public void tick() {
    rituals.removeIf(State::tick);
  }
  
  public void start(ServerWorld world, BlockPos pos, ItemEntity ritualTarget, RitualDef def) {
    rituals.add(new State(nextId, world, pos, ritualTarget, def));
    nextId++;
  }
}
