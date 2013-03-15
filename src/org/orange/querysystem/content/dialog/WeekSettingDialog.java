package org.orange.querysystem.content.dialog;

import org.orange.querysystem.R;

import util.BitOperate.BitOperateException;
import util.webpage.Course.TimeAndAddress;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class WeekSettingDialog extends DialogFragment{
	/** {@link WeekSettingDialog#setArguments(Bundle)}参数的Key */
	public static final String WEEK_KEY = WeekSettingDialog.class.getName() + "week_key";

	/** 课程多选框的选项 */
	private static final String[] MULTI_CHOICE_ITEMS = new String[21];
	static {
		for(int i=0 ; i<=20 ; i++)
			MULTI_CHOICE_ITEMS[i] = String.valueOf(i);
	}
	private final TimeAndAddress mWeek = new TimeAndAddress();
	private WeekSettingDialogListener mListener;

	/**
	 * 构造此{@link DialogFragment}的参数
	 * @param week 待设置的星期数。见{@link TimeAndAddress#getWeek()}
	 * @return {@link WeekSettingDialog#setArguments(Bundle)}可用的参数
	 * @see TimeAndAddress#setWeek(int)
	 */
	public static Bundle buildArgument(int week){
		Bundle args = new Bundle();
		args.putInt(WEEK_KEY, week);
		return args;
	}
	/**
	 * 创建此类的新实例。
	 * @param week 待设置的星期数。见{@link TimeAndAddress#getWeek()}
	 * @return 此类的新实例
	 * @see TimeAndAddress#setWeek(int)
	 */
	public static WeekSettingDialog newInstance(int week){
		WeekSettingDialog instance = new WeekSettingDialog();
		instance.setArguments(buildArgument(week));
		return instance;
	}
	/**
	 * 应用通过{@link WeekSettingDialog#setArguments(Bundle)}传递过来的参数。即初始周数状态
	 */
	public void applyArguments(){
		try {
			//TODO 常量
			mWeek.setWeek(getArguments().getInt(WEEK_KEY, 0));
		} catch (BitOperateException e) {
			throw new IllegalArgumentException("非法参数"+getArguments().getInt(WEEK_KEY, 0), e);
		}
	}

	/**
	 * 取得当前设置的Week
	 * @return 当前的week设置状态
	 */
	public int getWeek(){
		return mWeek.getWeek();
	}
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Object host = getParentFragment();
		if(host == null)
			host = activity;
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the WeekSettingDialog so we can send events to the host
			mListener = (WeekSettingDialogListener) host;
		} catch (ClassCastException e) {
			// The parent doesn't implement the interface, throw exception
			throw new ClassCastException(host.toString()
					+ " must implement NoticeDialogListener");
		}
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		applyArguments();
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
			.setTitle(R.string.week_number)
			.setMultiChoiceItems(MULTI_CHOICE_ITEMS, mWeek.getWeekByBooleanArray(), new OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					try {
						if(isChecked)
							mWeek.addWeek(which);
						else
							mWeek.removeWeek(which);
					} catch (BitOperateException e) {
						throw new IllegalArgumentException("非法参数" + which, e);
					}
				}
			})
			.setPositiveButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mListener.onDialogPositiveClick(WeekSettingDialog.this, getWeek());
				}
			})
			.setNegativeButton(android.R.string.cancel, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					applyArguments();
					mListener.onDialogNegativeClick(WeekSettingDialog.this);
				}
			})
			.create();
	}
	/** The activity that creates an instance of this {@link WeekSettingDialog} must
	 * implement this interface in order to receive event callbacks.
	 * Each method passes the {@link WeekSettingDialog} in case the host needs to query it.
	 */
	public static interface WeekSettingDialogListener {
		/**
		 * 当用户确认周数设置后调用
		 * @param dialog 触发此调用的{@link DialogFragment}
		 * @param week 设置后的week值
		 */
		public void onDialogPositiveClick(WeekSettingDialog dialog, int week);
		public void onDialogNegativeClick(WeekSettingDialog dialog);
	}
}
