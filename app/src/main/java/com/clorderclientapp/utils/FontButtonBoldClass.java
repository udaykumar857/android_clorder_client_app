package com.clorderclientapp.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

public class FontButtonBoldClass extends AppCompatButton {
    public FontButtonBoldClass(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Typeface face = Typeface.createFromAsset(context.getAssets(), "Lora-Bold.ttf");
        this.setTypeface(face);
    }
}
