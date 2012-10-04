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
import java.util.Date;
import java.util.List;

import org.orange.querysystem.R;
import org.orange.querysystem.content.ListPostsFragment.SimplePost;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.webpage.Post;
import util.webpage.ReadPageHelper.OnReadPageListener;
import util.webpage.SchoolWebpageParser;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

/**
 * @author Bai Jie
 */
public class ListPostsActivity extends FragmentActivity{
	public static final String ARRAYLIST_OF_POSTS_KEY
		= ListPostsActivity.class.getName()+"arraylist_of_posts_key";
	private static final String TAG = ListPostsActivity.class.getName();
	private static final String LAST_UPDATED_TIME_KEY = "LAST_UPDATED_TIME_KEY";
	
	private TextView currentTime;
	TabHost mTabHost;
	ViewPager  mViewPager;
	TabsAdapter mTabsAdapter;

	SharedPreferences mPreferences = null;
	long longUpdateInterval = 0;
	long updateInterval = 0;
	

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
		mPreferences = getPreferences(MODE_PRIVATE);
		longUpdateInterval = PreferenceManager.getDefaultSharedPreferences(this).getLong("", 31*24*60*60*1000);
		updateInterval = PreferenceManager.getDefaultSharedPreferences(this).getLong("", 4*24*60*60*1000);
		
		loadPosts();
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

	public void loadPosts(){
		long lastUpdatedTime = mPreferences.getLong(LAST_UPDATED_TIME_KEY, 0);
		long now = new Date().getTime();
		if(now - lastUpdatedTime > longUpdateInterval){
			if(lastUpdatedTime == 0){
				Log.v(TAG, "第一次更新");
				//警告
			}
			new UpdatePostsListToDatabase().execute(UpdatePostsListToDatabase.ALL_POSTS);
		}else if(now - lastUpdatedTime > updateInterval){
			new UpdatePostsListToDatabase().execute(UpdatePostsListToDatabase.COMMON_POSTS);
		}
		
		new LoadPostsListFromDatabase().execute("");
	}

	private class LoadPostsListFromDatabase extends AsyncTask<String, Void, ArrayList<Post>>{

		@Override
		protected ArrayList<Post> doInBackground(String... where) {
			StudentInfDBAdapter database = new StudentInfDBAdapter(ListPostsActivity.this);
			ArrayList<Post> result = null;
			try{
				database.open();
				result = database.getPostsFromDB(where[0], StudentInfDBAdapter.KEY_DATE+" DESC", "500");
			} catch (SQLiteException e){
				Log.e(TAG, "打开数据库异常！");
				e.printStackTrace();
			} catch (SQLException e){
				Log.e(TAG, "从数据库读取Post列表时遇到异常！");
				e.printStackTrace();
			} finally{
				try{
					if(database!=null)
						database.close();
				} catch(Exception e){
					Log.e(TAG, "关闭数据库时遇到异常！");
					e.printStackTrace();
				}
			}
			return result;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(ArrayList<Post> result) {
			if(result != null)
				addPostsListFromMultipleSource(result);
		}
		
	}

	private class UpdatePostsListToDatabase extends AsyncTask<Integer, Void, Void>{
		public static final int COMMON_POSTS = 1;
		public static final int ALL_POSTS = 2;

		@Override
		protected Void doInBackground(Integer... params) {
			StudentInfDBAdapter database = new StudentInfDBAdapter(ListPostsActivity.this);
			MyOnReadPageListener readPageListener = new MyOnReadPageListener();
			Date lastUpdatedTime = new Date(mPreferences.getLong(LAST_UPDATED_TIME_KEY, 0));
			try {
				SchoolWebpageParser parser = new SchoolWebpageParser(new MyParserListener());
				parser.setOnReadPageListener(readPageListener);
				List<Post> posts = null;
				if(params[0] == COMMON_POSTS)
					posts = parser.parseCommonPosts(lastUpdatedTime, null, -1);
				else
					posts = parser.parsePosts(lastUpdatedTime, null, -1);
				database.open();
				database.autoInsertArrayPostsInf(posts);
				mPreferences.edit().putLong(LAST_UPDATED_TIME_KEY, new Date().getTime()).commit();
				Log.i(TAG, "共 "+posts.size()+" 条， "+readPageListener.pageNumber+" 页 "+readPageListener.totalSize/1024.0+" KB");
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			} catch (java.io.UnsupportedEncodingException e){
				e.printStackTrace();
			} catch (java.io.IOException e){
				e.printStackTrace();
			} catch (SQLiteException e){
				Log.e(TAG, "打开数据库异常！");
				e.printStackTrace();
			} finally{
				try{
					if(database!=null)
						database.close();
				} catch (Exception e){
					Log.e(TAG, "关闭数据库时遇到异常！");
					e.printStackTrace();
				}
			}
			return null;
		}

		private class MyOnReadPageListener implements OnReadPageListener{
			int pageNumber = 0;
			int totalSize = 0;

			@Override
			public void onRequest(String url, int statusCode, String statusMessage,
					int pageSize) {
				Log.v(TAG,"URL: "+url+"\nStatus Code: "+statusCode+"\tStatusMessage: "
						+statusMessage+"\t Page Size: "+pageSize);
				totalSize+=pageSize;
				pageNumber++;
			}
			
		}
		private class MyParserListener extends SchoolWebpageParser.ParserListenerAdapter{
			String TAG = ListPostsActivity.class.getName();
		
			/* (non-Javadoc)
			 * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onError(int, java.lang.String)
			 */
			@Override
			public void onError(int code, String message) {
				Log.e(TAG, message);
			}
		
			/* (non-Javadoc)
			 * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onWarn(int, java.lang.String)
			 */
			@Override
			public void onWarn(int code, String message) {
				Log.w(TAG, message);
			}
		
			/* (non-Javadoc)
			 * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onInformation(int, java.lang.String)
			 */
			@Override
			public void onInformation(int code, String message) {
				Log.i(TAG, message);
			}
		}
	}
}
