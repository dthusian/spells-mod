package dev.wateralt.mc.weapontroll.asm;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class Program {
  private final List<Instructions.Instr> instrs;
  private final HashMap<String, Integer> labels;
  private final int maxSlots;
  
  public Program(String src, int maxSlots) {
    this.maxSlots = maxSlots;
    instrs = src.lines().map(Instructions::parse).toList();
    labels = new HashMap<>();
    for(int i = 0; i < instrs.size(); i++) {
      if(instrs.get(i) instanceof Instructions.Label label) {
        labels.put(label.label(), i);
      }
    }

    // validate instrs
    for(int i = 0; i < instrs.size(); i++) {
      int finalI = i;
      Consumer<Integer> validateSlot = s -> {
        if(s >= maxSlots) {
          throw new AsmError("at pc %d: slot %d is greater than the maximum (%d)".formatted(finalI, s, maxSlots - 1));
        }
      };
      Consumer<String> validateLabel = s -> {
        if(!labels.containsKey(s)) {
          throw new AsmError("at pc %d: slot %d is greater than the maximum (%d)".formatted(finalI, s, maxSlots - 1));
        }
      };
      switch(instrs.get(i)) {
        case Instructions.LoadNum x -> {
          validateSlot.accept(x.slotDst());
        }
        case Instructions.Add x -> {
          validateSlot.accept(x.slotDst());
          validateSlot.accept(x.slotA());
          validateSlot.accept(x.slotB());
        }
        case Instructions.Sub x -> {
          validateSlot.accept(x.slotDst());
          validateSlot.accept(x.slotA());
          validateSlot.accept(x.slotB());
        }
        case Instructions.Mul x -> {
          validateSlot.accept(x.slotDst());
          validateSlot.accept(x.slotA());
          validateSlot.accept(x.slotB());
        }
        case Instructions.Div x -> {
          validateSlot.accept(x.slotDst());
          validateSlot.accept(x.slotA());
          validateSlot.accept(x.slotB());
        }
        case Instructions.Round x -> {
          validateSlot.accept(x.slotDst());
          validateSlot.accept(x.slotSrc());
        }
        case Instructions.Copy x -> {
          validateSlot.accept(x.slotDst());
          validateSlot.accept(x.slotSrc());
        }

        case Instructions.LoadVec x -> {
          validateSlot.accept(x.slotDst());
        }
        case Instructions.MakeVec x -> {
          validateSlot.accept(x.slotDst());
          validateSlot.accept(x.slotX());
          validateSlot.accept(x.slotY());
          validateSlot.accept(x.slotZ());
        }
        case Instructions.SplitVec x -> {
          validateSlot.accept(x.slotX());
          validateSlot.accept(x.slotY());
          validateSlot.accept(x.slotZ());
          validateSlot.accept(x.slotSrc());
        }

        case Instructions.JumpIfEqual x -> {
          validateLabel.accept(x.label());
          validateSlot.accept(x.slotA());
          validateSlot.accept(x.slotB());
        }
        case Instructions.JumpIfLess x -> {
          validateLabel.accept(x.label());
          validateSlot.accept(x.slotA());
          validateSlot.accept(x.slotB());
        }
        case Instructions.Label x -> {}

        case Instructions.AccelEntity x -> {
          validateSlot.accept(x.slotEntity());
          validateSlot.accept(x.slotVel());
        }
        case Instructions.DamageEntity x -> {
          validateSlot.accept(x.slotEntity());
          validateSlot.accept(x.slotDmg());
        }
        case Instructions.EntityFacing x -> {
          validateSlot.accept(x.slotEntity());
          validateSlot.accept(x.slotFacing());
        }
        case Instructions.EntityPos x -> {
          validateSlot.accept(x.slotEntity());
          validateSlot.accept(x.slotPos());
        }
        case Instructions.EntityVel x -> {
          validateSlot.accept(x.slotEntity());
          validateSlot.accept(x.slotVel());
        }
        case Instructions.Explode x -> {
          validateSlot.accept(x.slotPos());
          validateSlot.accept(x.slotPower());
        }
        case Instructions.NearestEntity x -> {
          validateSlot.accept(x.slotEntity());
          validateSlot.accept(x.slotIndex());
          validateSlot.accept(x.slotPos());
        }
        case Instructions.PlaceBlock x -> {
          validateSlot.accept(x.slotPos());
        }
        case Instructions.SummonFireball x -> {
          validateSlot.accept(x.slotPos());
          validateSlot.accept(x.slotFireball());
        }
        case Instructions.SummonLightning x -> {
          validateSlot.accept(x.slotPos());
        }
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
