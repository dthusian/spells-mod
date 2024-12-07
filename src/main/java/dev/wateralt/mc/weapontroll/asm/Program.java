package dev.wateralt.mc.weapontroll.asm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class Program {
  private final List<Instructions.Instr> instrs;
  private final HashMap<String, Integer> labels;
  private final int maxSlots;
  
  public Program(String src, int maxSlots) {
    this.maxSlots = maxSlots;
    String[] lines = src.split("\n");
    instrs = new ArrayList<>();
    for(int i = 0; i < lines.length; i++) {
      try {
        instrs.add(Instructions.parse(lines[i]));
      } catch(AsmError err) {
        throw new AsmError("at line %d: %s".formatted(i + 1, err.getMessage()));
      }
    }
    labels = new HashMap<>();
    for(int i = 0; i < instrs.size(); i++) {
      if(instrs.get(i) instanceof Instructions.Label label) {
        labels.put(label.label(), i);
      }
    }

    // validate instrs
    for(int i = 0; i < instrs.size(); i++) {
      int finalI = i;
      Consumer<Short> validateSlot = s -> {
        if(s >= maxSlots) {
          throw new AsmError("at pc %d: slot %d is greater than the maximum (%d)".formatted(finalI, s, maxSlots - 1));
        }
      };
      Consumer<String> validateLabel = s -> {
        if(!labels.containsKey(s)) {
          throw new AsmError("at pc %d: slot %d is greater than the maximum (%d)".formatted(finalI, s, maxSlots - 1));
        }
      };
      Instructions.Instr instr = instrs.get(i);
      Field[] fields = instr.getClass().getFields();
      for(int j = 0; j < fields.length; j++) {
        if(fields[i].getType() == Short.class) {
          try {
            validateSlot.accept((Short) fields[i].get(instr));
          } catch(Exception err) {
            throw new RuntimeException(err);
          }
        }
      }
      switch(instr) {
        case Instructions.JumpIfEqual x -> validateLabel.accept(x.label());
        case Instructions.JumpIfLess x -> validateLabel.accept(x.label());
        default -> {}
      }
    }
  }
  
  public List<Instructions.Instr> getInstrs() {
    return instrs;
  }
  
  public HashMap<String, Integer> getLabels() {
    return labels;
  }
  
  public int getMaxSlots() {
    return maxSlots;
  }
}
