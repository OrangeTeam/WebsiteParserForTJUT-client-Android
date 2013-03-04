package org.orange.querysystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import org.orange.querysystem.R.drawable;
import org.orange.querysystem.content.AccountSettingPreference;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {
	public static final String KEY_PREF_ACCOUNT = "pref_account";
	public static final String KEY_PREF_ACCOUNT_STUDENT_ID =
			KEY_PREF_ACCOUNT + AccountSettingPreference.STUDENT_ID_SUFFIX;
	public static final String KEY_PREF_ACCOUNT_PASSWORD =
			KEY_PREF_ACCOUNT + AccountSettingPreference.PASSWORD_SUFFIX;
	public static final String KEY_PREF_SCHOOL_STARTING_DATE = "pref_startingDate";

	@TargetApi(11)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		getListView().setBackgroundResource(R.drawable.back);
		getListView().setCacheColorHint(Color.TRANSPARENT);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar mActionBar = getActionBar();
			mActionBar.setTitle(R.string.settings);
			//横屏时，为节省空间隐藏ActionBar
			if(getResources().getConfiguration().orientation ==
					android.content.res.Configuration.ORIENTATION_LANDSCAPE)
				mActionBar.hide();
		}
		
		
		//导入静态数据库
		importInitialDB();
	}

	/**
	 * 取得指定周的周一。以周一为一周的开始。
	 * @param milliseconds 目标周中的一个时间戳，单位ms
	 * @return milliseconds所在周的周一。小时、分、秒、毫秒都为0
	 */
	public static Calendar getMondayOfWeek(long milliseconds) {
		Calendar result = Calendar.getInstance();
		result.setFirstDayOfWeek(Calendar.MONDAY);
		result.setTimeInMillis(milliseconds);
		result.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		result.set(Calendar.HOUR_OF_DAY, 0);
		result.set(Calendar.MINUTE, 0);
		result.set(Calendar.SECOND, 0);
		result.set(Calendar.MILLISECOND, 0);
		return result;
	}
	/**
	 * 取得第0周的星期一
	 * @param context 上下文环境
	 * @return 如果设置过开学日期，返回第0周的星期一；如果尚没设置开学时间，返回null
	 */
	public static Calendar getMondayOfZeroWeek(Context context) {
		long schoolStarting = PreferenceManager.getDefaultSharedPreferences(context)
				.getLong(SettingsActivity.KEY_PREF_SCHOOL_STARTING_DATE, -1);
		if(schoolStarting == -1)
			return null;
		else
			return getMondayOfWeek(schoolStarting);
	}

	/**
	 * 返回现在距指定时间的周数
	 * @param timestampOfZeroWeek 起始时间戳，单位ms
	 * @return 现在距指定时间的周次
	 */
	public static int getCurrentWeekNumber(long timestampOfZeroWeek) {
		return (int)((new Date().getTime() - timestampOfZeroWeek) / (7L * 24 * 60 * 60 * 1000));
	}
	/**
	 * 取得当前周次。以周一为一周的开始
	 * @param context 上下文环境
	 * @return 如果设置过开学时间，返回当前周次；如果尚没设置开学时间，返回null
	 */
	public static Integer getCurrentWeekNumber(Context context) {
		Calendar c = getMondayOfZeroWeek(context);
		if(c == null)
			return null;
		else
			return getCurrentWeekNumber(c.getTimeInMillis());
	}
	/**
	 * 取得账号的学号
	 * @param context 上下文环境
	 * @return 如果设置过学号，返回此学号；如果尚没设置学号，返回null
	 */
	public static String getAccountStudentID(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString(KEY_PREF_ACCOUNT_STUDENT_ID, null);
	}
	/**
	 * 取得账号的密码
	 * @param context 上下文环境
	 * @return 如果设置过密码，返回此密码；如果尚没设置密码，返回null
	 */
	public static String getAccountPassword(Context context){
		try {
			return AccountSettingPreference.decode(
					PreferenceManager.getDefaultSharedPreferences(context)
					.getString(KEY_PREF_ACCOUNT_STUDENT_ID, null),
					PreferenceManager.getDefaultSharedPreferences(context)
					.getString(KEY_PREF_ACCOUNT_PASSWORD, null)
					);
		} catch (Exception e) {
		}
		return null;
	}
	
	/**
	 * 导入静态数据库
	 */
	private void importInitialDB(){
        if(getAccountStudentID(this) == null){
            DBManager manager = new DBManager();
    		try {
    			manager.openHelper(this);
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
        }
	}
	
	private class DBManager{
		protected void openHelper(Context context) throws IOException{
			 String dbDirPath = "/data/data/org.orange.querysystem/databases";
			 File dbDir = new File(dbDirPath);
			 if(!dbDir.exists())
				 dbDir.mkdir();
			 InputStream is = context.getResources().openRawResource(R.raw.studentinf);
			 FileOutputStream os = new FileOutputStream(dbDirPath+"/studentInf.db");
			 byte[] buffer = new byte[1024];
			 int count = 0;
			 while ((count = is.read(buffer)) > 0) {    os.write(buffer, 0, count);  }  
			 is.close();
			 os.close();
		}
	}
}
