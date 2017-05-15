package com.afwsamples.testdpc.search;

import android.content.Context;

import com.afwsamples.testdpc.policy.UserRestriction;
import com.afwsamples.testdpc.policy.UserRestrictionsDisplayFragment;

import java.util.ArrayList;
import java.util.List;

public class UserRestrictionIndexableFragment extends BaseIndexableFragment {
    public UserRestrictionIndexableFragment() {
        super(UserRestrictionsDisplayFragment.class);
    }

    @Override
    public List<PreferenceIndex> index(Context context) {
        List<PreferenceIndex> preferenceIndices = new ArrayList<>();
        for (UserRestriction userRestriction : UserRestriction.ALL_USER_RESTRICTIONS) {
            preferenceIndices.add(new PreferenceIndex(
                    userRestriction.key,
                    context.getString(userRestriction.titleResId),
                    fragmentName));
        }
        return preferenceIndices;
    }
}
