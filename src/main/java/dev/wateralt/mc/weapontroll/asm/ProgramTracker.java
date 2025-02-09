package dev.wateralt.mc.weapontroll.asm;

import dev.wateralt.mc.weapontroll.Weapontroll;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProgramTracker {
  private long tick;
  private final HashMap<Long, List<Program.State>> programs;
  
  public ProgramTracker() {
    tick = 0;
    programs = new HashMap<>();
  }
  
  public void schedule(Program.State state, int tickDelay) {
    long future = tick + tickDelay;
    if(!programs.containsKey(future)) {
      programs.put(tick + tickDelay, new ArrayList<>());
    }
    programs.get(tick + tickDelay).add(state);
  }
  
  public void run(Program.State state) {
    ExecContext ctx = state.getContext();
    try {
      if(state.isFinished()) {
        return;
      }
      int wait = state.run();
      if(!state.isFinished() && wait != 0) {
        this.schedule(state, wait);
      }
    } catch(AsmError err) {
      if(ctx.user() instanceof ServerPlayerEntity spe) {
        spe.sendMessage(Text.of("Program failed: " + err.getMessage()));
      }
    } catch(Exception err) {
      if(ctx.user() instanceof ServerPlayerEntity spe) {
        spe.sendMessage(Text.of("Program failed with an unknown error"));
      }
      Weapontroll.LOGGER.warn("Exception occurred while executing program: " + err);
      err.printStackTrace();
    }
  }
  
  public void tick() {
    tick++;
    List<Long> keys = programs.keySet().stream().toList();
    keys.stream().filter(i -> i <= tick).forEach(i -> programs.remove(i).forEach(this::run));
  }
}
