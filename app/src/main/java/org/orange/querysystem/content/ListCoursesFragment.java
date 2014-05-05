/**
 *
 */
package org.orange.querysystem.content;

import org.orange.querysystem.CourseDetailsActivity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Bai Jie
 */
public class ListCoursesFragment extends ListFragment {

    public static final String COURSES_KEY = "org.orange.querysystem.simplecourses.key";

    private boolean hasRemovedRepeated = false;

    public static ListCoursesFragment newInstance(ArrayList<SimpleCourse> courses) {
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
    public void applyArguments() {
        Bundle args = getArguments();
        if (args != null) {
            ArrayList<SimpleCourse> courses = args.getParcelableArrayList(COURSES_KEY);
            if (!hasRemovedRepeated) {
                mergeRepeatedCourses(courses);
            }
            setListAdapter(new CoursesAdapter(getActivity(), courses));
        }
    }

    @Override
    public void onListItemClick(ListView l, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), CourseDetailsActivity.class);
        intent.putExtra(CourseDetailsActivity.KEY_COURSE_ID, (int) id);
        startActivity(intent);
    }

    /**
     * 合并课程列表中重复的项目（除时间外相同）
     *
     * @param courses 待合并的课程列表
     * @precondition 课程列表中无完全重复项且已被排序
     */
    public void mergeRepeatedCourses(List<SimpleCourse> courses) {
        if (!courses.isEmpty()) {
            SimpleCourse checkedCourse = null, currentCourse = null;
            String checkedTime = null, nextTime = null;
            int checkedPosition;
            boolean merged = false;
            ListIterator<SimpleCourse> iterator = courses.listIterator();
            //一次合并一个SimpleCourse
            out:
            while (iterator.hasNext()) {
                checkedPosition = iterator.nextIndex();
                checkedCourse = iterator.next();
                //找到检查中课程紧后边的课程时间
                nextTime = checkedTime = checkedCourse.getTime();
                while (checkedTime != null ? checkedTime.equals(nextTime) : nextTime == null) {
                    if (!iterator.hasNext()) {
                        break out;
                    }
                    currentCourse = iterator.next();
                    nextTime = currentCourse.getTime();
                }
                //检查是否有与checkedCourse相连的Course
                while (nextTime != null ? nextTime.equals(currentCourse.getTime())
                        : currentCourse.getTime() == null) {
                    if (checkedCourse.equalsExceptTime(currentCourse)) {
                        checkedCourse.time += ", " + currentCourse.time;
                        iterator.remove();
                        merged = true;
                        break;        //Attention: 这里只在无完全重复 SimpleCourse时有效
                    } else {
                        if (iterator.hasNext()) {
                            currentCourse = iterator.next();
                        } else {
                            break;
                        }
                    }
                }
                //恢复iterator位置
                iterator = courses.listIterator(checkedPosition);
                if (!merged)        //上次无重复项，则转至下一位置
                {
                    iterator.next();
                } else                //上次有重复项，则再次检查当前checkedCourse；恢复merged状态
                {
                    merged = false;
                }
            }
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
        this.getListView().setCacheColorHint(Color.TRANSPARENT);

        setEmptyText(getResources().getText(R.string.no_course));
    }

    public static class SimpleCourse implements Parcelable {

        private long id;

        private String name;

        private String time;

        private String otherInfo;

        public SimpleCourse(long id, String name, String time, String otherInfo) {
            this.id = id;
            this.name = name;
            this.time = time;
            this.otherInfo = otherInfo;
        }

        private SimpleCourse(Parcel in) {
            this(in.readLong(), in.readString(), in.readString(), in.readString());
        }

        /**
         * @return the ID
         */
        public long getId() {
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

        public boolean equalsExceptTime(Object otherObject) {
            if (otherObject == null) {
                return false;
            }
            if (this.getClass() != otherObject.getClass()) {
                return false;
            }
            SimpleCourse otherCourse = (SimpleCourse) otherObject;
            if (this.id != otherCourse.id) {
                return false;
            }
            //ID、名称一样，不考虑时间，只剩otherInfo了
            if (this.otherInfo == null) {
                return otherCourse.otherInfo == null;
            } else {
                return this.otherInfo.equals(otherCourse.otherInfo);
            }
        }

        @Override
        public boolean equals(Object otherObject) {
            if (!equalsExceptTime(otherObject)) {
                return false;
            }
            SimpleCourse otherCourse = (SimpleCourse) otherObject;
            //只剩时间了
            if (this.time == null) {
                return otherCourse.time == null;
            } else {
                return this.time.equals(otherCourse.time);
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeLong(id);
            out.writeString(name);
            out.writeString(time);
            out.writeString(otherInfo);

        }

        public static final Parcelable.Creator<SimpleCourse> CREATOR
                = new Parcelable.Creator<SimpleCourse>() {

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

    public static class CoursesAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        private List<SimpleCourse> courses;

        public CoursesAdapter(Context context, List<SimpleCourse> courses) {
            mInflater = LayoutInflater.from(context);
            setCourse(courses);
        }

        public void setCourse(List<SimpleCourse> courses) {
            this.courses = courses;
        }

        @Override
        public int getCount() {
            if (courses != null) {
                return courses.size();
            }
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
                convertView = mInflater.inflate(R.layout.fragment_list_course_row, parent, false);

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

        private static class ViewHolder {

            TextView name;

            TextView time;

            TextView otherInfo;
        }
    }
}
