package com.clorderclientapp.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class FontTextViewRegularClass extends AppCompatTextView {
    public FontTextViewRegularClass(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Typeface face = Typeface.createFromAsset(context.getAssets(), "Lora-Regular.ttf");
        this.setTypeface(face);
    }
}