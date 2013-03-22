package org.orange.querysystem.content;

import java.io.File;

import org.orange.querysystem.R;
import org.orange.querysystem.util.Crypto;

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
		try {
			password.setText(decode(studentID.getText().toString(), pref.getString(getKey()+PASSWORD_SUFFIX, "")));
		} catch (Exception e) {
		}
		super.onBindDialogView(view);
	}
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// When the user selects "OK", persist the new value
		if (positiveResult) {
			deleteStudentInf();
			SharedPreferences.Editor editor = getEditor();
			editor.putString(getKey()+STUDENT_ID_SUFFIX, studentID.getText().toString());
			try {
				editor.putString(getKey()+PASSWORD_SUFFIX, encode(studentID.getText().toString(), password.getText().toString()));
			} catch (Exception e) {
			}
			editor.commit();
		}
	}
	
	/**
	 * 当用户账号设置成功时就删除前一个用户的学生信息
	 */
	private void deleteStudentInf(){
		File imageFile = new File("data/data/org.orange.querysystem/files/", "student_image.jpg");
		imageFile.delete();
		File fileObject = new File("data/data/org.orange.querysystem/files/", "student_info.txt");
		fileObject.delete();
	}
	//TODO 加密
	private static String encode(String seed, String plaintext) throws Exception{
		String encryptingCode = Crypto.encrypt(seed, plaintext);
		return encryptingCode;
	}
	//TODO 解密
	public static String decode(String seed, String ciphertext) throws Exception{
		String decryptingCode = Crypto.decrypt(seed, ciphertext);
		return decryptingCode;
	}

}
