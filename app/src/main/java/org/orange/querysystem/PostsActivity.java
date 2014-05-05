/*
 * Copyright (C) 2011 The Android Open Source Project
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
package org.orange.querysystem;

import org.orange.querysystem.content.ListPostsFragment;
import org.orange.querysystem.content.TabsAdapter;
import org.orange.querysystem.util.PostUpdater;
import org.orange.querysystem.util.PostUpdater.OnPostUpdateListener;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import util.webpage.Post;


/**
 * @author Bai Jie
 */
public class PostsActivity extends FragmentActivity {

    private TextView currentTime;

    TabHost mTabHost;

    ViewPager mViewPager;

    TabsAdapter mTabsAdapter;

    PostUpdater mWebUpdaterToDB;

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @TargetApi(11)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_tabs_pager);
        currentTime = (TextView) findViewById(R.id.currentTime);
        currentTime.setText(R.string.post);

        mWebUpdaterToDB = new PostUpdater(this);
        mWebUpdaterToDB.setOnPostExecuteListener(new OnPostUpdateListener() {
            @Override
            public void onPostUpdate(long numberOfInsertedPosts, boolean mandatorily) {
                if (mandatorily || numberOfInsertedPosts > 0) {
                    if (numberOfInsertedPosts > 0) {
                        loadPosts();
                        String message = PostsActivity.this.getResources()
                                .getString(R.string.has_updated_posts, numberOfInsertedPosts);
                        Toast.makeText(PostsActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else if (numberOfInsertedPosts == 0) {
                        Toast.makeText(PostsActivity.this, R.string.no_new_post, Toast.LENGTH_SHORT)
                                .show();
                    } else        //numberOfInsertedPosts < 0
                    {
                        Toast.makeText(PostsActivity.this, R.string.fail_to_update_post,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        if (mWebUpdaterToDB.updatePostsAutomatically()) {
            Toast.makeText(this, R.string.start_to_update_post_automatically, Toast.LENGTH_SHORT)
                    .show();
        }

        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();

        mViewPager = (ViewPager) findViewById(R.id.pager);

        mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);

        //3.0以上版本，使用ActionBar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar mActionBar = getActionBar();
            mActionBar.setTitle(R.string.post);
            currentTime.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
            //横屏时，为节省空间隐藏ActionBar
            if (getResources().getConfiguration().orientation ==
                    android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                mActionBar.hide();
            }
        }
        loadPosts();
    }

    @Override
    protected void onStop() {
        mWebUpdaterToDB.stop();
        super.onStop();
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
    }

    /**
     * 以资源标识ID形式返回对应的通知源
     *
     * @param source 要查找的来源。如：Post.SOURCES.WEBSITE_OF_TEACHING_AFFAIRS
     * @return 资源标识ID。如：R.string.teaching_affairs
     * @see Post.SOURCES
     */
    public static int getSourceString(byte source) {
        switch (source) {
            case Post.SOURCES.WEBSITE_OF_TEACHING_AFFAIRS:
                return R.string.teaching_affairs;
            case Post.SOURCES.WEBSITE_OF_SCCE:
                return R.string.school_of_computer_and_communication_engineering;
            case Post.SOURCES.STUDENT_WEBSITE_OF_SCCE:
                return R.string.student_website_of_SCCE;
            case Post.SOURCES.UNKNOWN_SOURCE:
                return R.string.unknown_source;
            default:
                throw new IllegalArgumentException("Unknown post source: " + source);
        }
    }

    public void addTab(byte source) {
        String sourceString = getResources().getText(getSourceString(source)).toString();
        mTabsAdapter.addTab(mTabHost.newTabSpec(sourceString).setIndicator(sourceString),
                ListPostsFragment.class, ListPostsFragment.buildArgument(source));
    }

    public void loadPosts() {
        mTabsAdapter.clear();
        addTab(Post.SOURCES.WEBSITE_OF_TEACHING_AFFAIRS);
        addTab(Post.SOURCES.STUDENT_WEBSITE_OF_SCCE);
        addTab(Post.SOURCES.WEBSITE_OF_SCCE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.refresh);
        menu.add(0, 2, 2, R.string.settings);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == 1) {
            if (mWebUpdaterToDB.updatePosts(true)) {
                Toast.makeText(this, R.string.updating, Toast.LENGTH_SHORT).show();
            }
        } else if (item.getItemId() == 2) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        return super.onMenuItemSelected(featureId, item);
    }
}
