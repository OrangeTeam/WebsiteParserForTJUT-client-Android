/**
 * 
 */
package org.orange.querysystem.util;

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.List;

import org.orange.querysystem.util.Network;
import org.orange.studentinformationdatabase.Contract;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.GetterInterface;
import util.webpage.Post;
import util.webpage.SchoolWebpageParser;
import util.webpage.ReadPageHelper.OnReadPageListener;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.client.MyHessianSocketConnectionFactory;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * 通知更新器，用于更新通知列表。
 * @author Bai Jie
 */
public class PostUpdater {
	String TAG = PostUpdater.class.getName();

	Context mContext;
	Date lastUpdatedTime;

	UpdatePostsListToDatabase mWebUpdaterToDB;
	OnPostExecuteListener mOnPostExecuteListener;

	public PostUpdater(Context context) {
		mContext = context;
	}

	/**
	 * 设置通知更新监听器，在更新完通知时回调。
	 * @param mOnPostExecuteListener 通知更新监听器
	 */
	public void setOnPostExecuteListener(OnPostExecuteListener mOnPostExecuteListener) {
		this.mOnPostExecuteListener = mOnPostExecuteListener;
	}

	/**
	 * 取得上次更新通知的时间。
	 * @return 上次更新通知的时间
	 */
	public Date getLastUpdateTime(){
		if(lastUpdatedTime != null)
			return (Date) lastUpdatedTime.clone();
		else
			return null;
	}
	/**
	 * 更新通知。自动判断更新间隔、是否正在更新等。
	 * @return 若开始（正在）更新返回true；若不足最小更新间隔而没有更新，返回false 
	 */
	public boolean autoUpdatePosts(){
		if(mWebUpdaterToDB != null)
			return true;
		long updateInterval = PreferenceManager.getDefaultSharedPreferences(mContext).getLong("", 4L*24*60*60*1000);
		//TODO lastUpdatedTime==null怎么办
		if(lastUpdatedTime!=null && System.currentTimeMillis() - lastUpdatedTime.getTime() < updateInterval){
			return false;
		}
		else if(!Network.getInstance(mContext).isConnected()){
			//TODO 完善
			Toast.makeText(mContext, "无网络连接", Toast.LENGTH_SHORT).show();
			return false;
		}
		else{
			mWebUpdaterToDB = new UpdatePostsListToDatabase();
			mWebUpdaterToDB.execute();
			return true;
		}
	}
	/**
	 * 正在向数据库更新通知
	 * @return 正在向数据库更新通知时放回true；没有更新（空闲）返回false
	 */
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

	private class UpdatePostsListToDatabase extends AsyncTask<Void, Void, Void>{
		private static final String hessianUrl = "http://schoolwebpageparser.appspot.com/getter";
		private static final int maxAttempts = 10;
		private StudentInfDBAdapter database = new StudentInfDBAdapter(mContext);;

		@Override
		protected Void doInBackground(Void... params) {
			autoUpdatePosts();
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

		private void refreshLastUpdatedTime() {
			try{
				database.open();
				Post lastPost = database.getPostsFromDB(null, Contract.Posts.COLUMN_NAME_DATE + " DESC", "1").get(0);
				lastUpdatedTime = lastPost.getDate();
			} catch (SQLiteException e){
				Log.e(TAG, "打开数据库异常！");
				e.printStackTrace();
			} catch (SQLException e){
				Log.e(TAG, "从数据库读取最新Post时遇到异常！");
				e.printStackTrace();
			} finally{
				database.close();
			}
		}

		/**
		 * 更新通知
		 * @param quickUpdateOrAllUpdate true for 快速（常用）更新；false for 完整更新
		 */
		private void updatePosts(boolean quickUpdateOrAllUpdate){
			List<Post> posts = null;

			//准备用Hessian连接GAE代理
			//TODO 去掉timeout常量
			int timeout = quickUpdateOrAllUpdate?2000:5000;
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
						Log.i(TAG, "用Hessian连接GAE代理更新通知时，遇到SocketTimeoutException"+e.getMessage());
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
					if(quickUpdateOrAllUpdate)
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
				try{
					database.open();
					database.autoInsertArrayPostsInf(posts);
				} catch (SQLiteException e){
					Log.e(TAG, "打开数据库异常！");
					e.printStackTrace();
				} catch (SQLException e){
					Log.e(TAG, "向数据库更新Posts时遇到异常！");
					e.printStackTrace();
				} finally{
					database.close();
				}
			}else{
				Log.e(TAG, "更新posts失败");
			}
		}

		/**
		 * 更新通知。自动判断更新间隔、是否正在更新等。
		 */
		private void autoUpdatePosts(){
			//更新时间
			refreshLastUpdatedTime();
			long lastUpdated = 0;
			if(lastUpdatedTime != null)
				lastUpdated = lastUpdatedTime.getTime();
			long now = System.currentTimeMillis();
			long longUpdateInterval = PreferenceManager.getDefaultSharedPreferences(mContext).getLong("", 31L*24*60*60*1000);
			long updateInterval = PreferenceManager.getDefaultSharedPreferences(mContext).getLong("", 4L*24*60*60*1000);
			//根据时间间隔更新
			if(now - lastUpdated > longUpdateInterval){
				if(lastUpdated == 0){
					Log.v(TAG, "第一次更新");
					//警告
				}
				updatePosts(false);
				lastUpdatedTime = new Date(now);
			}else if(now - lastUpdated > updateInterval){
				updatePosts(true);
				lastUpdatedTime = new Date(now);
			}
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
