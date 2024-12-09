package dev.wateralt.mc.weapontroll.asm.amagus;

import dev.wateralt.mc.weapontroll.asm.AsmError;

import java.util.List;

public class Parser {
  public sealed interface Element {}
  public record SExpr(FunctionDef function, List<Element> elements) implements Element { }
  public record Str(String str) implements Element { }
  
  /// The builtin StringReader is dog
  private static class StringReader {
    int pos;
    String str;
    public StringReader(String s) {
      str = s;
      pos = 0;
    }
    public int peek() {
      if(pos == str.length()) return 0;
      return str.charAt(pos);
    }
    public int next() {
      if(pos == str.length()) return 0;
      return str.charAt(pos++);
    }
    public void skip(int n) {
      pos += n;
      if(pos > str.length()) pos = str.length();
    }
    public boolean eof() {
      return pos == str.length();
    }
  }
  
  private String nextToken(StringReader reader) {
    while(Character.isWhitespace(reader.peek())) {
      reader.next();
    }
    if(reader.eof()) return null;
    if(reader.peek() == '(') {
      reader.next();
      return "(";
    } else if(reader.peek() == ')') {
      reader.next();
      return ")";
    } else if(Character.isLetterOrDigit(reader.peek())) {
      StringBuilder builder = new StringBuilder();
      while(Character.isLetterOrDigit(reader.peek())) {
        builder.append(reader.next());
      }
      return builder.toString();
    } else {
      throw new AsmError("Illegal character '%c'".formatted(reader.peek()));
    }
  }
  public SExpr parse(String str) {
    List<String> tokens;
  }
}
