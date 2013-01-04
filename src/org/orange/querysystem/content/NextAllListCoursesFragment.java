package org.orange.querysystem.content;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.orange.querysystem.R;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class NextAllListCoursesFragment extends ListFragment {
	public static final String COURSES_KEY = "org.orange.querysystem.simplecourses.key";

	private boolean hasRemovedRepeated = false;

	public static ListCoursesFragment newInstance(ArrayList<SimpleCourse> courses){
		ListCoursesFragment listADay = new ListCoursesFragment();
		Bundle args = new Bundle();
		args.putParcelableArrayList(COURSES_KEY, courses);
		listADay.setArguments(args);
		return listADay;
	}

	@Override
	public void setArguments(Bundle args) {
		super.setArguments(args);
		hasRemovedRepeated = false;
	}

	/**
	 * 应用{@link #setArguments(Bundle args)}设置的Courses，{@link #onCreate(Bundle)}会自动调用此方法<br />
	 * 使用方法：<br />
	 * 先用{@link #setArguments(Bundle args)}方法传递课程列表，再用此方法应用传递过来的列表
	 * <code>
	 * ArrayList<SimpleCourse> courses;
	 * ...
	 * Bundle arg = new Bundle();
	 * arg.putParcelableArrayList(ListCoursesFragment.COURSES_KEY, courses);
	 * thisFragment.setArguments(arg);
	 * thisFragment.applyArguments();
	 * </code>
	 */
	public void applyArguments(){
		Bundle args = getArguments();
		if(args != null){
			ArrayList<SimpleCourse> courses = args.getParcelableArrayList(COURSES_KEY);
			if(!hasRemovedRepeated) mergeRepeatedCourses(courses);
			setListAdapter(new CoursesAdapter(getActivity(), courses));
		}
	}

	@Override
	public void onListItemClick(ListView l, View view, int position, long id){
		l.setCacheColorHint(Color.TRANSPARENT);
		Intent intent = new Intent(getActivity(), CourseInfoActivity.class);
		intent.putExtra("course_info", (int)id);
		startActivity(intent);
	}

	/**合并课程列表中重复的项目*/
	public void mergeRepeatedCourses(List<SimpleCourse> courses){
		SimpleCourse lastCourse = null, currentCourse = null;
		Iterator<SimpleCourse> iterator = courses.iterator();
		while(iterator.hasNext()){
			currentCourse = iterator.next();
			if(lastCourse == null){
				lastCourse = currentCourse;
				continue;
			}
			if(lastCourse.equalsExceptTime(currentCourse)){
				lastCourse.time += ", "+currentCourse.time;
				iterator.remove();
			}else
				lastCourse = currentCourse;
		}
		hasRemovedRepeated = true;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		applyArguments();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setEmptyText(getResources().getText(R.string.no_course));
	}

	public static class SimpleCourse implements Parcelable{
		private int id;
		private String name;
		private String time;
		private String otherInfo;
		public SimpleCourse(int id, String name, String time, String otherInfo){
			this.id = id;
			this.name = name;
			this.time = time;
			this.otherInfo = otherInfo;
		}
		public SimpleCourse(Parcel in){
			this(in.readInt(), in.readString(), in.readString(), in.readString());
		}
		/**
		 * @return the ID
		 */
		public int getId() {
			return id;
		}
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		/**
		 * @return the time
		 */
		public String getTime() {
			return time;
		}
		/**
		 * @return the other information
		 */
		public String getOtherInfo() {
			return otherInfo;
		}
		public boolean equalsExceptTime(Object otherObject){
			if(otherObject == null)
				return false;
			if(this.getClass() != otherObject.getClass())
				return false;
			SimpleCourse otherCourse = (SimpleCourse) otherObject;
			if(this.id != otherCourse.id)
				return false;
			//ID、名称一样，不考虑时间，只剩otherInfo了
			if(this.otherInfo==null)
				return otherCourse.otherInfo==null;
			else
				return this.otherInfo.equals(otherCourse.otherInfo);
		}
		@Override
		public boolean equals(Object otherObject) {
			if(!equalsExceptTime(otherObject))
				return false;
			SimpleCourse otherCourse = (SimpleCourse) otherObject;
			//只剩时间了
			if(this.time==null)
				return otherCourse.time==null;
			else
				return this.time.equals(otherCourse.time);
		}
		@Override
		public int describeContents() {
			return 0;
		}
		@Override
		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(id);
			out.writeString(name);
			out.writeString(time);
			out.writeString(otherInfo);
			
		}
		public static final Parcelable.Creator<SimpleCourse> CREATOR 
		= new Parcelable.Creator<SimpleCourse>(){

			@Override
			public SimpleCourse createFromParcel(Parcel source) {
				return new SimpleCourse(source);
			}

			@Override
			public SimpleCourse[] newArray(int size) {
				return new SimpleCourse[size];
			}
			
		};
	}
	
	public static class CoursesAdapter extends BaseAdapter{
		private LayoutInflater mInflater;
		private List<SimpleCourse> courses;
		public CoursesAdapter(Context context, List<SimpleCourse> courses){
			mInflater = LayoutInflater.from(context);
			setCourse(courses);
		}
		public void setCourse(List<SimpleCourse> courses){
			this.courses = courses;
		}

		@Override
		public int getCount() {
			if(courses!=null)
				return courses.size();
			return 0;
		}

		@Override
		public SimpleCourse getItem(int position) {
			return courses.get(position);
		}

		@Override
		public long getItemId(int position) {
			return courses.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            ViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.fragment_list_course_row,parent,false);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.course_name);
                holder.time = (TextView) convertView.findViewById(R.id.course_time);
                holder.otherInfo = (TextView) convertView.findViewById(R.id.course_otherInfo);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            holder.name.setText(courses.get(position).getName());
            holder.time.setText(courses.get(position).getTime());
            holder.otherInfo.setText(courses.get(position).getOtherInfo());

            return convertView;
		}
		
		private static class ViewHolder{
			TextView name;
			TextView time;
			TextView otherInfo;
		}
	}
}
