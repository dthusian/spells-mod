package dev.wateralt.mc.weapontroll.asm;

import dev.wateralt.mc.weapontroll.asm.std20.Std20Language;

public class Languages {
  public static final Std20Language STD20 = new Std20Language();
  
  public static Language identify(String name) {
    if(name.equals("lazy")) {
      throw new AsmError("The runtime was too lazy and didn't run your program");
    } else if(name.equals("std20")) {
      return STD20;
    } else if(name.equals("amagus")) {
      throw new AsmError("Not implemented");
    }
    return null;
  }
}
