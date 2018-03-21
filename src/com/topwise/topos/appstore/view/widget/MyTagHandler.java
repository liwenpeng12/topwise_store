package com.topwise.topos.appstore.view.widget;

import org.xml.sax.XMLReader;

import android.text.Editable;
import android.text.Html.TagHandler;
import android.text.Spanned;

public class MyTagHandler implements TagHandler {
    private int sIndex = 0;  
    private  int eIndex=0;
    public MyTagHandler() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (tag.toLowerCase().equals("strike")) {
            if (opening) {
                sIndex=output.length();
            }else {
                eIndex=output.length();
                output.setSpan(new StrikeSpan(), sIndex, eIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

}
