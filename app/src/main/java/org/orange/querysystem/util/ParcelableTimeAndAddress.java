package org.orange.querysystem.util;

import org.orange.parser.entity.Course.TimeAndAddress;
import org.orange.parser.util.BitOperate.BitOperateException;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableTimeAndAddress extends TimeAndAddress implements Parcelable {

    public ParcelableTimeAndAddress() {
        super();
    }

    private ParcelableTimeAndAddress(Parcel in) throws BitOperateException {
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

    public static ParcelableTimeAndAddress wrap(TimeAndAddress timeAndAddress) {
        if (timeAndAddress instanceof ParcelableTimeAndAddress) {
            return (ParcelableTimeAndAddress) timeAndAddress;
        } else {
            try {
                ParcelableTimeAndAddress wrapper = new ParcelableTimeAndAddress();
                wrapper.setWeek(timeAndAddress.getWeek());
                wrapper.setDay(timeAndAddress.getDay());
                wrapper.setPeriod(timeAndAddress.getPeriod());
                wrapper.setAddress(timeAndAddress.getAddress());
                return wrapper;
            } catch (BitOperateException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static final Parcelable.Creator<ParcelableTimeAndAddress> CREATOR =
            new Parcelable.Creator<ParcelableTimeAndAddress>() {
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
