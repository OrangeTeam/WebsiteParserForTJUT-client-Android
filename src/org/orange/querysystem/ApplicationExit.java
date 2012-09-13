package org.orange.querysystem;

import android.app.Application;

public class ApplicationExit extends Application{
	
	//程序退出标记
	private static boolean isProgramExit = false;
	
	public void setExit(boolean exit){
		isProgramExit = exit;
	}
	
	public boolean isExit(){
		return isProgramExit;
	}
	

}

