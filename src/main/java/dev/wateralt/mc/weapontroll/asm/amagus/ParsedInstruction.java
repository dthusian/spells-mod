package dev.wateralt.mc.weapontroll.asm.amagus;

import dev.wateralt.mc.weapontroll.asm.AsmError;

public record ParsedInstruction(String command, int amp, Target target) {
  public enum Target {
    ME,
    YOU,
    EVERYONE,
  }
  
  public static ParsedInstruction parse(String s) {
    String[] splits = s.split(" ");
    if(splits.length < 2) {
      throw new AsmError("Missing target selector");
    }
    
    // read amp
    int amp = 0;
    int pos = splits[0].length();
    while(true) {
      pos--;
      if(splits[0].charAt(pos) == '-') {
        amp--;
      } else if(splits[0].charAt(pos) == '+') {
        amp++;
      } else {
        break;
      }
    }
    pos++;
    
    // read target
    Target target;
    if(splits[1].equals("me")) {
      target = Target.ME;
    } else if(splits[1].equals("you")) {
      target = Target.YOU;
    } else if(splits[1].equals("everyone")) {
      target = Target.EVERYONE;
    } else {
      throw new AsmError("Invalid target, expected one of \"me\", \"you\", or \"everyone\"");
    }
    return new ParsedInstruction(splits[0].substring(0, pos), amp, target);
  }
}
