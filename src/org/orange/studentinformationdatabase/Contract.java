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


import android.net.Uri;
import android.provider.BaseColumns;

public class Contract {
    public static final String AUTHORITY = "org.orange.querysystem.provider";

    // This class cannot be instantiated
    private Contract() {}

    public static final class Posts implements BaseColumns {
        // This class cannot be instantiated
        private Posts() {}

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "post";

        /*
         * URI definitions
         */

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        /**
         * Path parts for the URIs
         */

        /**
         * Path part for the Notes URI
         */
        private static final String PATH_POSTS = "/posts";

        /**
         * Path part for the Note ID URI
         */
        private static final String PATH_POST_ID = "/posts/";

        /**
         * 0-relative position of a post ID segment in the path part of a note ID URI
         */
        public static final int POST_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_POSTS);

        /**
         * The content URI base for a single post. Callers must
         * append a numeric post id to this Uri to retrieve a post
         */
        public static final Uri CONTENT_ID_URI_BASE
            = Uri.parse(SCHEME + AUTHORITY + PATH_POST_ID);

        /**
         * The content URI match pattern for a single post, specified by its ID. Use this to match
         * incoming URIs or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN
            = Uri.parse(SCHEME + AUTHORITY + PATH_POST_ID + "/#");

        /*
         * MIME type definitions
         */

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of posts.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.orange.post";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single post.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.orange.post";

        /*
         * Column definitions
         */

        /**
         * Column name for the title of the post
         * <P>Type: ?</P>
         */
        public static final String COLUMN_NAME_TITLE	= "title";
        /**
         * Column name of the post content
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_NAME_MAINBODY	= "mainbody";
        /**
         * Column name of the post source from
         * <P>Type: INTEGER</P>
         */
        public static final String COLUMN_NAME_SOURCE	= "source";
        /**
         * Column name of the post category
         * <P>Type: ?</P>
         */
        public static final String COLUMN_NAME_CATEGORY	= "category";
        /**
         * Column name of the post URL
         * <P>Type: ?</P>
         */
        public static final String COLUMN_NAME_URL		= "url";
        /**
         * Column name of the post author
         * <P>Type: ?</P>
         */
        public static final String COLUMN_NAME_AUTHOR	= "author";
        /**
         * Column name for the date timestamp
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String COLUMN_NAME_DATE		= "date";


        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = COLUMN_NAME_DATE+" DESC";
    }
}
