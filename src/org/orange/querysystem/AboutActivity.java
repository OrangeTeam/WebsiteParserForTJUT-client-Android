package org.orange.querysystem;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;

public class AboutActivity extends Activity{
	private WebView content;
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		content = new WebView(this);
		setContentView(content);
		content.loadUrl("file:///android_asset/aboutme.html");
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			getActionBar().setTitle(R.string.about_me);
	}
}
