package dev.wateralt.mc.weapontroll.asm;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Instructions {

  public sealed interface Instr { }
  
  // Number manip instrs
  public record LoadNum(int slotDst, double val) implements Instr { }
  public record Add(int slotDst, int slotA, int slotB) implements Instr { }
  public record Sub(int slotDst, int slotA, int slotB) implements Instr { }
  public record Mul(int slotDst, int slotA, int slotB) implements Instr { }
  public record Div(int slotDst, int slotA, int slotB) implements Instr { }
  public record Round(int slotDst, int slotSrc) implements Instr { }
  
  // Branching instrs
  public record Label(String label) implements Instr { }
  public record JumpIfLess(String label, int slotA, int slotB) implements Instr { }
  public record JumpIfEqual(String label, int slotA, int slotB) implements Instr { }
  
  // Vector manip instrs
  public record LoadVec(int slotDst, double x, double y, double z) implements Instr { }
  public record MakeVec(int slotDst, int slotX, int slotY, int slotZ) implements Instr { }
  public record SplitVec(int slotX, int slotY, int slotZ, int slotSrc) implements Instr { }
  
  // Entity manip instrs
  public record NearestEntity(int slotEntity, int slotPos, int slotIndex) implements Instr { }
  public record EntityPos(int slotPos, int slotEntity) implements Instr { }
  public record EntityVel(int slotVel, int slotEntity) implements Instr { }
  public record EntityFacing(int slotFacing, int slotEntity) implements Instr { }
  
  // In-world effects
  public record AccelEntity(int slotEntity, int slotVel) implements Instr { }
  public record DamageEntity(int slotEntity, int slotDmg) implements Instr { }
  public record PlaceBlock(int slotPos, String block) implements Instr { }
  public record Explode(int slotPos, int slotPower) implements Instr { }
  public record SummonLightning(int slotPos) implements Instr { }
  public record SummonFireball(int slotFireball, int slotPos) implements Instr { }
  
  // Misc
  public record Copy(int slotDst, int slotSrc) implements Instr { }
  
  private static final HashMap<String, Class<? extends Instr>> trivialParse = new HashMap<>();
  
  static {
    trivialParse.put("add", Add.class);
    trivialParse.put("sub", Sub.class);
    trivialParse.put("mul", Mul.class);
    trivialParse.put("div", Div.class);
    trivialParse.put("round", Round.class);
    trivialParse.put("makevec", MakeVec.class);
    trivialParse.put("splitvec", SplitVec.class);
    trivialParse.put("nearestent", NearestEntity.class);
    trivialParse.put("entpos", EntityPos.class);
    trivialParse.put("entvel", EntityVel.class);
    trivialParse.put("entfacing", EntityFacing.class);
    trivialParse.put("accelent", AccelEntity.class);
    trivialParse.put("damageent", DamageEntity.class);
    trivialParse.put("explode", Explode.class);
    trivialParse.put("summonlightning", SummonLightning.class);
    trivialParse.put("summonfireball", SummonFireball.class);
    trivialParse.put("copy", Copy.class);
  }
  
  public static Instr parse(String x) {
    String[] parts = x.split(" ");
    String op = parts[0];
    if("loadnum".equals(op)) {
      return new LoadNum(Integer.parseInt(parts[1]), Double.parseDouble(parts[2]));
    } else if("loadvec".equals(op)) {
      return new LoadVec(Integer.parseInt(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Double.parseDouble(parts[4]));
    } else if("label".equals(op)) {
      return new Label(parts[1]);
    } else if("jmpl".equals(op)) {
      return new JumpIfLess(parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
    } else if("jmpe".equals(op)) {
      return new JumpIfEqual(parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
    } else if("placeblock".equals(op)) {
      return new PlaceBlock(Integer.parseInt(parts[1]), parts[2]);
    } else if(trivialParse.containsKey(op)) {
      Class<? extends Instr> cls = trivialParse.get(op);
      Constructor<?> ctor = cls.getConstructors()[0];
      Object[] ints = Arrays.stream(parts).skip(1).map(Integer::parseInt).toArray();
      try {
        return (Instr) ctor.newInstance(ints);
      } catch(Exception err) {
        // womp womp
        return null;
      }
    } else {
      return null;
    }
  }
}
