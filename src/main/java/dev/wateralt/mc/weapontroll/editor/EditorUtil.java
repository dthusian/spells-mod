package dev.wateralt.mc.weapontroll.editor;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.Arrays;

public class EditorUtil {
  public static int[] snoc(int[] x, int e) {
    int[] newArr = Arrays.copyOf(x, x.length + 1);
    newArr[x.length] = e;
    return newArr;
  }
  
  public static Object[] snocObj(Object[] x, Object e) {
    Object[] newArr = Arrays.copyOf(x, x.length + 1);
    newArr[x.length] = e;
    return newArr;
  }

  public static int[] stoia(String s) {
    return Arrays.stream(s.split(" ")).mapToInt(Integer::parseInt).toArray();
  }

  public static String iatos(int[] ia) {
    return String.join(",", Arrays.stream(ia).mapToObj(Integer::toString).toList());
  }

  public static Style makeStyle(Formatting col, boolean underline) {
    return Style.EMPTY.withColor(TextColor.fromFormatting(col)).withUnderline(underline);
  }
  
  public static Object[] traverseTo(CommandContext<ServerCommandSource> context, Object[] sexpr, int[] path) {
    Object[] node = sexpr;
    for(int i = 0; i < path.length - 1; i++) {
      if(node[i] instanceof Object[] innerNode) {
        node = innerNode;
      } else {
        throw new StructuralError();
      }
    }
    return node;
  }
  
}
