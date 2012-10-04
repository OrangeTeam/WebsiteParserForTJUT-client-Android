package org.orange.querysystem.content;

import java.util.ArrayList;
import java.util.HashMap;
import org.orange.querysystem.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainMenuActivity extends Activity{
	private GridView gridView;
	private TextView title;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);
		
		title = (TextView)findViewById(R.id.title);
		gridView = (GridView)findViewById(R.id.gridView);
		title.setText("主菜单");
		
		//生成动态数组，并且转入数据
		ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
		for(int i=0; i<6; i++){
			HashMap<String ,Object> map = new HashMap<String, Object>();
			map.put("ItemImage", R.drawable.logo_3);//添加图像资源的ID
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
				startActivity(new Intent(MainMenuActivity.this, ListCoursesActivity.class));
			}
			if(args3 == 3){
				startActivity(new Intent(MainMenuActivity.this, ListPostsActivity.class));
			}
			if(args3 == 4){
				startActivity(new Intent(MainMenuActivity.this, StudentInfoActivity.class));
			}
		}
	}
	
	private static Integer[] imgs = {};
	
	private static String[] texts = {"本周课程表", "总课程表", "成绩单", "通知", "学生信息" ,"开学时间设置"};

}
