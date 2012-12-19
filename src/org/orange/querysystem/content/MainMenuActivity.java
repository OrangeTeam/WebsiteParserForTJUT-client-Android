package org.orange.querysystem.content;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.orange.querysystem.LoginActivity;
import org.orange.querysystem.R;
import org.orange.querysystem.SettingsActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainMenuActivity extends Activity{
	private GridView gridView;
	private TextView title;
	private int mYear = 0;
	private int mMonth = 0;
	private int mDayOfMonth = 0;
	private int mDayOfYear = 0;
	private int exit = 0;
	
	
	static final int DATE_DIALOG_ID = 1;	
	
	@Override
	public void onResume(){
		super.onResume();
		exit = 0;
	}
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);
		SharedPreferences shareData = getSharedPreferences("data", 0);
    	//判断是否第一次登陆
    	if(shareData.getString("userName", null) == null || shareData.getString("password", null) == null){
        	startActivity(new Intent(this, LoginActivity.class));
        }
		title = (TextView)findViewById(R.id.title);
		gridView = (GridView)findViewById(R.id.gridView);
		title.setText("主菜单");
		Calendar calendar = Calendar.getInstance();
		mYear = calendar.get(Calendar.YEAR);
		mMonth = calendar.get(Calendar.MONTH);//比正常少一个月
		mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		mDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
		
		
		//生成动态数组，并且转入数据
		ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
		for(int i=0; i<8; i++){
			HashMap<String ,Object> map = new HashMap<String, Object>();
			map.put("ItemImage", imgs[i]);//添加图像资源的ID
			map.put("ItemText", texts[i]);//按序号做ItemText
			lstImageItem.add(map);
		}
		//生成适配器的ImageItem<====>动态元素，两者一一对应
		SimpleAdapter saImageItems = new SimpleAdapter(this, 
														lstImageItem, //数据来源
														R.layout.menu_item, 
														new String[]{"ItemImage" ,"ItemText"},
														new int[]{R.id.itemImage, R.id.itemText});
		//添加并且显示
		gridView.setAdapter(saImageItems);
		//添加消息处理 
		gridView.setOnItemClickListener(new ItemClickListener());
	}
	
	//当AdapterView被单机（触摸屏或者键盘),则返回的Item单击事件
	class ItemClickListener implements OnItemClickListener{
		public void onItemClick(AdapterView<?> args0,//the AdapterView where the click happend
								View args1, //The view whithin the AdapterView that was clicked
								int args2, //The position of the view in the adapter
								long args3 //The row id of the item that was clicked
								){
			if(args3 == 0){
				startActivity(new Intent(MainMenuActivity.this, ListCoursesActivity.class));
			}
			if(args3 == 1){
				startActivity(new Intent(MainMenuActivity.this, AllListCoursesActivity.class));
			}
			if(args3 == 2){
				startActivity(new Intent(MainMenuActivity.this, ListScoresActivity.class));
			}
			if(args3 == 3){
				startActivity(new Intent(MainMenuActivity.this, ListPostsActivity.class));
			}
			if(args3 == 4){
				startActivity(new Intent(MainMenuActivity.this, StudentInfoActivity.class));
			}
			if(args3 == 5){
				showDialog(DATE_DIALOG_ID);
			}
			if(args3 == 6){
				startActivity(new Intent(MainMenuActivity.this, AddCourseInfoActivity.class));
			}
			if(args3 == 7){
				startActivity(new Intent(MainMenuActivity.this, SettingsActivity.class));
			}
		}
	}
	
	@Override   
	protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                            mDateSetListener,
                            mYear, mMonth, mDayOfMonth);
        }
        return null;
    }    
	    
	@Override    
	protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DATE_DIALOG_ID:
            	//设置初始时间为当前时间
                ((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDayOfMonth);
                break;
        }
    }        

	private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year_choice, int month_choice,
                        int day_choice) {
                	Calendar calendar_2 = Calendar.getInstance();
                	calendar_2.set(year_choice, month_choice, day_choice);
                	if(calendar_2.get(Calendar.YEAR) > mYear || ((mYear-calendar_2.get(Calendar.YEAR))*365 + mDayOfYear - calendar_2.get(Calendar.DAY_OF_YEAR)) < 0 || ((mYear-calendar_2.get(Calendar.YEAR))*365 + mDayOfYear - calendar_2.get(Calendar.DAY_OF_YEAR)) > 22*7){
                		Toast.makeText(MainMenuActivity.this, "此为不恰当的日期，请重新设置！", Toast.LENGTH_LONG).show();
                	}
                	else{
                		Editor editor = getSharedPreferences("data", 0).edit();
                        editor.putString("start_year", Integer.toString(year_choice));
                        editor.putString("start_month", Integer.toString(month_choice+1));
                        editor.putString("start_day", Integer.toString(day_choice));
                        editor.commit();
                	}
                	
                }
            };    
	
	private static Integer[] imgs = {R.drawable.lsyweek, R.drawable.lsyall, R.drawable.lsyscore, R.drawable.lsyinform, R.drawable.lsystumessage, R.drawable.lsystart, R.drawable.lsyadd, R.drawable.lsyadd};
	
	private static String[] texts = {"本周课程表", "总课程表", "成绩单", "通知", "学生信息" ,"开课时间设置", "增加课程", "设置"};
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(exit == 0){
				Toast.makeText(this, "再一次点击返回键退出程序", Toast.LENGTH_SHORT).show();
				exit++;
				return false;
			}else{
				Editor editor = getSharedPreferences("data", 0).edit();
                editor.putString("passMainMenu", "false");
                editor.commit();
				finish();
			}
			
		}
		return super.onKeyDown(keyCode, event);
	}
}
