package com.moziy.hollerback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import com.moziy.hollerback.R;
import com.moziy.hollerback.view.FontManager;

public class CustomEditText extends EditText {

    private int mBorderColor;
    private float mBorderWidth;
    private Paint mBorderPaint;

    public CustomEditText(Context context) {
        this(context, null);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (!isInEditMode()) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomFont, defStyle, R.style.DefaultEditText);
            setTypefaceFromAttrs(a);
            a.recycle();
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
            mBorderPaint.setAntiAlias(true);
            mBorderPaint.setStyle(Style.STROKE);
        }
    }

    private void setTypefaceFromAttrs(TypedArray array) {
        String fontName = array.getString(R.styleable.CustomFont_typeface);
        Log.d("ct", "font name: " + fontName);
        if (fontName != null) {
            setTypeface(FontManager.getFont(fontName));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBorderPaint != null) {
            canvas.drawRect(getScrollX(), getScrollY(), getWidth() + getScrollX(), getHeight() + getScrollY(), mBorderPaint);
        }

    }
}
