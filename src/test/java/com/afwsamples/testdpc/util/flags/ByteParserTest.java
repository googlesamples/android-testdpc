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

import com.afwsamples.testdpc.util.flags.Utils.ByteCallback;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ByteParserTest {
  private final String argument;

  private final boolean callbackShouldBeCalled;

  private final byte expectedValue;

  public ByteParserTest(String argument, boolean callbackShouldBeCalled, byte expectedValue) {
    this.argument = argument;
    this.callbackShouldBeCalled = callbackShouldBeCalled;
    this.expectedValue = expectedValue;
  }

  @Parameters(name = "parse=''{0}'', isValidInput={1}")
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          // input string - is valid - parsed value
          {"-128", true, (byte) -128},
          {"-99", true, (byte) -99},
          {"0", true, (byte) 0},
          {"99", true, (byte) 99},
          {"127", true, (byte) 127},
          {"-129", false, (byte) 0},
          {"128", false, (byte) 0},
        });
  }

  @Test
  public void byte_flagSpecifiedAsPrimitiveType_isParsedCorrectly() {
    ByteCallback callback = new ByteCallback();
    Flags flags = new Flags();
    flags.addCommand(command("command", callback::callback, ordinalParam(byte.class, "arg")));

    flags.run(asArgs("command " + argument));

    assertThat(callback.wasCalled).isEqualTo(callbackShouldBeCalled);
    assertThat(callback.value).isEqualTo(expectedValue);
  }

  @Test
  public void byte_flagSpecifiedAsObjectType_isParsedCorrectly() {
    ByteCallback callback = new ByteCallback();
    Flags flags = new Flags();
    flags.addCommand(command("command", callback::callback, ordinalParam(Byte.class, "arg")));

    flags.run(asArgs("command " + argument));

    assertThat(callback.wasCalled).isEqualTo(callbackShouldBeCalled);
    assertThat(callback.value).isEqualTo(expectedValue);
  }
}
