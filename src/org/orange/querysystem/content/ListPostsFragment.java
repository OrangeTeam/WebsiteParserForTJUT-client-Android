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

import org.orange.querysystem.R;
import org.orange.studentinformationdatabase.Contract;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Give some text to display if there is no data.
        setEmptyText(getResources().getText(R.string.no_post));

        // We have a menu item to show in action bar.
        //setHasOptionsMenu(true);

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
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(getActivity(), ShowOnePostActivity.class);
        intent.putExtra(ShowOnePostActivity.EXTRA_POST_ID, id);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.
        Uri baseUri;
//        if (mCurFilter != null) {
//            baseUri = Uri.withAppendedPath(People.CONTENT_FILTER_URI, Uri.encode(mCurFilter));
//        } else {
            baseUri = Contract.Posts.CONTENT_URI;
//        }

        //TODO 不会null?
        String source = getArguments().getString(SOURCE);
        String selection = null;
        String[] selectionArgs = null;
        if(!TextUtils.isEmpty(source)){
            selection = Contract.Posts.COLUMN_NAME_SOURCE + "= ?";
            selectionArgs = new String[]{source};
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
        private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");

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
