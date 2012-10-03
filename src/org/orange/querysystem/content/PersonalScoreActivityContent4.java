package org.orange.querysystem.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.orange.querysystem.ApplicationExit;
import org.orange.querysystem.LoginActivity;
import org.orange.querysystem.MenuActivity;
import org.orange.querysystem.R;

import util.BitOperate.BitOperateException;
import util.webpage.Constant;
import util.webpage.Course;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.korovyansk.android.slideout.SlideoutActivity;

public class PersonalScoreActivityContent4 extends ListActivity implements ParseWebPage.CoursesInfo{
	
	private TextView courseListTitle;
	private Button refresh;
	private Spinner spinner = null;
	private int j = 0;
	private String term;
	private String semester;
	
	@Override
	protected void onStart(){
		   super.onStart();
		   ApplicationExit appExit = (ApplicationExit)getApplication();
		   if(appExit.isExit()){
			   finish();
		   }
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personal_score_content_main);
		
		readWebPage();
	    refresh = (Button)findViewById(R.id.refresh);
        refresh.setBackgroundResource(R.drawable.ic_action_refresh);
		findViewById(R.id.sample_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
						SlideoutActivity.prepare(PersonalScoreActivityContent4.this, R.id.inner_content, width);
						startActivity(new Intent(PersonalScoreActivityContent4.this,
								MenuActivity.class));
						overridePendingTransition(0, 0);
					}
				});
		ArrayAdapter<CharSequence> menu_adapter = ArrayAdapter.createFromResource(this, R.array.personal_score_menu_array, android.R.layout.simple_spinner_item); 
		menu_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner = (Spinner)findViewById(R.id.score_spinner_menu);
		spinner.setAdapter(menu_adapter);
		spinner.setPrompt("学生成绩单");
		spinner.setOnItemSelectedListener(new SpinnerOnSelectedListener());
    }
    
    class SpinnerOnSelectedListener implements OnItemSelectedListener{
    	@Override
    	public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id){
    		if(adapterView.getItemIdAtPosition(position) == 0){
    			System.out.println("测试");
    		}
    		if(adapterView.getItemIdAtPosition(position) == 1){
    			startActivity(new Intent(PersonalScoreActivityContent4.this, TerminalScoreActivity.class));
    		}
    			
    	}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
			// TODO Auto-generated method stub
			
		}
    }
	
	public void readWebPage(){
    	new ParseWebPage().execute(ParseWebPage.PARSE_SCORE, Constant.url.个人全部成绩, this);
    }
    
    public void coursesInfo(List<Course> courses){
//    	courseListTitle = (TextView)findViewById(R.id.course_list_title);
//        courseListTitle.setText("期末成绩单");
        
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map1 = new HashMap<String, String>();
        map1.put("courseName", "课程名称");
		map1.put("score", "成绩");
		map1.put("credit", "学分");
		map1.put("point", "绩点");
		map1.put("courseProperty", "课程性质");
		map1.put("yearAndTerm", "学年-学期");
		list.add(map1);
		
//		Intent intent = getIntent();
//		term = intent.getStringExtra("term1");
//		semester = intent.getStringExtra("semester");
//		if(semester.equals("1")){
			while(!(!courses.get(j).isFirstSemester() && (courses.get(j).getYear() == Integer.parseInt("2011")))){
				j++;
			}
	        for(int i=j,k=0; i<courses.size() && !courses.get(i).isFirstSemester() && (courses.get(i).getYear() == Integer.parseInt("2011")); i++){
	        	HashMap<String, String> map = new HashMap<String, String>();
	        	map.put("courseName", courses.get(i).getName());
	        	map.put("score", Integer.toString(courses.get(i).getTotalScore()));
	        	map.put("credit", Integer.toString(courses.get(i).getCredit()));
	        	map.put("point", courses.get(i).getNote());
	        	map.put("courseProperty", courses.get(i).getKind());
	        	if(courses.get(i).isFirstSemester())
	        		k = 1;
	        	else 
	        		k = 2;
	        	map.put("yearAndTerm", Integer.toString(courses.get(i).getYear()) + "-" + Integer.toString(k));
	        	
	        	list.add(map);
	        }
//		}
//		if(semester.equals("2")){
//			while(!(courses.get(j).isFirstSemester()==false && (courses.get(j).getYear() == Integer.parseInt(term)))){
//				j++;
//			}
//	        for(int i=j,k=0; i<courses.size() && courses.get(i).isFirstSemester()==false && (courses.get(i).getYear() == Integer.parseInt(term)); i++){
//	        	HashMap<String, String> map = new HashMap<String, String>();
//	        	map.put("courseName", courses.get(i).getName());
//	        	map.put("score", Integer.toString(courses.get(i).getTotalScore()));
//	        	map.put("credit", Integer.toString(courses.get(i).getCredit()));
//	        	map.put("point", courses.get(i).getNote());
//	        	map.put("courseProperty", courses.get(i).getKind());
//	        	if(courses.get(i).isFirstSemester())
//	        		k = 1;
//	        	else 
//	        		k = 2;
//	        	map.put("yearAndTerm", Integer.toString(courses.get(i).getYear()) + "-" + Integer.toString(k));
//	        	
//	        	list.add(map);
//	        }
//		}
		SimpleAdapter listAdapter = new MySimpleAdapter(this, list, R.layout.personal_score_content_listview, new String[]{"courseName", "score", "credit", "point", "courseProperty", "yearAndTerm"}, new int[] {R.id.courseName,R.id.score, R.id.credit, R.id.point, R.id.courseProperty, R.id.yearAndTerm});
		setListAdapter(listAdapter);
		
//		ListView listView = (ListView)findViewById(android.R.id.list);
//		Drawable drawable = getResources().getDrawable(R.drawable.pic);
//		listView.setSelector(drawable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.exit);
        menu.add(0, 2, 2, R.string.change_number);
        menu.add(0, 3, 3, R.string.about);
        return super.onCreateOptionsMenu(menu); 
    }
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	// TODO Auto-generated method stub\
    	if(item.getItemId()==1){
    		finish();
    	}
    	if(item.getItemId()==2){
    		startActivity(new Intent(PersonalScoreActivityContent4.this, LoginActivity.class));
    	}
    	return super.onMenuItemSelected(featureId, item);
    }

    
    @Override
   	public boolean onKeyDown(int keyCode, KeyEvent event) {
   		if(keyCode == KeyEvent.KEYCODE_BACK){
   			int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
   			SlideoutActivity.prepare(PersonalScoreActivityContent4.this, R.id.inner_content, width);
   			startActivity(new Intent(PersonalScoreActivityContent4.this,
   					MenuActivity.class));
   			overridePendingTransition(0, 0);
   		}
   		return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public void onBackPressed() {
    	//实现Home键效果
    	//super.onBackPressed();这句话一定要注掉,不然又去调用默认的back处理方式了
    	Intent i= new Intent(Intent.ACTION_MAIN);
    	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	i.addCategory(Intent.CATEGORY_HOME);
    	startActivity(i);
    }
    
    
    public void test(View view){
    	System.out.println("hello");
    }
    
}