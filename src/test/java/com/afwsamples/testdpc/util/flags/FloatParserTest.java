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

import com.afwsamples.testdpc.util.flags.Utils.FloatCallback;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class FloatParserTest {
  private final String argument;

  private final boolean callbackShouldBeCalled;

  private final float expectedValue;

  public FloatParserTest(String argument, boolean callbackShouldBeCalled, float expectedValue) {
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
          {"0", true, 0f},
          {"9", true, 9f},
          {"0.1", true, 0.1f},
          {"0.123456", true, 0.123456f},
          {"0.12345678", true, 0.12345678f},
          {"12345678.123456", true, 12345678.123456f},
          {"9999999999999999.999999", true, 9999999999999999.999999f},
          {".5", true, 0.5f},
          {"0123.", true, 123f},
          {"0100", true, 100f},
          {"0..1", false, 0f},
          {"10^23.5", false, 0f},
        });
  }

  @Test
  public void float_flagSpecifiedAsPrimitiveType_isParsedCorrectly() {
    FloatCallback callback = new FloatCallback();
    Flags flags = new Flags();
    flags.addCommand(command("command", callback::callback, ordinalParam(float.class, "arg")));

    flags.run(asArgs("command " + argument));

    assertThat(callback.wasCalled).isEqualTo(callbackShouldBeCalled);
    assertThat(callback.value).isEqualTo(expectedValue);
  }

  @Test
  public void float_flagSpecifiedAsObjectType_isParsedCorrectly() {
    FloatCallback callback = new FloatCallback();
    Flags flags = new Flags();
    flags.addCommand(command("command", callback::callback, ordinalParam(Float.class, "arg")));

    flags.run(asArgs("command " + argument));

    assertThat(callback.wasCalled).isEqualTo(callbackShouldBeCalled);
    assertThat(callback.value).isEqualTo(expectedValue);
  }
}
