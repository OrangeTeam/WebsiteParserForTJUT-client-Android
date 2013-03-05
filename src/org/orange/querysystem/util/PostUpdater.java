/**
 * 
 */
package org.orange.querysystem.util;

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.List;

import org.orange.querysystem.SettingsActivity;
import org.orange.studentinformationdatabase.Contract;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.GetterInterface;
import util.webpage.Post;
import util.webpage.ReadPageHelper.OnReadPageListener;
import util.webpage.SchoolWebpageParser;
import android.content.Context;
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
	 * @return 若开始（正在）更新返回true；若已禁止自动更新或不足更新间隔时间而没有更新，返回false 
	 */
	public boolean autoUpdatePosts(){
		if(mWebUpdaterToDB != null)
			return true;
		if(!SettingsActivity.isUpdatePostAutomatically(mContext))
			return false;
		long updateInterval = SettingsActivity.getIntervalOfPostUpdating(mContext);
		// lastUpdatedTime==null时仍然继续，以刷新lastUpdatedTime
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

	private class UpdatePostsListToDatabase extends AsyncTask<Void, Void, Integer>{
		private static final String hessianUrl = "http://schoolwebpageparser.appspot.com/getter";
		private static final int maxAttempts = 10;
		private StudentInfDBAdapter database = new StudentInfDBAdapter(mContext);;

		@Override
		protected Integer doInBackground(Void... params) {
			return autoUpdatePosts();
		}

		@Override
		protected void onPostExecute(Integer numberOfInsertedPosts) {
			//TODO 字符串常量放资源文件
			if(numberOfInsertedPosts > 0)
				Toast.makeText(mContext, "更新了"+numberOfInsertedPosts+"条通知", Toast.LENGTH_SHORT).show();
			if(mOnPostExecuteListener != null)
				mOnPostExecuteListener.onPostExecute();
		}

		@Override
		protected void onCancelled(Integer numberOfInsertedPosts) {
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
		 * @return 更新的通知条数
		 */
		private int updatePosts(boolean quickUpdateOrAllUpdate){
			List<Post> posts = null;
			int numberOfInsertedPosts = 0;

			//准备用Hessian连接GAE代理
			//TODO 去掉timeout常量
			int timeout = quickUpdateOrAllUpdate?4000:50000;
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
				if(SettingsActivity.useAlternativeInMobileConnection(mContext) || !Network.getInstance(mContext).isMobileConnected()){
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
				try{
					database.open();
					numberOfInsertedPosts = database.autoInsertArrayPostsInf(posts);
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
				Log.i(TAG, "无新通知");
			}
			return numberOfInsertedPosts;
		}

		/**
		 * 更新通知。自动判断更新间隔、是否正在更新等。
		 * @return 更新的通知条数
		 */
		private int autoUpdatePosts(){
			int numberOfInsertedPosts = 0;

			//更新时间
			refreshLastUpdatedTime();
			long lastUpdated = 0;
			if(lastUpdatedTime != null)
				lastUpdated = lastUpdatedTime.getTime();
			long now = System.currentTimeMillis();
			long updateInterval = SettingsActivity.getIntervalOfPostUpdating(mContext);
			long longUpdateInterval = updateInterval * 8L;
			//根据时间间隔更新
			if(now - lastUpdated > longUpdateInterval){
				if(lastUpdated == 0){
					Log.w(TAG, "第一次更新，费较大流量");
					//警告
				}
				numberOfInsertedPosts = updatePosts(false);
			}else if(now - lastUpdated > updateInterval){
				numberOfInsertedPosts = updatePosts(true);
			}
			lastUpdatedTime = new Date(now);
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
