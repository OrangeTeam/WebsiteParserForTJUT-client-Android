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

import java.util.ArrayList;

import org.orange.querysystem.AboutActivity;
import org.orange.querysystem.LoginActivity;
import org.orange.querysystem.R;
import org.orange.querysystem.content.ListPostsFragment.SimplePost;
import org.orange.querysystem.content.PostUpdater.OnPostExecuteListener;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.webpage.Post;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Bai Jie
 */
public class ListPostsActivity extends FragmentActivity{
	public static final String ARRAYLIST_OF_POSTS_KEY
		= ListPostsActivity.class.getName()+"arraylist_of_posts_key";
	private static final String TAG = ListPostsActivity.class.getName();
	
	private int start_resume = 0;
	private TextView currentTime;
	TabHost mTabHost;
	ViewPager  mViewPager;
	TabsAdapter mTabsAdapter;

	LoadPostsListFromDatabase mLoaderFromDB;
	PostUpdater mWebUpdaterToDB;

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@TargetApi(11)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		ArrayList<Bundle> args = getIntent().getParcelableArrayListExtra(ARRAYLIST_OF_POSTS_KEY);
//		if(args == null)
//			finish();
		
		setContentView(R.layout.fragment_tabs_pager);
		currentTime = (TextView)findViewById(R.id.currentTime);
		currentTime.setText("通知");

		mWebUpdaterToDB = new PostUpdater(this);
		mWebUpdaterToDB.setOnPostExecuteListener(new OnPostExecuteListener() {
			@Override
			public void onPostExecute() {
				if(mLoaderFromDB != null){
					mLoaderFromDB.cancel(false);
				}
				mLoaderFromDB = new LoadPostsListFromDatabase();
				mLoaderFromDB.execute(null, null);
			}
		});

		mTabHost = (TabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mViewPager = (ViewPager)findViewById(R.id.pager);

		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if(getResources().getConfiguration().orientation == 
					android.content.res.Configuration.ORIENTATION_LANDSCAPE)
				getActionBar().hide();
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
	}

	@Override
	protected void onStart() {
		loadPosts();
		super.onStart();
	}

	@Override
	protected void onStop() {
		mWebUpdaterToDB.stop();
		if(mLoaderFromDB != null){
			mLoaderFromDB.cancel(false);
			mLoaderFromDB = null;
		}
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

	public void addPostsListFromMultipleSource(ArrayList<Post> posts){
		//按来源source分类
		//TODO 改掉3
		ArrayList<ArrayList<Post>> arrayListOfPosts = new ArrayList<ArrayList<Post>>(3);
		for(int i=0;i<3;i++)
			arrayListOfPosts.add(new ArrayList<Post>());
		for(Post post:posts)
			arrayListOfPosts.get(post.getSource()-1).add(post);
		//移除ArrayList<ArrayList<Post>> arrayListOfPosts中空的ArrayList<Post>
		for(int i=0;i<arrayListOfPosts.size();){
			if(arrayListOfPosts.get(i).isEmpty())
				arrayListOfPosts.remove(i);
			else
				i++;
		}
		for(ArrayList<Post> postsInArray:arrayListOfPosts){
			addPostsListFromOneSource(postsInArray);
		}
	}

	public void addPostsListFromOneSource(ArrayList<Post> posts){
		if(posts.isEmpty() || posts.get(0)==null)
			return;
		Bundle arg = new Bundle();
		ArrayList<SimplePost> simplePosts = new ArrayList<SimplePost>();
		
		String source = posts.get(0).getSourceString();
		for(Post post:posts){
			if(!source.equals(post.getSourceString())){
				Log.e(TAG, "addPostsListFromOneSource() 的参数post列表不是来自同一个来源");
				return;
			}
			simplePosts.add(new SimplePost(post.getId(), post.getTitle(), post.getCategory(),
					post.getAuthor(), post.getDateString()));
		}
		arg.putParcelableArrayList(ListPostsFragment.POSTS_KEY, simplePosts);
		
		mTabsAdapter.addTab(mTabHost.newTabSpec(source).setIndicator(source),
				ListPostsFragment.class, arg);
		return;
	}

	public void clear(){
		mTabsAdapter.clear();
	}

	public void loadPosts(){
		if(!mWebUpdaterToDB.autoUpdatePosts()){
			if(mLoaderFromDB != null)
				mLoaderFromDB.cancel(false);
			mLoaderFromDB = new LoadPostsListFromDatabase();
			mLoaderFromDB.execute(null, null);
		}
	}
	/**
	 * 用法：new LoadPostsListFromDatabase.execute(where, limit);
	 * @author Bai Jie
	 *
	 */
	private class LoadPostsListFromDatabase extends AsyncTask<String, Void, ArrayList<Post>>{
		StudentInfDBAdapter database = new StudentInfDBAdapter(ListPostsActivity.this);

		@Override
		protected ArrayList<Post> doInBackground(String... sqlParms) {
			ArrayList<Post> result = null;
			try{
				database.open();
				result = database.getPostsFromDB(sqlParms[0], StudentInfDBAdapter.KEY_DATE+" DESC", sqlParms[1]);
			} catch (SQLiteException e){
				Log.e(TAG, "打开数据库异常！");
				e.printStackTrace();
			} catch (SQLException e){
				Log.e(TAG, "从数据库读取Post列表时遇到异常！");
				e.printStackTrace();
			} finally{
				database.close();
				database = null;
			}
			return result;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(ArrayList<Post> result) {
			if(result != null){
				clear();
				addPostsListFromMultipleSource(result);
			}
		}

		@Override
		protected void onCancelled(ArrayList<Post> result) {
			if(database != null)
				database.close();
			mLoaderFromDB = null;
		}
		
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
    		Editor editor = getSharedPreferences("data", 0).edit();
			editor.putString("passMainMenu", "true");
            editor.commit();
    		startActivity(new Intent(this, MainMenuActivity.class));
    	}
    	else if(item.getItemId() == 2){
    		Editor editor = getSharedPreferences("data", 0).edit();
    		editor.putBoolean("logIn_auto", false);
    		editor.commit();
    		startActivity(new Intent(this, LoginActivity.class));
    	}
    	else if(item.getItemId() == 3){
//    		startActivity(new Intent(this, AllListCoursesActivity.class));
    	}
    	else if(item.getItemId() == 4){
    		if(isNetworkConnected()){
    			start_resume = 1;
        		startActivity(new Intent(this, InsertDBFragmentActivity.class));
        		//TODO startActivity后不会继续运行
//        		readDB();
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
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Editor editor = getSharedPreferences("data", 0).edit();
			editor.putString("passMainMenu", "true");
            editor.commit();
		}
		return super.onKeyDown(keyCode, event);
	}
}
