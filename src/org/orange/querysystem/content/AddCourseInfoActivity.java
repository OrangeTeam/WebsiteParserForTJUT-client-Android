package org.orange.querysystem.content;
import java.util.ArrayList;

import org.orange.querysystem.R;
import org.orange.querysystem.SettingsActivity;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.BitOperate.BitOperateException;
import util.webpage.Course;
import util.webpage.Course.TimeAndAddress;
import util.webpage.Course.TimeAndAddress.TimeAndAddressException;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AddCourseInfoActivity extends FragmentActivity{
	private EditText course_code_input;
	private EditText course_class_number_input;
	private EditText course_teacher_input;
	private EditText course_credit_input;
	private EditText course_kind_input;
	private EditText course_test_score_input;
	private EditText course_total_score_input;
	private EditText course_grade_point_input;
	private EditText course_name_input;
	private EditText course_time_and_adress_input;
	private static EditText week_input;
	private static EditText day_of_week_input;
	private static EditText period_input;
	private EditText classroom_input;
	private Button add;
	private String[] week_get = new String[10];
	private String[] day_of_week_get = new String[10];
	private String[] period_get = new String[10];
	private String[] classroom_get = new String[10];
	private static String choice_result = "";
	private int add_num = 1;
	private static int address_choice = 0;
	private static int choice_input_address = 0;
	private static int choice_num = 0;
	private static String address_choice_title = "";
	private static ArrayList mSelectedItems;

	private static final int DIALOG_TEXT_ENTRY = 1;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_course_info);

        course_name_input = (EditText)findViewById(R.id.course_name_input);
        course_time_and_adress_input = (EditText)findViewById(R.id.course_time_and_adress_input);

        course_code_input = (EditText)findViewById(R.id.course_code_input);
        course_class_number_input = (EditText)findViewById(R.id.course_class_number_input);
        course_teacher_input = (EditText)findViewById(R.id.course_teacher_input);
        course_credit_input = (EditText)findViewById(R.id.course_credit_input);
        course_kind_input = (EditText)findViewById(R.id.course_kind_input);
        course_test_score_input = (EditText)findViewById(R.id.course_test_score_input);
        course_total_score_input = (EditText)findViewById(R.id.course_total_score_input);
        course_grade_point_input = (EditText)findViewById(R.id.course_grade_point_input);

        add = (Button)findViewById(R.id.add);
        add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				add_num++;
				addTimeAndAddress();
			}
		});
        
		course_time_and_adress_input.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDialog(DIALOG_TEXT_ENTRY);
			}
		});
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
		TextView textView = new TextView(this);
		textView.setText("时间地点" + add_num + "：");
		textView.setId((add_num-2)*2 + 1);
		textView.setTextSize(18);
		textView.setTypeface(Typeface.SERIF);
		textView.setTextColor(Color.BLACK);
		EditText editText = new EditText(this);
		editText.setId((add_num-2)*2 + 2);
		editText.setCursorVisible(false);
		editText.setLongClickable(false);
		editText.setFocusable(false);
		
		RelativeLayout.LayoutParams tvlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams etlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		if((add_num-2) == 0){
			tvlp.addRule(RelativeLayout.BELOW, R.id.course_time_and_adress_input);
			etlp.addRule(RelativeLayout.BELOW, R.id.course_time_and_adress_input);
		}
		else{
			tvlp.addRule(RelativeLayout.BELOW, (add_num-2)*2 - 1);
			etlp.addRule(RelativeLayout.BELOW, (add_num-2)*2);
		}
		tvlp.addRule(RelativeLayout.ALIGN_BASELINE, (add_num-2)*2 + 2);
		etlp.addRule(RelativeLayout.RIGHT_OF, (add_num-2)*2 + 1);
		((RelativeLayout)findViewById(R.id.relativeLayout)).addView(textView, tvlp);
		((RelativeLayout)findViewById(R.id.relativeLayout)).addView(editText, etlp);
		findViewById((add_num-2)*2 + 2).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDialog(DIALOG_TEXT_ENTRY);
			}
		});
		
	}

    @Override
    protected Dialog onCreateDialog(int id) {
    	switch(id){
    	 case DIALOG_TEXT_ENTRY:
             // This example shows how to add a custom layout to an AlertDialog
             LayoutInflater factory = LayoutInflater.from(this);
             final View textEntryView = factory.inflate(R.layout.time_and_adress_entry, null);

             week_input = (EditText)textEntryView.findViewById(R.id.week_input);
             day_of_week_input = (EditText)textEntryView.findViewById(R.id.day_of_week_input);
             period_input = (EditText)textEntryView.findViewById(R.id.period_input);
             classroom_input = (EditText)textEntryView.findViewById(R.id.classroom_input);

             week_input.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					address_choice = R.array.week;
					address_choice_title = "周数";
					choice_num = 0;
					choice_result = "";
					choice_input_address = R.id.week_input;
					showDialog();
				}
			 });
             day_of_week_input.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					address_choice = R.array.days_of_week;
					address_choice_title = "星期数";
					choice_num = 0;
					choice_result = "";
					choice_input_address = R.id.day_of_week_input;
					showDialog();
				}
			 });
             period_input.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					address_choice_title = "节数";
					address_choice = R.array.period;
					choice_num = 0;
					choice_result = "";
					choice_input_address = R.id.period_input;
					showDialog();
				}
			 });
     		 
             return new AlertDialog.Builder(this)
                 .setView(textEntryView)
                 .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                    	 
                         /* User clicked OK so do some stuff */
                         
                         week_get[add_num-1] = week_input.getText().toString();
                         day_of_week_get[add_num-1] = day_of_week_input.getText().toString();
                         period_get[add_num-1] = period_input.getText().toString();
                         classroom_get[add_num-1] = classroom_input.getText().toString();
                         if(add_num == 1){
                    		 course_time_and_adress_input.setText(week_get[add_num-1] + "周" + " " + day_of_week_get[add_num-1] + " " + period_get[add_num-1] + "节" + " " + classroom_get[add_num-1]); 
                    	 }
                    	 else
                    	 {
                    		 ((EditText)findViewById((add_num-2)*2 + 2)).setText(week_get[add_num-1] + "周" + " " + day_of_week_get[add_num-1] + " " + period_get[add_num-1] + "节" + " " + classroom_get[add_num-1]);
                    	 }	 
                         updateCoursesListToDatabase();
                     }
                 })
                 .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {

                         /* User clicked cancel so do some stuff */
                     }
                 })
                 .create();
    	}
    	return null;
    }
    
    public void showDialog(){
    	DialogFragment newFragment = MyAlertDialogFragment.newInstance(
                android.R.string.ok);
        newFragment.show(getSupportFragmentManager(), "dialog");
    }
    
    public void doPositiveClick() {
        // Do stuff here.
        Log.i("FragmentAlertDialog", "Positive click!");
    }
    
    public void doNegativeClick() {
        // Do stuff here.
        Log.i("FragmentAlertDialog", "Negative click!");
    }

    

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int title) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }
        
        @Override
    	public Dialog onCreateDialog(Bundle savedInstanceState) {
    	    mSelectedItems = new ArrayList();  // Where we track the selected items
    	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	    // Set the dialog title
    	    builder.setTitle(address_choice_title)
    	    // Specify the list array, the items to be selected by default (null for none),
    	    // and the listener through which to receive callbacks when items are selected
    	           .setMultiChoiceItems(address_choice, null,
    	                      new DialogInterface.OnMultiChoiceClickListener() {
    	               @Override
    	               public void onClick(DialogInterface dialog, int which,
    	                       boolean isChecked) {
    	                   if (isChecked) {
    	                       // If the user checked the item, add it to the selected items
    	                       mSelectedItems.add(which);
    	                       choice_num++;
    	                   } else if (mSelectedItems.contains(which)) {
    	                       // Else, if the item is already in the array, remove it 
    	                       mSelectedItems.remove(Integer.valueOf(which));
    	                       choice_num--;
    	                   }
    	               }
    	           })
    	    // Set the action buttons
    	           .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
    	               @Override
    	               public void onClick(DialogInterface dialog, int id) {
    	                   // User clicked OK, so save the mSelectedItems results somewhere
    	                   // or return them to the component that opened the dialog
//    	                   ...
    	            	   Resources res = getResources();
    	            	   String[] content = res.getStringArray(address_choice);
    	            	   System.out.println(choice_result);
    	            	   if(choice_input_address == R.id.week_input){
    	            		   for(int i=0; i<choice_num; i++){
        	            		   if(i == choice_num-1){
        	            			   choice_result = choice_result + content[(Integer) mSelectedItems.get(i)];
        	            			   break;
        	            		   }
        	                	   choice_result = choice_result + content[(Integer) mSelectedItems.get(i)] + ",";
        	                   } 
    	            		   week_input.setText(choice_result); 
    	            	   }
    	            	   if(choice_input_address == R.id.day_of_week_input){
    	            		   for(int i=0; i<choice_num; i++){
        	            		   if(i == choice_num-1){
        	            			   choice_result = choice_result + content[(Integer) mSelectedItems.get(i)];
        	            			   break;
        	            		   }
        	                	   choice_result = choice_result + content[(Integer)mSelectedItems.get(i)] + ",";
        	                   } 
    	            		   day_of_week_input.setText(choice_result);  
    	            	   }
    	            	   if(choice_input_address == R.id.period_input){
    	            		   for(int i=0; i<choice_num; i++){
        	            		   if(i == choice_num-1){
        	            			   choice_result = choice_result + content[(Integer) mSelectedItems.get(i)];
        	            			   break;
        	            		   }
        	                	   choice_result = choice_result + content[(Integer) mSelectedItems.get(i)] + ",";
        	                   } 
    	            		   period_input.setText(choice_result); 
    	            	   }
    	            	   
    	               }
    	           })
    	           .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
    	               @Override
    	               public void onClick(DialogInterface dialog, int id) {
//    	                   ...
    	               }
    	           });

    	    return builder.create();
    	}
    }
    
    public void updateCoursesListToDatabase(){
        Course course = new Course();
        course.setName(course_name_input.getText().toString());
        course.setCode(course_code_input.getText().toString());
        course.setClassNumber(course_class_number_input.getText().toString());
        course.setTeachers(course_teacher_input.getText().toString());
        try {
        	for(int j=0; j<add_num; j++){
        		TimeAndAddress timeAndAddress = new TimeAndAddress();
        		timeAndAddress.addDays(day_of_week_get[j]);
        		timeAndAddress.addPeriods(period_get[j]);
        		timeAndAddress.addWeeks(week_get[j]);
        		timeAndAddress.setAddress(classroom_get[j]);
        		course.addTimeAndAddress(timeAndAddress);
        	}
	        
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
//			// TODO Auto-generated catch block
		} catch (TimeAndAddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 } catch (BitOperateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 } catch (NullPointerException e){
			 e.printStackTrace();
		 }
        course.setKind(course_kind_input.getText().toString());
        String userName = SettingsActivity.getAccountStudentID(this);
        new AddCourseToDatabase().execute(course, userName);
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
