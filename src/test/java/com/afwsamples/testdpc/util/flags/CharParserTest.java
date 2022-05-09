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

import com.afwsamples.testdpc.util.flags.Utils.CharCallback;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CharParserTest {
  private final String argument;

  private final boolean callbackShouldBeCalled;

  private final char expectedValue;

  public CharParserTest(String argument, boolean callbackShouldBeCalled, char expectedValue) {
    this.argument = argument;
    this.callbackShouldBeCalled = callbackShouldBeCalled;
    this.expectedValue = expectedValue;
  }

  @Parameters(name = "parse=''{0}'', isValidInput={1}")
  public static Collection<Object[]> data() {
    List<Object[]> params = new ArrayList<>();

    // Valid arguments
    for (char c = 0; c < 256; ++c) {
      if (c == ' ') {
        continue;
      }

      params.add(new Object[] {Character.toString(c), true, c});
    }

    // Invalid arguments
    params.add(new Object[] {"ab", false, '\0'});
    params.add(new Object[] {"abcdefghijklmnop", false, '\0'});

    return params;
  }

  @Test
  public void char_flagSpecifiedAsPrimitiveType_isParsedCorrectly() {
    CharCallback callback = new CharCallback();
    Flags flags = new Flags();
    flags.addCommand(command("command", callback::callback, ordinalParam(char.class, "arg")));

    flags.run(asArgs("command " + argument));

    assertThat(callback.wasCalled).isEqualTo(callbackShouldBeCalled);
    assertThat(callback.value).isEqualTo(expectedValue);
  }

  @Test
  public void char_flagSpecifiedAsObjectType_isParsedCorrectly() {
    CharCallback callback = new CharCallback();
    Flags flags = new Flags();
    flags.addCommand(command("command", callback::callback, ordinalParam(Character.class, "arg")));

    flags.run(asArgs("command " + argument));

    assertThat(callback.wasCalled).isEqualTo(callbackShouldBeCalled);
    assertThat(callback.value).isEqualTo(expectedValue);
  }
}
