/*
 * Copyright (C) 2007 The Android Open Source Project
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
package org.orange.studentinformationdatabase;


import org.orange.studentinformationdatabase.StudentInfDBAdapter.StudentInfDBOpenHelper;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;

/**
 * modified from sample program NotePad's NotePadProvider
 *
 * @modifiedBy Bai Jie
 */
public class QuerySystemProvider extends ContentProvider {

    // Handle to a new DatabaseHelper.
    private StudentInfDBOpenHelper mOpenHelper;

    /**
     * A projection map used to select columns from the database
     */
    private static HashMap<String, String> sPostsProjectionMap;

    /*
     * Constants used by the Uri matcher to choose an action based on the pattern
     * of the incoming URI
     */
    // The incoming URI matches the Posts URI pattern
    private static final int POSTS = 1;

    // The incoming URI matches the Post ID URI pattern
    private static final int POST_ID = 2;

    private static final UriMatcher sUriMatcher;

    /**
     * A block that instantiates and sets static objects
     */
    static {

        /*
         * Creates and initializes the URI matcher
         */
        // Create a new instance
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Add a pattern that routes URIs terminated with "posts" to a POSTS operation
        sUriMatcher.addURI(Contract.AUTHORITY, "posts", POSTS);

        // Add a pattern that routes URIs terminated with "posts" plus an integer
        // to a post ID operation
        sUriMatcher.addURI(Contract.AUTHORITY, "posts/#", POST_ID);

        /*
         * Creates and initializes a projection map that returns all columns
         */

        // Creates a new projection map instance. The map returns a column name
        // given a string. The two are usually equal.
        sPostsProjectionMap = new HashMap<String, String>();

        // Maps the string "_ID" to the column name "_ID"
        sPostsProjectionMap.put(Contract.Posts._ID, Contract.Posts._ID);
        // Maps "title" to "title"
        sPostsProjectionMap.put(Contract.Posts.COLUMN_NAME_TITLE, Contract.Posts.COLUMN_NAME_TITLE);
        sPostsProjectionMap
                .put(Contract.Posts.COLUMN_NAME_MAINBODY, Contract.Posts.COLUMN_NAME_MAINBODY);
        sPostsProjectionMap
                .put(Contract.Posts.COLUMN_NAME_SOURCE, Contract.Posts.COLUMN_NAME_SOURCE);
        sPostsProjectionMap
                .put(Contract.Posts.COLUMN_NAME_CATEGORY, Contract.Posts.COLUMN_NAME_CATEGORY);
        sPostsProjectionMap.put(Contract.Posts.COLUMN_NAME_URL, Contract.Posts.COLUMN_NAME_URL);
        sPostsProjectionMap
                .put(Contract.Posts.COLUMN_NAME_AUTHOR, Contract.Posts.COLUMN_NAME_AUTHOR);
        sPostsProjectionMap.put(Contract.Posts.COLUMN_NAME_DATE, Contract.Posts.COLUMN_NAME_DATE);

    }

    /* (non-Javadoc)
     * @see android.content.ContentProvider#onCreate()
     */
    @Override
    public boolean onCreate() {
        // Creates a new helper object. Note that the database itself isn't opened until
        // something tries to access it, and it's only created if it doesn't already exist.
        mOpenHelper = new StudentInfDBOpenHelper(getContext());

        // Assumes that any failures will be reported by a thrown exception.
        return true;
    }

    /* (non-Javadoc)
     * @see android.content.ContentProvider#getType(android.net.Uri)
     */
    @Override
    public String getType(Uri uri) {
        /**
         * Chooses the MIME type based on the incoming URI pattern
         */
        switch (sUriMatcher.match(uri)) {

            // If the pattern is for posts, returns the general content type.
            case POSTS:
                return Contract.Posts.CONTENT_TYPE;

            // If the pattern is for post IDs, returns the post ID content type.
            case POST_ID:
                return Contract.Posts.CONTENT_ITEM_TYPE;

            // If the URI pattern doesn't match any permitted patterns, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    /* (non-Javadoc)
     * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        // Constructs a new query builder and sets its table name
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        /**
         * Choose the projection and adjust the "where" clause based on URI pattern-matching.
         */
        switch (sUriMatcher.match(uri)) {
            // If the incoming URI is for posts, chooses the Posts projection
            case POSTS:
                qb.setTables(Contract.Posts.TABLE_NAME);
                qb.setProjectionMap(sPostsProjectionMap);
                break;

               /* If the incoming URI is for a single post identified by its ID, chooses the
                * post ID projection, and appends "_ID = <postID>" to the where clause, so that
                * it selects that single post
                */
            case POST_ID:
                qb.setTables(Contract.Posts.TABLE_NAME);
                qb.setProjectionMap(sPostsProjectionMap);
                qb.appendWhere(
                        Contract.Posts._ID +    // the name of the ID column
                                "=" +
                                // the position of the post ID itself in the incoming URI
                                uri.getPathSegments().get(Contract.Posts.POST_ID_PATH_POSITION)
                );
                break;

            default:
                // If the URI doesn't match any of the known patterns, throw an exception.
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        String orderBy;
        // If no sort order is specified, uses the default
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = Contract.Posts.DEFAULT_SORT_ORDER;
        } else {
            // otherwise, uses the incoming sort order
            orderBy = sortOrder;
        }

        // Opens the database object in "read" mode, since no writes need to be done.
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

           /*
            * Performs the query. If no problems occur trying to read the database, then a Cursor
            * object is returned; otherwise, the cursor variable contains null. If no records were
            * selected, then the Cursor object is empty, and Cursor.getCount() returns 0.
            */
        Cursor c = qb.query(
                db,            // The database to query
                projection,    // The columns to return from the query
                selection,     // The columns for the where clause
                selectionArgs, // The values for the where clause
                null,          // don't group the rows
                null,          // don't filter by row groups
                orderBy        // The sort order
        );

        // Tells the Cursor what URI to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    /* (non-Javadoc)
     * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
     */
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validates the incoming URI. Only the full provider URI is allowed for inserts.
        if (sUriMatcher.match(uri) != POSTS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // A map to hold the new record's values.
        ContentValues values;

        // If the incoming values map is not null, uses it for the new values.
        if (initialValues != null) {
            values = new ContentValues(initialValues);

        } else {
            // Otherwise, create a new value map
            values = new ContentValues();
        }

        // If the values map doesn't contain a title, sets the value to the default title.
        if (values.containsKey(Contract.Posts.COLUMN_NAME_TITLE) == false) {
            values.put(Contract.Posts.COLUMN_NAME_TITLE,
                    Resources.getSystem().getString(android.R.string.untitled));
        }

        // Opens the database object in "write" mode.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Performs the insert and returns the ID of the new post.
        long rowId = db.insert(
                Contract.Posts.TABLE_NAME,           // The table to insert into.
                Contract.Posts.COLUMN_NAME_MAINBODY,
                // A hack, SQLite sets this column value to null
                // if values is empty.
                values
                // A map of column names, and the values to insert
                // into the columns.
        );

        // If the insert succeeded, the row ID exists.
        if (rowId > 0) {
            // Creates a URI with the post ID pattern and the new row ID appended to it.
            Uri postUri = ContentUris.withAppendedId(Contract.Posts.CONTENT_ID_URI_BASE, rowId);

            // Notifies observers registered against this provider that the data changed.
            getContext().getContentResolver().notifyChange(postUri, null);
            return postUri;
        }

        // If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
        throw new SQLException("Failed to insert row into " + uri);
    }

    /* (non-Javadoc)
     * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // Opens the database object in "write" mode.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        String finalWhere;

        // Does the update based on the incoming URI pattern
        switch (sUriMatcher.match(uri)) {

            // If the incoming URI matches the general posts pattern, does the update based on
            // the incoming data.
            case POSTS:

                // Does the update and returns the number of rows updated.
                count = db.update(
                        Contract.Posts.TABLE_NAME,// The database table name.
                        values,                   // A map of column names and new values to use.
                        selection,                // The where clause column names.
                        selectionArgs             // The where clause column values to select on.
                );
                break;

            // If the incoming URI matches a single post ID, does the update based on the incoming
            // data, but modifies the where clause to restrict it to the particular post ID.
            case POST_ID:

                /*
                 * Starts creating the final WHERE clause by restricting it to the incoming
                 * post ID.
                 */
                finalWhere =
                        Contract.Posts._ID +                             // The ID column name
                                " = " +
                                // test for equality
                                uri.getPathSegments()
                                        .                           // the incoming post ID
                                                get(Contract.Posts.POST_ID_PATH_POSITION)
                ;

                // If there were additional selection criteria, append them to the final WHERE
                // clause
                if (selection != null) {
                    finalWhere = finalWhere + " AND " + selection;
                }

                // Does the update and returns the number of rows updated.
                count = db.update(
                        Contract.Posts.TABLE_NAME,// The database table name.
                        values,                   // A map of column names and new values to use.
                        finalWhere,               // The final WHERE clause to use
                        // placeholders for whereArgs
                        selectionArgs             // The where clause column values to select on, or
                        // null if the values are in the where argument.
                );
                break;
            // If the incoming pattern is invalid, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        /*Gets a handle to the content resolver object for the current context, and notifies it
         * that the incoming URI changed. The object passes this along to the resolver framework,
         * and observers that have registered themselves for the provider are notified.
         */
        getContext().getContentResolver().notifyChange(uri, null);

        // Returns the number of rows updated.
        return count;
    }

    /* (non-Javadoc)
     * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Opens the database object in "write" mode.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String finalWhere;

        int count;

        // Does the delete based on the incoming URI pattern.
        switch (sUriMatcher.match(uri)) {

            // If the incoming pattern matches the general pattern for posts, does a delete
            // based on the incoming "where" columns and arguments.
            case POSTS:
                count = db.delete(
                        Contract.Posts.TABLE_NAME, // The database table name
                        selection,                     // The incoming where clause column names
                        selectionArgs                  // The incoming where clause values
                );
                break;

            // If the incoming URI matches a single post ID, does the delete based on the
            // incoming data, but modifies the where clause to restrict it to the
            // particular post ID.
            case POST_ID:
                /*
                 * Starts a final WHERE clause by restricting it to the
                 * desired post ID.
                 */
                finalWhere =
                        Contract.Posts._ID +                             // The ID column name
                                " = " +
                                // test for equality
                                uri.getPathSegments()
                                        .                           // the incoming post ID
                                                get(Contract.Posts.POST_ID_PATH_POSITION)
                ;

                // If there were additional selection criteria, append them to the final
                // WHERE clause
                if (selection != null) {
                    finalWhere = finalWhere + " AND " + selection;
                }

                // Performs the delete.
                count = db.delete(
                        Contract.Posts.TABLE_NAME,  // The database table name.
                        finalWhere,                // The final WHERE clause
                        selectionArgs                  // The incoming where clause values.
                );
                break;

            // If the incoming pattern is invalid, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        /*Gets a handle to the content resolver object for the current context, and notifies it
         * that the incoming URI changed. The object passes this along to the resolver framework,
         * and observers that have registered themselves for the provider are notified.
         */
        getContext().getContentResolver().notifyChange(uri, null);

        // Returns the number of rows deleted.
        return count;
    }

}
