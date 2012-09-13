package org.orange.querysystem.content;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

public class MySimpleAdapter extends SimpleAdapter{

	 public MySimpleAdapter(Context context, ArrayList<HashMap<String, String>> list, int layout,String[] from, int[] to) {  
		 super(context, list, layout, from, to);    
	        // TODO Auto-generated constructor stub  
	 }  

	 @Override
	public View getView(final int position, View convertView, ViewGroup parent){
		 View view = null;  
		 if (convertView != null) {  
			 view = convertView;  
			// 当listview的item过多时，拖动会遮住一部分item，被遮住的item的view就是convertView保存着。  
	        // 当滚动条回到之前被遮住的item时，直接使用convertView，而不必再去new view()  
		 } 
		 else{  
			 view = super.getView(position, convertView, parent);  
		 }            
		 int[] colors = { Color.rgb(169, 169, 169), Color.WHITE};//RGB颜色  
		 if(position == 0){
			 view.setBackgroundColor(colors[0]);
		 }
		 else{
			 view.setBackgroundColor(colors[1]);// 每隔item之间颜色不同 
		 }
		 
		 return super.getView(position, view, parent);   
		        

		        
	}  
 }

