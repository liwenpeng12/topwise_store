package com.topwise.topos.appstore.view.widget;

import android.graphics.Paint;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

public class StrikeSpan extends CharacterStyle implements UpdateAppearance, ParcelableSpan {

    public StrikeSpan() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getSpanTypeId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        // TODO Auto-generated method stub
        tp.setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        tp.setAntiAlias(true);
    }

    /** @hide */
    public int getSpanTypeIdInternal() {
      return 0;
      }

    /** @hide */
    public void writeToParcelInternal(Parcel dest, int flags) {}

}
