package org.orange.querysystem.content;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
	Context mContext;
	Date mCurrentValue;

	DatePicker mDatePicker;

	public DatePickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mCurrentValue = new Date(DEFAULT_VALUE);
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
	protected Object onGetDefaultValue(TypedArray a, int index) {
		String date = a.getString(index);
		if(TextUtils.isEmpty(date)||!date.matches("\\d+[a-zA-Z]"))
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
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+08"), Locale.PRC);
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
		// TODO Auto-generated method stub
	}
}
