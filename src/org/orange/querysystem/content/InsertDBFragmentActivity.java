package org.orange.querysystem.content;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import org.orange.querysystem.R;

public class InsertDBFragmentActivity extends FragmentActivity{
	private String userName = null;
	private String password = null;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences shareData = getSharedPreferences("data", 0);
        userName = shareData.getString("userName", null);
        password = shareData.getString("password", null);
        getSupportFragmentManager().beginTransaction().add(android.R.id.content,new InsertDBFragment(userName, password)).commit();
	}
	
}
	

