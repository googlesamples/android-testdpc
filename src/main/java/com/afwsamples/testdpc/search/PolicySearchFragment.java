package com.afwsamples.testdpc.search;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.BaseSearchablePolicyPreferenceFragment;
import java.util.ArrayList;
import java.util.List;

/** Fragment that processes the search query and shows the result. */
public class PolicySearchFragment extends Fragment
    implements SearchItemAdapter.OnItemClickListener {
  private static final String TAG = "PolicySearchFragment";
  private static final int MIN_LENGTH_TO_SEARCH = 3;

  private SearchView mSearchView;
  private PreferenceIndexSqliteOpenHelper mSqliteOpenHelper;
  private SearchItemAdapter mAdapter;
  private List<String> mAvailableFragments;

  public static PolicySearchFragment newInstance() {
    return new PolicySearchFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    mSqliteOpenHelper = PreferenceIndexSqliteOpenHelper.getInstance(getActivity());
    mAdapter = new SearchItemAdapter(this);
    mAvailableFragments = getAvailableFragments();
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    RecyclerView recyclerView =
        (RecyclerView) inflater.inflate(R.layout.search_result, container, false);
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    recyclerView.setAdapter(mAdapter);
    return recyclerView;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    MenuItem showSearchMenu = menu.findItem(R.id.action_show_search);
    if (showSearchMenu != null) {
      showSearchMenu.setVisible(false);
    }
    inflater.inflate(R.menu.policy_search_menu, menu);
    MenuItem searchItem = menu.findItem(R.id.action_search);
    searchItem.expandActionView();
    mSearchView = (SearchView) searchItem.getActionView();
    mSearchView.setOnQueryTextListener(
        new SearchView.OnQueryTextListener() {
          @Override
          public boolean onQueryTextSubmit(String s) {
            doSearchAsync(s);
            return true;
          }

          @Override
          public boolean onQueryTextChange(String s) {
            if (s != null && s.length() >= MIN_LENGTH_TO_SEARCH) {
              doSearchAsync(s);
              return true;
            }
            return false;
          }
        });
    searchItem.setOnActionExpandListener(
        new MenuItem.OnActionExpandListener() {
          @Override
          public boolean onMenuItemActionExpand(MenuItem menuItem) {
            return false;
          }

          @Override
          public boolean onMenuItemActionCollapse(MenuItem menuItem) {
            getFragmentManager().popBackStack();
            return true;
          }
        });
  }

  private void doSearchAsync(final String query) {
    new AsyncTask<Void, Void, List<PreferenceIndex>>() {
      @Override
      protected List<PreferenceIndex> doInBackground(Void... voids) {
        return mSqliteOpenHelper.lookup(query, mAvailableFragments);
      }

      @Override
      protected void onPostExecute(List<PreferenceIndex> result) {
        mAdapter.setSearchResult(result);
        mAdapter.notifyDataSetChanged();
      }
    }.execute();
  }

  @Override
  public void onItemClick(PreferenceIndex preferenceIndex) {
    try {
      // Show the fragment that holds the preference.
      Fragment fragment = (Fragment) Class.forName(preferenceIndex.fragmentClass).newInstance();
      Bundle arguments = new Bundle();
      arguments.putString(
          BaseSearchablePolicyPreferenceFragment.EXTRA_PREFERENCE_KEY, preferenceIndex.key);
      fragment.setArguments(arguments);
      getFragmentManager()
          .beginTransaction()
          .replace(R.id.container, fragment)
          .addToBackStack("search_" + fragment.getClass().getName())
          .commit();
    } catch (IllegalAccessException
        | ClassNotFoundException
        | java.lang.InstantiationException ex) {
      Log.e(TAG, "Fail to create the target fragment: ", ex);
    }
  }

  /** @return a list of fragments that we are going to search for. */
  private List<String> getAvailableFragments() {
    List<BaseIndexableFragment> fragments = IndexableFragments.values();
    List<String> availableFragments = new ArrayList<>();
    for (BaseIndexableFragment fragment : fragments) {
      if (fragment.isAvailable(getActivity())) {
        availableFragments.add(fragment.fragmentName);
      }
    }
    return availableFragments;
  }
}
