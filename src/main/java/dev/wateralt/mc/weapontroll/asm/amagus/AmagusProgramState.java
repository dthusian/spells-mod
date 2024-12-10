package dev.wateralt.mc.weapontroll.asm.amagus;

import dev.wateralt.mc.weapontroll.asm.AsmError;
import dev.wateralt.mc.weapontroll.asm.Program;
import dev.wateralt.mc.weapontroll.spell.ExecContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class AmagusProgramState implements Program.State {
  private AmagusProgram program;
  private ExecContext ctx;
  private int pc;
  
  public AmagusProgramState(AmagusProgram program, ExecContext ctx) {
    this.program = program;
    this.ctx = ctx;
    this.pc = 0;
  }
  
  public ExecContext getContext() {
    return ctx;
  }

  private static Object evalConstant(ExecContext ctx, String name) {
    if(name.equals("caster")) {
      return List.of(ctx.user());
    } else if(name.equals("target")) {
      if(ctx.target() != null) {
        return List.of(ctx.target());
      } else {
        return List.of();
      }
    } else if(name.equals("everyone")) {
      return ctx.world().getEntitiesByType(
        TypeFilter.instanceOf(Entity.class),
        new Box(
          ctx.targetPos().add(EnergyCosts.AOE_RADIUS, EnergyCosts.AOE_RADIUS, EnergyCosts.AOE_RADIUS),
          ctx.targetPos().add(-EnergyCosts.AOE_RADIUS, -EnergyCosts.AOE_RADIUS, -EnergyCosts.AOE_RADIUS)
        ),
        v -> v.getPos().distanceTo(ctx.targetPos()) < EnergyCosts.AOE_RADIUS);
    } else {
      throw new AsmError("Unknown constant " + name);
    }
  }
  
  public Object eval(ExecContext ctx, Object[] cmd) {
    if(cmd[0] instanceof String funcName) {
      Functions.Def funcDef = Functions.FUNCTIONS.get(funcName);
      if(funcDef == null) throw new AsmError("No function named " + funcName);
      // validate args
      Method method = funcDef.method();
      Object[] args;
      if(method.getParameters()[1].isVarArgs()) {
        // varargs recieves everything raw
        args = new Object[]{ this, Arrays.copyOfRange(cmd, 1, cmd.length) };
      } else {
        // non-varargs evaluates subexpressions
        int argsLen = method.getParameterCount();
        if(argsLen != cmd.length) {
          throw new AsmError("Incorrect number of arguments for " + funcName);
        }
        args = new Object[argsLen];
        args[0] = this;
        for(int i = 1; i < cmd.length; i++) {
          if(cmd[i] instanceof Object[] subCmd) {
            args[i] = eval(ctx, subCmd);
          } else if(cmd[i] instanceof String constant) {
            args[i] = evalConstant(ctx, constant);
          } else {
            throw new RuntimeException("Invalid S-expression");
          }
        }
      }

      Object ret;
      try {
        ret = method.invoke(null, args);
      } catch(Exception err) {
        if(err.getCause() instanceof AsmError err2) {
          throw err2;
        } else {
          throw new RuntimeException(err);
        }
      }
      return ret;
    } else {
      throw new AsmError("Unexpected array in command-name");
    }
  }


  @Override
  public boolean isFinished() {
    return pc == program.getSExpr().length;
  }
  
  @Override
  public int run() {
    Object[] sExpression = program.getSExpr();
    for(; pc < sExpression.length; pc++) {
      if(sExpression[pc] instanceof Object[] cmd) {
        Object ret = eval(ctx, cmd);
        if(ret instanceof WaitSideEffect wait) {
          return wait.time();
        }
      }
    }
    return 0;
  }
}
