package dev.wateralt.mc.weapontroll.asm.magic;

public record InstructionStatus(int waitTicks, boolean halt) {
  public static final InstructionStatus DEFAULT = new InstructionStatus(0, false);
  public static final InstructionStatus HALT = new InstructionStatus(0, true);
}
