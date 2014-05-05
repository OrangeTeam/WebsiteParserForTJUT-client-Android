package org.orange.querysystem.content.dialog;

import org.orange.parser.entity.Course.TimeAndAddress;
import org.orange.parser.util.BitOperate;
import org.orange.querysystem.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


public class PeriodSettingDialog extends DialogFragment {

    /**
     * {@link PeriodSettingDialog#setArguments(Bundle)}参数的Key
     */
    public static final String PERIOD_KEY = PeriodSettingDialog.class.getName() + "period_key";

    /**
     * 课程多选框的选项
     */
    private static final String[] MULTI_CHOICE_ITEMS = new String[13];

    static {
        for (int i = 1; i <= 13; i++) {
            MULTI_CHOICE_ITEMS[i - 1] = String.valueOf(i);
        }
    }

    private final TimeAndAddress mPeriod = new TimeAndAddress();

    private PeriodSettingDialogListener mListener;

    /**
     * 构造此{@link DialogFragment}的参数
     *
     * @param period 待设置的节次。见{@link TimeAndAddress#getPeriod()}
     * @return {@link PeriodSettingDialog#setArguments(Bundle)}可用的参数
     * @see TimeAndAddress#setPeriod(int)
     */
    public static Bundle buildArgument(int period) {
        Bundle args = new Bundle();
        args.putInt(PERIOD_KEY, period);
        return args;
    }

    /**
     * 创建此类的新实例。
     *
     * @param period 待设置的节次。见{@link TimeAndAddress#getPeriod()}
     * @return 此类的新实例
     * @see TimeAndAddress#setPeriod(int)
     */
    public static PeriodSettingDialog newInstance(int period) {
        PeriodSettingDialog instance = new PeriodSettingDialog();
        instance.setArguments(buildArgument(period));
        return instance;
    }

    /**
     * 应用通过{@link PeriodSettingDialog#setArguments(Bundle)}传递过来的参数。即初始节次状态
     */
    public void applyArguments() {
        try {
            //TODO 常量
            mPeriod.setPeriod(getArguments().getInt(PERIOD_KEY, 0));
        } catch (BitOperate.BitOperateException e) {
            throw new IllegalArgumentException("非法参数" + getArguments().getInt(PERIOD_KEY, 0), e);
        }
    }

    /**
     * 取得当前设置的period
     *
     * @return 当前的period设置状态
     */
    public short getPeriod() {
        return mPeriod.getPeriod();
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
            // Instantiate the PeriodSettingDialog so we can send events to the host
            mListener = (PeriodSettingDialogListener) host;
        } catch (ClassCastException e) {
            // The parent doesn't implement the interface, throw exception
            throw new ClassCastException(host.toString()
                    + " must implement " + PeriodSettingDialogListener.class.getSimpleName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyArguments();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        boolean[] checkedItems = new boolean[13];
        System.arraycopy(mPeriod.getPeriodByBooleanArray(), 1, checkedItems, 0, 13);
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.period)
                .setMultiChoiceItems(MULTI_CHOICE_ITEMS, checkedItems,
                        new OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                    boolean isChecked) {
                                which++;
                                try {
                                    if (isChecked) {
                                        mPeriod.addPeriod(which);
                                    } else {
                                        mPeriod.removePeriod(which);
                                    }
                                } catch (BitOperate.BitOperateException e) {
                                    throw new IllegalArgumentException("非法参数：" + which, e);
                                }
                            }
                        }
                )
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogPositiveClick(PeriodSettingDialog.this, getPeriod());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        applyArguments();
                        mListener.onDialogNegativeClick(PeriodSettingDialog.this);
                    }
                })
                .create();
    }

    /**
     * The activity that creates an instance of this {@link PeriodSettingDialog} must
     * implement this interface in order to receive event callbacks.
     * Each method passes the {@link PeriodSettingDialog} in case the host needs to query it.
     */
    public static interface PeriodSettingDialogListener {

        /**
         * 当用户确认节次设置后调用
         *
         * @param dialog 触发此调用的{@link DialogFragment}
         * @param period 设置后的period值
         */
        public void onDialogPositiveClick(PeriodSettingDialog dialog, short period);

        public void onDialogNegativeClick(PeriodSettingDialog dialog);
    }
}
