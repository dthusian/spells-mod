package dev.wateralt.mc.weapontroll.asm.std20;

import dev.wateralt.mc.weapontroll.asm.Language;

public class Std20Language implements Language {
  @Override
  public Std20Program compile(String[] source) {
    return new Std20Program(String.join(",", source), 8);
  }
}
