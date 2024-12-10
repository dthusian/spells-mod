package dev.wateralt.mc.weapontroll.asm.amagus;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import dev.wateralt.mc.weapontroll.asm.Language;
import dev.wateralt.mc.weapontroll.asm.Program;

import java.util.List;

public class AmagusLanguage implements Language {
  private Object[] convertArray(JsonArray src) {
    return src.asList().stream().map(v -> {
      if(v.isJsonArray()) return convertArray(v.getAsJsonArray());
      else if(v.isJsonPrimitive() && v.getAsJsonPrimitive().isString()) return v.getAsString();
      else throw new RuntimeException("Invalid JSON");
    }).toArray();
  }
  
  @Override
  public Program compile(List<String> source) {
    String src = String.join("", source);
    JsonArray array = JsonParser.parseString(src).getAsJsonArray();
    return new AmagusProgram(convertArray(array));
  }
}
