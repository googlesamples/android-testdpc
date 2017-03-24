package com.afwsamples.testdpc.comp;

import android.os.RemoteException;
import android.support.annotation.UiThread;

public interface OnServiceConnectedListener<T> {
    @UiThread
    void onServiceConnected(T service) throws RemoteException;
}
