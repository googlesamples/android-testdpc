package com.afwsamples.testdpc.search;

import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.BaseSearchablePolicyPreferenceFragment;
import com.afwsamples.testdpc.comp.BindDeviceAdminFragment;
import com.afwsamples.testdpc.policy.PolicyManagementFragment;
import com.afwsamples.testdpc.policy.keyguard.LockScreenPolicyFragment;
import com.afwsamples.testdpc.policy.keyguard.PasswordConstraintsFragment;
import com.afwsamples.testdpc.profilepolicy.ProfilePolicyManagementFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Stores all the indexable fragments.
 * </p>
 * <p>
 * To index a newly added fragment, there are only two things needed to be done.
 * Make you fragment extends {@link BaseSearchablePolicyPreferenceFragment}
 * and add it to this class.
 * </p>
 */
public class IndexableFragments {
    private static final List<IndexableFragment> sIndexableFragments = new ArrayList<>();

    static {
        sIndexableFragments.add(new IndexableFragment(PolicyManagementFragment.class,
                R.xml.device_policy_header));
        sIndexableFragments.add(new IndexableFragment(ProfilePolicyManagementFragment.class,
                R.xml.profile_policy_header));
        sIndexableFragments.add(new IndexableFragment(LockScreenPolicyFragment.class,
                R.xml.lock_screen_preferences));
        sIndexableFragments.add(new IndexableFragment(PasswordConstraintsFragment.class,
                R.xml.password_constraint_preferences));
        sIndexableFragments.add(new IndexableFragment(BindDeviceAdminFragment.class,
                R.xml.bind_device_admin_policies));
    }

    public static List<IndexableFragment> values() {
        return new ArrayList<>(sIndexableFragments);
    }
}
