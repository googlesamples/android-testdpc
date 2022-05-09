/*
 * Copyright (C) 2020 The Android Open Source Project
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
 * limitations under the License.
 */
package com.afwsamples.testdpc.common;

import android.app.Activity;
import android.app.Fragment;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

/** Base class for all activities that implements {@code dump()}. */
public abstract class DumpableActivity extends Activity {

  @Override
  public void dump(String prefix, FileDescriptor fd, PrintWriter pw, String[] args) {
    boolean quietMode = Dumpable.isQuietMode(args);
    if (quietMode) {
      List<Fragment> fragments = getFragmentManager().getFragments();
      pw.println("*** Dumping Dumpable fragments only ***");
      String prefix2 = prefix + prefix;
      for (Fragment fragment : fragments) {
        if (fragment instanceof Dumpable) {
          pw.printf("%s%s:\n", prefix, fragment);
          ((Dumpable) fragment).dump(prefix2, pw, fd, quietMode, args);
        }
      }
      return;
    }

    super.dump(prefix, fd, pw, args);
  }
}
