package com.clorderclientapp.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class FontButtonRegularClass extends androidx.appcompat.widget.AppCompatButton{
    public FontButtonRegularClass(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface face = Typeface.createFromAsset(context.getAssets(), "Lora-Regular.ttf");
        this.setTypeface(face);
    }
}
