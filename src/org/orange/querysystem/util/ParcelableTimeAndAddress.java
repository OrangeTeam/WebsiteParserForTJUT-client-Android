package org.orange.querysystem.util;

import util.BitOperate.BitOperateException;
import util.webpage.Course.TimeAndAddress;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableTimeAndAddress extends TimeAndAddress implements Parcelable{
	/**
	 * 拷贝构造方法
	 * @param aTimeAndAddress 被复制的时间地点
	 * @see TimeAndAddress#TimeAndAddress(TimeAndAddress)
	 */
	public ParcelableTimeAndAddress(TimeAndAddress aTimeAndAddress){
		super(aTimeAndAddress);
	}
	private ParcelableTimeAndAddress(Parcel in) throws BitOperateException{
		super(in.readInt(), in.readByte(), in.readInt(), in.readString());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(getWeek());
		dest.writeByte(getDay());
		dest.writeInt(getPeriod());
		dest.writeString(getAddress());
	}

	public static final Parcelable.Creator<ParcelableTimeAndAddress> CREATOR =
			new Parcelable.Creator<ParcelableTimeAndAddress>(){
				@Override
				public ParcelableTimeAndAddress createFromParcel(Parcel in) {
					try {
						return new ParcelableTimeAndAddress(in);
					} catch (BitOperateException e) {
						throw new IllegalArgumentException("非法参数：" + in, e);
					}
				}
				@Override
				public ParcelableTimeAndAddress[] newArray(int size) {
					return new ParcelableTimeAndAddress[size];
				}
	};
}
