package dev.wateralt.mc.weapontroll.asm.std20;

import dev.wateralt.mc.weapontroll.asm.AsmError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record Instruction(Optional<Short> returnInto, String function, List<Arg> args) {
  public sealed interface Arg {}
  public record SlotArg(short slot) implements Arg {}
  public record NumArg(double number) implements Arg {}
  public record StringArg(String str) implements Arg {}
  
  private static Arg parseArg(String part) {
    if(part.matches("^[0-9]")) {
      return new NumArg(Double.parseDouble(part));
    } else if(part.startsWith("$")) {
      return new SlotArg(Short.parseShort(part.substring(1)));
    } else {
      return new StringArg(part);
    }
  }
  
  public static Instruction parse(String line) {
    try {
      String[] splits = line.split("((?<==)|( ))");
      int pos = 0;
      Optional<Short> returnInto = Optional.empty();
      String function;
      ArrayList<Arg> args = new ArrayList<>();
      
      if(splits[pos].startsWith("$")) {
        returnInto = Optional.of(Short.parseShort(splits[pos].substring(1)));
        pos++;
        if(splits.length <= pos || !splits[pos].equals("=")) {
          throw new AsmError("Expected '=' after the first slot name");
        }
        pos++;
      }
      
      if(splits.length <= pos) {
        throw new AsmError("Expected a function name before end of line");
      }
      function = splits[pos];
      pos++;
      for(int i = pos; i < splits.length; i++) {
        args.add(parseArg(splits[i]));
      }
      
      return new Instruction(returnInto, function, args);
    } catch(NumberFormatException err) {
      throw new AsmError("Invalid number: " + err.getMessage());
    }
  }
}
