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

import com.afwsamples.testdpc.util.flags.Utils.BooleanArrayCallback;
import com.afwsamples.testdpc.util.flags.Utils.Callback2Arg;
import com.afwsamples.testdpc.util.flags.Utils.ObjectCallback;
import com.afwsamples.testdpc.util.flags.Utils.StringArrayCallback;
import com.afwsamples.testdpc.util.flags.Utils.StringCallback;
import com.google.testing.junit.testparameterinjector.TestParameter;
import com.google.testing.junit.testparameterinjector.TestParameterInjector;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(TestParameterInjector.class)
public class ParamTest {
  @Test
  public void ordinalParam_works() {
    StringCallback callback = new StringCallback();
    Flags flags = new Flags();
    flags.addCommand(command("command", callback::callback, ordinalParam(String.class, "arg")));

    flags.run(asArgs("command hello"));

    assertThat(callback.wasCalled).isTrue();
    assertThat(callback.value).isEqualTo("hello");
  }

  @Test
  public void namedParam_works() {
    StringCallback callback = new StringCallback();
    Flags flags = new Flags();
    flags.addCommand(command("command", callback::callback, namedParam(String.class, "arg")));

    flags.run(asArgs("command --arg hello"));

    assertThat(callback.wasCalled).isTrue();
    assertThat(callback.value).isEqualTo("hello");
  }

  @Test
  public void optionalParam_provided_works() {
    StringCallback callback = new StringCallback();
    Flags flags = new Flags();
    flags.addCommand(
        command("command", callback::callback, optional(namedParam(String.class, "arg"))));

    flags.run(asArgs("command --arg hello"));

    assertThat(callback.wasCalled).isTrue();
    assertThat(callback.value).isEqualTo("hello");
  }

  @Test
  public void optionalParam_notProvided_isNull() {
    StringCallback callback = new StringCallback();
    Flags flags = new Flags();
    flags.addCommand(
        command("command", callback::callback, optional(namedParam(String.class, "arg"))));

    flags.run(asArgs("command"));

    assertThat(callback.wasCalled).isTrue();
    assertThat(callback.value).isNull();
  }

  @Test
  public void optionalParam_valueType_notProvided_isDefault(@TestParameter ValueType valueType) {
    ObjectCallback callback = new ObjectCallback();
    Flags flags = new Flags();
    flags.addCommand(
        command("command", callback::callback, optional(namedParam(valueType.type, "arg"))));

    flags.run(asArgs("command"));

    assertThat(callback.wasCalled).isTrue();
    assertThat(callback.value).isEqualTo(valueType.defaultValue);
  }

  @Test
  public void repeatedOrdinalParam_isValid() {
    StringArrayCallback callback = new StringArrayCallback();
    Flags flags = new Flags();
    flags.addCommand(
        command("command", callback::callback, repeated(ordinalParam(String.class, "arg"))));

    flags.run(asArgs("command 1 2 3 4 5"));

    assertThat(callback.wasCalled).isTrue();
    assertThat(callback.value).asList().containsExactly("1", "2", "3", "4", "5");
  }

  @Test
  public void repeatedOrdinalParam_noValuesProvided_producesEmptyArray() {
    StringArrayCallback callback = new StringArrayCallback();
    Flags flags = new Flags();
    flags.addCommand(
        command("command", callback::callback, repeated(ordinalParam(String.class, "arg"))));

    flags.run(asArgs("command"));

    assertThat(callback.wasCalled).isTrue();
    assertThat(callback.value).isNotNull();
    assertThat(callback.value).asList().isEmpty();
  }

  @Test
  public void repeatedOrdinalParam_primitiveType_isValid() {
    BooleanArrayCallback callback = new BooleanArrayCallback();
    Flags flags = new Flags();
    flags.addCommand(
        command("command", callback::callback, repeated(ordinalParam(Boolean.class, "arg"))));

    flags.run(asArgs("command true false true false true"));

    assertThat(callback.wasCalled).isTrue();
    assertThat(callback.value).asList().containsExactly(true, false, true, false, true);
  }

  @Test
  public void repeatedNamedParam_isValid() {
    StringArrayCallback callback = new StringArrayCallback();
    Flags flags = new Flags();
    flags.addCommand(
        command("command", callback::callback, repeated(namedParam(String.class, "arg"))));

    flags.run(asArgs("command --arg 1 2 3 4 5"));

    assertThat(callback.wasCalled).isTrue();
    assertThat(callback.value).asList().containsExactly("1", "2", "3", "4", "5");
  }

  @Test
  public void repeatedNamedParam_noValuesProvided_producesEmptyArray() {
    StringArrayCallback callback = new StringArrayCallback();
    Flags flags = new Flags();
    flags.addCommand(
        command("command", callback::callback, repeated(namedParam(String.class, "arg"))));

    flags.run(asArgs("command"));

    assertThat(callback.wasCalled).isTrue();
    assertThat(callback.value).isNotNull();
    assertThat(callback.value).asList().isEmpty();
  }

  @Test
  public void repeatedNamedParam_calledByNameWithNoValues_producesEmptyArray() {
    StringArrayCallback callback = new StringArrayCallback();
    Flags flags = new Flags();
    flags.addCommand(
        command("command", callback::callback, repeated(namedParam(String.class, "arg"))));

    flags.run(asArgs("command --arg"));

    assertThat(callback.wasCalled).isTrue();
    assertThat(callback.value).isNotNull();
    assertThat(callback.value).asList().isEmpty();
  }

  @Test
  public void namedParam_calledBeforeOrdinalParams_executesCorrectly() {
    Callback2Arg callback = new Callback2Arg();
    StringWriter string = new StringWriter();
    Flags flags = new Flags(new PrintWriter(string));
    flags.addCommand(
        command(
            "command",
            callback::callback,
            ordinalParam(String.class, "arg1"),
            namedParam(String.class, "arg2")));

    flags.run(asArgs("command --arg2 arg2Value arg1Value"));

    assertThat(string.getBuffer().toString()).isEmpty();
    assertThat(callback.wasCalled).isTrue();
    assertThat(callback.arg1).isEqualTo("arg1Value");
    assertThat(callback.arg2).isEqualTo("arg2Value");
  }

  private enum ValueType {
    BOOLEAN(boolean.class, false),
    CHAR(char.class, '\0'),
    FLOAT(float.class, 0f),
    DOUBLE(double.class, 0d),
    BYTE(byte.class, (byte) 0),
    SHORT(short.class, (short) 0),
    INT(int.class, 0),
    LONG(long.class, 0L);

    final Class<?> type;
    final Object defaultValue;

    ValueType(Class<?> type, Object defaultValue) {
      this.type = type;
      this.defaultValue = defaultValue;
    }
  }
}
