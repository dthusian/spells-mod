package dev.wateralt.mc.weapontroll.asm.amagus;

import dev.wateralt.mc.weapontroll.asm.Program;
import dev.wateralt.mc.weapontroll.spell.ExecContext;

public class AmagusProgram implements Program {
  private Object[] sexpr;
  
  public AmagusProgram(Object[] expr) {
    this.sexpr = expr;
  }
  
  public Object[] getSExpr() {
    return sexpr;
  }

  @Override
  public AmagusProgramState prepareRun(ExecContext ctx) {
    return new AmagusProgramState(this, ctx);
  }
}
