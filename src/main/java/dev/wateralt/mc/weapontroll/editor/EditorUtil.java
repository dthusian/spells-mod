package dev.wateralt.mc.weapontroll.editor;

import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
  
  public static Object[] removeArray(Object[] x, int i) {
    Object[] newArr = new Object[x.length - 1];
    for(int j = 0; j < newArr.length; j++) {
      if(j < i) {
        newArr[j] = x[j];
      } else {
        newArr[j] = x[j + 1];
      }
    }
    return newArr;
  }

  public static int[] stoia(String s) {
    String rmStart = s.substring(1);
    if(rmStart.isEmpty()) return new int[0];
    return Arrays.stream(rmStart.split("_")).mapToInt(Integer::parseInt).toArray();
  }

  public static String iatos(int[] ia) {
    return "A" + String.join("_", Arrays.stream(ia).mapToObj(Integer::toString).toList());
  }

  public static Style makeStyle(Formatting col, boolean underline) {
    return Style.EMPTY.withColor(TextColor.fromFormatting(col)).withUnderline(underline);
  }
  
  public static Object traverseGet(AtomicReference<Object[]> tree, int[] path, int depth) {
    Object node = tree.get();
    for(int i = 0; i < depth; i++) {
      if(node instanceof Object[] nodeArr) {
        node = nodeArr[path[i]];
      } else {
        throw new StructuralError();
      }
    }
    return node;
  }

  public static void traverseSet(AtomicReference<Object[]> tree, int[] path, int depth, Object val) {
    Object node = tree.get();
    if(path.length == 0) {
      if(val instanceof Object[] valArr) {
        tree.set(valArr);
        return;
      } else {
        throw new RuntimeException("idk what to do here");
      }
    }
    for(int i = 0; i < depth; i++) {
      if(node instanceof Object[] nodeArr) {
        if(i == depth - 1) {
          nodeArr[path[i]] = val;
        } else {
          node = nodeArr[path[i]];
        }
      } else {
        throw new StructuralError();
      }
    }
  }
  
  /// Applies a function on the object in the tree at that path, and replaces the object with it.
  public static void traverseMap(Function<Object, Object> func, AtomicReference<Object[]> tree, int[] path) {
    Object node = traverseGet(tree, path, path.length);
    Object newNode = func.apply(node);
    traverseSet(tree, path, path.length, newNode);
  }
  
  /// Traverses to one level behind the array-ref, and applies a function (which can mutate the array) on it.
  public static void traverseArrayRef(BiConsumer<Object[], Integer> func, AtomicReference<Object[]> tree, int[] path) {
    Object node = traverseGet(tree, path, path.length - 1);
    if(node instanceof Object[] nodeArr) {
      func.accept(nodeArr, path[path.length - 1]);
    } else {
      throw new StructuralError();
    }
  }
}
