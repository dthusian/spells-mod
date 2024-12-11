package dev.wateralt.mc.weapontroll.asm.amagus;

import dev.wateralt.mc.weapontroll.asm.AsmError;

public enum Type {
  EFFECT,
  ENTITY, // format %e
  POSITION, // format %p
  EFFECT_LIST; // format %x
  
  public static Type parse(String str) {
    switch(str) {
      case "%v" -> { return Type.EFFECT; }
      case "%e" -> { return Type.ENTITY; }
      case "%p" -> { return Type.POSITION; }
      case "%x" -> { return Type.EFFECT_LIST; }
    }
    throw new AsmError("Invalid type specifier");
  }
  
  @Override
  public String toString() {
    switch(this) {
      case EFFECT -> { return "%v"; }
      case ENTITY -> { return "%e"; }
      case POSITION -> { return "%p"; }
      case EFFECT_LIST -> { return "%x"; }
    }
    throw new RuntimeException("unreachable");
  }
  
  public String toHumanReadable() {
    switch(this) {
      case EFFECT -> { return "<effect>"; }
      case ENTITY -> { return "<entity>"; }
      case POSITION -> { return "<position>"; }
      case EFFECT_LIST -> { return "<...>"; }
    }
    throw new RuntimeException("unreachable");
  }
}
