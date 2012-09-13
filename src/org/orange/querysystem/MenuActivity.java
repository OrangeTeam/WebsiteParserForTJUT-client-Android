package org.orange.querysystem;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import org.orange.querysystem.R;

import com.korovyansk.android.slideout.SlideoutHelper;

public class MenuActivity extends FragmentActivity{

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    mSlideoutHelper = new SlideoutHelper(this);
	    mSlideoutHelper.activate();
	    getSupportFragmentManager().beginTransaction().add(org.orange.querysystem.R.id.slideout_placeholder, new MenuFragment(), "menu").commit();
	    mSlideoutHelper.open();
	}
	
	@Override
	protected void onStart(){
		   super.onStart();
		   ApplicationExit appExit = (ApplicationExit)getApplication();
		   if(appExit.isExit()){
			   finish();
		   }
	  }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			new AlertDialog.Builder(MenuActivity.this)
				.setTitle(R.string.menu_activity_title)
				.setIcon(R.drawable.ic_action_refresh)
				.setMessage(R.string.menu_activity_inform)
				.setPositiveButton(R.string.alert_dialog_ok, 
						new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								ApplicationExit appExit = (ApplicationExit)getApplication();
								appExit.setExit(true);
								finish();
							}
						}
				)
				.setNegativeButton(R.string.alert_dialog_cancel, 
						new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								
							}
						}
				).show();
		}
	
//			mSlideoutHelper.close();
//			return true;
//		
		return super.onKeyDown(keyCode, event);
	}


	public SlideoutHelper getSlideoutHelper(){
		return mSlideoutHelper;
	}
	public void exitProgrames(){
    	Intent startMain = new Intent(Intent.ACTION_MAIN);
    	startMain.addCategory(Intent.CATEGORY_HOME);
    	startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	startActivity(startMain);
    	android.os.Process.killProcess(android.os.Process.myPid());
    }
	
	private SlideoutHelper mSlideoutHelper;

}
