/*
 * Copyright (C) 2010 The Android Open Source Project
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
package org.orange.querysystem.content;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.orange.querysystem.PostDetailsActivity;
import org.orange.querysystem.R;
import org.orange.studentinformationdatabase.Contract;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * modified from Support4Demo's LoaderCursorSupport
 * @modifiedBy Bai Jie
 */
public class ListPostsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String SOURCE = Contract.Posts.COLUMN_NAME_SOURCE;

    private static final int SEARCH_EDIT_TEXT = 100;
    private static final int SEARCH = 200;
    private LinearLayout mLinearLayout;

    public static Bundle buildArgument(byte source) {
        Bundle args = new Bundle();
        args.putString(SOURCE, String.valueOf(source));
        return args;
    }
    public static ListPostsFragment newInstance(byte source) {
        ListPostsFragment frag = new ListPostsFragment();
        frag.setArguments(buildArgument(source));
        return frag;
    }

    // This is the Adapter being used to display the list's data.
    CursorAdapter mAdapter;

    // If non-null, this is the filter the user has provided.
    String mFilter;
    /** search mFilter by this clause */
    private static final String searchClause =
            Contract.Posts.COLUMN_NAME_TITLE + " LIKE ? OR " +
            Contract.Posts.COLUMN_NAME_CATEGORY +" LIKE ? OR "+
            Contract.Posts.COLUMN_NAME_AUTHOR + " LIKE ?";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View layout = super.onCreateView(inflater, container, savedInstanceState);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
            View lv = layout.findViewById(android.R.id.list);
            ViewGroup parent = (ViewGroup) lv.getParent();
            // Remove ListView and add CustomView  in its place
            int lvIndex = parent.indexOfChild(lv);
            parent.removeViewAt(lvIndex);

            mLinearLayout = new LinearLayout(getActivity());
            mLinearLayout.setOrientation(LinearLayout.VERTICAL);
            if(mFilter != null)
                insertSearchEditText().setText(mFilter);
            mLinearLayout.addView(lv);
            parent.addView(mLinearLayout, lvIndex, lv.getLayoutParams());
        }
        return layout;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLinearLayout = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.getListView().setCacheColorHint(Color.TRANSPARENT);

        // Give some text to display if there is no data.
        setEmptyText(getResources().getText(R.string.no_post));

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new PostsCursorAdapter(getActivity(), null, 0);
        setListAdapter(mAdapter);

        // Start out with a progress indicator.
        setListShown(false);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Place an action bar item for searching.
        MenuItem item = menu.add(Menu.NONE, SEARCH, Menu.NONE, R.string.search);
        item.setIcon(android.R.drawable.ic_menu_search);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_ALWAYS
                | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        View searchView = SearchViewCompat.newSearchView(getActivity());
        if (searchView != null) {
            SearchViewCompat.setOnQueryTextListener(searchView,
                    new OnQueryTextListenerCompat() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    onSearchTextChanged(newText);
                    return true;
                }
            });
            MenuItemCompat.setActionView(item, searchView);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
        case SEARCH:
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
                View searchEditText = mLinearLayout.findViewById(SEARCH_EDIT_TEXT);
                if(searchEditText == null){
                    searchEditText = insertSearchEditText();
                }
                searchEditText.requestFocus();
                return true;
            }
        default:return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 当用户输入的通知搜索关键字变化时，调用此方法，来更新筛选结果。
     * @param newText 新的搜索关键字
     */
    private void onSearchTextChanged(String newText){
        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        if(newText != null){
            newText = newText.trim();
            if(newText.length() == 0)
                newText = null;
        }
        // Don't do anything if the filter hasn't actually changed.
        // Prevents restarting the loader when restoring state.
        if (mFilter == null && newText == null)
            return;
        if (mFilter != null && mFilter.equals(newText))
            return;
        mFilter = newText;
        getLoaderManager().restartLoader(0, null, ListPostsFragment.this);
        return;
    }

    /**
     * <p><strong>Note</strong>：在{@link Build.VERSION_CODES.HONEYCOMB}以下（不包括
     * {@link Build.VERSION_CODES.HONEYCOMB HONEYCOMB}）版本使用。</p>
     * {@link Build.VERSION_CODES.HONEYCOMB HONEYCOMB}以下版本没有{@link android.widget.SearchView}，
     * 用此方法生成并装入用于代替它的{@link EditText}。
     * @return 新生成的用于代替{@link android.widget.SearchView SearchView}的{@link EditText}
     */
    private EditText insertSearchEditText(){
        final EditText mSearchEditText = new EditText(getActivity());
        mSearchEditText.setId(SEARCH_EDIT_TEXT);
        mSearchEditText.setHint(R.string.search_hint);
        mSearchEditText.setSingleLine();
        mSearchEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mSearchEditText.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                onSearchTextChanged(s.toString());
                if(s.length() == 0){
                    InputMethodManager imm = (InputMethodManager)
                            getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
                    mLinearLayout.removeView(mSearchEditText);
                }
            }
        });
        mLinearLayout.addView(mSearchEditText, 0);
        return mSearchEditText;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(getActivity(), PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.EXTRA_POST_ID, id);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.
        Uri baseUri;
//        if (mFilter != null) {
//            baseUri = Uri.withAppendedPath(People.CONTENT_FILTER_URI, Uri.encode(mFilter));
//        } else {
            baseUri = Contract.Posts.CONTENT_URI;
//        }

        String source = getArguments().getString(SOURCE);
        String selection = null;
        String[] selectionArgs = null;
        if(!TextUtils.isEmpty(source)){
            selection = Contract.Posts.COLUMN_NAME_SOURCE + "= ?";
            if(mFilter == null) {
                selectionArgs = new String[]{source};
            } else {
                selection += " AND (" + searchClause + ")";
                selectionArgs = new String[]{source, "%"+mFilter+"%", "%"+mFilter+"%", "%"+mFilter+"%"};
            }
        }else
            if(mFilter != null) {
                selection = searchClause;
                selectionArgs = new String[]{"%"+mFilter+"%", "%"+mFilter+"%", "%"+mFilter+"%"};
            }
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(getActivity(), baseUri,
                new String[] {Contract.Posts._ID,
                                Contract.Posts.COLUMN_NAME_TITLE,
                                Contract.Posts.COLUMN_NAME_CATEGORY,
                                Contract.Posts.COLUMN_NAME_DATE,
                                Contract.Posts.COLUMN_NAME_AUTHOR}
                , selection, selectionArgs,
                Contract.Posts.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);

        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

    public static class PostsCursorAdapter extends CursorAdapter {
        private final LayoutInflater mInflater;
        private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        public PostsCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String tempString;
            tempString = cursor.getString(cursor.getColumnIndex(Contract.Posts.COLUMN_NAME_TITLE));
            ((TextView) view.findViewById(R.id.post_title)).setText(tempString);
            tempString = cursor.getString(cursor.getColumnIndex(Contract.Posts.COLUMN_NAME_CATEGORY));
            ((TextView) view.findViewById(R.id.post_category)).setText(tempString);
            tempString = cursor.getString(cursor.getColumnIndex(Contract.Posts.COLUMN_NAME_AUTHOR));
            ((TextView) view.findViewById(R.id.post_author)).setText(tempString);
            long date = cursor.getLong(cursor.getColumnIndex(Contract.Posts.COLUMN_NAME_DATE));
            ((TextView) view.findViewById(R.id.post_date)).setText(mDateFormat.format(new Date(date)));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater.inflate(R.layout.fragment_list_post_row, parent, false);
        }
    }
}
