package dev.wateralt.mc.weapontroll.editor;

import com.google.gson.Gson;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.wateralt.mc.weapontroll.Weapontroll;
import dev.wateralt.mc.weapontroll.asm.AsmError;
import dev.wateralt.mc.weapontroll.asm.Languages;
import dev.wateralt.mc.weapontroll.asm.amagus.AmagusProgram;
import dev.wateralt.mc.weapontroll.asm.amagus.Functions;
import dev.wateralt.mc.weapontroll.asm.amagus.Type;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static net.minecraft.server.command.CommandManager.*;

public class EditorCmd {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(literal("editor")
      .executes(wrapAction(EditorCmd::cmdEditor))
      .then(literal("editor").executes(wrapAction(EditorCmd::cmdEditor)))
      .then(literal("_sela")
        .then(argument("param1", StringArgumentType.word())
          .executes(wrapAction(EditorCmd::cmdSelectArg))))
      .then(literal("_selb")
        .then(argument("param1", StringArgumentType.word())
          .executes(wrapAction(EditorCmd::cmdSelectList))))
      .then(literal("_ins")
        .then(argument("param1", StringArgumentType.word())
          .then(argument("param2", StringArgumentType.word())
            .executes(wrapAction(EditorCmd::cmdInsert)))))
      .then(literal("_app")
        .then(argument("param1", StringArgumentType.word())
          .executes(wrapAction(EditorCmd::cmdAppend))))
      .then(literal("_del")
        .then(argument("param1", StringArgumentType.word())
          .executes(wrapAction(EditorCmd::cmdDelete))))
      .then(literal("_rm")
        .then(argument("param1", StringArgumentType.word())
          .executes(wrapAction(EditorCmd::cmdRemove))))
    );
  }
  
  public static Command<ServerCommandSource> wrapAction(BiConsumer<CommandContext<ServerCommandSource>, AtomicReference<Object[]>> func) {
    return v -> {
      try {
        AtomicReference<Object[]> code = new AtomicReference<>(readCodeFromPlayer(v));
        func.accept(v, code);
        writeCodeToPlayer(v, code.get());
      } catch(StructuralError err) {
        v.getSource().sendError(Text.of("Parse error, re-run /editor when switching to new book"));
        return -1;
      } catch(AsmError err) {
        v.getSource().sendError(Text.of("Parse error, invalid code"));
        return -1;
      } catch(CustomEditorError err) {
        v.getSource().sendError(Text.of(err.getMessage()));
        return -1;
      } catch(Exception err) {
        v.getSource().sendError(Text.of("Unknown error, report bug"));
        Weapontroll.LOGGER.warn("Editor cmd failed: " + err.getMessage());
        err.printStackTrace();
        return -1;
      }
      return 1;
    };
  }
  
  /// Opens the editor.
  public static void cmdEditor(CommandContext<ServerCommandSource> context, AtomicReference<Object[]> code) {
    context.getSource().sendMessage(displayEditor(code.get(), new int[]{}, null));
  }

  /// (internal) selects the list element given by <param 1>
  public static void cmdSelectList(CommandContext<ServerCommandSource> context, AtomicReference<Object[]> code) {
    int[] selectPath = EditorUtil.stoia(context.getArgument("param1", String.class));
    // display editor
    context.getSource().sendMessage(displayEditor(code.get(), selectPath, Type.EFFECT));
  }
  
  /// (internal) selects the field given by <param 1>
  public static void cmdSelectArg(CommandContext<ServerCommandSource> context, AtomicReference<Object[]> code) {
    int[] selectPath = EditorUtil.stoia(context.getArgument("param1", String.class));
    // thanks java
    AtomicReference<Type> argType = new AtomicReference<>();
    EditorUtil.traverseArrayRef((v, i) -> {
      Functions.Def funcDef = Functions.FUNCTIONS.get((String) v[0]);
      argType.set(funcDef.getArgTypes().get(i - 1));
    }, code, selectPath);
    // display editor
    context.getSource().sendMessage(displayEditor(code.get(), selectPath, argType.get()));
  }
  
  /// (internal) inserts a default version of the function given by <param 1> into path <param 2>
  public static void cmdInsert(CommandContext<ServerCommandSource> context, AtomicReference<Object[]> code) {
    String funcName = context.getArgument("param1", String.class);
    int[] path = EditorUtil.stoia(context.getArgument("param2", String.class));
    // cmd
    EditorUtil.traverseMap(v -> Functions.FUNCTIONS.get(funcName).createDefault(), code, path);
    // display editor
    context.getSource().sendMessage(displayEditor(code.get(), new int[] {}, null));
  }

  /// (internal) appends a null entry to <param 1>
  public static void cmdAppend(CommandContext<ServerCommandSource> context, AtomicReference<Object[]> code) {
    int[] path = EditorUtil.stoia(context.getArgument("param1", String.class));
    // cmd
    EditorUtil.traverseMap(v -> {
      if(!(v instanceof Object[])) throw new StructuralError();
      return EditorUtil.snocObj((Object[]) v, null);
    }, code, path);
    // display editor
    context.getSource().sendMessage(displayEditor(code.get(), new int[] {}, null));
  }

  /// (internal) removes the function at path <param 1>
  public static void cmdDelete(CommandContext<ServerCommandSource> context, AtomicReference<Object[]> code) {
    int[] path = EditorUtil.stoia(context.getArgument("param1", String.class));
    EditorUtil.traverseArrayRef((v, i) -> v[i] = null, code, path);
    // display editor
    context.getSource().sendMessage(displayEditor(code.get(), new int[] {}, null));
  }

  /// (internal) removes the function at path, list-style <param 1>
  public static void cmdRemove(CommandContext<ServerCommandSource> context, AtomicReference<Object[]> code) {
    int[] path = EditorUtil.stoia(context.getArgument("param1", String.class));
    EditorUtil.traverseMap(v -> {
      if(v instanceof Object[]) {
        return EditorUtil.removeArray((Object[]) v, path[path.length - 1]);
      } else {
        throw new StructuralError();
      }
    }, code, Arrays.copyOf(path, path.length - 1));
    // display editor
    context.getSource().sendMessage(displayEditor(code.get(), new int[] {}, null));
  }
  
  /// Helper functions below
  
  private static Text formatSelectBtn(int[] path, Type type, boolean selected, boolean arg) {
    String cmd;
    if(arg) {
      cmd = "/editor _sela %s".formatted(EditorUtil.iatos(path));
    } else {
      cmd = "/editor _selb %s".formatted(EditorUtil.iatos(path));
    }
    Style style;
    if(selected) {
      style = EditorUtil.makeStyle(Formatting.AQUA, true);
    } else {
      style = EditorUtil.makeStyle(Formatting.GREEN, true);
    }
    style = style
      .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd))
      .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("click to select")));
    return Text.literal(type.toHumanReadable()).setStyle(style);
  }
  
  private static Text formatDeleteBtn(int[] path, String s, Formatting color) {
    String cmd = "/editor _del " + EditorUtil.iatos(path);
    return Text.literal(s).setStyle(EditorUtil.makeStyle(color, false)
      .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd))
      .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("click to remove"))));
  }
  
  private static Text formatFuncDecl(int[] selected, Functions.Def def) {
    MutableText ret = Text.literal("");
    List<Text> buf = ret.getSiblings();
    buf.add(Text.of("("));
    String[] splits = def.metadata().format().split(" ");
    for(int i = 0; i < splits.length; i++) {
      if(i != 0) buf.add(Text.of(" "));
      if(splits[i].startsWith("%")) {
        Type typ = Type.parse(splits[i]);
        buf.add(Text.literal(typ.toHumanReadable()).setStyle(EditorUtil.makeStyle(Formatting.GREEN, false)));
      } else {
        buf.add(Text.of(splits[i]));
      }
    }
    buf.add(Text.of(")"));
    String cmd = "/editor _ins %s %s".formatted(def.name(), EditorUtil.iatos(selected));
    ret.setStyle(EditorUtil.makeStyle(Formatting.WHITE, false)
      .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd))
    );
    return ret;
  }
  
  private static Text formatAppendBtn(int[] path) {
    String cmd = "/editor _app " + EditorUtil.iatos(path);
    return Text.literal("[+]\n").setStyle(EditorUtil.makeStyle(Formatting.GREEN, true)
      .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd))
      .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("add effect"))));
  }
  
  private static Text formatConst(int[] path, String name) {
    return formatDeleteBtn(path, name, Formatting.GOLD);
  }
  
  private static Text formatCode(Object[] sExpression, int[] path, int[] selected, int indentation) {
    MutableText ret = Text.literal("");
    List<Text> buf = ret.getSiblings();
    buf.add(Text.of("("));
    
    int currentArg = 1;
    String funcName = (String) sExpression[0];
    Functions.Def funcDef = Functions.FUNCTIONS.get(funcName);
    String[] splits = funcDef.metadata().format().split(" ");
    for(int i = 0; i < splits.length; i++) {
      int[] currentPath = EditorUtil.snoc(path, currentArg);
      if(splits[i].startsWith("%")) {
        Type type = Type.parse(splits[i]);
        if(type.equals(Type.EFFECT_LIST)) {
          buf.add(Text.of("\n"));
          if(sExpression[currentArg] instanceof Object[] varargs) {
            buf.add(formatCodeList(varargs, currentPath, selected, indentation + 1));
          } else {
            throw new StructuralError();
          }
        } else {
          if(i != 0) buf.add(Text.of(" "));
          if(sExpression[currentArg] == null) {
            buf.add(formatSelectBtn(currentPath, type, Arrays.equals(currentPath, selected), true));
          } else if(sExpression[currentArg] instanceof String constant) {
            buf.add(formatConst(currentPath, constant));
          } else if(sExpression[currentArg] instanceof Object[] subExpression) {
            buf.add(formatCode(subExpression, currentPath, selected, indentation));
          } else {
            throw new StructuralError();
          }
          currentArg++;
        }
      } else {
        if(i == 0) {
          buf.add(formatDeleteBtn(path, splits[i], Formatting.WHITE));
        } else {
          buf.add(formatDeleteBtn(path, " " + splits[i], Formatting.WHITE));
        }
      }
    }
    
    buf.add(Text.of(")"));
    return ret;
  }
  
  private static Text formatCodeList(Object[] sExpression, int[] path, int[] selected, int indent) {
    MutableText ret = Text.literal("");
    List<Text> buf = ret.getSiblings();
    for(int i = 0; i < sExpression.length; i++) {
      Object cmdo = sExpression[i];
      int[] currentPath = EditorUtil.snoc(path, i);
      if(indent > 0) buf.add(Text.of("  ".repeat(indent)));
      if(cmdo == null) {
        buf.add(formatSelectBtn(currentPath, Type.EFFECT, Arrays.equals(currentPath, selected), false));
      } else if(cmdo instanceof Object[] cmd) {
        buf.add(formatCode(cmd, EditorUtil.snoc(path, i), selected, 0));
      }
      buf.add(Text.of("\n"));
    }
    if(indent > 0) buf.add(Text.of("  ".repeat(indent)));
    buf.add(formatAppendBtn(path));
    buf.add(Text.of("\n"));
    return ret;
  }
  
  private static Text makeFuncDisplay(int[] selected, Type returnType) {
    MutableText ret = Text.literal("");
    List<Text> buf = ret.getSiblings();
    AtomicBoolean first = new AtomicBoolean(true);
    Functions.FUNCTIONS.values().stream().filter(v -> v.metadata().returns().equals(returnType)).forEach(v -> {
      if(!first.get()) {
        buf.add(Text.of("    "));
      }
      buf.add(formatFuncDecl(selected, v));
      first.set(false);
    });
    return ret;
  }
  
  private static Text displayEditor(Object[] sExpression, int[] selected, Type funcDisplay) {
    MutableText ret = Text.literal("");
    List<Text> buf = ret.getSiblings();
    if(funcDisplay != null) {
      buf.add(makeFuncDisplay(selected, funcDisplay));
    }
    buf.add(Text.literal("\n===========================\n").setStyle(EditorUtil.makeStyle(Formatting.DARK_GRAY, false)));
    buf.add(formatCodeList(sExpression, new int[0], selected, 0));
    return ret;
  }
  
  private static Object[] readCodeFromPlayer(CommandContext<ServerCommandSource> context) {
    if(context.getSource().getEntity() instanceof ServerPlayerEntity player) {
      ItemStack mainHand = player.getMainHandStack();
      if(mainHand.getItem().equals(Items.WRITABLE_BOOK)) {
        WritableBookContentComponent content = mainHand.get(DataComponentTypes.WRITABLE_BOOK_CONTENT);
        if(content != null) {
          List<String> pageContent = content.pages().stream().map(RawFilteredPair::raw).toList();
          AmagusProgram program = (AmagusProgram) Languages.AMAGUS.compile(pageContent);
          return program.getSExpr();
        }
      }
    }
    throw new CustomEditorError("You're not holding a writable book!");
  }
  
  private static void writeCodeToPlayer(CommandContext<ServerCommandSource> context, Object[] sExpression) {
    if(context.getSource().getEntity() instanceof ServerPlayerEntity player) {
      ItemStack mainHand = player.getMainHandStack();
      if(mainHand.getItem().equals(Items.WRITABLE_BOOK)) {
        Gson gson = new Gson();
        String code = "#lang amagus\n" + gson.toJson(sExpression);
        List<RawFilteredPair<String>> pages = new ArrayList<>();
        for(int i = 0; i < code.length(); i += 256) {
          String raw = code.substring(i, Math.min(i + 256, code.length()));
          pages.add(RawFilteredPair.of(raw));
        }
        WritableBookContentComponent data = new WritableBookContentComponent(pages);
        mainHand.set(DataComponentTypes.WRITABLE_BOOK_CONTENT, data);
      }
    }
  }
}