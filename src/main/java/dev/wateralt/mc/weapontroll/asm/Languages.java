package dev.wateralt.mc.weapontroll.asm;

import dev.wateralt.mc.weapontroll.asm.amagus.AmagusLanguage;
import dev.wateralt.mc.weapontroll.asm.std20.Std20Language;

public class Languages {
  public static final Std20Language STD20 = new Std20Language();
  public static final AmagusLanguage AMAGUS = new AmagusLanguage();
  
  public static Language identify(String src) {
    String firstLine = src.substring(0, src.indexOf('\n')).trim();
    if(firstLine.equals("#lang lazy")) {
      throw new AsmError("The runtime was too lazy to your program");
    } else if(firstLine.equals("#lang std20")) {
      return STD20;
    } else if(firstLine.equals("#lang amagus")) {
      return AMAGUS;
    }
    return null;
  }
}
