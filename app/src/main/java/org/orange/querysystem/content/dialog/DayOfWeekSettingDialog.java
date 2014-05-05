package org.orange.querysystem.content.dialog;

import org.orange.querysystem.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import util.BitOperate.BitOperateException;
import util.webpage.Course.TimeAndAddress;

public class DayOfWeekSettingDialog extends DialogFragment {

    /** {@link DayOfWeekSettingDialog#setArguments(Bundle)}参数的Key */
    public static final String DAY_OF_WEEK_KEY = DayOfWeekSettingDialog.class.getName()
            + "day_of_week_key";

    /** 课程多选框的选项 */
    private final String[] MULTI_CHOICE_ITEMS = new String[7];

    private final TimeAndAddress mDayOfWeek = new TimeAndAddress();

    private DayOfWeekSettingDialogListener mListener;

    /**
     * 构造此{@link DialogFragment}的参数
     *
     * @param dayOfWeek 待设置的星期。见{@link TimeAndAddress#getDay()}
     * @return {@link DayOfWeekSettingDialog#setArguments(Bundle)}可用的参数
     * @see TimeAndAddress#setDay(int)
     */
    public static Bundle buildArgument(int dayOfWeek) {
        Bundle args = new Bundle();
        args.putInt(DAY_OF_WEEK_KEY, dayOfWeek);
        return args;
    }

    /**
     * 创建此类的新实例。
     *
     * @param dayOfWeek 待设置的星期。见{@link TimeAndAddress#getDay()}
     * @return 此类的新实例
     * @see TimeAndAddress#setDay(int)
     */
    public static DayOfWeekSettingDialog newInstance(int dayOfWeek) {
        DayOfWeekSettingDialog instance = new DayOfWeekSettingDialog();
        instance.setArguments(buildArgument(dayOfWeek));
        return instance;
    }

    /**
     * 应用通过{@link DayOfWeekSettingDialog#setArguments(Bundle)}传递过来的参数。即初始星期选择状态
     */
    public void applyArguments() {
        try {
            //TODO 常量
            mDayOfWeek.setDay(getArguments().getInt(DAY_OF_WEEK_KEY, 0));
        } catch (BitOperateException e) {
            throw new IllegalArgumentException("非法参数" + getArguments().getInt(DAY_OF_WEEK_KEY, 0),
                    e);
        }
    }

    /**
     * 取得当前设置的day
     *
     * @return 当前的day设置状态
     * @see TimeAndAddress#getDay()
     */
    public byte getDay() {
        return mDayOfWeek.getDay();
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
            // Instantiate the DayOfWeekSettingDialog so we can send events to the host
            mListener = (DayOfWeekSettingDialogListener) host;
        } catch (ClassCastException e) {
            // The parent doesn't implement the interface, throw exception
            throw new ClassCastException(host.toString()
                    + " must implement " + DayOfWeekSettingDialogListener.class.getSimpleName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyArguments();
        System.arraycopy(getResources().getStringArray(R.array.days_of_week), 1, MULTI_CHOICE_ITEMS,
                0, 7);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //checkedItems是周一到周日，temp是周日到周六
        boolean[] checkedItems = new boolean[7], temp = mDayOfWeek.getDayByBooleanArray();
        System.arraycopy(temp, 1, checkedItems, 0, 6);
        checkedItems[6] = temp[0];
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.day_of_week)
                .setMultiChoiceItems(MULTI_CHOICE_ITEMS, checkedItems,
                        new OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                    boolean isChecked) {
                                which++;
                                try {
                                    if (isChecked) {
                                        mDayOfWeek.addDay(which);
                                    } else {
                                        mDayOfWeek.removeDay(which);
                                    }
                                } catch (BitOperateException e) {
                                    throw new IllegalArgumentException("非法参数：" + which, e);
                                }
                            }
                        }
                )
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogPositiveClick(DayOfWeekSettingDialog.this, getDay());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        applyArguments();
                        mListener.onDialogNegativeClick(DayOfWeekSettingDialog.this);
                    }
                })
                .create();
    }

    /**
     * The activity that creates an instance of this {@link DayOfWeekSettingDialog} must
     * implement this interface in order to receive event callbacks.
     * Each method passes the {@link DayOfWeekSettingDialog} in case the host needs to query it.
     */
    public static interface DayOfWeekSettingDialogListener {

        /**
         * 当用户确认星期设置后调用
         *
         * @param dialog    触发此调用的{@link DialogFragment}
         * @param dayOfWeek 设置后的day状态值
         */
        public void onDialogPositiveClick(DayOfWeekSettingDialog dialog, byte dayOfWeek);

        public void onDialogNegativeClick(DayOfWeekSettingDialog dialog);
    }
}
