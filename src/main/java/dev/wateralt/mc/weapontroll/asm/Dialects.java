package dev.wateralt.mc.weapontroll.asm;

import java.util.HashMap;

public class Dialects {
  public static final StdDialect STD_DIALECT = new StdDialect();
  
  public static class StdDialect extends Dialect {
    private static final HashMap<String, Class<? extends Instructions.Instr>> instrs = new HashMap<>();
    
    static {
      instrs.put("loadnum", Instructions.LoadNum.class);
      instrs.put("add", Instructions.Add.class);
      instrs.put("sub", Instructions.Sub.class);
      instrs.put("mul", Instructions.Mul.class);
      instrs.put("div", Instructions.Div.class);
      instrs.put("round", Instructions.Round.class);
      instrs.put("copy", Instructions.Copy.class);
      instrs.put("sqrt", Instructions.Sqrt.class);
      instrs.put("sin", Instructions.Sin.class);
      instrs.put("cos", Instructions.Cos.class);

      instrs.put("label", Instructions.Label.class);
      instrs.put("jmpl", Instructions.JumpIfLess.class);
      instrs.put("jmple", Instructions.JumpIfLessEqual.class);
      instrs.put("jmpg", Instructions.JumpIfGreater.class);
      instrs.put("jmpge", Instructions.JumpIfGreaterEqual.class);
      instrs.put("jmpe", Instructions.JumpIfEqual.class);
      instrs.put("jmpne", Instructions.JumpIfNotEqual.class);

      instrs.put("loadvec", Instructions.LoadVec.class);
      instrs.put("makevec", Instructions.MakeVec.class);
      instrs.put("splitvec", Instructions.SplitVec.class);
      instrs.put("vadd", Instructions.VAdd.class);
      instrs.put("vsub", Instructions.VSub.class);
      instrs.put("vmul", Instructions.VMul.class);
      instrs.put("vdiv", Instructions.VDiv.class);
      instrs.put("vdist", Instructions.VDist.class);
      instrs.put("vnorm", Instructions.VNorm.class);
      instrs.put("vdot", Instructions.VDot.class);
      instrs.put("vcross", Instructions.VCross.class);

      instrs.put("nearestent", Instructions.NearestEntity.class);
      instrs.put("entpos", Instructions.EntityPos.class);
      instrs.put("entvel", Instructions.EntityVel.class);
      instrs.put("entfacing", Instructions.EntityFacing.class);

      instrs.put("accelent", Instructions.AccelEntity.class);
      instrs.put("damageent", Instructions.DamageEntity.class);
      instrs.put("mountent", Instructions.MountEntity.class);
      instrs.put("explode", Instructions.Explode.class);
      instrs.put("placeblock", Instructions.PlaceBlock.class);
      instrs.put("destroyblock", Instructions.DestroyBlock.class);
      instrs.put("lightning", Instructions.SummonLightning.class);
      instrs.put("summon", Instructions.SummonMob.class);
    }
    
    @Override
    public short parseSlot(String str) {
      if(!str.startsWith("$")) throw new AsmError("Expected $ in front of slot name");
      return Short.parseShort(str.substring(1));
    }

    @Override
    public double parseDouble(String str) {
      return Double.parseDouble(str);
    }

    @Override
    public Class<? extends Instructions.Instr> lookupMnemonic(String name) {
      return instrs.get(name);
    }
  }
}
