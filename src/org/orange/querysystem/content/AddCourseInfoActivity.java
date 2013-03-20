package org.orange.querysystem.content;

import java.util.ArrayList;

import org.orange.querysystem.R;
import org.orange.querysystem.SettingsActivity;
import org.orange.querysystem.content.dialog.TimeAndAddressSettingDialog;
import org.orange.querysystem.content.dialog.TimeAndAddressSettingDialog.TimeAndAddressSettingDialogListener;
import org.orange.querysystem.util.ParcelableTimeAndAddress;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.webpage.Course;
import util.webpage.Course.CourseException;
import util.webpage.Course.TimeAndAddress;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
//TODO 测试不同生命周期状态下的正确性
public class AddCourseInfoActivity extends FragmentActivity implements TimeAndAddressSettingDialogListener{
	private static final String KEY_INSTANCE_STATE_TIME_AND_ADDRESS =
			AddCourseInfoActivity.class.getName() + ".key_instance_state_time_and_address";
	/** 正在设置的课程 */
	private Course mCourse = new Course();
	private EditText course_code_input;
	private EditText course_class_number_input;
	private EditText course_teacher_input;
	private EditText course_credit_input;
	private EditText course_kind_input;
	private EditText course_test_score_input;
	private EditText course_total_score_input;
	private EditText course_grade_point_input;
	private EditText course_name_input;
	private LinearLayout course_time_and_address_placeholder;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_course_info);
        course_name_input = (EditText)findViewById(R.id.course_name_input);
        course_code_input = (EditText)findViewById(R.id.course_code_input);
        course_class_number_input = (EditText)findViewById(R.id.course_class_number_input);
        course_teacher_input = (EditText)findViewById(R.id.course_teacher_input);
        course_credit_input = (EditText)findViewById(R.id.course_credit_input);
        course_kind_input = (EditText)findViewById(R.id.course_kind_input);
        course_test_score_input = (EditText)findViewById(R.id.course_test_score_input);
        course_total_score_input = (EditText)findViewById(R.id.course_total_score_input);
		course_grade_point_input = (EditText)findViewById(R.id.course_grade_point_input);
		course_time_and_address_placeholder = (LinearLayout) findViewById(R.id.course_time_and_address_placeholder);

		//用于输入新的时间地点的输入框
		addTimeAndAddressEditText();

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

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		ArrayList<ParcelableTimeAndAddress> saved =
				savedInstanceState.getParcelableArrayList(KEY_INSTANCE_STATE_TIME_AND_ADDRESS);
		mCourse.getTimeAndAddress().addAll(saved);
		loadCourse();
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		ArrayList<ParcelableTimeAndAddress> saving = new ArrayList<ParcelableTimeAndAddress>();
		for(TimeAndAddress aTimeAndAddress:mCourse.getTimeAndAddress())
			saving.add(new ParcelableTimeAndAddress(aTimeAndAddress));
		outState.putParcelableArrayList(KEY_INSTANCE_STATE_TIME_AND_ADDRESS, saving);
	}

	private void loadCourse(){
		loadCourse(mCourse);
	}
	private void loadCourse(Course course){
		course_name_input.setText(course.getName());
		course_code_input.setText(course.getCode());
		course_class_number_input.setText(course.getClassNumber());
		course_teacher_input.setText(course.getTeacherString());
		course_credit_input.setText(String.valueOf(course.getCredit()));
		course_kind_input.setText(course.getKind());
		//TODO 检测方法
		if(!Float.isNaN(course.getTestScore()))
			course_test_score_input.setText(String.valueOf(course.getTestScore()));
		if(!Float.isNaN(course.getTotalScore()))
			course_total_score_input.setText(String.valueOf(course.getTotalScore()));
		try {
			course_grade_point_input.setText(String.valueOf(course.getGradePoint()));
		} catch (CourseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//添加时间地点
		removeTimeAndAddressEditText(false, course_time_and_address_placeholder.getChildCount()-1);
		for(TimeAndAddress timeAndAddress:course.getTimeAndAddress()){
			EditText added = addTimeAndAddressEditText();
			addTimeAndAddress(course_time_and_address_placeholder.indexOfChild(added), timeAndAddress);
		}
		addTimeAndAddressEditText();
	}

	/**
	 * 把时间地点{@link EditText}列表的第{@code index}个更新，并设置{@link TimeAndAddress}列表的第{@code index}个
	 * @param index 索引
	 * @param aTimeAndAddress 新（被更新的）时间地点
	 */
	private void addTimeAndAddress(int index, TimeAndAddress aTimeAndAddress){
		if(index < 0)
			throw new IllegalArgumentException("非法索引：" + index);
		((EditText)course_time_and_address_placeholder.getChildAt(index)).setText(aTimeAndAddress.toString());
		//如果这是新时间地点，应该有index==mCourse.getTimeAndAddress().size()
		if(index < mCourse.getTimeAndAddress().size()){
			mCourse.getTimeAndAddress().set(index, new TimeAndAddress(aTimeAndAddress));
		}else if(index == mCourse.getTimeAndAddress().size()){	//新课程
			mCourse.getTimeAndAddress().add(new TimeAndAddress(aTimeAndAddress));
			addTimeAndAddressEditText();
		}else
			throw new IllegalArgumentException("非法索引：" + index);
	}

	private void removeTimeAndAddressEditText(boolean withData, int index){
		course_time_and_address_placeholder.removeViewAt(index);
		if(withData)
			mCourse.getTimeAndAddress().remove(index);
	}
	/**
	 * 新增一个时间地点输入框
	 * @return 新增的时间地点{@link EditText}
	 */
	private EditText addTimeAndAddressEditText(){
		EditText editText = new EditText(this);
		editText.setInputType(InputType.TYPE_NULL);
		editText.setCursorVisible(false);
		editText.setLongClickable(false);
		editText.setFocusable(false);
		editText.setOnClickListener(mOnClickTimeAndAddressEditTextListener);
		course_time_and_address_placeholder.addView(editText,
				new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		return editText;
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
	private void showTimeAndAddressSettingDialog(TimeAndAddress initialTimeAndAddress, String tag){
		DialogFragment mTimeAndAddressSettingsFragment = TimeAndAddressSettingDialog.newInstance(initialTimeAndAddress, tag);
		mTimeAndAddressSettingsFragment.show(getSupportFragmentManager(), "fragment_dialog");
	}
	@Override
	public void onDialogPositiveClick(TimeAndAddressSettingDialog dialog, String tag, TimeAndAddress aTimeAndAddress) {
		addTimeAndAddress(Integer.valueOf(tag), aTimeAndAddress);
	}
	@Override
	public void onDialogNegativeClick(TimeAndAddressSettingDialog dialog, String tag) {}

	private void updateCoursesListToDatabase(){
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
		}
		try {
			mCourse.setTestScore(Float.parseFloat(course_test_score_input.getText().toString()));
		} catch (Exception e1) {
			course_test_score_input.requestFocus();
			//TODO 提示
		}
		try {
			mCourse.setTotalScore(Float.parseFloat(course_total_score_input.getText().toString()));
		} catch (Exception e) {
			course_total_score_input.requestFocus();
			//TODO 提示
		}
        String userName = SettingsActivity.getAccountStudentID(this);
		new AddCourseToDatabase().execute(mCourse, userName);
    }

    /**
     * 向数据库添加新课程。用execute(Course course, String userName)启动异步线程
     * @author ChenCheng
     */
	private class AddCourseToDatabase extends AsyncTask<Object,Void,Void>{
		@Override
		protected Void doInBackground(Object... args) {
			StudentInfDBAdapter studentInfDBAdapter = new StudentInfDBAdapter(AddCourseInfoActivity.this);
			try {
				studentInfDBAdapter.open();
				studentInfDBAdapter.autoInsertCourseInf((Course)args[0], (String)args[1]);
				//TODO 失败提示及处理
				//此处调用的方法返回布尔值，当为true是表示成功插入了新增课程，且能显示在本学期课程项中，当为false时表示插入不成功，用户输入的课程代码在数据库中已经有了。要给用户一个提示。
				studentInfDBAdapter.updateCurrentSemesterOfAddCourseInf((Course)args[0]);
			} catch(SQLiteException e){
				//TODO 异常处理
				e.printStackTrace();
			} finally {
				studentInfDBAdapter.close();
			}
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
    		updateCoursesListToDatabase();
    		finish();
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
}
