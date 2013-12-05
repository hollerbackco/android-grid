package com.moziy.hollerback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.TextView;

import com.moziy.hollerback.R;
import com.moziy.hollerback.view.FontManager;

public class CustomTextView extends TextView {

    private int mBorderColor;
    private float mBorderWidth;
    private Paint mBorderPaint;

    public CustomTextView(Context context) {
        this(context, null);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (!isInEditMode()) {
            // retrieve the attributes pertaining to the custom textview
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CustomFont, defStyle, R.style.DefaultTextView);
            setTypefaceFromAttrs(array);
            array.recycle(); // recycle the array
        }

        if (!isInEditMode()) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomView, defStyle, R.style.DefaultEditText);
            mBorderWidth = a.getDimension(R.styleable.CustomView_borderWidth, 0f);
            mBorderColor = a.getColor(R.styleable.CustomView_borderColor, Color.parseColor("#00000000"));
            a.recycle();
        }

        if (mBorderWidth > 0) {
            mBorderPaint = new Paint();
            mBorderPaint.setColor(mBorderColor);
            mBorderPaint.setStrokeWidth(mBorderWidth);
            mBorderPaint.setStyle(Style.STROKE);
        }
    }

    private void setTypefaceFromAttrs(TypedArray array) {
        String fontName = array.getString(R.styleable.CustomFont_typeface);
        if (fontName != null) {
            setTypeface(FontManager.getFont(fontName));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBorderPaint != null) {
            canvas.drawRect(0, getScrollY(), getWidth(), getHeight() + getScrollY(), mBorderPaint);
        }

    }

}
