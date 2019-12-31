package com.clorderclientapp.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

public class FontButtonRegularClass extends android.support.v7.widget.AppCompatButton{
    public FontButtonRegularClass(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface face = Typeface.createFromAsset(context.getAssets(), "Lora-Regular.ttf");
        this.setTypeface(face);
    }
}
