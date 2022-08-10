package com.akribase.archycards.wheelview;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by shayan on 10/20/17.
 */
public class StickyWheelView extends WheelView implements WheelView.OnWheelAngleChangeListener {


    Timer timer;
    TimerTask timerTask;

    boolean handleSticky;
    Handler uiHandler;

    OnWheelAngleChangeListener onWheelAngleChangeListener;

    public StickyWheelView(Context context) {
        super(context);
        init();
    }


    public StickyWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StickyWheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        super.setOnWheelAngleChangeListener(this);
        uiHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void setOnWheelAngleChangeListener(OnWheelAngleChangeListener listener) {
        this.onWheelAngleChangeListener = listener;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            handleSticky = false;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            handleSticky = true;
            onWheelAngleChange(getAngle());
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void onWheelAngleChange(float angle) {
        timer = invalidateTimerAndCreateNew(timer, timerTask);
        timerTask = new TimerTask() {
            @Override
            public void run() {
                goToNearestFixItem();
            }
        };

        timer.schedule(timerTask, 100);

        // call listener if is not null
        if (onWheelAngleChangeListener != null) {
            onWheelAngleChangeListener.onWheelAngleChange(angle);
        }
    }

    private void goToNearestFixItem() {


        if (!handleSticky) {
            return;
        }
        // getAngle is wide range, we map it too 0 - 360
        float correctAngle = Math.abs(getAngle()) % 360;

        // each item angle
        float itemAngle = 360 / getWheelItemCount();

        // item is currently sticky.
        if (correctAngle % itemAngle == 0) {
            return;
        }

        int power = Math.abs((int) (getAngle() / 360));

        float nextAngle = ((int) (correctAngle / itemAngle) + 1) * itemAngle + (360 * power);
        float preAngle = ((int) (correctAngle / itemAngle)) * itemAngle + (360 * power);
        int sign = getAngle() < 0 ? -1 : 1;


        if (correctAngle % itemAngle > itemAngle / 2) {
            goToAngleWithAnimation(nextAngle * sign);
        } else {
            goToAngleWithAnimation(preAngle * sign);
        }
    }

    private float temporaryFinalAngel;

    private void goToAngleWithAnimation(float angle) {
        temporaryFinalAngel = angle;

        final boolean increase;
        increase = getAngle() <= angle;

        uiHandler.post(new Runnable() {
            @Override
            public void run() {

                if (temporaryFinalAngel == getAngle()) {
                    return;
                }

                // user changed angle again, don't set angle again
                if (!handleSticky) {
                    return;
                }

                float nextAngle;
                if (increase) {
                    nextAngle = Math.min(getAngle() + 3, temporaryFinalAngel);
                } else {
                    nextAngle = Math.max(getAngle() - 3, temporaryFinalAngel);
                }

                setAngle(nextAngle);
                uiHandler.postDelayed(this, 10);
            }
        });
    }


    private Timer invalidateTimerAndCreateNew(Timer timer, TimerTask timerTask) {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }


        if (timerTask != null) {
            timerTask.cancel();
        }

        timer = new Timer();
        return timer;
    }
}
