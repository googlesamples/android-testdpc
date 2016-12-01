package com.afwsamples.testdpc.comp;

interface IDeviceOwnerService {
    /**
     * Notify device owner that work profile is unlocked.
     */
    oneway void notifyUserIsUnlocked();
}
