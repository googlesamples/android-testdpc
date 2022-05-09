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
import static com.afwsamples.testdpc.util.flags.Flags.ordinalParam;
import static com.afwsamples.testdpc.util.flags.Flags.repeated;
import static com.afwsamples.testdpc.util.flags.Utils.asArgs;
import static com.google.common.truth.Truth.assertThat;

import com.afwsamples.testdpc.util.flags.Utils.IntCallback;
import com.afwsamples.testdpc.util.flags.Utils.NoArgsCallback;
import com.afwsamples.testdpc.util.flags.Utils.StringArrayCallback;
import com.afwsamples.testdpc.util.flags.Utils.StringCallback;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class InvalidCallsTest {
  @Test
  public void noParams_calledWithOrdinalArg_commandShortCircuitsWithHelpText() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    NoArgsCallback callback = new NoArgsCallback();
    flags.addCommand(command("command", callback::callback));

    flags.run(asArgs("command arg"));

    assertThat(callback.wasCalled).isFalse();
    assertThat(stringWriter.getBuffer().toString())
        .startsWith("More ordinal parameters were provided than the command accepts.\n\nUsage:");
  }

  @Test
  public void noParams_calledWithNamedArg_commandShortCircuitsWithHelpText() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    NoArgsCallback callback = new NoArgsCallback();
    flags.addCommand(command("command", callback::callback));

    flags.run(asArgs("command --namedArg 5"));

    assertThat(callback.wasCalled).isFalse();
    assertThat(stringWriter.getBuffer().toString())
        .startsWith("Named argument 'namedArg' does not exist.\n\nUsage:");
  }

  @Test
  public void ordinalParam_notProvided_commandShortCircuitsWithHelpText() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    IntCallback callback = new IntCallback();
    flags.addCommand(command("command", callback::callback, ordinalParam(int.class, "arg")));

    flags.run(asArgs("command"));

    assertThat(callback.wasCalled).isFalse();
    assertThat(stringWriter.getBuffer().toString())
        .startsWith("Ordinal parameter 'arg' was not provided.\n\nUsage:");
  }

  @Test
  public void namedParam_notProvided_commandShortCircuitsWithHelpText() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    IntCallback callback = new IntCallback();
    flags.addCommand(command("command", callback::callback, namedParam(int.class, "arg")));

    flags.run(asArgs("command"));

    assertThat(callback.wasCalled).isFalse();
    assertThat(stringWriter.getBuffer().toString())
        .startsWith("Named parameter 'arg' was not provided.\n\nUsage:");
  }

  @Test
  public void namedParam_calledButValueNotProvided_commandShortCircuitsWithHelpText() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    IntCallback callback = new IntCallback();
    flags.addCommand(command("command", callback::callback, namedParam(int.class, "arg")));

    flags.run(asArgs("command --arg"));

    assertThat(callback.wasCalled).isFalse();
    assertThat(stringWriter.getBuffer().toString())
        .startsWith("Expected value for parameter 'arg' but it was not provided.\n\nUsage:");
  }

  @Test
  public void repeatedNamedParam_calledByNameMultipleTimes_commandShortCircuitsWithHelpText() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    StringArrayCallback callback = new StringArrayCallback();
    flags.addCommand(
        command("command", callback::callback, repeated(namedParam(String.class, "arg"))));

    flags.run(asArgs("command --arg 1 --arg 2 --arg 3"));

    assertThat(callback.wasCalled).isFalse();
    assertThat(stringWriter.getBuffer().toString())
        .startsWith("Named argument 'arg' was called repeatedly.\n\nUsage:");
  }

  @Test
  public void argCanNotBeParsed_commandShortCircuitsWithHelpText() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    IntCallback callback = new IntCallback();
    flags.addCommand(command("command", callback::callback, ordinalParam(int.class, "arg")));

    flags.run(asArgs("command hello"));

    assertThat(callback.wasCalled).isFalse();
    assertThat(stringWriter.getBuffer().toString())
        .startsWith("Parameter 'arg' could not be parsed as 'int'.\n\nUsage:");
  }

  @Test
  public void unregisteredCommandCalled_commandShortCircuitsWithHelpText() {
    StringWriter stringWriter = new StringWriter();
    Flags flags = new Flags(new PrintWriter(stringWriter));
    StringCallback callback = new StringCallback();
    flags.addCommand(command("command", callback::callback, ordinalParam(String.class, "arg")));

    flags.run(asArgs("hello command"));

    assertThat(callback.wasCalled).isFalse();
    assertThat(stringWriter.getBuffer().toString())
        .startsWith("Command 'hello' was not found.\n\nUsage:");
  }
}
