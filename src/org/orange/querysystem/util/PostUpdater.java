/**
 * 
 */
package org.orange.querysystem.util;

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.List;

import org.orange.querysystem.R;
import org.orange.querysystem.SettingsActivity;
import org.orange.studentinformationdatabase.Contract;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.GetterInterface;
import util.webpage.Post;
import util.webpage.ReadPageHelper.OnReadPageListener;
import util.webpage.SchoolWebpageParser;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.client.MyHessianSocketConnectionFactory;

/**
 * 通知更新器，用于更新通知列表。
 * @author Bai Jie
 */
public class PostUpdater {
	private static String TAG = PostUpdater.class.getName();
	private static String KEY_OF_POST_UPDATER = PostUpdater.class.getName();
	private static String KEY_OF_LAST_UPDATED_TIME = "last_updated_time";

	private Context mContext;
	private Date lastUpdatedTime;

	private static UpdatePostsListToDatabase mWebUpdaterToDB;
	private OnPostUpdateListener mOnPostUpdateListener;

	public PostUpdater(Context context) {
		mContext = context;
	}

	/**
	 * 设置通知更新监听器，在更新完通知时回调。
	 * @param mOnPostExecuteListener 通知更新监听器
	 */
	public void setOnPostExecuteListener(OnPostUpdateListener mOnPostExecuteListener) {
		this.mOnPostUpdateListener = mOnPostExecuteListener;
	}

	/**
	 * 取得上次更新通知的时间。
	 * @return 如果尚未更新过，返回null；否则返回上次更新通知的时间
	 */
	public Date getLastUpdateTime(){
		if(lastUpdatedTime != null)
			return (Date) lastUpdatedTime.clone();
		else
			return null;
	}

	/**
	 * 保存指定的上次更新的时间到{@link SharedPreferences}
	 * @param lastUpdatedTime 要保存的更新时间
	 * @return Returns true if the new values were successfully written to persistent storage.
	 */
	private boolean saveLastUpdateTimeToSharedPreferences(Date lastUpdatedTime){
		if(lastUpdatedTime == null)
			return false;
		SharedPreferences.Editor editor
			= mContext.getSharedPreferences(KEY_OF_POST_UPDATER, Context.MODE_PRIVATE).edit();
		editor.putLong(KEY_OF_LAST_UPDATED_TIME, lastUpdatedTime.getTime());
		return editor.commit();
	}
	/**
	 * 保存上次更新的时间到{@link SharedPreferences}
	 * @return Returns true if the new values were successfully written to persistent storage.
	 */
	private boolean saveLastUpdateTimeToSharedPreferences(){
		return saveLastUpdateTimeToSharedPreferences(lastUpdatedTime);
	}
	/**
	 * 从{@link SharedPreferences}取得上次更新的时间
	 * @return 如果{@link SharedPreferences}中有保存值，返回相应{@link Date}；如果没有保存值，返回null
	 */
	public Date getLastUpdatedTimeFromSharedPreferences(){
		long time = mContext.getSharedPreferences(KEY_OF_POST_UPDATER,
				Context.MODE_PRIVATE).getLong(KEY_OF_LAST_UPDATED_TIME, -1);
		if(time == -1)
			return null;
		else
			return new Date(time);
	}

	/**
	 * 更新通知。自动判断是否正在更新、网络连通性。如果是自动更新，自动确认更新间隔。
	 * @param mandatorily true表示强制更新，不管上次更新时间；false表示这是自动更新，仅当符合自动更新条件时（时间间隔等）才真正更新
	 * @return 若开始（正在）更新返回true；若已禁止自动更新或不足更新间隔时间而没有更新，返回false
	 */
	public boolean updatePosts(boolean mandatorily){
		if(mWebUpdaterToDB != null)
			return true;
		if(!mandatorily){
			if(!SettingsActivity.isUpdatePostAutomatically(mContext))
				return false;
			long updateInterval = SettingsActivity.getIntervalOfPostUpdating(mContext);
			Date updatedTime = lastUpdatedTime==null ?
					getLastUpdatedTimeFromSharedPreferences() : lastUpdatedTime;
			// updatedTime==null时仍然继续，以刷新lastUpdatedTime
			if(updatedTime!=null && System.currentTimeMillis() - updatedTime.getTime() < updateInterval){
				return false;
			}
		}
		if(!Network.isConnected(mContext)){
			Toast.makeText(mContext, R.string.no_network, Toast.LENGTH_SHORT).show();
			return false;
		}
		else{
			mWebUpdaterToDB = new UpdatePostsListToDatabase(mandatorily);
			mWebUpdaterToDB.execute();
			return true;
		}
	}

	/**
	 * 自动更新通知。自动判断是否正在更新、网络连通性，自动确认更新间隔。
	 * @return 若开始（正在）更新返回true；若已禁止自动更新或不足更新间隔时间而没有更新，返回false
	 */
	public boolean updatePostsAutomatically(){
		return updatePosts(false);
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
	//TODO stop未实现
	public void stop(){
		if(mWebUpdaterToDB != null){
			mWebUpdaterToDB.cancel(false);
		}
	}

	public interface OnPostUpdateListener{
		/**
		 * 当异步线程更新完通知后，被回调
		 * @param numberOfInsertedPosts 如果更新了，返回更新的通知条数；如果没有更新，返回-1
		 * @param mandatorily true表示这是手动强制更新(不管上次更新时间)触发的更新；false表示这是自动更新触发的更新
		 */
		public void onPostUpdate(long numberOfInsertedPosts, boolean mandatorily);
	}

	private class UpdatePostsListToDatabase extends AsyncTask<Void, Void, Long>{
		private static final String hessianUrl = "http://schoolwebpageparser.appspot.com/getter";
		private static final int maxAttempts = 10;

		private StudentInfDBAdapter database = new StudentInfDBAdapter(mContext);
		/** true表示强制更新，不管上次更新时间；false表示这是自动更新，仅当符合自动更新条件时（时间间隔等）才真正更新 */
		private boolean mandatorily= false;

		/**
		 * 构造方法.
		 * @param mandatorily true表示强制更新，不管上次更新时间；false表示这是自动更新，仅当符合自动更新条件时（时间间隔等）才真正更新
		 */
		public UpdatePostsListToDatabase(boolean mandatorily){
			this.mandatorily = mandatorily;
		}

		@Override
		protected Long doInBackground(Void... params) {
			long result = updatePosts(mandatorily);
			end();
			return result;
		}

		@Override
		protected void onPostExecute(Long numberOfInsertedPosts) {
			if(mOnPostUpdateListener == null){
				if(numberOfInsertedPosts > 0)
					Toast.makeText(mContext, mContext.getResources().getString(R.string.has_updated_posts, numberOfInsertedPosts), Toast.LENGTH_SHORT).show();
			}else
				mOnPostUpdateListener.onPostUpdate(numberOfInsertedPosts, mandatorily);
		}

		@Override
		protected void onCancelled(Long numberOfInsertedPosts) {
			end();
		}

		private void end(){
			if(database != null)
				database.close();
			mWebUpdaterToDB = null;
		}

		/**
		 * 取得最新通知的发布日期
		 * @return 如果数据库中有通知，返回最新通知的发布日期；如果数据库中无通知或遇到其他异常，返回null
		 */
		private Date getTimeOfLatestPost(){
			Date result = null;
			try{
				if(!database.isOpen())
					database.open();
				Post lastPost = database.getPostsFromDB(null, Contract.Posts.COLUMN_NAME_DATE + " DESC", "1").get(0);
				result = lastPost.getDate();
			} catch (SQLiteException e){
				Log.e(TAG, "打开数据库异常！");
				e.printStackTrace();
			} catch (SQLException e){
				Log.e(TAG, "从数据库读取最新Post时遇到异常！");
				e.printStackTrace();
			}
			return result;
		}

		private void refreshLastUpdatedTime(long updateInterval) {
			if(lastUpdatedTime != null)
				return;
			Date updatedTime = getLastUpdatedTimeFromSharedPreferences();
			if(updatedTime==null || System.currentTimeMillis() - updatedTime.getTime() >= updateInterval){
				Date lastPostTime = getTimeOfLatestPost();
				if((updatedTime==null) || (lastPostTime!=null && lastPostTime.compareTo(updatedTime)>0))
					updatedTime = lastPostTime;
			}
			lastUpdatedTime = updatedTime;
		}

		/**
		 * 更新通知
		 * @param quickUpdateOrAllUpdate true for 快速（常用）更新；false for 完整更新
		 * @return 成功更新，返回更新的通知条数；更新失败，返回-1
		 */
		private long doUpdatePosts(boolean quickUpdateOrAllUpdate){
			List<Post> posts = null;
			long numberOfInsertedPosts = -1;

			//准备用Hessian连接GAE代理
			//TODO 去掉timeout常量
			int timeout = quickUpdateOrAllUpdate?15000:50000;
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

			if(posts == null){
				// 检查是否正使用移动网络，在移动网络下是否可使用备用方案
				if(SettingsActivity.useAlternativeInMobileConnection(mContext) || !Network.isMobileConnected(mContext)){
					//使用备用方案
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
			}

			//把新通知写入数据库
			if(posts != null){
				if(posts.isEmpty())
					return 0;
				try{
					if(!database.isOpen())
						database.open();
					numberOfInsertedPosts = database.savePosts(posts);
					//更新 lastUpdatedTime
					lastUpdatedTime = new Date();
					saveLastUpdateTimeToSharedPreferences();
				} catch (SQLiteException e){
					Log.e(TAG, "打开数据库异常！");
					e.printStackTrace();
				} catch (SQLException e){
					Log.e(TAG, "向数据库更新Posts时遇到异常！");
					e.printStackTrace();
				}
			}else{
				Log.i(TAG, "无新通知");
			}
			return numberOfInsertedPosts;
		}

		/**
		 * 更新通知，刷新lastUpdatedTime。自动判断更新间隔。
		 * @param mandatorily true表示强制更新，不管上次更新时间；false表示这是自动更新，仅当符合自动更新条件时（时间间隔等）才真正更新
		 * @return 如果更新了，返回更新的通知条数；如果没有更新，返回-1
		 */
		private long updatePosts(boolean mandatorily){
			long numberOfInsertedPosts = -1;

			//更新时间
			long now = System.currentTimeMillis();
			long updateInterval = SettingsActivity.getIntervalOfPostUpdating(mContext);
			long longUpdateInterval = updateInterval * 8L;
			refreshLastUpdatedTime(updateInterval);
			long lastUpdated = lastUpdatedTime!=null ? lastUpdatedTime.getTime() : 0;
			//根据时间间隔更新
			if(now - lastUpdated > longUpdateInterval){
				if(lastUpdated == 0){
					Log.w(TAG, "第一次更新，费较大流量");
					//警告
				}
				numberOfInsertedPosts = doUpdatePosts(false);
			}else if(mandatorily || (now - lastUpdated >= updateInterval)){
				numberOfInsertedPosts = doUpdatePosts(true);
			}
			return numberOfInsertedPosts;
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
