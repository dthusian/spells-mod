package dev.wateralt.mc.weapontroll.asm;

import dev.wateralt.mc.weapontroll.asm.std20.Std20Language;

public class Languages {
  public static final Std20Language STD20 = new Std20Language();
  
  public static Language identify(String src) {
    String firstLine = src.substring(0, src.indexOf('\n')).trim();
    if(firstLine.equals("#lang lazy")) {
      throw new AsmError("The runtime was too lazy and didn't run your program");
    } else if(firstLine.equals("#lang std20")) {
      return STD20;
    } else if(firstLine.equals("#lang amagus")) {
      throw new AsmError("Not implemented");
    }
    return null;
  }
}
