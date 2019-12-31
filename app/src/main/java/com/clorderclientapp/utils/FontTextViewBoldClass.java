package com.clorderclientapp.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class FontTextViewBoldClass extends AppCompatTextView {
    public FontTextViewBoldClass(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Typeface face = Typeface.createFromAsset(context.getAssets(), "Lora-Bold.ttf");
        this.setTypeface(face);
    }
}
