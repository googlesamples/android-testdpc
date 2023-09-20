package com.afwsamples.testdpc;

import android.app.admin.PolicyUpdateReceiver;
import android.app.admin.PolicyUpdateResult;
import android.app.admin.TargetUser;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/** Receiver for system notifications about device policy changes */
public class PolicyChangeMonitor extends PolicyUpdateReceiver {
  private static final String TAG = "PolicyChangeMonitor";

  public void onPolicyChanged(
      Context context,
      String policyIdentifier,
      Bundle additionalPolicyParams,
      TargetUser targetUser,
      PolicyUpdateResult policyUpdateResult) {
    if (policyUpdateResult.getResultCode() == PolicyUpdateResult.RESULT_POLICY_SET) {
      Log.i(TAG, context.getString(R.string.policy_restored_log_text, policyIdentifier));
    } else {
      Log.i(TAG, context.getString(R.string.policy_altered_log_text, policyIdentifier));
    }
    return;
  }

  public void onPolicySetResult(
      Context context,
      String policyIdentifier,
      Bundle additionalPolicyParams,
      TargetUser targetUser,
      PolicyUpdateResult policyUpdateResult) {
    if (policyUpdateResult.getResultCode() == PolicyUpdateResult.RESULT_POLICY_SET) {
      Log.i(TAG, context.getString(R.string.policy_set_log_text, policyIdentifier));
    } else if (policyUpdateResult.getResultCode() == PolicyUpdateResult.RESULT_POLICY_CLEARED) {
      Log.i(TAG, context.getString(R.string.policy_cleared_log_text, policyIdentifier));
    } else {
      Log.i(TAG, context.getString(R.string.policy_not_set_log_text, policyIdentifier));
    }
    return;
  }
}
