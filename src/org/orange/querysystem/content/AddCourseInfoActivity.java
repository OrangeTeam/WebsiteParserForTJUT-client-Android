package org.orange.querysystem.content;

import org.orange.querysystem.R;
import org.orange.querysystem.SettingsActivity;
import org.orange.querysystem.content.dialog.TimeAndAddressSettingDialog;
import org.orange.querysystem.content.dialog.TimeAndAddressSettingDialog.TimeAndAddressSettingDialogListener;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.webpage.Course;
import util.webpage.Course.TimeAndAddress;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
//TODO 测试不同生命周期状态下的正确性		现已知横竖屏转换会丢失时间地点数据
public class AddCourseInfoActivity extends FragmentActivity implements TimeAndAddressSettingDialogListener{
	/** 正在设置的课程 */
	private Course mCourse;
	private EditText course_code_input;
	private EditText course_class_number_input;
	private EditText course_teacher_input;
	private EditText course_credit_input;
	private EditText course_kind_input;
	private EditText course_test_score_input;
	private EditText course_total_score_input;
	private EditText course_name_input;
	private LinearLayout course_time_and_address_placeholder;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		//TODO 临时简单版本，以后加上getExtra
		mCourse = new Course();
        setContentView(R.layout.add_course_info);

        course_name_input = (EditText)findViewById(R.id.course_name_input);

        course_code_input = (EditText)findViewById(R.id.course_code_input);
        course_class_number_input = (EditText)findViewById(R.id.course_class_number_input);
        course_teacher_input = (EditText)findViewById(R.id.course_teacher_input);
        course_credit_input = (EditText)findViewById(R.id.course_credit_input);
        course_kind_input = (EditText)findViewById(R.id.course_kind_input);
        course_test_score_input = (EditText)findViewById(R.id.course_test_score_input);
        course_total_score_input = (EditText)findViewById(R.id.course_total_score_input);
		course_time_and_address_placeholder = (LinearLayout) findViewById(R.id.course_time_and_address_placeholder);

		//TODO 载入
		//用于输入新的时间地点的输入框
		addTimeAndAddress();

		//3.0以上版本，使用ActionBar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar mActionBar = getActionBar();
			mActionBar.setTitle(R.string.add_course);
			//横屏时，为节省空间隐藏ActionBar
			if(getResources().getConfiguration().orientation == 
					android.content.res.Configuration.ORIENTATION_LANDSCAPE)
				mActionBar.hide();
		}
	}

	public void addTimeAndAddress(){
		EditText editText = new EditText(this);
		editText.setInputType(InputType.TYPE_NULL);
		editText.setCursorVisible(false);
		editText.setLongClickable(false);
		editText.setFocusable(false);
		editText.setOnClickListener(mOnClickTimeAndAddressEditTextListener);
		course_time_and_address_placeholder.addView(editText,
				new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	}

	private final OnClickListener mOnClickTimeAndAddressEditTextListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int index = course_time_and_address_placeholder.indexOfChild(v);
			TimeAndAddress initialValue = null;
			//如果这是新时间地点，应该有index==mCourse.getTimeAndAddress().size()
			if(index != mCourse.getTimeAndAddress().size())
				initialValue = mCourse.getTimeAndAddress().get(index);
			showTimeAndAddressSettingDialog(initialValue, String.valueOf(index));
		}
	};
	public void showTimeAndAddressSettingDialog(TimeAndAddress initialTimeAndAddress, String tag){
		DialogFragment mTimeAndAddressSettingsFragment = TimeAndAddressSettingDialog.newInstance(initialTimeAndAddress, tag);
		mTimeAndAddressSettingsFragment.show(getSupportFragmentManager(), "fragment_dialog");
	}
	@Override
	public void onDialogPositiveClick(TimeAndAddressSettingDialog dialog, String tag, TimeAndAddress aTimeAndAddress) {
		System.out.println("index: "+tag);
		int index = Integer.valueOf(tag);
		((EditText)course_time_and_address_placeholder.getChildAt(index)).setText(aTimeAndAddress.toString());
		//如果这是新时间地点，应该有index==mCourse.getTimeAndAddress().size()
		if(index != mCourse.getTimeAndAddress().size()){
			mCourse.getTimeAndAddress().set(index, new TimeAndAddress(aTimeAndAddress));
		}else{	//新课程
			mCourse.getTimeAndAddress().add(new TimeAndAddress(aTimeAndAddress));
			addTimeAndAddress();
		}
	}
	@Override
	public void onDialogNegativeClick(TimeAndAddressSettingDialog dialog, String tag) {}

	public void updateCoursesListToDatabase(){
		mCourse.setCode(course_code_input.getText().toString());
		mCourse.setName(course_name_input.getText().toString());
		mCourse.setClassNumber(course_class_number_input.getText().toString());
		mCourse.setTeachers(course_teacher_input.getText().toString());
		mCourse.setKind(course_kind_input.getText().toString());
		try {
			mCourse.setCredit(Integer.parseInt(course_credit_input.getText().toString()));
		} catch (Exception e) {
			course_credit_input.requestFocus();
			//TODO 提示
			return;
		}
		try {
			mCourse.setTestScore(Float.parseFloat(course_test_score_input.getText().toString()));
		} catch (Exception e1) {
			course_test_score_input.requestFocus();
			//TODO 提示
			return;
		}
		try {
			mCourse.setTotalScore(Float.parseFloat(course_total_score_input.getText().toString()));
		} catch (Exception e) {
			course_total_score_input.requestFocus();
			//TODO 提示
			return;
		}
        String userName = SettingsActivity.getAccountStudentID(this);
		new AddCourseToDatabase().execute(mCourse, userName);
    }

    /**
     * 向数据库添加新课程。用execute(Course course, String userName)启动异步线程
     * @author ChenCheng
     */
    class AddCourseToDatabase extends AsyncTask<Object,Void,Void>{
		@Override
		protected Void doInBackground(Object... args) {
			StudentInfDBAdapter studentInfDBAdapter = new StudentInfDBAdapter(AddCourseInfoActivity.this);
			try {
				studentInfDBAdapter.open();
				studentInfDBAdapter.autoInsertCourseInf((Course)args[0], (String)args[1]);
				//此处调用的方法返回布尔值，当为true是表示成功插入了新增课程，且能显示在本学期课程项中，当为false时表示插入不成功，用户输入的课程代码在数据库中已经有了。要给用户一个提示。
				studentInfDBAdapter.updateCurrentSemesterOfAddCourseInf((Course)args[0]);
			} catch(SQLiteException e){
				e.printStackTrace();
			} 
			studentInfDBAdapter.close();
			return null;
		}
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.course_info_submit);
        
        return super.onCreateOptionsMenu(menu); 
    }
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	// TODO Auto-generated method stub\
    	if(item.getItemId() == 1){
    		Editor editor = getSharedPreferences("data", 0).edit();
			editor.putString("passMainMenu", "true");
            editor.commit();
    		updateCoursesListToDatabase();
    		finish();
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Editor editor = getSharedPreferences("data", 0).edit();
			editor.putString("passMainMenu", "true");
            editor.commit();
		}
		return super.onKeyDown(keyCode, event);
	}
}
