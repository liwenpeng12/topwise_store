package com.topwise.topos.appstore.view;

import com.topwise.topos.appstore.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.CheckBox;
/**
 * @author xiaowenhui
 */
@SuppressLint("ClickableViewAccessibility")
public class CheckSwitchButton extends CheckBox {
	/** 画笔 */
	private Paint mSwitchButtonPaint;
	private Paint mOpenedBitmapPaint;
	private Paint mClosedBitmapPaint;
	private ViewParent mParent;
	private Bitmap mBottom_opened;
	private Bitmap mBottom_closed;
	private Bitmap mSwitchButton;
	/**
	 * 总的宽度
	 */
	private int mWidth;
	/**
	 * 总的高度
	 */
	private int mHeight;
	private final float VELOCITY = 200;
	/** 滑动速度 */
	private float mVelocity;
	private float mFirstDownY;
	private float mFirstDownX;
	/**
	 * 按钮的位置
	 */
	private float mBtnPos;
	/**
	 * 打开的时候按钮的位置
	 */
	private float mBtnOnPos;
	/**
	 * 关闭的时候按钮的位置
	 */
	private float mBtnOffPos;
	private float mBtnInitPos;
	private int mClickTimeout;
	private int mTouchSlop;
	private final int MAX_ALPHA = 255;
	private boolean mChecked = false;
	private boolean mBroadcasting;
	private boolean mTurningOn;
	private PerformClick mPerformClick;
	private OnCheckedChangeListener mOnCheckedChangeListener;
	private OnCheckedChangeListener mOnCheckedChangeWidgetListener;
	/** 判断是否在进行动画  */
	private boolean mAnimating;
	private float mAnimationPosition;
	private float mAnimatedVelocity;

	public CheckSwitchButton(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.checkboxStyle);
	}

	public CheckSwitchButton(Context context) {
		this(context, null);
	}

	public CheckSwitchButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	private void initView(Context context) {
		mSwitchButtonPaint = new Paint();
		mSwitchButtonPaint.setColor(Color.WHITE);
		mOpenedBitmapPaint = new Paint();
		//表示默认的时候是关闭的
		mOpenedBitmapPaint.setAlpha(0);
		mClosedBitmapPaint = new Paint();
		//表示默认的时候是开启的
		mClosedBitmapPaint.setAlpha(MAX_ALPHA);
		Resources resources = context.getResources();

		// get viewConfiguration
		mClickTimeout = ViewConfiguration.getPressedStateDuration()
				+ ViewConfiguration.getTapTimeout();
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

		// get Bitmap
		mBottom_opened = BitmapFactory.decodeResource(resources, R.drawable.zkas_switch_bottom_opened);
		mBottom_closed = BitmapFactory.decodeResource(resources,R.drawable.zkas_switch_bottom_closed);
		mSwitchButton = BitmapFactory.decodeResource(resources,R.drawable.zkas_switch_button);
		
		mWidth = mBottom_opened.getWidth();
		mHeight = mBottom_opened.getHeight();
		
		mBtnOffPos = 0;
		mBtnOnPos = mWidth - mSwitchButton.getWidth();
		mBtnPos = mChecked?mBtnOnPos:mBtnOffPos;
		
		final float density = getResources().getDisplayMetrics().density;
        mVelocity = (int) (VELOCITY * density + 0.5f);
		
		invalidate();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	public boolean isChecked() {
		return mChecked;
	}

	/** 自动判断切换至相反的属性 : true -->false ;false -->true */
	public void toggle() {
		setChecked(!mChecked);
	}

	/**
	 * 内部调用此方法设置checked状态，此方法会延迟执行各种回调函数，保证动画的流畅度
	 * 
	 * @param checked
	 */
	private void setCheckedDelayed(final boolean checked) {
		this.postDelayed(new Runnable() {

			@Override
			public void run() {
				setChecked(checked);
			}
		}, 10);
	}

	/**
	 * <p>
	 * Changes the checked state of this button.
	 * </p>
	 * 
	 * @param checked
	 *            true to check the button, false to uncheck it
	 */
	public void setChecked(boolean checked) {
		if (mChecked != checked) {
			mChecked = checked;

			mBtnPos = checked ? mBtnOnPos : mBtnOffPos;
			int alpha = (int) (Math.abs(mBtnPos) / (mBtnOnPos - mBtnOffPos) * MAX_ALPHA);
			mOpenedBitmapPaint.setAlpha(alpha);
			invalidate();

			if (mBroadcasting) {
				return;
			}
			mBroadcasting = true;
			if (mOnCheckedChangeListener != null) {
				mOnCheckedChangeListener.onCheckedChanged(CheckSwitchButton.this,mChecked);
			}
			if (mOnCheckedChangeWidgetListener != null) {
				mOnCheckedChangeWidgetListener.onCheckedChanged(CheckSwitchButton.this, mChecked);
			}

			mBroadcasting = false;
		}
	}

	/**
	 * Register a callback to be invoked when the checked state of this button
	 * changes.
	 * 
	 * @param listener
	 *            the callback to call on checked state change
	 */
	public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
		mOnCheckedChangeListener = listener;
	}

	/**
	 * Register a callback to be invoked when the checked state of this button
	 * changes. This callback is used for internal purpose only.
	 * 
	 * @param listener
	 *            the callback to call on checked state change
	 * @hide
	 */
	void setOnCheckedChangeWidgetListener(OnCheckedChangeListener listener) {
		mOnCheckedChangeWidgetListener = listener;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();
		float deltaX = Math.abs(x - mFirstDownX);
		float deltaY = Math.abs(y - mFirstDownY);
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			attemptClaimDrag();
			mFirstDownX = x;
			mFirstDownY = y;
			mBtnInitPos = mChecked ? mBtnOnPos : mBtnOffPos;
			break;
		case MotionEvent.ACTION_MOVE:
			// 拖动着的时间
			float time = event.getEventTime() - event.getDownTime();
			// 当前按钮的位置
			mBtnPos = mBtnInitPos + event.getX() - mFirstDownX;
			if (mBtnPos <= mBtnOffPos) {
				mBtnPos = mBtnOffPos;
			}
			if (mBtnPos >= mBtnOnPos) {
				mBtnPos = mBtnOnPos;
			}
			mTurningOn = mBtnPos > (mBtnOnPos - mBtnOffPos) / 2 ;
			int alpha = (int) (Math.abs(mBtnPos) / (mBtnOnPos - mBtnOffPos) * MAX_ALPHA);
			mOpenedBitmapPaint.setAlpha(alpha);
			break;
		case MotionEvent.ACTION_UP:
			time = event.getEventTime() - event.getDownTime();
			if (deltaY < mTouchSlop && deltaX < mTouchSlop
					&& time < mClickTimeout) {
				if (mPerformClick == null) {
					mPerformClick = new PerformClick();
				}
				if (!post(mPerformClick)) {
					performClick();
				}
			} else {
				startAnimation(mTurningOn);
			}
			break;
		}

		invalidate();
		return isEnabled();
	}

	private final class PerformClick implements Runnable {
		public void run() {
			performClick();
		}
	}

	@Override
	public boolean performClick() {
		startAnimation(!mChecked);
		if(mClickRunnable != null){
		    postDelayed(mClickRunnable, 200);
		}
		return true;
	}

	private void attemptClaimDrag() {
		mParent = getParent();
		if (mParent != null) {
			// 通知父类不要拦截touch事件
			mParent.requestDisallowInterceptTouchEvent(true);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(mBottom_closed, 0, 0, mClosedBitmapPaint);
		canvas.drawBitmap(mBottom_opened, 0, 0, mOpenedBitmapPaint);
		canvas.drawBitmap(mSwitchButton, mBtnPos, 0, mSwitchButtonPaint);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mWidth,mHeight);
	}

	private void startAnimation(boolean turnOn) {
		mTurningOn = turnOn;
		mAnimating = true;
		mAnimatedVelocity = turnOn ? mVelocity : -mVelocity;
		mAnimationPosition = mBtnPos;

		new SwitchAnimation().run();
	}

	private void stopAnimation() {
		mAnimating = false;
	}

	private final class SwitchAnimation implements Runnable {

		@Override
		public void run() {
			if (!mAnimating) {
				return;
			}
			doAnimation();
			FrameAnimationController.requestAnimationFrame(this);
		}
	}

	private void doAnimation() {
		mAnimationPosition += mAnimatedVelocity * FrameAnimationController.ANIMATION_FRAME_DURATION / 1000;
		if (mAnimationPosition >= mBtnOnPos) {
			stopAnimation();
			mAnimationPosition = mBtnOnPos;
			setCheckedDelayed(true);
		} else if (mAnimationPosition <= mBtnOffPos) {
			stopAnimation();
			mAnimationPosition = mBtnOffPos;
			setCheckedDelayed(false);
		}
		int alpha = (int) (Math.abs(mAnimationPosition) / (mBtnOnPos - mBtnOffPos) * MAX_ALPHA);
		mOpenedBitmapPaint.setAlpha(alpha);
		moveView(mAnimationPosition);
	}

	private void moveView(float position) {
		mBtnPos = position;
		invalidate();
	}
	
	private Runnable mClickRunnable;
	public void setClickRunnable(Runnable runable){
	    mClickRunnable = runable;
	}
	
	@Override
	protected void onDetachedFromWindow() {
	    super.onDetachedFromWindow();
	    removeCallbacks(mClickRunnable);
	}
}
