package dev.wateralt.mc.weapontroll.asm;

import java.util.List;

public interface Language {
  /// Compiles a new program from a list of sources
  Program compile(List<String> source);
}
