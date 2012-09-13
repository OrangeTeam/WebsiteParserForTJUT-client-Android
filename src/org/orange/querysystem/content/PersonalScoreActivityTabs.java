package org.orange.querysystem.content;

import org.orange.querysystem.ApplicationExit;
import org.orange.querysystem.R;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class PersonalScoreActivityTabs extends TabActivity{
	
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
		setContentView(R.layout.personal_score_tabs);
		
		TabHost tabHost = getTabHost();
		Resources res = getResources();
		
//		Intent intent = new Intent(this, PersonalScoreActivityContent.class);
		for(int i=0; i<4; i++){
			TabHost.TabSpec personalScoreSpec = tabHost.newTabSpec("大" + (i/2+1) + "第" + (i%2+1) + "学期");
			personalScoreSpec.setIndicator("大" + (i/2+1) + "第" + (i%2+1) + "学期", res.getDrawable(android.R.drawable.btn_star));
			if(i == 0){
				Intent intent = new Intent(this, PersonalScoreActivityContent1.class);
				System.out.println("1");
//				intent.putExtra("term", "2010");
//				intent.putExtra("semester", "1");
				personalScoreSpec.setContent(intent);
			}
			if(i == 1){
				Intent intent = new Intent(this, PersonalScoreActivityContent2.class);
				System.out.println("2");
//				intent.putExtra("term1", "2010");
//				intent.putExtra("semester", "2");
				personalScoreSpec.setContent(intent);
			}
			if(i == 2){
				Intent intent = new Intent(this, PersonalScoreActivityContent3.class);
				System.out.println("3");
//				intent.putExtra("term2", "2011");
//				intent.putExtra("semester", "1");
				personalScoreSpec.setContent(intent);
			}
			if(i == 3){
				Intent intent = new Intent(this, PersonalScoreActivityContent4.class);
				System.out.println("4");
//				intent.putExtra("term3", "2011");
//				intent.putExtra("semester", "2");
				personalScoreSpec.setContent(intent);
			}
			tabHost.addTab(personalScoreSpec);
		}
	}
}
