package dev.wateralt.mc.weapontroll.asm.magic;

import dev.wateralt.mc.weapontroll.asm.AsmError;
import dev.wateralt.mc.weapontroll.asm.Program;
import dev.wateralt.mc.weapontroll.spell.ExecContext;

import java.util.function.Function;

import static dev.wateralt.mc.weapontroll.asm.magic.Instructions.INSTRUCTIONS;

public class MagicProgramState implements Program.State {
  private MagicProgram program;
  private ExecContext ctx;
  private int pc;
  
  public MagicProgramState(MagicProgram program, ExecContext ctx) {
    this.program = program;
    this.ctx = ctx;
  }
  
  public MagicProgram program() {
    return program;
  }
  
  public ExecContext ctx() {
    return ctx;
  }
  
  public int pc() { return pc; }
    
  @Override
  public int run() {
    String[] instrs = program.getInstructions();
    for(; pc < instrs.length; pc++) {
      Function<MagicProgramState, InstructionStatus> func = INSTRUCTIONS.get(instrs[pc]);
      if(func == null) {
        throw new AsmError("\"%s\" isn't an incantation".formatted(instrs[pc]));
      }
      InstructionStatus status = func.apply(this);
      if(status.halt()) {
        pc = program.getInstructions().length;
        return 0;
      } else if(status.waitTicks() != 0) {
        return status.waitTicks();
      }
    }
    return 0;
  }

  @Override
  public boolean isFinished() {
    return pc == program.getInstructions().length;
  }
}
