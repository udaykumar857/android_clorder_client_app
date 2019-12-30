package com.clorderclientapp.utils;

import android.content.Context;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatEditText;
import android.util.AttributeSet;

public class FontEditTextRegularClass extends AppCompatEditText{
    public FontEditTextRegularClass(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface face = Typeface.createFromAsset(context.getAssets(), "Lora-Regular.ttf");
        this.setTypeface(face);
    }
}
