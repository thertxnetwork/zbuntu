package com.zbuntu.terminal.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple terminal view that displays text output
 */
public class TerminalView extends View {
    private static final String TAG = "TerminalView";
    
    private static final int DEFAULT_TEXT_SIZE = 14;
    private static final int DEFAULT_ROWS = 24;
    private static final int DEFAULT_COLS = 80;
    
    private Paint mTextPaint;
    private Paint mBackgroundPaint;
    
    private float mFontWidth;
    private float mFontHeight;
    private float mFontLineSpacing;
    
    private List<String> mLines;
    private int mMaxLines = 1000;
    private int mScrollOffset = 0;
    
    private TerminalViewClient mClient;
    
    public interface TerminalViewClient {
        void onKeyDown(int keyCode, KeyEvent event);
        void onKeyUp(int keyCode, KeyEvent event);
        void onTextInput(String text);
    }
    
    public TerminalView(Context context) {
        this(context, null);
    }
    
    public TerminalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        mLines = new ArrayList<>();
        mLines.add("Zbuntu Terminal");
        mLines.add("Initializing...");
        
        // Setup text paint
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(DEFAULT_TEXT_SIZE * getResources().getDisplayMetrics().density);
        mTextPaint.setTypeface(Typeface.MONOSPACE);
        mTextPaint.setAntiAlias(true);
        
        // Setup background paint
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.BLACK);
        
        // Calculate font metrics
        Paint.FontMetrics metrics = mTextPaint.getFontMetrics();
        mFontHeight = metrics.descent - metrics.ascent;
        mFontLineSpacing = mFontHeight * 1.2f;
        mFontWidth = mTextPaint.measureText("M");
        
        setFocusable(true);
        setFocusableInTouchMode(true);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw background
        canvas.drawRect(0, 0, getWidth(), getHeight(), mBackgroundPaint);
        
        // Draw text lines
        int visibleLines = (int) (getHeight() / mFontLineSpacing) + 1;
        int startLine = Math.max(0, mLines.size() - visibleLines - mScrollOffset);
        int endLine = Math.min(mLines.size(), startLine + visibleLines);
        
        float y = mFontHeight;
        for (int i = startLine; i < endLine; i++) {
            String line = mLines.get(i);
            if (line != null) {
                canvas.drawText(line, 5, y, mTextPaint);
            }
            y += mFontLineSpacing;
        }
    }
    
    /**
     * Add text to terminal
     */
    public void appendText(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        
        String[] lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                // New line
                mLines.add("");
            }
            if (!lines[i].isEmpty()) {
                // Append to current line or add new line
                if (mLines.isEmpty()) {
                    mLines.add(lines[i]);
                } else {
                    int lastIndex = mLines.size() - 1;
                    String currentLine = mLines.get(lastIndex);
                    mLines.set(lastIndex, currentLine + lines[i]);
                }
            }
        }
        
        // Limit number of lines
        while (mLines.size() > mMaxLines) {
            mLines.remove(0);
        }
        
        invalidate();
    }
    
    /**
     * Clear terminal
     */
    public void clear() {
        mLines.clear();
        mScrollOffset = 0;
        invalidate();
    }
    
    public void setClient(TerminalViewClient client) {
        mClient = client;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            requestFocus();
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
            return true;
        }
        return super.onTouchEvent(event);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mClient != null) {
            mClient.onKeyDown(keyCode, event);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mClient != null) {
            mClient.onKeyUp(keyCode, event);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
    
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = EditorInfo.TYPE_NULL;
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN;
        return new TerminalInputConnection(this, true);
    }
    
    public int getRows() {
        return (int) (getHeight() / mFontLineSpacing);
    }
    
    public int getColumns() {
        return (int) (getWidth() / mFontWidth);
    }
    
    /**
     * Inner class for handling input
     */
    private class TerminalInputConnection extends android.view.inputmethod.BaseInputConnection {
        public TerminalInputConnection(View targetView, boolean fullEditor) {
            super(targetView, fullEditor);
        }
        
        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            if (mClient != null && text != null) {
                mClient.onTextInput(text.toString());
            }
            return true;
        }
        
        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            // Handle backspace - send multiple backspace characters if needed
            if (beforeLength > 0 && mClient != null) {
                StringBuilder backspaces = new StringBuilder();
                for (int i = 0; i < beforeLength; i++) {
                    backspaces.append("\b");
                }
                mClient.onTextInput(backspaces.toString());
            }
            return true;
        }
    }
}
