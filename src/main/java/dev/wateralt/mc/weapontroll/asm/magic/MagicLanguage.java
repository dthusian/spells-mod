package dev.wateralt.mc.weapontroll.asm.magic;

import dev.wateralt.mc.weapontroll.asm.Language;

import java.util.Arrays;
import java.util.List;

public class MagicLanguage implements Language {
  @Override
  public MagicProgram compile(List<String> source) {
    String[] strs = Arrays.stream(String.join("\n", source).split("\n"))
      .map(String::trim)
      .filter(v -> !v.startsWith("#"))
      .filter(v -> !v.isEmpty())
      .toArray(String[]::new);
    return new MagicProgram(strs);
  }
}
