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
import static com.afwsamples.testdpc.util.flags.Flags.ordinalParam;
import static com.afwsamples.testdpc.util.flags.Utils.asArgs;
import static com.google.common.truth.Truth.assertThat;

import com.afwsamples.testdpc.util.flags.Utils.Callback10Arg;
import com.afwsamples.testdpc.util.flags.Utils.Callback11Arg;
import com.afwsamples.testdpc.util.flags.Utils.Callback12Arg;
import com.afwsamples.testdpc.util.flags.Utils.Callback13Arg;
import com.afwsamples.testdpc.util.flags.Utils.Callback14Arg;
import com.afwsamples.testdpc.util.flags.Utils.Callback15Arg;
import com.afwsamples.testdpc.util.flags.Utils.Callback16Arg;
import com.afwsamples.testdpc.util.flags.Utils.Callback1Arg;
import com.afwsamples.testdpc.util.flags.Utils.Callback2Arg;
import com.afwsamples.testdpc.util.flags.Utils.Callback3Arg;
import com.afwsamples.testdpc.util.flags.Utils.Callback4Arg;
import com.afwsamples.testdpc.util.flags.Utils.Callback5Arg;
import com.afwsamples.testdpc.util.flags.Utils.Callback6Arg;
import com.afwsamples.testdpc.util.flags.Utils.Callback7Arg;
import com.afwsamples.testdpc.util.flags.Utils.Callback8Arg;
import com.afwsamples.testdpc.util.flags.Utils.Callback9Arg;
import com.afwsamples.testdpc.util.flags.Utils.NoArgsCallback;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CallbackTest {
  @Test
  public void noArgsCallback_isInvoked() {
    NoArgsCallback callback = new NoArgsCallback();
    Flags flags = new Flags();
    flags.addCommand(command("command", callback::callback));

    flags.run(asArgs("command"));

    assertThat(callback.wasCalled).isTrue();
  }

  @Test
  public void callback1Arg_isInvoked() {
    Callback1Arg callback = new Callback1Arg();
    Flags flags = new Flags();
    flags.addCommand(command("command", callback::callback, ordinalParam(String.class, "arg1")));

    flags.run(asArgs("command 1"));

    assertThat(callback.arg1).isEqualTo("1");
  }

  @Test
  public void callback2Arg_isInvoked() {
    Callback2Arg callback = new Callback2Arg();
    Flags flags = new Flags();
    flags.addCommand(
        command(
            "command",
            callback::callback,
            ordinalParam(String.class, "arg1"),
            ordinalParam(String.class, "arg2")));

    flags.run(asArgs("command 1 2"));

    assertThat(callback.arg1).isEqualTo("1");
    assertThat(callback.arg2).isEqualTo("2");
  }

  @Test
  public void callback3Arg_isInvoked() {
    Callback3Arg callback = new Callback3Arg();
    Flags flags = new Flags();
    flags.addCommand(
        command(
            "command",
            callback::callback,
            ordinalParam(String.class, "arg1"),
            ordinalParam(String.class, "arg2"),
            ordinalParam(String.class, "arg3")));

    flags.run(asArgs("command 1 2 3"));

    assertThat(callback.arg1).isEqualTo("1");
    assertThat(callback.arg2).isEqualTo("2");
    assertThat(callback.arg3).isEqualTo("3");
  }

  @Test
  public void callback4Arg_isInvoked() {
    Callback4Arg callback = new Callback4Arg();
    Flags flags = new Flags();
    flags.addCommand(
        command(
            "command",
            callback::callback,
            ordinalParam(String.class, "arg1"),
            ordinalParam(String.class, "arg2"),
            ordinalParam(String.class, "arg3"),
            ordinalParam(String.class, "arg4")));

    flags.run(asArgs("command 1 2 3 4"));

    assertThat(callback.arg1).isEqualTo("1");
    assertThat(callback.arg2).isEqualTo("2");
    assertThat(callback.arg3).isEqualTo("3");
    assertThat(callback.arg4).isEqualTo("4");
  }

  @Test
  public void callback5Arg_isInvoked() {
    Callback5Arg callback = new Callback5Arg();
    Flags flags = new Flags();
    flags.addCommand(
        command(
            "command",
            callback::callback,
            ordinalParam(String.class, "arg1"),
            ordinalParam(String.class, "arg2"),
            ordinalParam(String.class, "arg3"),
            ordinalParam(String.class, "arg4"),
            ordinalParam(String.class, "arg5")));

    flags.run(asArgs("command 1 2 3 4 5"));

    assertThat(callback.arg1).isEqualTo("1");
    assertThat(callback.arg2).isEqualTo("2");
    assertThat(callback.arg3).isEqualTo("3");
    assertThat(callback.arg4).isEqualTo("4");
    assertThat(callback.arg5).isEqualTo("5");
  }

  @Test
  public void callback6Arg_isInvoked() {
    Callback6Arg callback = new Callback6Arg();
    Flags flags = new Flags();
    flags.addCommand(
        command(
            "command",
            callback::callback,
            ordinalParam(String.class, "arg1"),
            ordinalParam(String.class, "arg2"),
            ordinalParam(String.class, "arg3"),
            ordinalParam(String.class, "arg4"),
            ordinalParam(String.class, "arg5"),
            ordinalParam(String.class, "arg6")));

    flags.run(asArgs("command 1 2 3 4 5 6"));

    assertThat(callback.arg1).isEqualTo("1");
    assertThat(callback.arg2).isEqualTo("2");
    assertThat(callback.arg3).isEqualTo("3");
    assertThat(callback.arg4).isEqualTo("4");
    assertThat(callback.arg5).isEqualTo("5");
    assertThat(callback.arg6).isEqualTo("6");
  }

  @Test
  public void callback7Arg_isInvoked() {
    Callback7Arg callback = new Callback7Arg();
    Flags flags = new Flags();
    flags.addCommand(
        command(
            "command",
            callback::callback,
            ordinalParam(String.class, "arg1"),
            ordinalParam(String.class, "arg2"),
            ordinalParam(String.class, "arg3"),
            ordinalParam(String.class, "arg4"),
            ordinalParam(String.class, "arg5"),
            ordinalParam(String.class, "arg6"),
            ordinalParam(String.class, "arg7")));

    flags.run(asArgs("command 1 2 3 4 5 6 7"));

    assertThat(callback.arg1).isEqualTo("1");
    assertThat(callback.arg2).isEqualTo("2");
    assertThat(callback.arg3).isEqualTo("3");
    assertThat(callback.arg4).isEqualTo("4");
    assertThat(callback.arg5).isEqualTo("5");
    assertThat(callback.arg6).isEqualTo("6");
    assertThat(callback.arg7).isEqualTo("7");
  }

  @Test
  public void callback8Arg_isInvoked() {
    Callback8Arg callback = new Callback8Arg();
    Flags flags = new Flags();
    flags.addCommand(
        command(
            "command",
            callback::callback,
            ordinalParam(String.class, "arg1"),
            ordinalParam(String.class, "arg2"),
            ordinalParam(String.class, "arg3"),
            ordinalParam(String.class, "arg4"),
            ordinalParam(String.class, "arg5"),
            ordinalParam(String.class, "arg6"),
            ordinalParam(String.class, "arg7"),
            ordinalParam(String.class, "arg8")));

    flags.run(asArgs("command 1 2 3 4 5 6 7 8"));

    assertThat(callback.arg1).isEqualTo("1");
    assertThat(callback.arg2).isEqualTo("2");
    assertThat(callback.arg3).isEqualTo("3");
    assertThat(callback.arg4).isEqualTo("4");
    assertThat(callback.arg5).isEqualTo("5");
    assertThat(callback.arg6).isEqualTo("6");
    assertThat(callback.arg7).isEqualTo("7");
    assertThat(callback.arg8).isEqualTo("8");
  }

  @Test
  public void callback9Arg_isInvoked() {
    Callback9Arg callback = new Callback9Arg();
    Flags flags = new Flags();
    flags.addCommand(
        command(
            "command",
            callback::callback,
            ordinalParam(String.class, "arg1"),
            ordinalParam(String.class, "arg2"),
            ordinalParam(String.class, "arg3"),
            ordinalParam(String.class, "arg4"),
            ordinalParam(String.class, "arg5"),
            ordinalParam(String.class, "arg6"),
            ordinalParam(String.class, "arg7"),
            ordinalParam(String.class, "arg8"),
            ordinalParam(String.class, "arg9")));

    flags.run(asArgs("command 1 2 3 4 5 6 7 8 9"));

    assertThat(callback.arg1).isEqualTo("1");
    assertThat(callback.arg2).isEqualTo("2");
    assertThat(callback.arg3).isEqualTo("3");
    assertThat(callback.arg4).isEqualTo("4");
    assertThat(callback.arg5).isEqualTo("5");
    assertThat(callback.arg6).isEqualTo("6");
    assertThat(callback.arg7).isEqualTo("7");
    assertThat(callback.arg8).isEqualTo("8");
    assertThat(callback.arg9).isEqualTo("9");
  }

  @Test
  public void callback10Arg_isInvoked() {
    Callback10Arg callback = new Callback10Arg();
    Flags flags = new Flags();
    flags.addCommand(
        command(
            "command",
            callback::callback,
            ordinalParam(String.class, "arg1"),
            ordinalParam(String.class, "arg2"),
            ordinalParam(String.class, "arg3"),
            ordinalParam(String.class, "arg4"),
            ordinalParam(String.class, "arg5"),
            ordinalParam(String.class, "arg6"),
            ordinalParam(String.class, "arg7"),
            ordinalParam(String.class, "arg8"),
            ordinalParam(String.class, "arg9"),
            ordinalParam(String.class, "arg10")));

    flags.run(asArgs("command 1 2 3 4 5 6 7 8 9 10"));

    assertThat(callback.arg1).isEqualTo("1");
    assertThat(callback.arg2).isEqualTo("2");
    assertThat(callback.arg3).isEqualTo("3");
    assertThat(callback.arg4).isEqualTo("4");
    assertThat(callback.arg5).isEqualTo("5");
    assertThat(callback.arg6).isEqualTo("6");
    assertThat(callback.arg7).isEqualTo("7");
    assertThat(callback.arg8).isEqualTo("8");
    assertThat(callback.arg9).isEqualTo("9");
    assertThat(callback.arg10).isEqualTo("10");
  }

  @Test
  public void callback11Arg_isInvoked() {
    Callback11Arg callback = new Callback11Arg();
    Flags flags = new Flags();
    flags.addCommand(
        command(
            "command",
            callback::callback,
            ordinalParam(String.class, "arg1"),
            ordinalParam(String.class, "arg2"),
            ordinalParam(String.class, "arg3"),
            ordinalParam(String.class, "arg4"),
            ordinalParam(String.class, "arg5"),
            ordinalParam(String.class, "arg6"),
            ordinalParam(String.class, "arg7"),
            ordinalParam(String.class, "arg8"),
            ordinalParam(String.class, "arg9"),
            ordinalParam(String.class, "arg10"),
            ordinalParam(String.class, "arg11")));

    flags.run(asArgs("command 1 2 3 4 5 6 7 8 9 10 11"));

    assertThat(callback.arg1).isEqualTo("1");
    assertThat(callback.arg2).isEqualTo("2");
    assertThat(callback.arg3).isEqualTo("3");
    assertThat(callback.arg4).isEqualTo("4");
    assertThat(callback.arg5).isEqualTo("5");
    assertThat(callback.arg6).isEqualTo("6");
    assertThat(callback.arg7).isEqualTo("7");
    assertThat(callback.arg8).isEqualTo("8");
    assertThat(callback.arg9).isEqualTo("9");
    assertThat(callback.arg10).isEqualTo("10");
    assertThat(callback.arg11).isEqualTo("11");
  }

  @Test
  public void callback12Arg_isInvoked() {
    Callback12Arg callback = new Callback12Arg();
    Flags flags = new Flags();
    flags.addCommand(
        command(
            "command",
            callback::callback,
            ordinalParam(String.class, "arg1"),
            ordinalParam(String.class, "arg2"),
            ordinalParam(String.class, "arg3"),
            ordinalParam(String.class, "arg4"),
            ordinalParam(String.class, "arg5"),
            ordinalParam(String.class, "arg6"),
            ordinalParam(String.class, "arg7"),
            ordinalParam(String.class, "arg8"),
            ordinalParam(String.class, "arg9"),
            ordinalParam(String.class, "arg10"),
            ordinalParam(String.class, "arg11"),
            ordinalParam(String.class, "arg12")));

    flags.run(asArgs("command 1 2 3 4 5 6 7 8 9 10 11 12"));

    assertThat(callback.arg1).isEqualTo("1");
    assertThat(callback.arg2).isEqualTo("2");
    assertThat(callback.arg3).isEqualTo("3");
    assertThat(callback.arg4).isEqualTo("4");
    assertThat(callback.arg5).isEqualTo("5");
    assertThat(callback.arg6).isEqualTo("6");
    assertThat(callback.arg7).isEqualTo("7");
    assertThat(callback.arg8).isEqualTo("8");
    assertThat(callback.arg9).isEqualTo("9");
    assertThat(callback.arg10).isEqualTo("10");
    assertThat(callback.arg11).isEqualTo("11");
    assertThat(callback.arg12).isEqualTo("12");
  }

  @Test
  public void callback13Arg_isInvoked() {
    Callback13Arg callback = new Callback13Arg();
    Flags flags = new Flags();
    flags.addCommand(
        command(
            "command",
            callback::callback,
            ordinalParam(String.class, "arg1"),
            ordinalParam(String.class, "arg2"),
            ordinalParam(String.class, "arg3"),
            ordinalParam(String.class, "arg4"),
            ordinalParam(String.class, "arg5"),
            ordinalParam(String.class, "arg6"),
            ordinalParam(String.class, "arg7"),
            ordinalParam(String.class, "arg8"),
            ordinalParam(String.class, "arg9"),
            ordinalParam(String.class, "arg10"),
            ordinalParam(String.class, "arg11"),
            ordinalParam(String.class, "arg12"),
            ordinalParam(String.class, "arg13")));

    flags.run(asArgs("command 1 2 3 4 5 6 7 8 9 10 11 12 13"));

    assertThat(callback.arg1).isEqualTo("1");
    assertThat(callback.arg2).isEqualTo("2");
    assertThat(callback.arg3).isEqualTo("3");
    assertThat(callback.arg4).isEqualTo("4");
    assertThat(callback.arg5).isEqualTo("5");
    assertThat(callback.arg6).isEqualTo("6");
    assertThat(callback.arg7).isEqualTo("7");
    assertThat(callback.arg8).isEqualTo("8");
    assertThat(callback.arg9).isEqualTo("9");
    assertThat(callback.arg10).isEqualTo("10");
    assertThat(callback.arg11).isEqualTo("11");
    assertThat(callback.arg12).isEqualTo("12");
    assertThat(callback.arg13).isEqualTo("13");
  }

  @Test
  public void callback14Arg_isInvoked() {
    Callback14Arg callback = new Callback14Arg();
    Flags flags = new Flags();
    flags.addCommand(
        command(
            "command",
            callback::callback,
            ordinalParam(String.class, "arg1"),
            ordinalParam(String.class, "arg2"),
            ordinalParam(String.class, "arg3"),
            ordinalParam(String.class, "arg4"),
            ordinalParam(String.class, "arg5"),
            ordinalParam(String.class, "arg6"),
            ordinalParam(String.class, "arg7"),
            ordinalParam(String.class, "arg8"),
            ordinalParam(String.class, "arg9"),
            ordinalParam(String.class, "arg10"),
            ordinalParam(String.class, "arg11"),
            ordinalParam(String.class, "arg12"),
            ordinalParam(String.class, "arg13"),
            ordinalParam(String.class, "arg14")));

    flags.run(asArgs("command 1 2 3 4 5 6 7 8 9 10 11 12 13 14"));

    assertThat(callback.arg1).isEqualTo("1");
    assertThat(callback.arg2).isEqualTo("2");
    assertThat(callback.arg3).isEqualTo("3");
    assertThat(callback.arg4).isEqualTo("4");
    assertThat(callback.arg5).isEqualTo("5");
    assertThat(callback.arg6).isEqualTo("6");
    assertThat(callback.arg7).isEqualTo("7");
    assertThat(callback.arg8).isEqualTo("8");
    assertThat(callback.arg9).isEqualTo("9");
    assertThat(callback.arg10).isEqualTo("10");
    assertThat(callback.arg11).isEqualTo("11");
    assertThat(callback.arg12).isEqualTo("12");
    assertThat(callback.arg13).isEqualTo("13");
    assertThat(callback.arg14).isEqualTo("14");
  }

  @Test
  public void callback15Arg_isInvoked() {
    Callback15Arg callback = new Callback15Arg();
    Flags flags = new Flags();
    flags.addCommand(
        command(
            "command",
            callback::callback,
            ordinalParam(String.class, "arg1"),
            ordinalParam(String.class, "arg2"),
            ordinalParam(String.class, "arg3"),
            ordinalParam(String.class, "arg4"),
            ordinalParam(String.class, "arg5"),
            ordinalParam(String.class, "arg6"),
            ordinalParam(String.class, "arg7"),
            ordinalParam(String.class, "arg8"),
            ordinalParam(String.class, "arg9"),
            ordinalParam(String.class, "arg10"),
            ordinalParam(String.class, "arg11"),
            ordinalParam(String.class, "arg12"),
            ordinalParam(String.class, "arg13"),
            ordinalParam(String.class, "arg14"),
            ordinalParam(String.class, "arg15")));

    flags.run(asArgs("command 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15"));

    assertThat(callback.arg1).isEqualTo("1");
    assertThat(callback.arg2).isEqualTo("2");
    assertThat(callback.arg3).isEqualTo("3");
    assertThat(callback.arg4).isEqualTo("4");
    assertThat(callback.arg5).isEqualTo("5");
    assertThat(callback.arg6).isEqualTo("6");
    assertThat(callback.arg7).isEqualTo("7");
    assertThat(callback.arg8).isEqualTo("8");
    assertThat(callback.arg9).isEqualTo("9");
    assertThat(callback.arg10).isEqualTo("10");
    assertThat(callback.arg11).isEqualTo("11");
    assertThat(callback.arg12).isEqualTo("12");
    assertThat(callback.arg13).isEqualTo("13");
    assertThat(callback.arg14).isEqualTo("14");
    assertThat(callback.arg15).isEqualTo("15");
  }

  @Test
  public void callback16Arg_isInvoked() {
    Callback16Arg callback = new Callback16Arg();
    Flags flags = new Flags();
    flags.addCommand(
        command(
            "command",
            callback::callback,
            ordinalParam(String.class, "arg1"),
            ordinalParam(String.class, "arg2"),
            ordinalParam(String.class, "arg3"),
            ordinalParam(String.class, "arg4"),
            ordinalParam(String.class, "arg5"),
            ordinalParam(String.class, "arg6"),
            ordinalParam(String.class, "arg7"),
            ordinalParam(String.class, "arg8"),
            ordinalParam(String.class, "arg9"),
            ordinalParam(String.class, "arg10"),
            ordinalParam(String.class, "arg11"),
            ordinalParam(String.class, "arg12"),
            ordinalParam(String.class, "arg13"),
            ordinalParam(String.class, "arg14"),
            ordinalParam(String.class, "arg15"),
            ordinalParam(String.class, "arg16")));

    flags.run(asArgs("command 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16"));

    assertThat(callback.arg1).isEqualTo("1");
    assertThat(callback.arg2).isEqualTo("2");
    assertThat(callback.arg3).isEqualTo("3");
    assertThat(callback.arg4).isEqualTo("4");
    assertThat(callback.arg5).isEqualTo("5");
    assertThat(callback.arg6).isEqualTo("6");
    assertThat(callback.arg7).isEqualTo("7");
    assertThat(callback.arg8).isEqualTo("8");
    assertThat(callback.arg9).isEqualTo("9");
    assertThat(callback.arg10).isEqualTo("10");
    assertThat(callback.arg11).isEqualTo("11");
    assertThat(callback.arg12).isEqualTo("12");
    assertThat(callback.arg13).isEqualTo("13");
    assertThat(callback.arg14).isEqualTo("14");
    assertThat(callback.arg15).isEqualTo("15");
    assertThat(callback.arg16).isEqualTo("16");
  }
}
