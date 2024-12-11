package dev.wateralt.mc.weapontroll.asm;

public interface Program {
  interface State {
    /// Runs the program until the next wait point,
    /// and return the number of ticks to wait.
    /// If isFinished would return true, must not do anything.
    int run();
    /// Whether the function is finished executing.
    boolean isFinished();
  }
  State prepareRun(ExecContext context);
}
