package org.orange.querysystem;

import org.orange.parser.connection.SSFWWebsiteConnectionAgent;
import org.orange.parser.parser.PersonalInformationParser;
import org.orange.querysystem.content.ListViewAdapter;
import org.orange.querysystem.util.Network;
import org.orange.querysystem.util.PersonalInformationUpdater;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class StudentInfoActivity extends ListActivity {

    private static final String KEY_AUTHENTICATED = "authenticated";

    private ListView listView;

    private ListViewAdapter adapter;

    private View showImage;

    private ImageView imageView;

    private boolean authenticated;

    public static final int PASSWORD_PROMPT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_info);

        if (savedInstanceState == null) {
            authenticated = false;
            showDialog(PASSWORD_PROMPT);
        } else {
            authenticated = savedInstanceState.getBoolean(KEY_AUTHENTICATED, false);
            if (authenticated) {
                enterActivity();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_AUTHENTICATED, authenticated);
    }

    public void enterActivity() {
        showImage = getLayoutInflater().inflate(R.layout.show_image, null);
        imageView = (ImageView) showImage.findViewById(R.id.studentImageView);

        listView = getListView();
        listView.setCacheColorHint(Color.TRANSPARENT);
        listView.addFooterView(showImage);
        initAdapter();
        setListAdapter(adapter);

        new StudentInfoFromDatabase().execute();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        switch (id) {
            case PASSWORD_PROMPT:
                final TextView textView = new TextView(this);
                textView.setText("请输入登陆密码：");
                textView.setTextSize(14);
                textView.setId(1);
                final EditText editText = new EditText(this);
                editText.setId(2);
                editText.setEnabled(true);
                editText.setCursorVisible(true);
                editText.setLongClickable(true);
                editText.setFocusable(true);
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());

                RelativeLayout.LayoutParams tvlp = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                RelativeLayout.LayoutParams etlp = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                RelativeLayout relativeLayout = new RelativeLayout(this);
                tvlp.addRule(RelativeLayout.ALIGN_BASELINE, 2);
                etlp.addRule(RelativeLayout.RIGHT_OF, 1);
                relativeLayout.addView(textView, tvlp);
                relativeLayout.addView(editText, etlp);
                return new AlertDialog.Builder(this)
                        .setView(relativeLayout)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                    /* User clicked OK so do some stuff */
                                        if (editText.getText().toString().equals(SettingsActivity
                                                .getAccountPassword(StudentInfoActivity.this))) {
                                            authenticated = true;
                                            enterActivity();
                                        } else {
                                            editText.setText("");
                                            Toast.makeText(StudentInfoActivity.this, "密码输入错误，请重试！！",
                                                    Toast.LENGTH_LONG).show();
                                            finish();
                                        }

                                    }
                                }
                        )
                        .setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                    /* User clicked cancel so do some stuff */
                                        finish();
                                    }
                                }
                        ).setOnKeyListener(new OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface dialog, int keyCode,
                                    KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_BACK) {
                                    finish();
                                    return true;
                                }
                                return false;
                            }
                        }).create();
        }
        return null;
    }

    private void initAdapter() {
        Resources res = getResources();
        ArrayList<String> items = new ArrayList<String>();
        items.add(res.getString(R.string.student_number));
        items.add(res.getString(R.string.student_name));
        items.add(res.getString(R.string.gender));
        items.add(res.getString(R.string.birthday));
        items.add(res.getString(R.string.academic_period));
        items.add(res.getString(R.string.admission_time));
        items.add(res.getString(R.string.school));
        items.add(res.getString(R.string.major));
        items.add(res.getString(R.string.class_name));
        items.add(res.getString(R.string.photo));
        adapter = new ListViewAdapter(this, items);
    }

    public Bitmap getHttpBitmap(String url) {
        URL myFileURL;
        Bitmap theBitmap = null;
        try {
            myFileURL = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) myFileURL.openConnection();
            conn.setConnectTimeout(100000);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            InputStream is = conn.getInputStream();
            theBitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {

        }
        return theBitmap;
    }

    private void storeImage(Bitmap bitmap) {
        File file = new File("data/data/org.orange.querysystem/files/");
        File imageFile = new File(file, "student_image.jpg");
        try {
            imageFile.createNewFile();
            FileOutputStream os = new FileOutputStream(imageFile);
            bitmap.compress(CompressFormat.JPEG, 50, os);
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
    }

    private Bitmap getBitmap() {
        File imageFile = new File("data/data/org.orange.querysystem/files/", "student_image.jpg");
        Bitmap theBitmap = null;
        if (imageFile.exists()) {
            try {
                theBitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));
            } catch (FileNotFoundException e) {

            }
        }
        return theBitmap;
    }

    private class StudentInfoFromWeb extends AsyncTask<Void, Void, Long> {

        private PersonalInformationUpdater mUpdater;

        public StudentInfoFromWeb(Context context, PersonalInformationParser parser) {
            mUpdater = new PersonalInformationUpdater(context, parser);
        }

        @Override
        protected Long doInBackground(Void... params) {
            return mUpdater.update();
        }

        @Override
        protected void onPostExecute(Long counter) {
            //TODO 改善
            if (counter == 0) {
                Toast.makeText(StudentInfoActivity.this, "学生信息更新失败，请点击刷新来更新信息", Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(StudentInfoActivity.this, "成功更新了 " + counter + " 条个人信息",
                        Toast.LENGTH_LONG).show();
                new StudentInfoFromDatabase().execute();
            }
        }
    }

    private class StudentInfoFromDatabase
            extends AsyncTask<Void, Void, Map<String, Map<String, String>>> {

        protected Map<String, Map<String, String>> doInBackground(Void... agrs) {
            StudentInfDBAdapter dbAdapter = new StudentInfDBAdapter(StudentInfoActivity.this);
            dbAdapter.open();
            Map<String, Map<String, String>> result = dbAdapter
                    .retrieveTwodimensionalMap(StudentInfDBAdapter.ENTITY_PERSONAL_INFORMATION);
            dbAdapter.close();
            return result;
        }

        protected void onPostExecute(Map<String, Map<String, String>> student) {
            if (student == null) {
                return;
            }
            ArrayList<String> items = new ArrayList<String>();
            for (Map.Entry<String, Map<String, String>> group : student.entrySet()) {
                String groupName = group.getKey();
                items.add(String.format("---------- %s ----------", groupName)); //TODO 这不支持多国语言
                for (Map.Entry<String, String> keyValue : group.getValue().entrySet()) {
                    items.add(keyValue.getKey() + "：" + keyValue.getValue()); //TODO 这不支持多国语言
                }
            }

//            imageView.setImageBitmap(getBitmap());
            adapter = new ListViewAdapter(StudentInfoActivity.this, items);
            setListAdapter(adapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.refresh);
        menu.add(0, 2, 2, R.string.settings);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        // TODO Auto-generated method stub\
        if (item.getItemId() == 1) {
            if (Network.isConnected(this)) {
                String username = SettingsActivity.getAccountStudentID(this);
                String password = SettingsActivity.getAccountPassword(this);
                PersonalInformationParser parser = new PersonalInformationParser();
                parser.setConnectionAgent(
                        new SSFWWebsiteConnectionAgent().setAccount(username, password));
                new StudentInfoFromWeb(this, parser).execute();
//                start_resume = 1;
//                startActivity(new Intent(this, InsertDBFragmentActivity.class));
                //TODO startActivity后不会继续运行
//                readDB();
            } else {
                Toast.makeText(this, "网络异常！请检查网络设置！", Toast.LENGTH_LONG).show();
            }
        } else if (item.getItemId() == 2) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        return super.onMenuItemSelected(featureId, item);
    }


    /**
     * 与性别相关的工具类
     */
    private static class Gender {

        private static final int unknown = 0;

        private static final int male = 1;

        private static final int female = 2;

        private Gender() {
        }

        /**
         * 取得与指定性别相对应的String资源id
         *
         * @param isMale 性别 。true表示男，false表示女，null表示未知
         * @return 相应的strings.xml中字符串资源id
         */
        public static int getStringId(Boolean isMale) {
            if (isMale == null) {
                return R.string.unknown;
            } else if (isMale) {
                return R.string.male;
            } else {
                return R.string.female;
            }
        }

        /**
         * 把性别转化为{@link Gender}中的性别代码
         *
         * @param isMale 性别 。true表示男，false表示女，null表示未知
         * @return {@link Gender}中的性别代码
         * @see #isMale(int)
         */
        public static int getGenderCode(Boolean isMale) {
            if (isMale == null) {
                return unknown;
            } else if (isMale) {
                return male;
            } else {
                return female;
            }
        }

        /**
         * 把{@link Gender}中的性别代码恢复为性别
         *
         * @param gender 从{@link #getGenderCode(Boolean)}得到的性别代码
         * @return 性别 。true表示男，false表示女，null表示未知
         * @see #getGenderCode(Boolean)
         */
        public static Boolean isMale(int gender) {
            switch (gender) {
                case male:
                    return true;
                case female:
                    return false;
                case unknown:
                    return null;
                default:
                    throw new IllegalArgumentException("非法参数：" + gender);
            }
        }
    }
}
