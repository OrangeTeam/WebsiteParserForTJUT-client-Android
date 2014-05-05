package org.orange.querysystem.content.dialog;

import org.orange.querysystem.R;
import org.orange.querysystem.content.dialog.DayOfWeekSettingDialog.DayOfWeekSettingDialogListener;
import org.orange.querysystem.content.dialog.PeriodSettingDialog.PeriodSettingDialogListener;
import org.orange.querysystem.content.dialog.WeekSettingDialog.WeekSettingDialogListener;
import org.orange.querysystem.util.ParcelableTimeAndAddress;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

import util.BitOperate.BitOperateException;
import util.webpage.Course.TimeAndAddress;

/**
 * 用于设置时间地点的{@link DialogFragment}
 */
public class TimeAndAddressSettingDialog extends DialogFragment implements
        WeekSettingDialogListener, PeriodSettingDialogListener, DayOfWeekSettingDialogListener {

    /** {@link TimeAndAddressSettingDialog#setArguments(Bundle)}参数的Key，对应待设置的时间地点 */
    public static final String TIME_AND_ADDRESS_KEY =
            TimeAndAddressSettingDialog.class.getName() + "time_and_address_key";

    /** {@link TimeAndAddressSettingDialog#setArguments(Bundle)}参数的Key，对应回调时使用的识别标签 */
    public static final String TAG_KEY = TimeAndAddressSettingDialog.class.getName() + "tag_key";

    private static final String DIALOG_TAG = "time_and_address_item_dialog_tag";

    private static final int DIALOG_ALL = 0;

    private static final int DIALOG_WEEK = 1;

    private static final int DIALOG_DAY_OF_WEEK = 2;

    private static final int DIALOG_PERIOD = 3;

    private static final int DIALOG_ADDRESS = 4;

    private TimeAndAddress mTimeAndAddress;

    private TimeAndAddressSettingDialogListener mListener;

    private EditText mWeekInput;

    private EditText mDayOfWeekInput;

    private EditText mPeriodInput;

    private EditText mAddressInput;

    /**
     * 构造此{@link DialogFragment}的参数
     *
     * @param aTimeAndAddress 待设置时间地点
     * @param tag             确认或取消后，回调时使用的识别标签
     * @return {@link TimeAndAddressSettingDialog#setArguments(Bundle)}可用的参数
     */
    public static Bundle buildArgument(TimeAndAddress aTimeAndAddress, String tag) {
        Bundle args = new Bundle();
        args.putParcelable(TIME_AND_ADDRESS_KEY,
                aTimeAndAddress != null ? new ParcelableTimeAndAddress(aTimeAndAddress) : null);
        args.putString(TAG_KEY, tag);
        return args;
    }

    /**
     * 创建此类的新实例。
     *
     * @param aTimeAndAddress 待设置时间地点
     * @param tag             确认或取消后，回调时使用的识别标签
     * @return 此类的新实例
     */
    public static TimeAndAddressSettingDialog newInstance(TimeAndAddress aTimeAndAddress,
            String tag) {
        TimeAndAddressSettingDialog instance = new TimeAndAddressSettingDialog();
        instance.setArguments(buildArgument(aTimeAndAddress, tag));
        return instance;
    }

    /**
     * 应用通过{@link TimeAndAddressSettingDialog#setArguments(Bundle)}传递过来的参数。即初始时间地点状态
     */
    public void applyArgument() {
        TimeAndAddress args = getArguments().getParcelable(TIME_AND_ADDRESS_KEY);
        mTimeAndAddress = args != null ? new TimeAndAddress(args) : new TimeAndAddress();
    }

    /**
     * 取得当前时间地点设置状态的深拷贝
     *
     * @return 当前的时间地点设置状态
     */
    public TimeAndAddress getTimeAndAddress() {
        return new TimeAndAddress(mTimeAndAddress);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Object host = getParentFragment();
        if (host == null) {
            host = activity;
        }
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the TimeAndAddressSettingDialog so we can send events to the host
            mListener = (TimeAndAddressSettingDialogListener) host;
        } catch (ClassCastException e) {
            // The parent doesn't implement the interface, throw exception
            throw new ClassCastException(host.toString() + " must implement "
                    + TimeAndAddressSettingDialogListener.class.getSimpleName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyArgument();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //构建并设置对话框
        View dialogContent = getActivity().getLayoutInflater()
                .inflate(R.layout.time_and_adress_entry, null);

        mWeekInput = (EditText) dialogContent.findViewById(R.id.week_input);
        mDayOfWeekInput = (EditText) dialogContent.findViewById(R.id.day_of_week_input);
        mPeriodInput = (EditText) dialogContent.findViewById(R.id.period_input);
        mAddressInput = (EditText) dialogContent.findViewById(R.id.classroom_input);
        refreshDialog(DIALOG_ALL);
        mWeekInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_WEEK);
            }
        });
        mDayOfWeekInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_DAY_OF_WEEK);
            }
        });
        mPeriodInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_PERIOD);
            }
        });

        builder.setView(dialogContent)
                .setTitle(R.string.time_and_address)
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTimeAndAddress.setAddress(mAddressInput.getText().toString());
                        mListener.onDialogPositiveClick(TimeAndAddressSettingDialog.this,
                                getArguments().getString(TAG_KEY), getTimeAndAddress());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        applyArgument();
                        mListener.onDialogNegativeClick(TimeAndAddressSettingDialog.this,
                                getArguments().getString(TAG_KEY));
                    }
                });
        return builder.create();
    }

    /**
     * 根据{@link #mTimeAndAddress}刷新对话框
     *
     * @param id 要刷新的对话框元素
     */
    private void refreshDialog(int id) {
        boolean isNotAll = id != DIALOG_ALL;
        switch (id) {
            case DIALOG_ALL:
            case DIALOG_WEEK:
                if (!mTimeAndAddress.isEmpty(TimeAndAddress.Property.WEEK)) {
                    mWeekInput.setText(mTimeAndAddress.getWeekString());
                } else {
                    mWeekInput.setText(null);
                }
                if (isNotAll) {
                    return;
                }
            case DIALOG_DAY_OF_WEEK:
                if (!mTimeAndAddress.isEmpty(TimeAndAddress.Property.DAY)) {
                    mDayOfWeekInput.setText(mTimeAndAddress.getDayString(false));
                } else {
                    mDayOfWeekInput.setText(null);
                }
                if (isNotAll) {
                    return;
                }
            case DIALOG_PERIOD:
                if (!mTimeAndAddress.isEmpty(TimeAndAddress.Property.PERIOD)) {
                    mPeriodInput.setText(mTimeAndAddress.getPeriodString());
                } else {
                    mPeriodInput.setText(null);
                }
                if (isNotAll) {
                    return;
                }
            case DIALOG_ADDRESS:
                if (!mTimeAndAddress.isEmpty(TimeAndAddress.Property.ADDRESS)) {
                    mAddressInput.setText(mTimeAndAddress.getAddress());
                } else {
                    mAddressInput.setText(null);
                }
                return;
            default:
                throw new IllegalArgumentException("非法参数：" + id);
        }
    }

    private void showDialog(int id) {
        DialogFragment mDialogFragment = null;
        switch (id) {
            case DIALOG_WEEK:
                mDialogFragment = WeekSettingDialog.newInstance(mTimeAndAddress.getWeek());
                break;
            case DIALOG_DAY_OF_WEEK:
                mDialogFragment = DayOfWeekSettingDialog.newInstance(mTimeAndAddress.getDay());
                break;
            case DIALOG_PERIOD:
                mDialogFragment = PeriodSettingDialog.newInstance(mTimeAndAddress.getPeriod());
                break;
            default:
                throw new IllegalArgumentException("非法参数：" + id);
        }
        mDialogFragment.show(getChildFragmentManager(), DIALOG_TAG);
    }

    @Override
    public void onDialogPositiveClick(WeekSettingDialog dialog, int week) {
        try {
            mTimeAndAddress.setWeek(week);
            refreshDialog(DIALOG_WEEK);
        } catch (BitOperateException e) {
            throw new IllegalArgumentException("非法参数：" + week, e);
        }
    }

    @Override
    public void onDialogPositiveClick(DayOfWeekSettingDialog dialog,
            byte dayOfWeek) {
        try {
            mTimeAndAddress.setDay(dayOfWeek);
            refreshDialog(DIALOG_DAY_OF_WEEK);
        } catch (BitOperateException e) {
            throw new IllegalArgumentException("非法参数：" + dayOfWeek, e);
        }
    }

    @Override
    public void onDialogPositiveClick(PeriodSettingDialog dialog, short period) {
        try {
            mTimeAndAddress.setPeriod(period);
            refreshDialog(DIALOG_PERIOD);
        } catch (BitOperateException e) {
            throw new IllegalArgumentException("非法参数：" + period, e);
        }
    }

    @Override
    public void onDialogNegativeClick(WeekSettingDialog dialog) {
    }

    @Override
    public void onDialogNegativeClick(DayOfWeekSettingDialog dialog) {
    }

    @Override
    public void onDialogNegativeClick(PeriodSettingDialog dialog) {
    }

    /**
     * The activity that creates an instance of this {@link TimeAndAddressSettingDialog} must
     * implement this interface in order to receive event callbacks.
     * Each method passes the {@link TimeAndAddressSettingDialog} in case the host needs to query
     * it.
     */
    public static interface TimeAndAddressSettingDialogListener {

        /**
         * 当用户确认时间日期设置后被调用
         *
         * @param dialog          触发此调用的{@link DialogFragment}
         * @param tag             识别标签。如果实例化此{@link TimeAndAddressSettingDialog}时设置了识别标签，则返回之；否则返回null
         * @param aTimeAndAddress 设置后的时间日期
         */
        public void onDialogPositiveClick(TimeAndAddressSettingDialog dialog, String tag,
                TimeAndAddress aTimeAndAddress);

        public void onDialogNegativeClick(TimeAndAddressSettingDialog dialog, String tag);
    }

}
