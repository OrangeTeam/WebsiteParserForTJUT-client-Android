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

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.orange.querysystem.AboutActivity;
import org.orange.querysystem.LoginActivity;
import org.orange.querysystem.R;
import org.orange.querysystem.content.ListPostsFragment.SimplePost;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.client.HessianRuntimeException;
import com.caucho.hessian.client.MyHessianURLConnectionFactory;

import util.GetterInterface;
import util.webpage.Post;
import util.webpage.ReadPageHelper.OnReadPageListener;
import util.webpage.SchoolWebpageParser;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
	private static final String LAST_UPDATED_TIME_KEY = "LAST_UPDATED_TIME_KEY";
	
	private int start_resume = 0;
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
		longUpdateInterval = PreferenceManager.getDefaultSharedPreferences(this).getLong("", 31L*24*60*60*1000);
		updateInterval = PreferenceManager.getDefaultSharedPreferences(this).getLong("", 4L*24*60*60*1000);
		
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
		//TODO 这里是读取的旧的
		new LoadPostsListFromDatabase().execute("");
	}

	private class LoadPostsListFromDatabase extends AsyncTask<String, Void, ArrayList<Post>>{

		@Override
		protected ArrayList<Post> doInBackground(String... where) {
			StudentInfDBAdapter database = new StudentInfDBAdapter(ListPostsActivity.this);
			ArrayList<Post> result = null;
			try{
				database.open();
				result = database.getPostsFromDB(where[0], StudentInfDBAdapter.KEY_DATE+" DESC", null);
			} catch (SQLiteException e){
				Log.e(TAG, "打开数据库异常！");
				e.printStackTrace();
			} catch (SQLException e){
				Log.e(TAG, "从数据库读取Post列表时遇到异常！");
				e.printStackTrace();
			} finally{
				database.close();
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
			List<Post> posts = null;
			Date lastUpdatedTime = new Date(mPreferences.getLong(LAST_UPDATED_TIME_KEY, 0));

			//准备用Hessian连接GAE代理
			String url = "http://schoolwebpageparser.appspot.com/getter";
			int maxAttempts = 10;
			int timeout = 2000;
			HessianProxyFactory factory = new HessianProxyFactory();
			MyHessianURLConnectionFactory mHessianURLConnectionFactory =
					new MyHessianURLConnectionFactory();
			mHessianURLConnectionFactory.setHessianProxyFactory(factory);
			factory.setConnectionFactory(mHessianURLConnectionFactory);
			factory.setConnectTimeout(timeout);
			GetterInterface getter;
			//用Hessian连接GAE代理
			for(int counter = 1;counter <= maxAttempts;counter++){
				try {
					getter = (GetterInterface) factory.create(GetterInterface.class, url);
					posts = getter.getPosts(lastUpdatedTime, null, -1);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (HessianRuntimeException e){
					if(e.getCause() instanceof SocketTimeoutException)
						continue;
					else
						e.printStackTrace();
				}
				break;
			}
			//备用方案
			if(posts == null){
				MyOnReadPageListener readPageListener = new MyOnReadPageListener();
				try {
					SchoolWebpageParser parser = new SchoolWebpageParser(new MyParserListener());
					parser.setOnReadPageListener(readPageListener);
					if(params[0] == COMMON_POSTS)
						posts = parser.parseCommonPosts(lastUpdatedTime, null, -1);
					else
						posts = parser.parsePosts(lastUpdatedTime, null, -1);
					
					Log.i(TAG, "共 "+posts.size()+" 条， "+readPageListener.pageNumber+" 页 "+readPageListener.totalSize/1024.0+" KB");
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				} catch (java.io.UnsupportedEncodingException e){
					e.printStackTrace();
				} catch (java.io.IOException e){
					e.printStackTrace();
				}
			}
			if(posts != null){
				StudentInfDBAdapter database = new StudentInfDBAdapter(ListPostsActivity.this);
				database.open();
				database.autoInsertArrayPostsInf(posts);
				mPreferences.edit().putLong(LAST_UPDATED_TIME_KEY, new Date().getTime()).commit();
				database.close();
			}else{
				Log.e("BaiJie", "更新posts失败");
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
