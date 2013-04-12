/**
 * 
 */
package org.orange.querysystem;

import java.io.IOException;

import org.orange.querysystem.R;
import org.orange.querysystem.util.Network;
import org.orange.studentinformationdatabase.Contract;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.webpage.Post;
import util.webpage.SchoolWebpageParser;
import util.webpage.SchoolWebpageParser.ParserException;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Bai Jie
 */
@TargetApi(11)
public class PostDetailsActivity extends FragmentActivity {
	public static final String EXTRA_POST_ID = PostDetailsActivity.class.getName()+"EXTRA_POST_ID";
	private static final String TAG = PostDetailsActivity.class.getName();

	TextView titleView;
	WebView mainBodyView;
	TextView authorView;
	TextView dateView;
	TextView sourceAndCategoryView;
	ActionBar mActionBar = null;
	
	Post mPost;

	private static UpdatePostMainBody updater;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_post_activity);
		titleView = (TextView) findViewById(R.id.title);
		titleView.setMinimumHeight(0);
		titleView.setMinimumWidth(0);
		mainBodyView = (WebView) findViewById(R.id.main_body);
		mainBodyView.getSettings().setBuiltInZoomControls(true);
		authorView = (TextView) findViewById(R.id.author);
		dateView = (TextView) findViewById(R.id.date);
		sourceAndCategoryView = (TextView) findViewById(R.id.source_and_category);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			mActionBar = getActionBar();
		
		long id = getIntent().getLongExtra(EXTRA_POST_ID, 0);
		if(id!=0)
			new LoadPost().execute(id);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem openWithBrowser = menu
				.add(Menu.NONE, 1, Menu.NONE, R.string.open_with_browser)
				.setIcon(R.drawable.add_to_queue);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			openWithBrowser.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case 1:
			if(mPost != null){
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mPost.getUrl()));
				startActivity(intent);
			}
			return true;
		default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * 更新通知正文。自动管理异步更新线程。
	 * @param target 要更新的通知
	 * @return  若开始（正在）更新返回true；若无法连接网络，返回false 
	 */
	//TODO 完善。代替Toast
	public boolean updatePostMainBody(Post target){
		if(updater != null){
			Toast.makeText(this, "正在更新，请稍后...", Toast.LENGTH_SHORT).show();
			return true;
		}
		else if(!Network.isConnected(this)){
			Network.openNoConnectionDialog(this);
			return false;
		}else{
			Toast.makeText(this, "正在更新，请稍后...", Toast.LENGTH_SHORT).show();
			updater = new UpdatePostMainBody();
			updater.execute(target);
			return true;
		}
	}

	public void loadPost(Post post){
		if(post == null)
			return;
		mPost = post;
		if(mActionBar != null){
			mActionBar.setTitle(post.getTitle());
			mActionBar.setDisplayShowTitleEnabled(true);
		}//else
			titleView.setText(post.getTitle());
		mainBodyView.loadDataWithBaseURL(post.getUrl(), post.getMainBody(), "text/html", null, null);
		authorView.setText(post.getAuthor());
		dateView.setText(post.getDateString());
		sourceAndCategoryView.setText(post.getSourceString()+" "+post.getCategory());
	}

	private class LoadPost extends AsyncTask<Long, Void, Post>{

		@Override
		protected Post doInBackground(Long... id) {
			Post post = null;
			StudentInfDBAdapter database = new StudentInfDBAdapter(PostDetailsActivity.this);
			try{
				database.open();
				post = database.getPostsFromDB(Contract.Posts._ID+" = "+id[0], null, null).get(0);
			} catch (SQLiteException e){
				Log.e(TAG, "打开数据库异常！");
				e.printStackTrace();
			} catch (SQLException e){
				Log.e(TAG, "从数据库读取Post列表时遇到异常！");
				e.printStackTrace();
			} finally{
				database.close();
			}
			return post;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Post result) {
			if(result == null)
				return;
			loadPost(result);
			if(result.getMainBody() == null)
				updatePostMainBody(result);
		}
		
	}

	private class UpdatePostMainBody extends AsyncTask<Post, Void, Post>{

		@Override
		protected Post doInBackground(Post... post) {
			Post result = post[0];
			if(result == null)
				return null;
			SchoolWebpageParser parser = new SchoolWebpageParser();
			StudentInfDBAdapter database = new StudentInfDBAdapter(PostDetailsActivity.this);
			try {
				parser.parsePostMainBody(result);
				database.open();
				database.updatePostInf(result);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserException e) {
				e.printStackTrace();
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
		protected void onPostExecute(Post result) {
			if(result!=null)
				loadPost(result);
			updater = null;
		}
		
	}
}
