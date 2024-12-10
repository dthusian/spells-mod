package dev.wateralt.mc.weapontroll.asm.std20;

import dev.wateralt.mc.weapontroll.asm.*;
import dev.wateralt.mc.weapontroll.spell.ExecContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Std20Program implements Program {
  private final List<Instruction> instrs;
  private final HashMap<String, Integer> labels;
  private final int maxSlots;
  
  public Std20Program(String src, int maxSlots) {
    this.maxSlots = maxSlots;
    String[] lines = src.split("\n");
    
    instrs = new ArrayList<>();
    for(int i = 0; i < lines.length; i++) {
      int lineNo = i + 1;
      try {
        // parse
        String line = lines[i].trim();
        if(line.isEmpty() || line.startsWith("#")) {
          continue;
        }
        Instruction instr = Instruction.parse(line);
        instrs.add(instr);
        
        // validate
        if(instr.returnInto().map(v -> v >= maxSlots).orElse(false)) {
          throw new AsmError("Slot %d is greater than the maximum (%d)".formatted(instr.returnInto().get(), maxSlots));
        }
        for(int j = 0; j < instr.args().size(); j++) {
          if(instr.args().get(j) instanceof Instruction.SlotArg slot && slot.slot() >= maxSlots) {
            throw new AsmError("Slot %d is greater than the maximum (%d)".formatted(slot.slot(), maxSlots));
          }
        }
      } catch(AsmError err) {
        throw new AsmError("Parse error at line %d (`%s`): %s".formatted(lineNo, lines[i], err.getMessage()));
      }
    }
    
    labels = new HashMap<>();
    for(int i = 0; i < instrs.size(); i++) {
      if(instrs.get(i).function().equals("label")) {
        Instruction.Arg name = instrs.get(i).args().getFirst();
        if(name instanceof Instruction.StringArg nameStr) {
          labels.put(nameStr.str(), i);
        } else {
          throw new AsmError("Parse error at line %d: Invalid label: expected string".formatted(i + 1));
        }
      }
    }
  }
  
  public List<Instruction> getInstrs() {
    return instrs;
  }
  
  public HashMap<String, Integer> getLabels() {
    return labels;
  }
  
  public int getMaxSlots() {
    return maxSlots;
  }
  
  @Override
  public Std20ProgramState prepareRun(ExecContext ctx) {
    return new Std20ProgramState(this, ctx, maxSlots, 1024);
  }
}
