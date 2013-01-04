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
package org.orange.querysystem.content;

import org.orange.querysystem.AboutActivity;
import org.orange.querysystem.LoginActivity;
import org.orange.querysystem.R;
import org.orange.querysystem.content.PostUpdater.OnPostExecuteListener;

import util.webpage.Post;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

/**
 * @author Bai Jie
 */
public class ListPostsActivity extends FragmentActivity{
	
	private int start_resume = 0;
	private TextView currentTime;
	TabHost mTabHost;
	ViewPager  mViewPager;
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
		currentTime = (TextView)findViewById(R.id.currentTime);
		currentTime.setText(R.string.post);

		mWebUpdaterToDB = new PostUpdater(this);
		mWebUpdaterToDB.setOnPostExecuteListener(new OnPostExecuteListener() {
			@Override
			public void onPostExecute() {
				loadPosts();
			}
		});
		mWebUpdaterToDB.autoUpdatePosts();

		mTabHost = (TabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mViewPager = (ViewPager)findViewById(R.id.pager);

		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);

		//3.0以上版本，使用ActionBar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar mActionBar = getActionBar();
			mActionBar.setTitle(R.string.post);
			currentTime.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
			//横屏时，为节省空间隐藏ActionBar
			if(getResources().getConfiguration().orientation == 
					android.content.res.Configuration.ORIENTATION_LANDSCAPE)
				mActionBar.hide();
		}else{
			TabWidget tabWidget = mTabHost.getTabWidget();
			for (int i = 0; i < tabWidget.getChildCount(); i++) {  
				View child = tabWidget.getChildAt(i);  

				final TextView tv = (TextView)child.findViewById(android.R.id.title);  
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv.getLayoutParams();  
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0); //取消文字底边对齐  
				params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE); //设置文字居中对齐  

				//child.getLayoutParams().height = tv.getHeight();
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
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		mTabsAdapter.adjustSelectedTabToCenter();
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
	 * @param source 要查找的来源。如：Post.SOURCES.WEBSITE_OF_TEACHING_AFFAIRS
	 * @return 资源标识ID。如：R.string.teaching_affairs
	 * @see Post.SOURCES
	 */
	public static int getSourceString(byte source){
		switch(source){
		case Post.SOURCES.WEBSITE_OF_TEACHING_AFFAIRS:return R.string.teaching_affairs;
		case Post.SOURCES.WEBSITE_OF_SCCE:return R.string.school_of_computer_and_communication_engineering;
		case Post.SOURCES.STUDENT_WEBSITE_OF_SCCE:return R.string.student_website_of_SCCE;
		case Post.SOURCES.UNKNOWN_SOURCE:return R.string.unknown_source;
		default:throw new IllegalArgumentException("Unknown post source: " + source);
		}
	}

	public void addTab(byte source){
		String sourceString = getResources().getText(getSourceString(source)).toString();
		mTabsAdapter.addTab(mTabHost.newTabSpec(sourceString).setIndicator(sourceString),
				ListPostsFragment.class, ListPostsFragment.buildArgument(source));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			
		}else{
			mTabHost.getTabWidget().getChildAt(mTabHost.getTabWidget().getChildCount()-1).setBackgroundResource(Color.TRANSPARENT);
		}				
	}

	public void loadPosts(){
		mTabsAdapter.clear();
		addTab(Post.SOURCES.WEBSITE_OF_TEACHING_AFFAIRS);
		addTab(Post.SOURCES.STUDENT_WEBSITE_OF_SCCE);
		addTab(Post.SOURCES.WEBSITE_OF_SCCE);
	}
	public boolean isNetworkConnected(){
    	ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if(networkInfo !=null && networkInfo.isConnected()){
			return true;
		}
		else{
		    return false;
		}
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.main_menu);
        menu.add(0, 2, 2, R.string.change_number);
        menu.add(0, 3, 3, R.string.settings);
        menu.add(0, 4, 4, R.string.refresh);
        menu.add(0, 5, 5, R.string.about);
        return super.onCreateOptionsMenu(menu); 
    }
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	// TODO Auto-generated method stub\
    	if(item.getItemId() == 1){
    		finish();
    	}
    	else if(item.getItemId() == 2){
    		Editor editor = getSharedPreferences("data", 0).edit();
    		editor.putBoolean("logIn_auto", false);
    		editor.commit();
    		startActivity(new Intent(this, LoginActivity.class));
    	}
    	else if(item.getItemId() == 3){
    		
    	}
    	else if(item.getItemId() == 4){
    		if(isNetworkConnected()){
    			start_resume = 1;
        		startActivity(new Intent(this, InsertDBFragmentActivity.class));
        		//TODO startActivity后不会继续运行

            }
            else{
            	Toast.makeText(this, "网络异常！请检查网络设置！", Toast.LENGTH_LONG).show();
            }
    	}
    	else if(item.getItemId() == 5){
    		startActivity(new Intent(this, AboutActivity.class));
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
}
