package org.orange.querysystem.content;

import org.orange.querysystem.R;
import org.orange.querysystem.util.Crypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        studentID = (EditText) view.findViewById(R.id.student_id);
        password = (EditText) view.findViewById(R.id.password);

        SharedPreferences pref = getSharedPreferences();
        String userid = pref.getString(getKey() + STUDENT_ID_SUFFIX, null);
        studentID.setText(userid);
        if (pref.contains(getKey() + PASSWORD_SUFFIX)) {
            password.setText(decrypt(getStoragePassword(userid),
                    pref.getString(getKey() + PASSWORD_SUFFIX, null)));
        }

        super.onBindDialogView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        if (positiveResult) {
            deleteStudentInf();
            SharedPreferences.Editor editor = getEditor();
            String username = studentID.getText().toString();
            String password = this.password.getText().toString();
            editor.putString(getKey() + STUDENT_ID_SUFFIX, username);
            editor.putString(getKey() + PASSWORD_SUFFIX,
                    encrypt(getStoragePassword(username), password));
            editor.commit();
        }
    }

    /**
     * 当用户账号设置成功时就删除前一个用户的学生信息
     */
    private void deleteStudentInf() {
        File imageFile = new File("data/data/org.orange.querysystem/files/", "student_image.jpg");
        imageFile.delete();
        File fileObject = new File("data/data/org.orange.querysystem/files/", "student_info.txt");
        fileObject.delete();
    }

    /**
     * 把plaintext加密为密文
     */
    private static String encrypt(String password, String plaintext) {
        return Crypto.encrypt(plaintext, password);
    }

    /**
     * 把{@code ciphertext}解密为明文
     *
     * @param password   解密密钥（暂时为账户ID）
     * @param ciphertext 待解密的密文
     * @return 解密得到的明文
     */
    public static String decrypt(String password, String ciphertext) {
        return Crypto.decrypt(ciphertext, password);
    }

    /**
     * @param userName 可以是null或空字符串""
     */
    public static String getStoragePassword(String userName) {
        if (userName == null || userName.length() < 3) {
            userName = "HhAkM5BpDFtMByffteLgkkzq9HFUtVueynFRjk5zMJkt9" +
                    "CN82s8jGvjAww5AdsqL2mvAj3E3b8bX8pXbrRLsuSeq23jwgdLEzmMMsaWTJVd4HcXjcHCDged6";
        }
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
            md.update(userName.getBytes("UTF-8"));
            byte[] digest = md.digest();
            return new String(digest, "UTF-8");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return userName;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
