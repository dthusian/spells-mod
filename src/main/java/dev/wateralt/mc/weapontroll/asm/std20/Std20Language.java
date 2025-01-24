package dev.wateralt.mc.weapontroll.asm.std20;

import dev.wateralt.mc.weapontroll.asm.Language;

import java.util.List;

public class Std20Language implements Language {
  @Override
  public Std20Program compile(List<String> source) {
    return new Std20Program(String.join("\n", source), 128);
  }
}
