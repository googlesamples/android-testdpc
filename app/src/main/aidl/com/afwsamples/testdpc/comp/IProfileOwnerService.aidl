package com.afwsamples.testdpc.comp;

interface IProfileOwnerService {
    oneway void setLauncherIconHidden(boolean hidden);
    boolean isLauncherIconHidden();
}
