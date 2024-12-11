package dev.wateralt.mc.weapontroll.asm.magic;

import dev.wateralt.mc.weapontroll.asm.Program;
import dev.wateralt.mc.weapontroll.spell.ExecContext;

public class MagicProgram implements Program {
  private String[] instructions;
  
  public MagicProgram(String[] lines) {
    instructions = lines;
  }
  
  public String[] getInstructions() {
    return instructions;
  }

  @Override
  public MagicProgramState prepareRun(ExecContext context) {
    return new MagicProgramState(this, context);
  }
}
