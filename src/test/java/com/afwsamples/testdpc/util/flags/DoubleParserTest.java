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

import com.afwsamples.testdpc.util.flags.Utils.DoubleCallback;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DoubleParserTest {
  private final String argument;

  private final boolean callbackShouldBeCalled;

  private final double expectedValue;

  public DoubleParserTest(String argument, boolean callbackShouldBeCalled, double expectedValue) {
    this.argument = argument;
    this.callbackShouldBeCalled = callbackShouldBeCalled;
    this.expectedValue = expectedValue;
  }

  @Parameters
  @SuppressWarnings("FloatingPointLiteralPrecision")
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          // input string - is valid - parsed value
          {"0", true, 0d},
          {"9", true, 9d},
          {"0.1", true, 0.1d},
          {"0.123456", true, 0.123456d},
          {"0.12345678", true, 0.12345678d},
          {"12345678.123456", true, 12345678.123456d},
          {"9999999999999999.999999", true, 9999999999999999.999999d},
          {".5", true, 0.5d},
          {"0123.", true, 123d},
          {"0100", true, 100d},
          {"0..1", false, 0d},
          {"10^23.5", false, 0d},
        });
  }

  @Test
  public void double_flagSpecifiedAsPrimitiveType_isParsedCorrectly() {
    DoubleCallback callback = new DoubleCallback();
    Flags flags = new Flags();
    flags.addCommand(command("command", callback::callback, ordinalParam(double.class, "arg")));

    flags.run(asArgs("command " + argument));

    assertThat(callback.wasCalled).isEqualTo(callbackShouldBeCalled);
    assertThat(callback.value).isEqualTo(expectedValue);
  }

  @Test
  public void double_flagSpecifiedAsObjectType_isParsedCorrectly() {
    DoubleCallback callback = new DoubleCallback();
    Flags flags = new Flags();
    flags.addCommand(command("command", callback::callback, ordinalParam(Double.class, "arg")));

    flags.run(asArgs("command " + argument));

    assertThat(callback.wasCalled).isEqualTo(callbackShouldBeCalled);
    assertThat(callback.value).isEqualTo(expectedValue);
  }
}
