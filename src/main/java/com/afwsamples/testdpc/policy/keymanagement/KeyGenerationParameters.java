package com.afwsamples.testdpc.policy.keymanagement;

public class KeyGenerationParameters {
  public final String alias;
  public final boolean isUserSelectable;
  public final byte[] attestationChallenge;
  public final int idAttestationFlags;
  public final boolean useStrongBox;
  public final boolean generateEcKey;

  public KeyGenerationParameters(
      String alias,
      boolean isUserSelectable,
      byte[] attestationChallenge,
      int idAttestationFlags,
      boolean useStrongBox,
      boolean generateEcKey) {
    this.alias = alias;
    this.isUserSelectable = isUserSelectable;
    this.attestationChallenge = attestationChallenge;
    this.idAttestationFlags = idAttestationFlags;
    this.useStrongBox = useStrongBox;
    this.generateEcKey = generateEcKey;
  }

  public static class Builder {
    private String mAlias;
    private boolean mIsUserSelectable;
    private byte[] mAttestationChallenge;
    private int mIdAttestationFlags;
    private boolean mUseStrongBox;
    private boolean mGenerateEcKey;

    public Builder setAlias(String alias) {
      mAlias = alias;
      return this;
    }

    public Builder setIsUserSelectable(boolean isUserSelectable) {
      mIsUserSelectable = isUserSelectable;
      return this;
    }

    public Builder setAttestationChallenge(byte[] attestationChallenge) {
      mAttestationChallenge = attestationChallenge;
      return this;
    }

    public Builder setIdAttestationFlags(int idAttestationFlags) {
      mIdAttestationFlags = idAttestationFlags;
      return this;
    }

    public Builder setUseStrongBox(boolean useStrongBox) {
      mUseStrongBox = useStrongBox;
      return this;
    }

    public Builder setGenerateEcKey(boolean generateEcKey) {
      mGenerateEcKey = generateEcKey;
      return this;
    }

    public KeyGenerationParameters build() {
      return new KeyGenerationParameters(
          mAlias,
          mIsUserSelectable,
          mAttestationChallenge,
          mIdAttestationFlags,
          mUseStrongBox,
          mGenerateEcKey);
    }
  }
}
