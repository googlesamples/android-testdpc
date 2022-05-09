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

public final class Utils {
  private Utils() {}

  public static String[] asArgs(String string) {
    return string.split(" ");
  }

  public static class NoArgsCallback {
    public boolean wasCalled;

    public void callback() {
      wasCalled = true;
    }
  }

  public static class BooleanCallback {
    public boolean wasCalled;
    public boolean value;

    public void callback(boolean value) {
      wasCalled = true;
      this.value = value;
    }
  }

  public static class CharCallback {
    public boolean wasCalled;
    public char value;

    public void callback(char value) {
      wasCalled = true;
      this.value = value;
    }
  }

  public static class FloatCallback {
    public boolean wasCalled;
    public float value;

    public void callback(float value) {
      wasCalled = true;
      this.value = value;
    }
  }

  public static class DoubleCallback {
    public boolean wasCalled;
    public double value;

    public void callback(double value) {
      wasCalled = true;
      this.value = value;
    }
  }

  public static class ByteCallback {
    public boolean wasCalled;
    public byte value;

    public void callback(byte value) {
      wasCalled = true;
      this.value = value;
    }
  }

  public static class ShortCallback {
    public boolean wasCalled;
    public short value;

    public void callback(short value) {
      wasCalled = true;
      this.value = value;
    }
  }

  public static class IntCallback {
    public boolean wasCalled;
    public int value;

    public void callback(int value) {
      wasCalled = true;
      this.value = value;
    }
  }

  public static class LongCallback {
    public boolean wasCalled;
    public long value;

    public void callback(long value) {
      wasCalled = true;
      this.value = value;
    }
  }

  public static class StringCallback {
    public boolean wasCalled;
    public String value;

    public void callback(String value) {
      wasCalled = true;
      this.value = value;
    }
  }

  public static class ObjectCallback {
    public boolean wasCalled;
    public Object value;

    public void callback(Object value) {
      wasCalled = true;
      this.value = value;
    }
  }

  public static class BooleanArrayCallback {
    public boolean wasCalled;
    public Boolean[] value;

    public void callback(Boolean[] value) {
      wasCalled = true;
      this.value = value;
    }
  }

  public static class StringArrayCallback {
    public boolean wasCalled;
    public String[] value;

    public void callback(String[] value) {
      wasCalled = true;
      this.value = value;
    }
  }

  public static class KeyValueType {
    public final String key;
    public final String value;

    public KeyValueType(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }

  public static class KeyValueCallback {
    public boolean wasCalled;
    public KeyValueType value;

    public void callback(KeyValueType value) {
      wasCalled = true;
      this.value = value;
    }
  }

  public static class Callback1Arg {
    public boolean wasCalled;

    public String arg1;

    public void callback(String arg1) {
      wasCalled = true;

      this.arg1 = arg1;
    }
  }

  public static class Callback2Arg {
    public boolean wasCalled;

    public String arg1;
    public String arg2;

    public void callback(String arg1, String arg2) {
      wasCalled = true;

      this.arg1 = arg1;
      this.arg2 = arg2;
    }
  }

  public static class Callback3Arg {
    public boolean wasCalled;

    public String arg1;
    public String arg2;
    public String arg3;

    public void callback(String arg1, String arg2, String arg3) {
      wasCalled = true;

      this.arg1 = arg1;
      this.arg2 = arg2;
      this.arg3 = arg3;
    }
  }

  public static class Callback4Arg {
    public boolean wasCalled;

    public String arg1;
    public String arg2;
    public String arg3;
    public String arg4;

    public void callback(String arg1, String arg2, String arg3, String arg4) {
      wasCalled = true;

      this.arg1 = arg1;
      this.arg2 = arg2;
      this.arg3 = arg3;
      this.arg4 = arg4;
    }
  }

  public static class Callback5Arg {
    public boolean wasCalled;

    public String arg1;
    public String arg2;
    public String arg3;
    public String arg4;
    public String arg5;

    public void callback(String arg1, String arg2, String arg3, String arg4, String arg5) {
      wasCalled = true;

      this.arg1 = arg1;
      this.arg2 = arg2;
      this.arg3 = arg3;
      this.arg4 = arg4;
      this.arg5 = arg5;
    }
  }

  public static class Callback6Arg {
    public boolean wasCalled;

    public String arg1;
    public String arg2;
    public String arg3;
    public String arg4;
    public String arg5;
    public String arg6;

    public void callback(
        String arg1, String arg2, String arg3, String arg4, String arg5, String arg6) {
      wasCalled = true;

      this.arg1 = arg1;
      this.arg2 = arg2;
      this.arg3 = arg3;
      this.arg4 = arg4;
      this.arg5 = arg5;
      this.arg6 = arg6;
    }
  }

  public static class Callback7Arg {
    public boolean wasCalled;

    public String arg1;
    public String arg2;
    public String arg3;
    public String arg4;
    public String arg5;
    public String arg6;
    public String arg7;

    public void callback(
        String arg1, String arg2, String arg3, String arg4, String arg5, String arg6, String arg7) {
      wasCalled = true;

      this.arg1 = arg1;
      this.arg2 = arg2;
      this.arg3 = arg3;
      this.arg4 = arg4;
      this.arg5 = arg5;
      this.arg6 = arg6;
      this.arg7 = arg7;
    }
  }

  public static class Callback8Arg {
    public boolean wasCalled;

    public String arg1;
    public String arg2;
    public String arg3;
    public String arg4;
    public String arg5;
    public String arg6;
    public String arg7;
    public String arg8;

    public void callback(
        String arg1,
        String arg2,
        String arg3,
        String arg4,
        String arg5,
        String arg6,
        String arg7,
        String arg8) {
      wasCalled = true;

      this.arg1 = arg1;
      this.arg2 = arg2;
      this.arg3 = arg3;
      this.arg4 = arg4;
      this.arg5 = arg5;
      this.arg6 = arg6;
      this.arg7 = arg7;
      this.arg8 = arg8;
    }
  }

  public static class Callback9Arg {
    public boolean wasCalled;

    public String arg1;
    public String arg2;
    public String arg3;
    public String arg4;
    public String arg5;
    public String arg6;
    public String arg7;
    public String arg8;
    public String arg9;

    public void callback(
        String arg1,
        String arg2,
        String arg3,
        String arg4,
        String arg5,
        String arg6,
        String arg7,
        String arg8,
        String arg9) {
      wasCalled = true;

      this.arg1 = arg1;
      this.arg2 = arg2;
      this.arg3 = arg3;
      this.arg4 = arg4;
      this.arg5 = arg5;
      this.arg6 = arg6;
      this.arg7 = arg7;
      this.arg8 = arg8;
      this.arg9 = arg9;
    }
  }

  public static class Callback10Arg {
    public boolean wasCalled;

    public String arg1;
    public String arg2;
    public String arg3;
    public String arg4;
    public String arg5;
    public String arg6;
    public String arg7;
    public String arg8;
    public String arg9;
    public String arg10;

    public void callback(
        String arg1,
        String arg2,
        String arg3,
        String arg4,
        String arg5,
        String arg6,
        String arg7,
        String arg8,
        String arg9,
        String arg10) {
      wasCalled = true;

      this.arg1 = arg1;
      this.arg2 = arg2;
      this.arg3 = arg3;
      this.arg4 = arg4;
      this.arg5 = arg5;
      this.arg6 = arg6;
      this.arg7 = arg7;
      this.arg8 = arg8;
      this.arg9 = arg9;
      this.arg10 = arg10;
    }
  }

  public static class Callback11Arg {
    public boolean wasCalled;

    public String arg1;
    public String arg2;
    public String arg3;
    public String arg4;
    public String arg5;
    public String arg6;
    public String arg7;
    public String arg8;
    public String arg9;
    public String arg10;
    public String arg11;

    public void callback(
        String arg1,
        String arg2,
        String arg3,
        String arg4,
        String arg5,
        String arg6,
        String arg7,
        String arg8,
        String arg9,
        String arg10,
        String arg11) {
      wasCalled = true;

      this.arg1 = arg1;
      this.arg2 = arg2;
      this.arg3 = arg3;
      this.arg4 = arg4;
      this.arg5 = arg5;
      this.arg6 = arg6;
      this.arg7 = arg7;
      this.arg8 = arg8;
      this.arg9 = arg9;
      this.arg10 = arg10;
      this.arg11 = arg11;
    }
  }

  public static class Callback12Arg {
    public boolean wasCalled;

    public String arg1;
    public String arg2;
    public String arg3;
    public String arg4;
    public String arg5;
    public String arg6;
    public String arg7;
    public String arg8;
    public String arg9;
    public String arg10;
    public String arg11;
    public String arg12;

    public void callback(
        String arg1,
        String arg2,
        String arg3,
        String arg4,
        String arg5,
        String arg6,
        String arg7,
        String arg8,
        String arg9,
        String arg10,
        String arg11,
        String arg12) {
      wasCalled = true;

      this.arg1 = arg1;
      this.arg2 = arg2;
      this.arg3 = arg3;
      this.arg4 = arg4;
      this.arg5 = arg5;
      this.arg6 = arg6;
      this.arg7 = arg7;
      this.arg8 = arg8;
      this.arg9 = arg9;
      this.arg10 = arg10;
      this.arg11 = arg11;
      this.arg12 = arg12;
    }
  }

  public static class Callback13Arg {
    public boolean wasCalled;

    public String arg1;
    public String arg2;
    public String arg3;
    public String arg4;
    public String arg5;
    public String arg6;
    public String arg7;
    public String arg8;
    public String arg9;
    public String arg10;
    public String arg11;
    public String arg12;
    public String arg13;

    public void callback(
        String arg1,
        String arg2,
        String arg3,
        String arg4,
        String arg5,
        String arg6,
        String arg7,
        String arg8,
        String arg9,
        String arg10,
        String arg11,
        String arg12,
        String arg13) {
      wasCalled = true;

      this.arg1 = arg1;
      this.arg2 = arg2;
      this.arg3 = arg3;
      this.arg4 = arg4;
      this.arg5 = arg5;
      this.arg6 = arg6;
      this.arg7 = arg7;
      this.arg8 = arg8;
      this.arg9 = arg9;
      this.arg10 = arg10;
      this.arg11 = arg11;
      this.arg12 = arg12;
      this.arg13 = arg13;
    }
  }

  public static class Callback14Arg {
    public boolean wasCalled;

    public String arg1;
    public String arg2;
    public String arg3;
    public String arg4;
    public String arg5;
    public String arg6;
    public String arg7;
    public String arg8;
    public String arg9;
    public String arg10;
    public String arg11;
    public String arg12;
    public String arg13;
    public String arg14;

    public void callback(
        String arg1,
        String arg2,
        String arg3,
        String arg4,
        String arg5,
        String arg6,
        String arg7,
        String arg8,
        String arg9,
        String arg10,
        String arg11,
        String arg12,
        String arg13,
        String arg14) {
      wasCalled = true;

      this.arg1 = arg1;
      this.arg2 = arg2;
      this.arg3 = arg3;
      this.arg4 = arg4;
      this.arg5 = arg5;
      this.arg6 = arg6;
      this.arg7 = arg7;
      this.arg8 = arg8;
      this.arg9 = arg9;
      this.arg10 = arg10;
      this.arg11 = arg11;
      this.arg12 = arg12;
      this.arg13 = arg13;
      this.arg14 = arg14;
    }
  }

  public static class Callback15Arg {
    public boolean wasCalled;

    public String arg1;
    public String arg2;
    public String arg3;
    public String arg4;
    public String arg5;
    public String arg6;
    public String arg7;
    public String arg8;
    public String arg9;
    public String arg10;
    public String arg11;
    public String arg12;
    public String arg13;
    public String arg14;
    public String arg15;

    public void callback(
        String arg1,
        String arg2,
        String arg3,
        String arg4,
        String arg5,
        String arg6,
        String arg7,
        String arg8,
        String arg9,
        String arg10,
        String arg11,
        String arg12,
        String arg13,
        String arg14,
        String arg15) {
      wasCalled = true;

      this.arg1 = arg1;
      this.arg2 = arg2;
      this.arg3 = arg3;
      this.arg4 = arg4;
      this.arg5 = arg5;
      this.arg6 = arg6;
      this.arg7 = arg7;
      this.arg8 = arg8;
      this.arg9 = arg9;
      this.arg10 = arg10;
      this.arg11 = arg11;
      this.arg12 = arg12;
      this.arg13 = arg13;
      this.arg14 = arg14;
      this.arg15 = arg15;
    }
  }

  public static class Callback16Arg {
    public boolean wasCalled;

    public String arg1;
    public String arg2;
    public String arg3;
    public String arg4;
    public String arg5;
    public String arg6;
    public String arg7;
    public String arg8;
    public String arg9;
    public String arg10;
    public String arg11;
    public String arg12;
    public String arg13;
    public String arg14;
    public String arg15;
    public String arg16;

    public void callback(
        String arg1,
        String arg2,
        String arg3,
        String arg4,
        String arg5,
        String arg6,
        String arg7,
        String arg8,
        String arg9,
        String arg10,
        String arg11,
        String arg12,
        String arg13,
        String arg14,
        String arg15,
        String arg16) {
      wasCalled = true;

      this.arg1 = arg1;
      this.arg2 = arg2;
      this.arg3 = arg3;
      this.arg4 = arg4;
      this.arg5 = arg5;
      this.arg6 = arg6;
      this.arg7 = arg7;
      this.arg8 = arg8;
      this.arg9 = arg9;
      this.arg10 = arg10;
      this.arg11 = arg11;
      this.arg12 = arg12;
      this.arg13 = arg13;
      this.arg14 = arg14;
      this.arg15 = arg15;
      this.arg16 = arg16;
    }
  }
}
