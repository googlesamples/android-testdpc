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

import com.afwsamples.testdpc.util.flags.Utils.KeyValueCallback;
import com.afwsamples.testdpc.util.flags.Utils.KeyValueType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CustomParserTest {
  @Test
  public void customParser_works() {
    Flags flags = new Flags();
    flags.registerCustomParser(
        KeyValueType.class,
        (string, validator) -> {
          if (!string.contains("=")) {
            return validator.invalid("Key-value type must contain '=' separator.");
          }

          String[] parts = string.split("=");
          return validator.valid(new KeyValueType(parts[0], parts[1]));
        });
    KeyValueCallback callback = new KeyValueCallback();
    flags.addCommand(
        command("command", callback::callback, ordinalParam(KeyValueType.class, "arg")));

    flags.run(asArgs("command key=value"));

    assertThat(callback.wasCalled).isTrue();
    assertThat(callback.value.key).isEqualTo("key");
    assertThat(callback.value.value).isEqualTo("value");
  }
}
