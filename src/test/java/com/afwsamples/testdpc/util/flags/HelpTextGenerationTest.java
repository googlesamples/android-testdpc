/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.afwsamples.testdpc.util.flags;

import static com.afwsamples.testdpc.util.flags.Flags.command;
import static com.afwsamples.testdpc.util.flags.Flags.namedParam;
import static com.afwsamples.testdpc.util.flags.Flags.optional;
import static com.afwsamples.testdpc.util.flags.Flags.ordinalParam;
import static com.afwsamples.testdpc.util.flags.Flags.repeated;
import static com.afwsamples.testdpc.util.flags.Utils.asArgs;
import static com.google.common.truth.Truth.assertThat;

import com.afwsamples.testdpc.util.flags.Utils.KeyValueCallback;
import com.afwsamples.testdpc.util.flags.Utils.KeyValueType;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class HelpTextGenerationTest {
  @Test
  public void noParamsCommand_generatesDocumentation() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    flags.addCommand(command("say-hello", () -> {}).setDescription("Says hello."));

    flags.run(new String[0]);

    assertThat(stringWriter.getBuffer().toString()).contains("  say-hello\n    Says hello.\n\n");
  }

  @Test
  public void ordinalParam_generatesDocumentation() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    flags.addCommand(
        command("say-hello", (a) -> {}, ordinalParam(String.class, "name"))
            .setDescription("Says hello to the given name."));

    flags.run(new String[0]);

    assertThat(stringWriter.getBuffer().toString())
        .contains("  say-hello <name>\n    Says hello to the given name.\n\n");
  }

  @Test
  public void repeatedOrdinalParam_generatesDocumentation() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    flags.addCommand(
        command("say-hello", (a) -> {}, repeated(ordinalParam(String.class, "name")))
            .setDescription("Says hello to the given names."));

    flags.run(new String[0]);

    assertThat(stringWriter.getBuffer().toString())
        .contains("  say-hello <name>...\n    Says hello to the given names.\n\n");
  }

  @Test
  public void namedParam_generatesDocumentation() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    flags.addCommand(
        command("say-hello", (a) -> {}, namedParam(String.class, "name"))
            .setDescription("Says hello to the given name."));

    flags.run(new String[0]);

    assertThat(stringWriter.getBuffer().toString())
        .contains("  say-hello --name <value>\n    Says hello to the given name.\n\n");
  }

  @Test
  public void optionalNamedParam_generatesDocumentation() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    flags.addCommand(
        command("say-hello", (a) -> {}, optional(namedParam(String.class, "name")))
            .setDescription("Says hello to the given name, if provided."));

    flags.run(new String[0]);

    assertThat(stringWriter.getBuffer().toString())
        .contains(
            "  say-hello [--name <value>]\n    Says hello to the given name, if provided.\n\n");
  }

  @Test
  public void repeatedNamedParam_generatesDocumentation() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    flags.addCommand(
        command("say-hello", (a) -> {}, repeated(namedParam(String.class, "name")))
            .setDescription("Says hello to the given name, if provided."));

    flags.run(new String[0]);

    assertThat(stringWriter.getBuffer().toString())
        .contains(
            "  say-hello --name <value>...\n    Says hello to the given name, if provided.\n\n");
  }

  @Test
  public void multipleCommands_generatesDocumentation() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    flags.addCommand(command("say-hello", () -> {}).setDescription("Says hello."));
    flags.addCommand(
        command("enable-setting", (a) -> {}, ordinalParam(boolean.class, "value"))
            .setDescription("Enables setting depending on whether value is true or false."));
    flags.addCommand(
        command(
                "command-2",
                (a, b) -> {},
                ordinalParam(boolean.class, "true-or-false"),
                ordinalParam(int.class, "quantity"))
            .setDescription("Does thing 2."));
    flags.addCommand(
        command(
                "command-3",
                (a, b, c, d) -> {},
                ordinalParam(boolean.class, "true-or-false"),
                optional(namedParam(String.class, "string")),
                repeated(ordinalParam(String.class, "strings")),
                namedParam(int.class, "quantity"))
            .setDescription("Does thing 3."));

    flags.run(new String[0]);

    assertThat(stringWriter.getBuffer().toString())
        .contains(
            "  say-hello\n"
                + "    Says hello.\n\n"
                + "  enable-setting <value>\n"
                + "    Enables setting depending on whether value is true or false.\n\n"
                + "  command-2 <true-or-false> <quantity>\n"
                + "    Does thing 2.\n\n"
                + "  command-3 <true-or-false> <strings>... [--string <value>] --quantity <value>\n"
                + "    Does thing 3.\n\n");
  }

  @Test
  public void customParser_invalidInput_announcesErrorMessage() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    flags.registerCustomParser(
        KeyValueType.class,
        (string, validator) -> {
          if (!string.contains("=")) {
            return validator.invalid("Key-value type did not contain '=' separator.");
          }

          String[] parts = string.split("=");
          return validator.valid(new KeyValueType(parts[0], parts[1]));
        });
    KeyValueCallback callback = new KeyValueCallback();
    flags.addCommand(
        command("command", callback::callback, ordinalParam(KeyValueType.class, "arg")));

    flags.run(asArgs("command key-value"));

    assertThat(callback.wasCalled).isFalse();
    assertThat(stringWriter.getBuffer().toString())
        .startsWith(
            "Parameter 'arg' could not be parsed as 'KeyValueType'.\n"
                + "Key-value type did not contain '=' separator.");
  }

  @Test
  public void longParamsAndDescription_wrappedAt80Chars() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    flags.addCommand(
        command(
                "very-long-command",
                (a, b, c, d) -> {},
                ordinalParam(String.class, "very-long-param-name1"),
                ordinalParam(String.class, "very-long-param-name2"),
                ordinalParam(String.class, "very-long-param-name3"),
                ordinalParam(String.class, "very-long-param-name4"))
            .setDescription(
                "Very long description that is very long. It contains lots of words but no actual"
                    + " content, as may be surprising to the reader; a feeling which turns yet more"
                    + " unsurprising the further on they read."));

    flags.run(new String[0]);

    assertThat(stringWriter.getBuffer().toString())
        .contains(
            "  very-long-command <very-long-param-name1> <very-long-param-name2> \\\n"
                + "        <very-long-param-name3> <very-long-param-name4>\n"
                + "    Very long description that is very long. It contains lots of words but no\n"
                + "    actual content, as may be surprising to the reader; a feeling which turns\n"
                + "    yet more unsurprising the further on they read.\n\n");
  }

  @Test
  public void longParamsAndDescription_maxLineLength_wrappedCorrectly() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    flags.addCommand(
        command(
                "very-long-command",
                (a, b, c) -> {},
                ordinalParam(
                    String.class, "param-name-causes-immediate-wrap-------------------------"),
                ordinalParam(
                    String.class,
                    "param-name-over-80-chars---------------------------------------------------------"),
                ordinalParam(
                    String.class,
                    "param-name-fills-line-3-----------------------------------------------"))
            .setDescription(
                "Fills first description line exactly to the end.aaaaaaaaaaaaaaaaaaaaaaaaaaaa Fills"
                    + " second description line exactly to the end.aaaaaaaaaaaaaaaaaaaaaaaaaaa"
                    + " Goes-over-third-description-line.aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));

    flags.run(new String[0]);

    assertThat(stringWriter.getBuffer().toString())
        .contains(
            "  very-long-command \\\n"
                + "        <param-name-causes-immediate-wrap-------------------------> \\\n"
                + "       "
                + " <param-name-over-80-chars--------------------------------------------------------->"
                + " \\\n"
                + "       "
                + " <param-name-fills-line-3----------------------------------------------->\n"
                + "    Fills first description line exactly to the"
                + " end.aaaaaaaaaaaaaaaaaaaaaaaaaaaa\n"
                + "    Fills second description line exactly to the"
                + " end.aaaaaaaaaaaaaaaaaaaaaaaaaaa\n"
                + "    Goes-over-third-description-line.aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n\n");
  }

  @Test
  public void shortWordsInDescription_wrappedCorrectly() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    flags.addCommand(
        command("very-long-command", () -> {})
            .setDescription(
                "A b c d e f g h i j k l m n o p q r s t u v w x y z a b c d e f g h i j k l m n"
                    + " o."));

    flags.run(new String[0]);

    assertThat(stringWriter.getBuffer().toString())
        .contains(
            "  very-long-command\n"
                + "    A b c d e f g h i j k l m n o p q r s t u v w x y z a b c d e f g h i j k"
                + " l\n"
                + "    m n o.\n\n");
  }

  @Test
  public void noDescription_skipsDescriptionLine() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    flags.addCommand(command("say-hello", () -> {}));
    flags.addCommand(command("say-hello2", () -> {}));

    flags.run(new String[0]);

    assertThat(stringWriter.getBuffer().toString()).contains("  say-hello\n\n  say-hello2\n\n");
  }

  @Test
  public void noCommandsRegistered_generatesHelpDocumentation() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));

    flags.run(new String[0]);

    assertThat(stringWriter.getBuffer().toString())
        .isEqualTo("Usage:\n  help\n    Prints this help text.\n\n");
  }

  @Test
  public void nullInput_generatesDocumentation() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    flags.addCommand(command("say-hello", () -> {}).setDescription("Says hello."));

    flags.run(null);

    assertThat(stringWriter.getBuffer().toString()).contains("  say-hello\n    Says hello.\n\n");
  }

  @Test
  public void helpCommandInput_generatesDocumentation() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    flags.addCommand(command("say-hello", () -> {}).setDescription("Says hello."));

    flags.run(new String[] {"help"});

    assertThat(stringWriter.getBuffer().toString()).contains("  say-hello\n    Says hello.\n\n");
  }

  @Test
  public void helpTextPrinted_flushesWriter() {
    CustomPrintWriter printWriter = new CustomPrintWriter();
    Flags flags = new Flags(printWriter);
    flags.addCommand(command("say-hello", () -> {}).setDescription("Says hello."));

    flags.run(new String[0]);

    assertThat(printWriter.hasBeenFlushed).isTrue();
  }

  private static class CustomPrintWriter extends PrintWriter {
    public boolean hasBeenFlushed;

    public CustomPrintWriter() {
      super(new StringWriter());
    }

    @Override
    public void flush() {
      hasBeenFlushed = true;
    }
  }
}
