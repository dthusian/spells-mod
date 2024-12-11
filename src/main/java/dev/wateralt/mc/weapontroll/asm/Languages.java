package dev.wateralt.mc.weapontroll.asm;

import dev.wateralt.mc.weapontroll.asm.magic.MagicLanguage;
import dev.wateralt.mc.weapontroll.asm.std20.Std20Language;

public class Languages {
  public static final Std20Language STD20 = new Std20Language();
  public static final MagicLanguage MAGIC = new MagicLanguage();
  
  public static Language identify(String src) {
    int linePos = src.indexOf('\n');
    if(linePos == -1) linePos = src.length();
    String firstLine = src.substring(0, linePos).trim();
    if(firstLine.equals("#lang lazy")) {
      throw new AsmError("The runtime was too lazy to your program");
    } else if(firstLine.equals("#lang std20")) {
      return STD20;
    } else if(firstLine.equals("#lang magic")) {
      return MAGIC;
    }
    return null;
  }
}
