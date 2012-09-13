package org.orange.querysystem.content;

import java.util.ArrayList;
import java.util.HashMap;

import org.orange.querysystem.ApplicationExit;
import org.orange.querysystem.LoginActivity;
import org.orange.querysystem.MenuActivity;
import org.orange.querysystem.R;

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

import com.korovyansk.android.slideout.SlideoutActivity;

public class TerminalScoreActivity extends ListActivity implements ParseWebPage.CoursesInfo{
	
	private Button refresh;
	private Spinner spinner = null;
	
	@Override
	protected void onStart(){
		   super.onStart();
		   ApplicationExit appExit = (ApplicationExit)getApplication();
		   if(appExit.isExit()){
			   finish();
		   }
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.terminal_score_main);
	    
	    readWebPage();
	    refresh = (Button)findViewById(R.id.refresh);
        refresh.setBackgroundResource(R.drawable.ic_action_refresh);
		findViewById(R.id.sample_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
						SlideoutActivity.prepare(TerminalScoreActivity.this, R.id.inner_content, width);
						startActivity(new Intent(TerminalScoreActivity.this,
								MenuActivity.class));
						overridePendingTransition(0, 0);
					}
				});
		ArrayAdapter<CharSequence> menu_adapter = ArrayAdapter.createFromResource(this, R.array.terminal_score_menu_array, android.R.layout.simple_spinner_item); 
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
    			
    		}
    		if(adapterView.getItemIdAtPosition(position) == 1){
    			startActivity(new Intent(TerminalScoreActivity.this, PersonalScoreActivityTabs.class));
    		}
    			
    	}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
			// TODO Auto-generated method stub
			
		}
    }
	
	public void readWebPage(){
    	new ParseWebPage().execute(ParseWebPage.PARSE_SCORE, Constant.url.期末最新成绩, this);
    }
    
    public void coursesInfo(ArrayList<Course> courses){
        
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map1 = new HashMap<String, String>();
        map1.put("courseName", "课程名称");
		map1.put("testScore", "考试成绩");
		map1.put("finalScore", "总评成绩");
		map1.put("credit", "学分");
		map1.put("yearAndTerm", "学年-学期");
		list.add(map1);
		
        for(int i=0, k=0; i<courses.size(); i++){
        	HashMap<String, String> map = new HashMap<String, String>();
        	map.put("courseName", courses.get(i).getName());
        	map.put("testScore", Integer.toString(courses.get(i).getTestScore()));
        	map.put("finalScore", Integer.toString(courses.get(i).getTotalScore()));
        	map.put("credit", Integer.toString(courses.get(i).getCredit()));
        	if(courses.get(i).isFirstSemester())
        		k = 1;
        	else 
        		k = 2;
        	map.put("yearAndTerm", Integer.toString(courses.get(i).getYear()) + "-" + Integer.toString(k));
        	
        	list.add(map);
        }
		SimpleAdapter listAdapter = new MySimpleAdapter(this, list, R.layout.terminal_score_listview, new String[]{"courseName", "testScore", "finalScore", "credit", "yearAndTerm"}, new int[] {R.id.courseName,R.id.testScore, R.id.finalScore, R.id.credit, R.id.yearAndTerm});
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
    		startActivity(new Intent(TerminalScoreActivity.this, LoginActivity.class));
    	}
    	return super.onMenuItemSelected(featureId, item);
    }

    
    @Override
   	public boolean onKeyDown(int keyCode, KeyEvent event) {
   		if(keyCode == KeyEvent.KEYCODE_BACK){
   			int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
   			SlideoutActivity.prepare(TerminalScoreActivity.this, R.id.inner_content, width);
   			startActivity(new Intent(TerminalScoreActivity.this,
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
