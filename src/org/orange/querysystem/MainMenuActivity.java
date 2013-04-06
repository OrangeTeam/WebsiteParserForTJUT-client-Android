package org.orange.querysystem;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(11)
public class MainMenuActivity extends Activity{
	private GridView gridView;
	private TextView title;
	private int exit = 0;
	
	@Override
	public void onResume(){
		super.onResume();
		exit = 0;
	}
	@TargetApi(11)
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);

		title = (TextView)findViewById(R.id.title);
		gridView = (GridView)findViewById(R.id.gridView);
		title.setText("主菜单");

		//3.0以上版本，使用ActionBar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar mActionBar = getActionBar();
			mActionBar.setTitle("主菜单");
			//横屏时，为节省空间隐藏ActionBar
			if(getResources().getConfiguration().orientation == 
					android.content.res.Configuration.ORIENTATION_LANDSCAPE)
				mActionBar.hide();
			title.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
		}else{
			
		}				
    	//判断是否第一次登陆
		if(!SettingsActivity.hasSetAccountStudentIDAndPassword(this)){
    		promptOfDownloadDataDialog();
        }
		
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
				startActivity(new Intent(MainMenuActivity.this, CoursesInThisWeekActivity.class));
			}
			if(args3 == 1){
				startActivity(new Intent(MainMenuActivity.this, AllCoursesActivity.class));
			}
			if(args3 == 2){
				startActivity(new Intent(MainMenuActivity.this, AllCoursesInNextSemesterActivity.class));
			}
			if(args3 == 3){
				startActivity(new Intent(MainMenuActivity.this, ScoresActivity.class));
			}
			if(args3 == 4){
				startActivity(new Intent(MainMenuActivity.this, PostsActivity.class));
			}
			if(args3 == 5){
				startActivity(new Intent(MainMenuActivity.this, StudentInfoActivity.class));
			}
			if(args3 == 6){
				startActivity(new Intent(MainMenuActivity.this, CourseDetailsActivity.class));
			}
			if(args3 == 7){
				startActivity(new Intent(MainMenuActivity.this, SettingsActivity.class));
			}
		}
	}
	
	private static Integer[] imgs = {R.drawable.lsyweek, R.drawable.lsyall, R.drawable.lsynext, R.drawable.lsyscore, R.drawable.lsyinform, R.drawable.lsystumessage, R.drawable.lsyadd, R.drawable.lsyset};
	
	private static String[] texts = {"本周课程表", "总课程表", "下学期课程表", "成绩单", "通知", "学生信息" , "增加课程", "设置"};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(exit == 0){
				Toast.makeText(this, "再一次点击返回键退出程序", Toast.LENGTH_SHORT).show();
				exit++;
				return false;
			}
			
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void promptOfDownloadDataDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(MainMenuActivity.this);
		builder.setTitle(R.string.prompt_of_download_data_title)
		.setMessage(R.string.prompt_of_download_data_message)
		.setPositiveButton(android.R.string.yes, new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startActivity(new Intent(MainMenuActivity.this, SettingsActivity.class));
			}
		});
		builder.create().show();
	}
}
