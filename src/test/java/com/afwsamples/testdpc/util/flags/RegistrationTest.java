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
import static org.junit.Assert.assertThrows;

import com.afwsamples.testdpc.util.flags.Utils.ObjectCallback;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RegistrationTest {
  @Test
  public void duplicateCommandName_throwsException() {
    Flags flags = new Flags();
    flags.addCommand(command("duplicate-command-name", () -> {}));

    assertThrows(
        UnsupportedOperationException.class,
        () -> flags.addCommand(command("duplicate-command-name", () -> {})));
  }

  @Test
  public void repeatedOrdinalParam_notLastOrdinal_throwsException() {
    Flags flags = new Flags();

    assertThrows(
        UnsupportedOperationException.class,
        () ->
            flags.addCommand(
                command(
                    "command",
                    (a, b) -> {},
                    repeated(ordinalParam(String.class, "arg1")),
                    ordinalParam(int.class, "arg2"))));
  }

  @Test
  public void duplicateParamName_throwsException() {
    Flags flags = new Flags();

    assertThrows(
        UnsupportedOperationException.class,
        () ->
            flags.addCommand(
                command(
                    "command",
                    (a, b) -> {},
                    ordinalParam(String.class, "duplicate-name"),
                    namedParam(int.class, "duplicate-name"))));
  }

  @Test
  public void noParserAvailable_registersParam_throwsException() {
    Flags flags = new Flags();
    ObjectCallback callback = new ObjectCallback();

    assertThrows(
        UnsupportedOperationException.class,
        () ->
            flags.addCommand(
                command("command", callback::callback, ordinalParam(Object.class, "arg"))));
  }
}
