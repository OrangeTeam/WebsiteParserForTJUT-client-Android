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

public class Network {
	/**
	 * {@link #openNoConnectionDialog(FragmentActivity)}打开的{@link DialogFragment}的tag
	 */
	public static final String FRAGMENT_TAG_NO_CONNECTION_DIALOG =
			Network.class.getName() + "noConnectionDialog";
	/**
	 * 禁止实例化（因为本类只有静态方法，实例无用）
	 */
	private Network(){}

	/**
	 * 根据{@link Context}取得{@link ConnectivityManager}
	 * @param context 应用程序环境
	 * @return {@code context.getSystemService(Context.CONNECTIVITY_SERVICE)}
	 */
	public static ConnectivityManager getConnectivityManager(Context context){
		return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	/**
	 * 取得当前正在使用的网络类型。
	 * @return null for no Internet connection or one of TYPE_MOBILE, TYPE_WIFI, TYPE_WIMAX,
	 * TYPE_ETHERNET, TYPE_BLUETOOTH, or other types defined by ConnectivityManager
	 */
	public static Integer getActiveNetworkType(Context context){
		NetworkInfo networkInfo = getConnectivityManager(context).getActiveNetworkInfo();
		return networkInfo==null||!networkInfo.isConnected() ? null : networkInfo.getType();
	}
	/**
	 * 是否可以建立Internet连接。
	 * @return 可以返回true；不可能建立返回false
	 */
	public static boolean isConnected(Context context){
		return getActiveNetworkType(context) != null;
	}
	/**
	 * 是否正在通过{@link ConnectivityManager#TYPE_MOBILE 移动网络}联网。
	 * @return 是返回true；不是返回false
	 */
	public static boolean isMobileConnected(Context context){
		Integer type = getActiveNetworkType(context);
		return type!=null && type==ConnectivityManager.TYPE_MOBILE;
	}
	/**
	 * 是否正在通过{@link ConnectivityManager#TYPE_WIFI WiFi}联网。
	 * @return 是返回true；不是返回false
	 */
	public static boolean isWifiConnected(Context context){
		Integer type = getActiveNetworkType(context);
		return type!=null && type==ConnectivityManager.TYPE_WIFI;
	}

	/**
	 * 打开网络设置Activity。
	 * @return 成功打开返回true，失败返回false
	 * @see android.provider.Settings#ACTION_WIRELESS_SETTINGS
	 */
	public static boolean openWirelessSettings(Context context){
		Intent intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
		// 验证此intent可被响应
		if(context.getPackageManager().queryIntentActivities(intent, 0).size() > 0){
			context.startActivity(intent);
			return true;
		}else
			return false;
	}

	/**
	 * 打开“无网络连接”对话框，提问是否打开网络设置Activity。
	 * @param fragmentActivity 要弹出对话框的支持包{@link FragmentActivity}
	 */
	public static void openNoConnectionDialog(FragmentActivity fragmentActivity){
		NoConnectionDialogFragment newDialog = new NoConnectionDialogFragment();
		newDialog.show(fragmentActivity.getSupportFragmentManager(),
				FRAGMENT_TAG_NO_CONNECTION_DIALOG);
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
					openWirelessSettings(getActivity());
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
