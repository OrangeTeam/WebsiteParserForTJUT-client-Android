package org.orange.querysystem.content;

import java.util.Calendar;
import java.util.Date;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

public class DatePickerPreference extends DialogPreference implements OnDateChangedListener{
	//TODO change it
	private static final long DEFAULT_VALUE = 0;
	private static final String ORANGE = "org.orange";
	private Context mContext;
	private Date mCurrentValue;
	private Date mMaxDate;
	private Date mMinDate;

	private DatePicker mDatePicker;

	public DatePickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPreference(context, attrs);
	}
	public DatePickerPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPreference(context, attrs);
	}
	private void initPreference(Context context, AttributeSet attrs) {
		mContext = context;
		mCurrentValue = new Date(DEFAULT_VALUE);
		setValuesFromXml(attrs);
	}
	/**
	 * 根据preferenceXML配置DatePicker的最早时间、最晚时间
	 * @param attrs XML中的属性集
	 * @throws IllegalArgumentException 如果最早时间晚于最晚时间
	 */
	private void setValuesFromXml(AttributeSet attrs) {
		Long minDate = null, maxDate = null, tempLong = null;
		long current = new Date().getTime();
		minDate = parseLong(attrs.getAttributeValue(ORANGE, "minDate"));
		maxDate = parseLong(attrs.getAttributeValue(ORANGE, "maxDate"));

		tempLong = parseLong(attrs.getAttributeValue(ORANGE, "minDateComparedWithRunTime"));
		if(minDate == null)
			minDate = current + tempLong;
		else if(tempLong != null) {
			tempLong += current;
			if(minDate < tempLong)
				minDate = tempLong;
		}//else minDate !=null && tempLong == null, do nothing
		tempLong = parseLong(attrs.getAttributeValue(ORANGE, "maxDateComparedWithRunTime"));
		if(maxDate == null)
			maxDate = current + tempLong;
		else if(tempLong != null) {
			tempLong += current;
			if(maxDate > tempLong)
				maxDate = tempLong;
		}//else maxDate !=null && tempLong == null, do nothing

		if(minDate != null)
			this.mMinDate = new Date(minDate);
		if(maxDate != null)
			this.mMaxDate = new Date(maxDate);
		if(mMinDate != null && mMaxDate != null && mMinDate.after(mMaxDate))
			throw new IllegalArgumentException("mMinDate after mMaxDate mMinDate:" 
					+ mMinDate + " mMaxDate:" + mMaxDate);
	}
	/**
	 * 把string解析为Long。sting可带一个字母后缀，若string为null返回null
	 * @param string 可带一个字母后缀的Long字符串
	 * @return string == null ? null : Long形式的string
	 */
	private Long parseLong(String string) {
		Long result = null;
		if(string != null) {
			if(string.matches("[+-]?\\d+"))
				result = Long.valueOf(string);
			else if(string.matches("[+-]?\\d+[a-zA-Z]"))
				result = Long.valueOf(string.substring(0, string.length()-1));
			else
				throw new NumberFormatException("数字\"" + string + "\"的格式错误");
		}
		return result;
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		if (restorePersistedValue) {
			// Restore existing state
			mCurrentValue = new Date(this.getPersistedLong(DEFAULT_VALUE));
		} else {
			// Set default state from the XML attribute
			mCurrentValue = new Date((Long) defaultValue);
			persistLong(mCurrentValue.getTime());
		}
	}

	@Override
	protected Long onGetDefaultValue(TypedArray a, int index) {
		String date = a.getString(index);
		if(TextUtils.isEmpty(date)||!date.matches("[+-]?\\d+[a-zA-Z]"))
			return DEFAULT_VALUE;
		else {
			try {
				return Long.parseLong(date.substring(0, date.length()-1));
			} catch (NumberFormatException e){
				return DEFAULT_VALUE;
			}
		}
	}

	@TargetApi(11)
	@Override
	protected View onCreateDialogView() {
		mDatePicker = new DatePicker(mContext);
		//Set View attributes
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			mDatePicker.setCalendarViewShown(false);
		Calendar c = Calendar.getInstance();
		c.setTime(mCurrentValue);
		mDatePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), this);
		if(mMinDate != null)
			mDatePicker.setMinDate(mMinDate.getTime());
		if(mMaxDate != null)
			mDatePicker.setMaxDate(mMaxDate.getTime());
		return mDatePicker;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if(positiveResult){
			mCurrentValue.setTime(getPickerTime());
			persistLong(mCurrentValue.getTime());
		}else{
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(mCurrentValue.getTime());
			mDatePicker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		}
	}
	private Long getPickerTime() {
		if(mDatePicker == null)
			return null;
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
		return calendar.getTimeInMillis();
	}

	private static class SavedState extends BaseSavedState {
		// Member that holds the setting's value
		// Change this data type to match the type saved by your Preference
		long currentTime;
		long pickerTime;

		public SavedState(Parcelable superState) {
			super(superState);
		}

		public SavedState(Parcel source) {
			super(source);
			// Get the current preference's value
			currentTime = source.readLong();  // Change this to read the appropriate data type
			pickerTime = source.readLong();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			// Write the preference's value
			dest.writeLong(currentTime);  // Change this to write the appropriate data type
			dest.writeLong(pickerTime);
		}

		// Standard creator object using an instance of this class
		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR =
				new Parcelable.Creator<SavedState>() {

			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		// Check whether this Preference is persistent (continually saved)
		Long pickerTime = getPickerTime();
		if (isPersistent() && (pickerTime==null || pickerTime==mCurrentValue.getTime())) {
			// No need to save instance state since it's persistent, use superclass state
			return superState;
		}

		// Create instance of custom BaseSavedState
		final SavedState myState = new SavedState(superState);
		// Set the state's value with the class member that holds current setting value
		myState.currentTime = mCurrentValue.getTime();
		myState.pickerTime = pickerTime;
		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		// Check whether we saved the state in onSaveInstanceState
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save the state, so call superclass
			super.onRestoreInstanceState(state);
			return;
		}

		// Cast state to custom BaseSavedState and pass to superclass
		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());

		// Set this Preference's widget to reflect the restored state
		mCurrentValue.setTime(myState.currentTime);
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(myState.pickerTime);
		mDatePicker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
	}

	@Override
	public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
	}
}
