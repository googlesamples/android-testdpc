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

import com.afwsamples.testdpc.util.flags.Utils.ShortCallback;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ShortParserTest {
  private final String argument;

  private final boolean callbackShouldBeCalled;

  private final short expectedValue;

  public ShortParserTest(String argument, boolean callbackShouldBeCalled, short expectedValue) {
    this.argument = argument;
    this.callbackShouldBeCalled = callbackShouldBeCalled;
    this.expectedValue = expectedValue;
  }

  @Parameters(name = "parse=''{0}'', isValidInput={1}")
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          // input string - is valid - parsed value
          {"-32768", true, (short) -32768},
          {"-99", true, (short) -99},
          {"0", true, (short) 0},
          {"99", true, (short) 99},
          {"32767", true, (short) 32767},
          {"-32769", false, (short) 0},
          {"32768", false, (short) 0},
        });
  }

  @Test
  public void short_flagSpecifiedAsPrimitiveType_isParsedCorrectly() {
    ShortCallback callback = new ShortCallback();
    Flags flags = new Flags();
    flags.addCommand(command("command", callback::callback, ordinalParam(short.class, "arg")));

    flags.run(asArgs("command " + argument));

    assertThat(callback.wasCalled).isEqualTo(callbackShouldBeCalled);
    assertThat(callback.value).isEqualTo(expectedValue);
  }

  @Test
  public void short_flagSpecifiedAsObjectType_isParsedCorrectly() {
    ShortCallback callback = new ShortCallback();
    Flags flags = new Flags();
    flags.addCommand(command("command", callback::callback, ordinalParam(Short.class, "arg")));

    flags.run(asArgs("command " + argument));

    assertThat(callback.wasCalled).isEqualTo(callbackShouldBeCalled);
    assertThat(callback.value).isEqualTo(expectedValue);
  }
}
