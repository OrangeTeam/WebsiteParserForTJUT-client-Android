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
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

/**
 * modified from Support4Demo's LoaderCursorSupport
 * @modifiedBy Bai Jie
 */
public class ListPostsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String SOURCE = Contract.Posts.COLUMN_NAME_SOURCE;

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
        MenuItem item = menu.add("Search");
        item.setIcon(android.R.drawable.ic_menu_search);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_ALWAYS
                | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        View searchView = SearchViewCompat.newSearchView(getActivity());
        if (searchView != null) {
            SearchViewCompat.setOnQueryTextListener(searchView,
                    new OnQueryTextListenerCompat() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    // Called when the action bar search text has changed.  Update
                    // the search filter, and restart the loader to do a new query
                    // with this filter.
                    if(TextUtils.isEmpty(newText))
                        newText = null;
                    // Don't do anything if the filter hasn't actually changed.
                    // Prevents restarting the loader when restoring state.
                    if (mFilter == null && newText == null) {
                        return true;
                    }
                    if (mFilter != null && mFilter.equals(newText)) {
                        return true;
                    }
                    mFilter = newText;
                    getLoaderManager().restartLoader(0, null, ListPostsFragment.this);
                    return true;
                }
            });
            MenuItemCompat.setActionView(item, searchView);
        }
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
