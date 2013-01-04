package org.orange.querysystem.content;

import org.orange.querysystem.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class AccountSettingPreference extends DialogPreference {
	public static final String STUDENT_ID_SUFFIX = "_student_id";
	public static final String PASSWORD_SUFFIX = "_password";

	EditText studentID;
	EditText password;

	public AccountSettingPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPreference(context, attrs);
	}
	public AccountSettingPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPreference(context, attrs);
	}
	private void initPreference(Context context, AttributeSet attrs) {
		this.setDialogLayoutResource(R.layout.account_setting_dialog);
	}
	@Override
	protected void onBindDialogView(View view) {
		studentID = (EditText)view.findViewById(R.id.student_id);
		password = (EditText)view.findViewById(R.id.password);

		SharedPreferences pref = getSharedPreferences();
		studentID.setText(pref.getString(getKey()+STUDENT_ID_SUFFIX, ""));
		password.setText(decode(pref.getString(getKey()+PASSWORD_SUFFIX, "")));
		super.onBindDialogView(view);
	}
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// When the user selects "OK", persist the new value
		if (positiveResult) {
			SharedPreferences.Editor editor = getEditor();
			editor.putString(getKey()+STUDENT_ID_SUFFIX, studentID.getText().toString());
			editor.putString(getKey()+PASSWORD_SUFFIX, encode(password.getText().toString()));
			editor.commit();
		}
	}
	//TODO 加密
	private static String encode(String plaintext){
		return plaintext;
	}
	//TODO 解密
	public static String decode(String ciphertext){
		return ciphertext;
	}

}
