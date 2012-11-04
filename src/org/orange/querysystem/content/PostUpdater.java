/**
 * 
 */
package org.orange.querysystem.content;

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.List;

import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.GetterInterface;
import util.webpage.Post;
import util.webpage.SchoolWebpageParser;
import util.webpage.ReadPageHelper.OnReadPageListener;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.client.MyHessianSocketConnectionFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author Bai Jie
 */
public class PostUpdater {
	public static final String LAST_UPDATED_TIME_KEY = "POST_LAST_UPDATED_TIME_KEY";
	String TAG = PostUpdater.class.getName();

	Context mContext;
	SharedPreferences mPreferences;
	long longUpdateInterval;
	long updateInterval;

	UpdatePostsListToDatabase mWebUpdaterToDB;
	OnPostExecuteListener mOnPostExecuteListener;

	public PostUpdater(Context context) {
		mContext = context;
		mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		longUpdateInterval = PreferenceManager.getDefaultSharedPreferences(context).getLong("", 31L*24*60*60*1000);
		updateInterval = PreferenceManager.getDefaultSharedPreferences(context).getLong("", 4L*24*60*60*1000);
	}

	public void setOnPostExecuteListener(OnPostExecuteListener mOnPostExecuteListener) {
		this.mOnPostExecuteListener = mOnPostExecuteListener;
	}

	/**
	 * 取得上次更新通知的时间。
	 * @return 上次更新通知的时间
	 */
	public Date getLastUpdateTime(){
		return new Date(mPreferences.getLong(LAST_UPDATED_TIME_KEY, 0));
	}
	/**
	 * 更新通知。自动判断更新间隔、是否正在更新等。
	 * @return 若正在更新返回true；若不足最小更新间隔而没有更新，返回false 
	 */
	public boolean autoUpdatePosts(){
		if(mWebUpdaterToDB != null)
			return true;
		long lastUpdatedTime = mPreferences.getLong(LAST_UPDATED_TIME_KEY, 0);
		long now = new Date().getTime();
		if(true){
			if(lastUpdatedTime == 0){
				Log.v(TAG, "第一次更新");
				//警告
			}
			mWebUpdaterToDB = new UpdatePostsListToDatabase();
			mWebUpdaterToDB.execute(UpdatePostsListToDatabase.ALL_POSTS);
			return true;
		}else if(now - lastUpdatedTime > updateInterval){
			mWebUpdaterToDB = new UpdatePostsListToDatabase();
			mWebUpdaterToDB.execute(UpdatePostsListToDatabase.COMMON_POSTS);
			return true;
		}else{
			return false;
		}
	}
	public boolean isUpdating(){
		return mWebUpdaterToDB != null;
	}
	/**
	 * 如果曾经开始了更新，停止更新。
	 */
	public void stop(){
		if(mWebUpdaterToDB != null){
			mWebUpdaterToDB.cancel(false);
			mWebUpdaterToDB = null;
		}
	}

	public interface OnPostExecuteListener{
		public void onPostExecute();
	}

	private class UpdatePostsListToDatabase extends AsyncTask<Integer, Void, Void>{
		public static final int COMMON_POSTS = 1;
		public static final int ALL_POSTS = 2;
		private static final String hessianUrl = "http://schoolwebpageparser.appspot.com/getter";
		private static final int maxAttempts = 10;
		StudentInfDBAdapter database;

		@Override
		protected Void doInBackground(Integer... params) {
			boolean isCommonUpdate = params[0] == COMMON_POSTS;
			List<Post> posts = null;
			Date lastUpdatedTime = getLastUpdateTime();

			//准备用Hessian连接GAE代理
			int timeout = isCommonUpdate?2000:5000;
			HessianProxyFactory factory = new HessianProxyFactory();
			MyHessianSocketConnectionFactory mHessianSocketConnectionFactory =
					new MyHessianSocketConnectionFactory();
			mHessianSocketConnectionFactory.setHessianProxyFactory(factory);
			factory.setConnectionFactory(mHessianSocketConnectionFactory);
			factory.setConnectTimeout(timeout);
			factory.setReadTimeout(timeout);
			GetterInterface getter;
			//用Hessian连接GAE代理
			for(int counter = 1;counter <= maxAttempts;counter++){
				try {
					getter = (GetterInterface) factory.create(GetterInterface.class, hessianUrl);
					posts = getter.getPosts(lastUpdatedTime, null, -1);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (Exception e){
					if(e.getCause() instanceof SocketTimeoutException){
						Log.i(TAG, "SocketTimeoutException"+e.getMessage());
						continue;
					}
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
					if(isCommonUpdate)
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
				database = new StudentInfDBAdapter(mContext);
				database.open();
				database.autoInsertArrayPostsInf(posts);
				mPreferences.edit().putLong(LAST_UPDATED_TIME_KEY, System.currentTimeMillis()).commit();
				database.close();
			}else{
				Log.e(TAG, "更新posts失败");
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mOnPostExecuteListener != null)
				mOnPostExecuteListener.onPostExecute();
		}

		@Override
		protected void onCancelled(Void result) {
			if(database != null)
				database.close();
			mWebUpdaterToDB = null;
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
