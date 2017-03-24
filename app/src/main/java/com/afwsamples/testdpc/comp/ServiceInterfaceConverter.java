package com.afwsamples.testdpc.comp;

import android.os.IBinder;

public interface ServiceInterfaceConverter<T> {
    T convert(IBinder iBinder);
}
