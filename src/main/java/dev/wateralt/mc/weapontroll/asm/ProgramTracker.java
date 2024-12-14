package dev.wateralt.mc.weapontroll.asm;

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
    programs.getOrDefault(tick + tickDelay, new ArrayList<>()).add(state);
  }
  
  public void run(Program.State state) {
    if(state.isFinished()) {
      return;
    }
    int wait = state.run();
    if(!state.isFinished() && wait != 0) {
      this.schedule(state, wait);
    }
  }
  
  public void tick() {
    tick++;
    List<Long> keys = programs.keySet().stream().toList();
    keys.stream().filter(i -> i <= tick).forEach(i -> programs.remove(i).forEach(this::run));
  }
}
