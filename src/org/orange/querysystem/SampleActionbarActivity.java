package org.orange.querysystem;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import com.korovyansk.android.slideout.SlideoutActivity;

public class SampleActionbarActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
	    	finish();
	    }
	    setContentView(R.layout.sample_actionbar);
	    //ActionBar actionBar = getActionBar();
	   // actionBar.setDisplayHomeAsUpEnabled(true);
	    
	}
	
	  @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        getMenuInflater().inflate(R.menu.activity_our_helper, menu);
	        return true;
	    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			int width_1 = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
			SlideoutActivity.prepare(SampleActionbarActivity.this, R.id.inner_content, width_1);
			startActivity(new Intent(SampleActionbarActivity.this, MenuActivity.class));
			overridePendingTransition(0, 0);
			return true;
		case R.id.menu_refresh:	
			int width_2 = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
			SlideoutActivity.prepare(SampleActionbarActivity.this, R.id.inner_content, width_2);
			startActivity(new Intent(SampleActionbarActivity.this, MenuActivity.class));
			overridePendingTransition(0, 0);
			return true;
		default:
            return super.onOptionsItemSelected(item);
		}
	}
	
}
