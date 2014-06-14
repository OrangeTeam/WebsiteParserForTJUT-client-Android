/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orange.querysystem;

import org.orange.parser.entity.Course;
import org.orange.querysystem.CoursesInThisWeekActivity.IncorrectIdOrPasswordDialogFragment;
import org.orange.querysystem.content.ListScoresFragment;
import org.orange.querysystem.content.ListScoresFragment.SimpleScore;
import org.orange.querysystem.content.RefreshScoresFragmentActivity;
import org.orange.querysystem.content.TabsAdapter;
import org.orange.querysystem.util.Network;
import org.orange.querysystem.util.ReadDBForScores;
import org.orange.querysystem.util.ReadDBForScores.OnPostExcuteListerner;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScoresActivity extends FragmentActivity implements OnPostExcuteListerner {

    private static final String KEY_AUTHENTICATED = "authenticated";

    private static final String KEY_CURRENT_TAB = "current_tab";

    private static final int REQUEST_UPDATE_COURSES_FROM_NETWORK = 1;

    private static final int DIALOG_NO_COURSES_IN_DATABASE = 1;

    private static final int DIALOG_INCORRECT_ID_OR_PASSWORD = 2;

    private TabHost mTabHost;

    private ViewPager mViewPager;

    private TabsAdapter mTabsAdapter;

    private boolean authenticated;

    private String currentTab;

    public static final int PASSWORD_PROMPT = 1;

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @TargetApi(11)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_tabs_pager);

        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();

        mViewPager = (ViewPager) findViewById(R.id.pager);

        mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);

        //3.0以上版本，使用ActionBar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //去掉低版本使用的Title
            findViewById(R.id.currentTime).setLayoutParams(new LinearLayout.LayoutParams(0, 0));
            ActionBar mActionBar = getActionBar();
            mActionBar.setTitle(R.string.transcripts);
            //横屏时，为节省空间隐藏ActionBar
            if (getResources().getConfiguration().orientation ==
                    android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                mActionBar.hide();
            }
        } else {
            //低版本使用的Title
            ((TextView) findViewById(R.id.currentTime)).setText(R.string.transcripts);
        }

        if (savedInstanceState == null) {
            authenticated = !SettingsActivity.requestPasswordForPrivateInformation(this);
            if (authenticated) {
                enterActivity();
            } else {
                showDialog(PASSWORD_PROMPT);
            }
        } else {
            authenticated = savedInstanceState.getBoolean(KEY_AUTHENTICATED, false);
            if (authenticated) {
                currentTab = savedInstanceState.getString(KEY_CURRENT_TAB);
                enterActivity();
            }
        }
    }

    public void enterActivity() {
        readDB();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PASSWORD_PROMPT:
                final TextView textView = new TextView(this);
                textView.setText("请输入登录密码：");
                textView.setTextSize(14);
                textView.setId(1);
                final EditText editText = new EditText(this);
                editText.setId(2);
                editText.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
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
                                                .getAccountPassword(ScoresActivity.this))) {
                                            authenticated = true;
                                            enterActivity();
                                        } else {
                                            editText.setText("");
                                            Toast.makeText(ScoresActivity.this, "密码输入错误，请重试！！",
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
                        )
                        .setOnKeyListener(new OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface dialog, int keyCode,
                                    KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_BACK) {
                                    finish();
                                    return true;
                                }
                                return false;
                            }
                        })
                        .create();

            default: return null;
        }
    }

    public void readDB() {
        new ReadDBForScores(this, this).execute(SettingsActivity.getAccountStudentID(this));
    }

    @Override
    public void onPostReadFromDBForScores(Map<Integer, Map<Integer, List<Course>>> courses) {
        if (courses != null) {
            showScoresInfo(courses);
            if (currentTab != null) {
                mTabHost.setCurrentTabByTag(currentTab);
            }
        } else {
            showDialogFragment(DIALOG_NO_COURSES_IN_DATABASE);
        }
    }

    public void showScoresInfo(Map<Integer, Map<Integer, List<Course>>> courses) {
        mTabsAdapter.clear();

        ArrayList<Bundle> args = new ArrayList<Bundle>(7);
        //TODO 下边Map遍历的顺序无保证
        for (Map<Integer, List<Course>> coursesInAYear : courses.values()) {
            for (List<Course> coursesInASemester : coursesInAYear.values()) {
                if (coursesInASemester.get(0).getYear() == 0)//去除本学年成绩未出就会显示在成绩单中的情况
                {
                    continue;
                }
                ArrayList<SimpleScore> scores = new ArrayList<SimpleScore>();
                for (Course course : coursesInASemester) {
                    scores.add(new SimpleScore(course.getId(), course.getName(),
                            course.getTestScore(), course.getTotalScore(),
                            course.getGradePoint(), course.getCredit(), course.getKind()));
                }
                Bundle arg = new Bundle();
                arg.putParcelableArrayList(ListScoresFragment.SCORES_KEY, scores);
                args.add(arg);
            }
        }
        int counter = 1;
        for (Bundle arg : args) {
            TabSpec tabSpec = mTabHost.newTabSpec(counter + "学期");
            mTabsAdapter.addTab(tabSpec.setIndicator((counter++) + "学期"),
                    ListScoresFragment.class, arg);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CURRENT_TAB, mTabHost.getCurrentTabTag());
        outState.putBoolean(KEY_AUTHENTICATED, authenticated);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.refresh);
        menu.add(0, 2, 2, R.string.settings);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == 1) {
            if (Network.isConnected(this)) {
                startActivityForResult(new Intent(this, RefreshScoresFragmentActivity.class),
                        REQUEST_UPDATE_COURSES_FROM_NETWORK);
            } else {
                Network.openNoConnectionDialog(this);
            }
        } else if (item.getItemId() == 2) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_UPDATE_COURSES_FROM_NETWORK) {
            switch (resultCode) {
                case RefreshScoresFragmentActivity.RESULT_OK:
                    readDB();
                    break;
                case RefreshScoresFragmentActivity.RESULT_CANNOT_LOGIN:
                case RefreshScoresFragmentActivity.RESULT_NO_STUDENT_ID_OR_PASSWORD:
                    showDialogFragment(DIALOG_INCORRECT_ID_OR_PASSWORD);
                    break;
            }
        }
    }

    private void showDialogFragment(int dialogCode) {
        switch (dialogCode) {
            case DIALOG_NO_COURSES_IN_DATABASE:
                new NoCoursesDialogFragment().show(getSupportFragmentManager(),
                        "NoCoursesInDatabaseDialog");
                break;
            case DIALOG_INCORRECT_ID_OR_PASSWORD:
                new IncorrectIdOrPasswordDialogFragment().show(getSupportFragmentManager(),
                        "IncorrectIdOrPasswordDialog");
                break;
            default:
                throw new IllegalArgumentException("非法参数：" + dialogCode);
        }
    }

    public static class NoCoursesDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.no_courses_in_database_dialog_title)
                    .setMessage(R.string.no_courses_in_database_dialog_message)
                    .setPositiveButton(R.string.no_courses_in_database_dialog_positive,
                            new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().startActivityForResult(
                                            new Intent(getActivity(),
                                                    RefreshScoresFragmentActivity.class),
                                            REQUEST_UPDATE_COURSES_FROM_NETWORK
                                    );
                                }
                            }
                    )
                    .setNegativeButton(R.string.no_courses_in_database_dialog_negative, null)
                    .create();
        }
    }
}
