package dev.wateralt.mc.weapontroll.editor;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static net.minecraft.server.command.CommandManager.*;

public class EditorCmd {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(literal("editor")
      .executes(wrapAction(EditorCmd::cmdEditor))
      .then(literal("editor").executes(wrapAction(EditorCmd::cmdEditor)))
      .then(literal("_sel")
        .then(argument("param1", StringArgumentType.word()))
          .executes(wrapAction(EditorCmd::cmdSelect)))
      .then(literal("_ins")
        .then(argument("param1", StringArgumentType.word())
          .then(argument("param2", StringArgumentType.word())
            .executes(wrapAction(EditorCmd::cmdIns)))))
      .then(literal("_app")
        .then(argument("param1", StringArgumentType.word())
          .then(argument("param2", StringArgumentType.word())
            .executes(wrapAction(EditorCmd::cmdApp)))))
      .then(literal("_rm")
        .then(argument("param1", StringArgumentType.word())
          .executes(wrapAction(EditorCmd::cmdRm))))
    );
  }
  
  public static Command<ServerCommandSource> wrapAction(BiConsumer<CommandContext<ServerCommandSource>, Object[]> func) {
    return v -> {
      try {
        Object[] code = readCodeFromPlayer(v);
        func.accept(v, code);
        writeCodeToPlayer(v, code);
      } catch(StructuralError err) {
        v.getSource().sendError(Text.of("Parse error, re-run /editor when switching to new book"));
        return -1;
      } catch(AsmError err) {
        v.getSource().sendError(Text.of("Parse error, invalid code"));
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
  public static void cmdEditor(CommandContext<ServerCommandSource> context, Object[] code) {
    context.getSource().sendMessage(displayEditor(code, new int[]{}, null));
  }
  
  /// (internal) selects the field given by <param 1>
  public static void cmdSelect(CommandContext<ServerCommandSource> context, Object[] code) {
    int[] selectPath = EditorUtil.stoia(context.getArgument("param1", String.class));
    Object[] node = EditorUtil.traverseTo(context, code, selectPath);
    Functions.Def funcDef = Functions.FUNCTIONS.get((String) node[0]);
    Type argType = funcDef.getArgTypes().get(selectPath[selectPath.length - 1]);
    // display editor
    context.getSource().sendMessage(displayEditor(code, selectPath, argType));
  }
  
  /// (internal) inserts a default version of the function given by <param 1> into path <param 2>
  public static void cmdIns(CommandContext<ServerCommandSource> context, Object[] code) {
    String funcName = context.getArgument("param1", String.class);
    int[] selectPath = EditorUtil.stoia(context.getArgument("param2", String.class));
    Object[] node = EditorUtil.traverseTo(context, code, selectPath);
    Functions.Def funcDef = Functions.FUNCTIONS.get(funcName);
    node[selectPath[selectPath.length - 1]] = funcDef.createDefault();
    // display editor
    context.getSource().sendMessage(displayEditor(code, new int[] {}, null));
  }

  /// (internal) appends a default version of the function given by <param 1> into path <param 2>
  public static void cmdApp(CommandContext<ServerCommandSource> context, Object[] code) {
    String funcName = context.getArgument("param1", String.class);
    int[] selectPath = EditorUtil.stoia(context.getArgument("param2", String.class));
    Object[] node = EditorUtil.traverseTo(context, code, selectPath);
    Functions.Def funcDef = Functions.FUNCTIONS.get(funcName);
    node[selectPath[selectPath.length - 1]] = EditorUtil.snocObj((Object[]) node[selectPath[selectPath.length - 1]], funcDef.createDefault());
    // display editor
    context.getSource().sendMessage(displayEditor(code, new int[] {}, null));
  }

  /// (internal) removes the function at path <param 1>
  public static void cmdRm(CommandContext<ServerCommandSource> context, Object[] code) {
    int[] selectPath = EditorUtil.stoia(context.getArgument("param1", String.class));
    Object[] node = EditorUtil.traverseTo(context, code, selectPath);
    node[selectPath[selectPath.length - 1]] = null;
    // display editor
    context.getSource().sendMessage(displayEditor(code, new int[] {}, null));
  }
  
  /// Helper functions below
  
  private static Text formatSelectBtn(int[] path, Type type, boolean selected) {
    String cmd = "/editor _sel %s".formatted(EditorUtil.iatos(path));
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
  
  private static Text formatDeleteBtn(int[] path, String s) {
    String cmd = "/editor _rm " + EditorUtil.iatos(path);
    return Text.literal(s).setStyle(EditorUtil.makeStyle(Formatting.WHITE, false)
      .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd))
      .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("click to remove"))));
  }
  
  private static Text formatFuncDecl(int[] selected, Functions.Def def) {
    MutableText ret = Text.literal("");
    List<Text> buf = ret.getSiblings();
    buf.add(Text.of("("));
    String[] splits = def.metadata().format().split(" ");
    for(int i = 0; i < splits.length; i++) {
      if(splits[i].startsWith("%")) {
        Type typ = Type.parse(splits[i]);
        buf.add(Text.literal(typ.toHumanReadable()).setStyle(EditorUtil.makeStyle(Formatting.GREEN, false)));
      } else {
        if(i == 0) {
          buf.add(Text.of(splits[i]));
        } else {
          buf.add(Text.of(" " + splits[i]));
        }
      }
    }
    buf.add(Text.of(")"));
    String cmd = "/editor _ins %s %s".formatted(def.name(), EditorUtil.iatos(selected));
    ret.setStyle(EditorUtil.makeStyle(Formatting.WHITE, false)
      .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd))
    );
    return ret;
  }
  
  private static Text formatConst(String name) {
    return Text.literal(name).setStyle(EditorUtil.makeStyle(Formatting.GOLD, false));
  }
  
  private static Text formatCode(Object[] sExpression, int[] path, int[] selected) {
    MutableText ret = Text.literal("");
    List<Text> buf = ret.getSiblings();
    buf.add(Text.of("("));
    
    int currentArg = 1;
    String funcName = (String) sExpression[0];
    Functions.Def funcDef = Functions.FUNCTIONS.get(funcName);
    String[] splits = funcDef.metadata().format().split(" ");
    for(int i = 0; i < splits.length; i++) {
      if(splits[i].startsWith("%")) {
        Type type = Type.parse(splits[i]);
        int[] currentPath = EditorUtil.snoc(path, currentArg);
        buf.add(Text.of(" "));
        if(sExpression[currentArg] == null) {
          buf.add(formatSelectBtn(currentPath, type, Arrays.equals(currentPath, selected)));
        } else if(sExpression[currentArg] instanceof String constant) {
          buf.add(formatConst(constant));
        } else if(sExpression[currentArg] instanceof Object[] subExpression) {
          buf.add(formatCode(subExpression, currentPath, selected));
        }
        currentArg++;
      } else {
        if(i == 0) {
          buf.add(formatDeleteBtn(path, splits[i]));
        } else {
          buf.add(formatDeleteBtn(path, " " + splits[i]));
        }
      }
    }
    
    buf.add(Text.of(")"));
    return ret;
  }
  
  private static Text makeFuncDisplay(int[] selected, Type returnType) {
    MutableText ret = Text.literal("");
    List<Text> buf = ret.getSiblings();
    AtomicBoolean first = new AtomicBoolean(true);
    Functions.FUNCTIONS.values().stream().filter(v -> v.metadata().returns().equals(returnType)).forEach(v -> {
      if(!first.get()) {
        buf.add(formatFuncDecl(selected, v));
      }
      buf.add(Text.of("    "));
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
    buf.add(Text.literal("===========================\n").setStyle(EditorUtil.makeStyle(Formatting.DARK_GRAY, false)));
    for(int i = 0; i < sExpression.length; i++) {
      Object cmdo = sExpression[i];
      if(cmdo instanceof Object[] cmd) {
        buf.add(formatCode(cmd, new int[]{i}, selected));
      }
    }
    return ret;
  }
  
  private static Object[] readCodeFromPlayer(CommandContext<ServerCommandSource> context) {
    if(context.getSource().getEntity() instanceof ServerPlayerEntity player) {
      ItemStack mainHand = player.getMainHandStack();
      if(mainHand.getItem().equals(Items.WRITABLE_BOOK)) {
        WritableBookContentComponent content = mainHand.get(DataComponentTypes.WRITABLE_BOOK_CONTENT);
        if(content != null) {
          List<String> pageContent = content.pages().stream().map(RawFilteredPair::raw).toList();
          try {
            AmagusProgram program = (AmagusProgram) Languages.AMAGUS.compile(pageContent);
            return program.getSExpr();
          } catch(Exception err) {
            context.getSource().sendError(Text.of("Parse error, run \"/editor\" again on new books"));
            return null;
          }
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
        String json = gson.toJson(sExpression);
        List<RawFilteredPair<String>> pages = new ArrayList<>();
        for(int i = 0; i < json.length(); i += 256) {
          String raw = json.substring(i, Math.min(i + 256, json.length()));
          pages.add(RawFilteredPair.of(raw));
        }
        WritableBookContentComponent data = new WritableBookContentComponent(pages);
        mainHand.set(DataComponentTypes.WRITABLE_BOOK_CONTENT, data);
      }
    }
  }
}