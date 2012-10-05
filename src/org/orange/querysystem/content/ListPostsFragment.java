/**
 * 
 */
package org.orange.querysystem.content;

import java.util.ArrayList;
import java.util.List;

import org.orange.querysystem.R;

import android.content.Context;
import android.content.Intent;
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

/**
 * @author Bai Jie
 */
public class ListPostsFragment extends ListFragment {
	public static final String POSTS_KEY = ListPostsFragment.class.getName()+"simplepost.key";
	
	public static ListPostsFragment newInstance(ArrayList<SimplePost> posts){
		ListPostsFragment listADay = new ListPostsFragment();
		Bundle args = new Bundle();
		args.putParcelableArrayList(POSTS_KEY, posts);
		listADay.setArguments(args);
		return listADay;
	}

	/**
	 * 应用{@link #setArguments(Bundle args)}设置的Posts，{@link #onCreate(Bundle)}会自动调用此方法<br />
	 * 使用方法：<br />
	 * 先用{@link #setArguments(Bundle args)}方法传递通知列表，再用此方法应用传递过来的列表
	 * <code>
	 * ArrayList<SimplePost> posts;
	 * ...
	 * Bundle arg = new Bundle();
	 * arg.putParcelableArrayList(ListPostsFragment.POSTS_KEY, posts);
	 * thisFragment.setArguments(arg);
	 * thisFragment.applyArguments();
	 * </code>
	 */
	public void applyArguments(){
		Bundle args = getArguments();
		if(args != null){
			ArrayList<SimplePost> posts = args.getParcelableArrayList(POSTS_KEY);
			setListAdapter(new PostsAdapter(getActivity(), posts));
		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		applyArguments();
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(getActivity(), ShowOnePostActivity.class);
		intent.putExtra(ShowOnePostActivity.EXTRA_POST_ID, (int)id);
		startActivity(intent);
	}

	public static class SimplePost implements Parcelable{
		private int id;
		private String title;
		private String category;
		private String date;
		private String author;
		public SimplePost(int id, String title, String category, String author, String date){
			this.id = id;
			this.title = title;
			this.category = category;
			this.date = date;
			this.author = author;
		}
		public SimplePost(Parcel in){
			this(in.readInt(), in.readString(), in.readString(), in.readString(), in.readString());
		}
		public int getId() {
			return id;
		}
		public String getTitle() {
			return title;
		}
		public String getCategory(){
			return category;
		}
		public String getDate() {
			return date;
		}
		public String getAuthor() {
			return author;
		}
		@Override
		public int describeContents() {
			return 0;
		}
		@Override
		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(id);
			out.writeString(title);
			out.writeString(category);
			out.writeString(author);
			out.writeString(date);
		}
		public static final Parcelable.Creator<SimplePost> CREATOR 
		= new Parcelable.Creator<SimplePost>(){

			@Override
			public SimplePost createFromParcel(Parcel source) {
				return new SimplePost(source);
			}

			@Override
			public SimplePost[] newArray(int size) {
				return new SimplePost[size];
			}
			
		};
	}
	
	public static class PostsAdapter extends BaseAdapter{
		private LayoutInflater mInflater;
		private List<SimplePost> posts;
		public PostsAdapter(Context context, List<SimplePost> posts){
			mInflater = LayoutInflater.from(context);
			setPosts(posts);
		}
		public void setPosts(List<SimplePost> posts){
			this.posts = posts;
		}

		@Override
		public int getCount() {
			if(posts!=null)
				return posts.size();
			return 0;
		}

		@Override
		public SimplePost getItem(int position) {
			return posts.get(position);
		}

		@Override
		public long getItemId(int position) {
			return posts.get(position).getId();
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
                convertView = mInflater.inflate(R.layout.fragment_list_post_row,parent,false);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.post_title);
                holder.category = (TextView) convertView.findViewById(R.id.post_category);
                holder.author = (TextView) convertView.findViewById(R.id.post_author);
                holder.date = (TextView) convertView.findViewById(R.id.post_date);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            holder.title.setText(posts.get(position).getTitle());
            holder.category.setText(posts.get(position).getCategory());
            holder.author.setText(posts.get(position).getAuthor());
            holder.date.setText(posts.get(position).getDate());

            return convertView;
		}
		
		private class ViewHolder{
			TextView title;
			TextView category;
			TextView author;
			TextView date;
		}
	}
}
