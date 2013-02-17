package org.orange.querysystem.util;

import org.orange.querysystem.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class Network {
	private static final String DEBUG_TAG = "NetworkUtility";
	private static Network instance;
	private static Context mContext = null;
	private static ConnectivityManager mConnectivityManager;


	private Network(Context context){
		mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		mContext = context;
		Log.d(DEBUG_TAG, "新Network工具类");
	}
	/**
	 * 取得{@link Network 本类}的的实例。会尽量返回之前创建过的实例。
	 * @param context 上下文环境
	 * @return {@link Network 本类}的的实例
	 */
	public static Network getInstance(Context context){
		if(mContext == null || mContext != context){
			instance = new Network(context);
		}
		return instance;
	}

	/**
	 * 取得当前正在使用的网络类型。
	 * @return null for no Internet connection or one of TYPE_MOBILE, TYPE_WIFI, TYPE_WIMAX,
	 * TYPE_ETHERNET, TYPE_BLUETOOTH, or other types defined by ConnectivityManager
	 */
	public Integer getActiveNetworkType(){
		NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
		return networkInfo==null||!networkInfo.isConnected() ? null : networkInfo.getType();
	}
	/**
	 * 是否可以建立Internet连接。
	 * @return 可以返回true；不可能建立返回false
	 */
	public boolean isConnected(){
		return getActiveNetworkType() != null;
	}
	/**
	 * 是否正在通过{@link ConnectivityManager#TYPE_MOBILE 移动网络}联网。
	 * @return 是返回true；不是返回false
	 */
	public boolean isMobileConnected(){
		Integer type = getActiveNetworkType();
		return type!=null && type==ConnectivityManager.TYPE_MOBILE;
	}
	/**
	 * 是否正在通过{@link ConnectivityManager#TYPE_WIFI WiFi}联网。
	 * @return 是返回true；不是返回false
	 */
	public boolean isWifiConnected(){
		Integer type = getActiveNetworkType();
		return type!=null && type==ConnectivityManager.TYPE_WIFI;
	}

	/**
	 * 打开网络设置Activity。
	 * @return 成功打开返回true，失败返回false
	 * @see android.provider.Settings#ACTION_WIRELESS_SETTINGS
	 */
	public boolean openWirelessSettings(){
		Intent intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
		// 验证此intent可被响应
		if(mContext.getPackageManager().queryIntentActivities(intent, 0).size() > 0){
			mContext.startActivity(intent);
			return true;
		}else
			return false;
	}

	/**
	 * 打开“无网络连接”对话框，提问是否打开网络设置Activity。
	 * @param fragmentActivity 要弹出对话框的支持包{@link FragmentActivity}
	 */
	public void openNoConnectionDialog(FragmentActivity fragmentActivity){
		NoConnectionDialogFragment newDialog = new NoConnectionDialogFragment();
		newDialog.show(fragmentActivity.getSupportFragmentManager(), "noConnectionDialog");
	}

	/**
	 * “无网络连接”对话框。提问是否打开网络设置Activity。
	 * @author Bai Jie
	 */
	public static class NoConnectionDialogFragment extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.no_connection_title)
			.setMessage(R.string.no_connection_message)
			.setPositiveButton(android.R.string.yes, new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Network.getInstance(getActivity()).openWirelessSettings();
				}
			})
			.setNegativeButton(android.R.string.cancel, new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});
			return builder.create();
		}

	}
}
