package com.topwise.topos.appstore.view.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class FitSystemWindowFrameLayout extends FrameLayout {

	public FitSystemWindowFrameLayout(Context context) {
		this(context, null);
	}

	public FitSystemWindowFrameLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FitSystemWindowFrameLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		setFitsSystemWindows(true);
	}

	@Override
	protected boolean fitSystemWindows(Rect insets) {
		insets.top = 0;
		super.fitSystemWindows(insets);
		return true;
	}
}
