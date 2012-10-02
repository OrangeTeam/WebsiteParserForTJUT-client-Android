package org.orange.querysystem;

import org.orange.querysystem.content.AllCourseListActivity;
import org.orange.querysystem.content.ListCoursesActivity;
import org.orange.querysystem.content.StudentInfoActivity;
import org.orange.querysystem.content.TerminalScoreActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MenuFragment extends ListFragment {

	private static final String ACTIVITY_SERVICE = null;
	
	private static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private Bitmap mIcon1;
        private Bitmap mIcon2;
        private Bitmap mIcon3;

        public EfficientAdapter(Context context) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);

            // Icons bound to the rows.
            mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.manager);
            mIcon2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
            mIcon3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.list);
        }
	
        @Override
		public int getCount() {
            return DATA.length;
        }
        
        @Override
		public Object getItem(int position) {
            return position;
        }
        
        @Override
		public long getItemId(int position) {
            return position;
        }

        /**
         * Make a view to hold each row.
         *
         * @see android.widget.ListAdapter#getView(int, android.view.View,
         *      android.view.ViewGroup)
         */
        @Override
		public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            ViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.menu_fragment, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            if(position == 1 || position == 7){
            	holder.text.setTextSize(15.6f);
//            	holder.text.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).setMargins(7, 7, 7, 7));
            	holder.icon.setLayoutParams(new LinearLayout.LayoutParams(50, 50));
//            	holder.icon.setLayoutParams(new LinearLayout.LayoutParams(50, 50).setMargins(7, 7, 7, 7));
             }
            if(position == 0){
            	holder.text.setTextSize(40.9f);
            	holder.icon.setLayoutParams(new LinearLayout.LayoutParams(130, 130));
            }
            
            holder.text.setText(DATA[position]);
            if(position == 0){
            	holder.icon.setImageBitmap(mIcon1);
            }
            else if(position == 1 || position == 7){
            	holder.icon.setImageBitmap(mIcon2);
            }
            else 
            	holder.icon.setImageBitmap(mIcon3);

            return convertView;
        }

        static class ViewHolder {
            TextView text;
            ImageView icon;
        }
    }
	private static final String[] DATA = {"用户名\n", "常用", "课程表", "成绩单", "学生信息", "课程信息", "通知", "操作", "选项"};    
	    
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(new EfficientAdapter(getActivity()));
		getListView().setCacheColorHint(0);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		MenuActivity parent = (MenuActivity)getActivity();
		if(id == 0){
			parent.getSlideoutHelper().close();
			new Handler().postDelayed(new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					startActivity(new Intent(getActivity(),SampleActivity.class));	}}, 500);	
		}
		if(id == 2){
			parent.getSlideoutHelper().close();
			new Handler().postDelayed(new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					startActivity(new Intent(getActivity(),AllCourseListActivity.class));	}}, 500);	
		}
		if(id == 3){
			parent.getSlideoutHelper().close();
			new Handler().postDelayed(new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					startActivity(new Intent(getActivity(),TerminalScoreActivity.class));	}}, 500);	
			
		}
		if(id == 4){
			parent.getSlideoutHelper().close();
			new Handler().postDelayed(new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					startActivity(new Intent(getActivity(),StudentInfoActivity.class));	}}, 500);	
			//startActivity(new Intent(getActivity(),WeekCourseListActivity.class));
			
		}
		if(id == 5){
			parent.getSlideoutHelper().close();
			new Handler().postDelayed(new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					startActivity(new Intent(getActivity(),AllCourseListActivity.class));	}}, 500);	
			//startActivity(new Intent(getActivity(),TerminalScoreActivity.class));
			
		}
//		if(id == 5){
//			parent.getSlideoutHelper().close();
//		new Handler().postDelayed(new Runnable(){
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				startActivity(new Intent(getActivity(),AllCourseListActivity.class));	}}, 500);	
//			startActivity(new Intent(getActivity(),PersonalScoreActivity.class));
//			
//		}
		
		
	}
}
