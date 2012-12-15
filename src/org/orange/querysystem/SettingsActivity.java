package org.orange.querysystem;

import java.util.Calendar;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {
	public static final String KEY_PREF_SCHOOL_STARTING_DATE = "pref_startingDate";

	@TargetApi(11)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar mActionBar = getActionBar();
			mActionBar.setTitle(R.string.settings);
			//横屏时，为节省空间隐藏ActionBar
			if(getResources().getConfiguration().orientation ==
					android.content.res.Configuration.ORIENTATION_LANDSCAPE)
				mActionBar.hide();
		}
	}

	/**
	 * 取得指定周的周一
	 * @param milliseconds 目标周中的一个时间戳，单位ms
	 * @return milliseconds所在周的周一。小时、分、秒、毫秒都为0
	 */
	public static Calendar getMondayOfWeek(long milliseconds) {
		Calendar result = Calendar.getInstance();
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
}
