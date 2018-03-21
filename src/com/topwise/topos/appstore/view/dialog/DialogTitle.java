package com.topwise.topos.appstore.view.dialog;

import com.topwise.topos.appstore.R;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * @hide 
 */
public class DialogTitle extends TextView {
	
	private TypedValue outValue = new TypedValue();
	
	public DialogTitle(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public DialogTitle(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DialogTitle(Context context) {
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final Layout layout = getLayout();
		if (layout != null) {
			final int lineCount = layout.getLineCount();
			if (lineCount > 0) {
				final int ellipsisCount = layout
						.getEllipsisCount(lineCount - 1);
				if (ellipsisCount > 0) {
					setSingleLine(false);
					setMaxLines(2);
					
			        getContext().getResources().getValue(R.dimen.BASIC_FONT_SIZE_D, outValue, true);
			        int textSizeUnit = (outValue.data >> TypedValue.COMPLEX_UNIT_SHIFT)& TypedValue.COMPLEX_UNIT_MASK;
			        int textSize = (int)TypedValue.complexToFloat(outValue.data);
			        setTextSize(textSizeUnit, textSize);

					super.onMeasure(widthMeasureSpec, heightMeasureSpec);
				}
			}
		}
	}

}
