package com.msolo.dynamiceffect.devil_yiqi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by mSolo on 2015/4/17.
 */
public class DyRotateEffectView extends View {

    private static final String TAG= "DyRotateEffectView";

    private static final int TRANSLATION_LINE = 1;
    private static final int ROTATION_LINE = 2;
    private static final int BOUND_PLUS = 3;
    private static final int RECOVER_PLUS = 4;
    private static final int ROTATION_PLUS = 5;
    private static final int BOUND_X = 6;
    private static final int BOUND_DOUBLE_X = 7;
    private static final int ADD_CIRCLE_X = 8;
    private static final int CHANGE_STYLE_ONE_CIRCLE = 9;
    private static final int CHANGE_STYLE_TWO_CIRCLE = 10;

    private static final int REFRESH_TIME_MS = 400;

    public static final int COLOR_WHITE = 0xFFFFFFFF;
    public static final int COLOR_BLACK = 0xFF000000;
    public static final int COLOR_GRAY = 0xFFF2F1F1;
    public static final int COLOR_RED = 0xFFBE0D15;
    public static final int COLOR_GREEN = 0xFF00C600;
    public static final int COLOR_YELLOW = 0xFFFFFF00;
    public static final int COLOR_ORANGE = 0xFFEC8C67;
    public static final int COLOR_BLUE = 0xFF249FF1;

    private static final int STROKE_WIDTH = 6;
    private static final int LINE_MAX_LENGTH = 64;
    private static final int CIRCLE_RADIUS = LINE_MAX_LENGTH * 3/4;
    private static final int CIRCLE_STEP_RADIUS = LINE_MAX_LENGTH/20;
    private static final int RADIUS_STOP = LINE_MAX_LENGTH / 2;
    private static final double ROTATE_STOP_X1_ANGLE = Math.toRadians(135.0);
    private static final double ROTATE_STOP_X2_ANGLE = Math.toRadians(-90.0);
    private static final double ROTATE_STOP_V_ANGLE = Math.toRadians(90.0);
    private static final double ROTATE_STOP_T_ANGLE = Math.toRadians(45.0);
    private static final double ROTATE_STEP_ANGLE = Math.toRadians(3.0);
    private static final int BOUND_ENLAGER_FACTOR = 6;

    private float mFinalStartX;
    private float mFinalStartY;

    private Paint mLinePaint;
    private int mParentWidth;
    private int mParentHeight;
    private int mStartX;
    private int mStartY;
    private int mTranslateStopX;
    private double mStartRotateAngle = ROTATE_STEP_ANGLE;
    private int mDrawIndicator = 0;
    private float mBoundFactor = 0;
    private float mCircleRadius = 0;

    private boolean mIsDrawSmallHalfCircle = false;

    private float mFinalLeftX = 0;
    private float mFinalRightX = 0;
    private float mFinalTopY = 0;
    private float mFinalBottomY = 0;

    private Canvas mCanvas;

    public DyRotateEffectView(Context context) {
        super(context);
        setUp(context);
    }

    public DyRotateEffectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp(context);
    }

    public DyRotateEffectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUp(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }

    private void init() {

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(COLOR_ORANGE);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mLinePaint.setStrokeWidth(STROKE_WIDTH);

        mBoundFactor = 0;
        mCircleRadius = CIRCLE_RADIUS;
        mDrawIndicator = TRANSLATION_LINE;

        mIsDrawSmallHalfCircle = false;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
        mParentWidth = getWidth();
        mParentHeight = getHeight();
        mTranslateStopX = (mParentWidth - LINE_MAX_LENGTH) / 2;
        mStartX = mParentWidth/4;
        mStartY = (mParentHeight - STROKE_WIDTH)/2;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    private int measureWidth(int measureSpec) {

        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if(mode == MeasureSpec.UNSPECIFIED){
            result = getSuggestedMinimumWidth();
        } else {
            result = size;
        }
        return result;
    }

    private int measureHeight(int measureSpec) {

        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if(mode == MeasureSpec.UNSPECIFIED){
            result = getSuggestedMinimumHeight();
        } else {
            result = size;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mCanvas = canvas;

        switch (mDrawIndicator) {
            case TRANSLATION_LINE:
                drawTranslateLine();
                break;
            case ROTATION_LINE:
                drawFinalHLine(0);
                drawRotatedLine();
                break;
            case BOUND_PLUS:
            case RECOVER_PLUS:
                drawFinalHLine(mBoundFactor);
                drawFinalVLine(mBoundFactor);
                break;
            case ROTATION_PLUS:
                drawRotatedPlus();
                break;
            case BOUND_X:
                drawFinalMaxX(mBoundFactor);
                break;
            case ADD_CIRCLE_X:
                drawFinalMaxX(mBoundFactor);
                mCanvas.drawCircle(mParentWidth/2, mParentHeight/2, mCircleRadius, mLinePaint);
                if (mIsDrawSmallHalfCircle) {
                    drawSmallHalfCircle(true);
                    mStartRotateAngle = ROTATE_STOP_X1_ANGLE;
                    mIsDrawSmallHalfCircle = false;
                    mHandler.sendEmptyMessageDelayed(CHANGE_STYLE_ONE_CIRCLE, REFRESH_TIME_MS);
                }
                break;
            case CHANGE_STYLE_ONE_CIRCLE:
                if (mIsDrawSmallHalfCircle) {
                    mLinePaint.setColor(COLOR_GREEN);
                    mCanvas.drawLine(mFinalLeftX, mFinalTopY, mFinalRightX, mFinalBottomY, mLinePaint);
                    drawSmallHalfCircle(false);
                    mIsDrawSmallHalfCircle = false;
                } else {
                    drawStyleOneRotatedLine();
                    mLinePaint.setColor(COLOR_ORANGE);
                }
                mCanvas.drawCircle(mParentWidth/2, mParentHeight/2, mCircleRadius, mLinePaint);
                break;
            case CHANGE_STYLE_TWO_CIRCLE:
                if (mIsDrawSmallHalfCircle) {
                    mLinePaint.setColor(COLOR_ORANGE);
                    mCanvas.drawLine(mFinalLeftX, mFinalTopY, mFinalRightX, mFinalBottomY, mLinePaint);
                    mCanvas.drawLine(mFinalRightX, mFinalTopY, mFinalLeftX, mFinalBottomY, mLinePaint);
                    drawSmallHalfCircle(true);
                } else {
                    drawStyleTwoRotatedLine();
                }
                mCanvas.drawCircle(mParentWidth / 2, mParentHeight / 2, mCircleRadius, mLinePaint);
                break;
            default:
                break;
        }

    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TRANSLATION_LINE:
                    drawTranslateLine();
                    break;
                case ROTATION_LINE:
                    mDrawIndicator = ROTATION_LINE;
                    drawRotatingLine();
                    break;
                case BOUND_PLUS:
                    mDrawIndicator = BOUND_PLUS;
                    drawBoundPlus();
                    break;
                case RECOVER_PLUS:
                    drawPlus();
                    break;
                case ROTATION_PLUS:
                    mDrawIndicator = ROTATION_PLUS;
                    drawRotatingPlus();
                    break;
                case BOUND_X:
                    mDrawIndicator = BOUND_X;
                    drawBoundFinalMaxX();
                    break;
                case ADD_CIRCLE_X:
                    mDrawIndicator = ADD_CIRCLE_X;
                    drawNarrowingCircleX();
                    break;
                case CHANGE_STYLE_ONE_CIRCLE:
                    mDrawIndicator = CHANGE_STYLE_ONE_CIRCLE;
                    drawChangeStyleOneCircle();
                    break;
                case CHANGE_STYLE_TWO_CIRCLE:
                    mDrawIndicator = CHANGE_STYLE_TWO_CIRCLE;
                    drawChangeStyleTwoCircle();
                    break;
                default:
                    break;
            }
        };
    };

    private void drawTranslateLine() {

        if ( mStartX < mTranslateStopX) {
            mCanvas.drawLine(mStartX, mStartY, mStartX + LINE_MAX_LENGTH, mStartY, mLinePaint);
            mStartX += STROKE_WIDTH;
            invalidate();
            mHandler.sendEmptyMessageDelayed(TRANSLATION_LINE, REFRESH_TIME_MS);
        } else {
            drawFinalHLine(0);
            mHandler.sendEmptyMessageDelayed(ROTATION_LINE, REFRESH_TIME_MS);
        }

    }

    private void drawRotatingLine() {

        if (mStartRotateAngle <= ROTATE_STOP_V_ANGLE) {
            mFinalStartX = getNextRotateStartX(mStartRotateAngle, RADIUS_STOP);
            mFinalStartY = getNextRotateStartY(mStartRotateAngle, RADIUS_STOP);
            invalidate();
            mStartRotateAngle += ROTATE_STEP_ANGLE;
            if (mStartRotateAngle <= ROTATE_STOP_V_ANGLE) {
                mHandler.sendEmptyMessageDelayed(ROTATION_LINE, REFRESH_TIME_MS);
            } else {
                mDrawIndicator = BOUND_PLUS;
                invalidate();
                mHandler.sendEmptyMessageDelayed(BOUND_PLUS, REFRESH_TIME_MS / 2);
            }
        }

    }

    private void drawBoundPlus() {

        mBoundFactor = BOUND_ENLAGER_FACTOR;
        mLinePaint.setStrokeWidth(STROKE_WIDTH * 3 / 2);
        invalidate();
        mHandler.sendEmptyMessageDelayed(RECOVER_PLUS, REFRESH_TIME_MS);

    }

    private void drawPlus() {

        mBoundFactor = -BOUND_ENLAGER_FACTOR/2;
        mLinePaint.setStrokeWidth(STROKE_WIDTH);
        invalidate();

        mStartRotateAngle = ROTATE_STEP_ANGLE;
        mHandler.sendEmptyMessageDelayed(ROTATION_PLUS, REFRESH_TIME_MS/2);

    }

    private void drawRotatingPlus() {

        mFinalStartX = getNextRotateStartX(mStartRotateAngle, RADIUS_STOP);
        mFinalStartY = getNextRotateStartY(mStartRotateAngle, RADIUS_STOP);
        if (mStartRotateAngle < ROTATE_STOP_T_ANGLE) {
            invalidate();
            mStartRotateAngle += ROTATE_STEP_ANGLE*2;
            mHandler.sendEmptyMessageDelayed(ROTATION_PLUS, 0);
        } else {
            mBoundFactor = BOUND_ENLAGER_FACTOR / 2;
            mDrawIndicator = BOUND_X;
            invalidate();
            mHandler.sendEmptyMessageDelayed(BOUND_X, REFRESH_TIME_MS);
        }
    }

    private void drawBoundFinalMaxX() {
        if (mBoundFactor < BOUND_ENLAGER_FACTOR && mBoundFactor > 0) {
            mBoundFactor = BOUND_ENLAGER_FACTOR;
            mHandler.sendEmptyMessageDelayed(BOUND_X, REFRESH_TIME_MS);
        } else {
            mLinePaint.setStrokeWidth(STROKE_WIDTH / 2);
            mLinePaint.setStyle(Paint.Style.STROKE);
            mBoundFactor = 0;
            mHandler.sendEmptyMessageDelayed(ADD_CIRCLE_X, REFRESH_TIME_MS);
        }
        invalidate();
    }

    private void drawNarrowingCircleX() {

        if (mCircleRadius >= LINE_MAX_LENGTH/2) {
            invalidate();
            mCircleRadius -= CIRCLE_STEP_RADIUS;
            mBoundFactor -= 4;
            mHandler.sendEmptyMessageDelayed(ADD_CIRCLE_X, 0);
        } else {
            mIsDrawSmallHalfCircle = true;
            invalidate();
        }

    }

    private void drawSmallHalfCircle(boolean isFour) {

        float distanceSmallCircle = (float) Math.sin(ROTATE_STOP_T_ANGLE) * RADIUS_STOP;

        float rightX = mParentWidth/2 - distanceSmallCircle + STROKE_WIDTH/2;
        float leftX = mParentWidth/2 + distanceSmallCircle - STROKE_WIDTH/2;
        float topY = mParentHeight/2 - distanceSmallCircle + STROKE_WIDTH/2;
        float bottomY = mParentHeight/2 + distanceSmallCircle - STROKE_WIDTH/2;

        float smallCircleVal = RADIUS_STOP/10;

        RectF oval=new RectF();

        oval.left=rightX-smallCircleVal;
        oval.top=topY-smallCircleVal;
        oval.right=rightX+smallCircleVal;
        oval.bottom=topY+smallCircleVal;
        mCanvas.drawArc(oval, -45, 180, true, mLinePaint);

        oval.left=leftX-smallCircleVal;
        oval.top=bottomY-smallCircleVal;
        oval.right=leftX+smallCircleVal;
        oval.bottom=bottomY+smallCircleVal;
        mCanvas.drawArc(oval, -225, 180, true, mLinePaint);

        if (!isFour) {
            return ;
        }

        oval.left=leftX-smallCircleVal;
        oval.top=topY-smallCircleVal;
        oval.right=leftX+smallCircleVal;
        oval.bottom=topY+smallCircleVal;
        mCanvas.drawArc(oval, 45, 180, true, mLinePaint);

        oval.left=rightX-smallCircleVal;
        oval.top=bottomY-smallCircleVal;
        oval.right=rightX+smallCircleVal;
        oval.bottom=bottomY+smallCircleVal;
        mCanvas.drawArc(oval, -135, 180, true, mLinePaint);

    }

    private void drawChangeStyleOneCircle() {

        if (mStartRotateAngle > 0) {
            mFinalStartX = getNextRotateStartX(mStartRotateAngle, (LINE_MAX_LENGTH + mBoundFactor)/2);
            mFinalStartY = getNextRotateStartY(mStartRotateAngle, (LINE_MAX_LENGTH + mBoundFactor)/6);
            invalidate();
            mStartRotateAngle -= 2*ROTATE_STEP_ANGLE;
            if (mStartRotateAngle > 0) {
                mHandler.sendEmptyMessageDelayed(CHANGE_STYLE_ONE_CIRCLE, 0);
            } else {
                mIsDrawSmallHalfCircle = true;
                invalidate();
                mStartRotateAngle = 0;
                mHandler.sendEmptyMessageDelayed(CHANGE_STYLE_TWO_CIRCLE, REFRESH_TIME_MS);
            }
        }

    }

    private void drawChangeStyleTwoCircle() {

        if (mStartRotateAngle > ROTATE_STOP_X2_ANGLE) {
            mFinalStartX = getNextRotateStartX(mStartRotateAngle, (LINE_MAX_LENGTH + mBoundFactor)/2);
            mFinalStartY = getNextRotateStartY(mStartRotateAngle, (LINE_MAX_LENGTH + mBoundFactor)/6);
            invalidate();
            mStartRotateAngle -= 2*ROTATE_STEP_ANGLE;
            if (mStartRotateAngle > ROTATE_STOP_X2_ANGLE) {
                mHandler.sendEmptyMessageDelayed(CHANGE_STYLE_TWO_CIRCLE, 0);
            } else {
                mIsDrawSmallHalfCircle = true;
                invalidate();
            }
        }

    }

    private void setUp(Context context) {
    }

    private float getNextRotateStartX(double angle, float lineLength) {
        return (float)(Math.cos(angle) * lineLength);
    }

    private float getNextRotateStartY(double angle, float lineLength) {
        return (float)(Math.sin(angle) * lineLength);
    }

    private void drawFinalHLine(float enlargefactor) {
        mCanvas.drawLine(mTranslateStopX - enlargefactor / 2, mStartY, mTranslateStopX + LINE_MAX_LENGTH + enlargefactor / 2, mStartY, mLinePaint);
    }

    private void drawFinalVLine(float enlargefactor) {
        mCanvas.drawLine((mParentWidth+STROKE_WIDTH/4)/2,
                (mParentHeight-LINE_MAX_LENGTH-STROKE_WIDTH/4)/2 - enlargefactor/2,
                (mParentWidth+STROKE_WIDTH/4)/2,
                (mParentHeight+LINE_MAX_LENGTH-STROKE_WIDTH/4)/2 + enlargefactor/2,
                mLinePaint);
    }

    private void drawRotatedLine() {

        mCanvas.drawLine((mParentWidth + STROKE_WIDTH / 4) / 2 - mFinalStartX,
                (mParentHeight - STROKE_WIDTH / 4) / 2 - mFinalStartY,
                (mParentWidth + STROKE_WIDTH / 4) / 2 + mFinalStartX,
                (mParentHeight - STROKE_WIDTH / 4) / 2 + mFinalStartY,
                mLinePaint
        );

    }

    private void drawStyleOneRotatedLine() {

        mLinePaint.setColor(COLOR_ORANGE);
        mCanvas.drawLine(mFinalLeftX, mFinalTopY, mFinalRightX, mFinalBottomY, mLinePaint);

        if (mFinalStartX < 0) {
            mCanvas.drawLine(mFinalRightX - mFinalStartX, mFinalTopY - mFinalStartY,
                    mFinalLeftX + mFinalStartX, mFinalBottomY + mFinalStartY,
                    mLinePaint
            );
            return ;
        }

        mCanvas.drawLine(mFinalRightX - mFinalStartX, mFinalTopY - mFinalStartY,
                mFinalLeftX + mFinalStartX, mFinalBottomY + mFinalStartY,
                mLinePaint
        );

    }

    private void drawStyleTwoRotatedLine() {

        mCanvas.drawLine(mFinalLeftX, mFinalTopY, mFinalRightX, mFinalBottomY, mLinePaint);

        Log.d(TAG, "mFinalStartX = " + mFinalStartX);

        mCanvas.drawLine(mFinalLeftX - mFinalStartX, mFinalTopY - mFinalStartY,
                mFinalRightX + mFinalStartX, mFinalBottomY + mFinalStartY,
                mLinePaint
        );

    }

    private void drawRotatedPlus() {

        mCanvas.drawLine(mParentWidth/2 - mFinalStartX,
                mParentHeight/2 - mFinalStartY,
                mParentWidth/2 + mFinalStartX,
                mParentHeight/2 + mFinalStartY,
                mLinePaint
        );
        mCanvas.drawLine(mParentWidth/2 + mFinalStartY,
                mParentHeight/2 - mFinalStartX,
                mParentWidth/2 - mFinalStartY,
                mParentHeight/2 + mFinalStartX,
                mLinePaint
        );

    }

    private void drawFinalMaxX(float enlargefactor) {

        mFinalLeftX = mParentWidth/2 - mFinalStartX - enlargefactor/2;
        mFinalRightX = mParentWidth/2 + mFinalStartX + enlargefactor/2;
        mFinalTopY = mParentHeight/2 - mFinalStartY - enlargefactor/2;
        mFinalBottomY = mParentHeight/2 + mFinalStartY + enlargefactor/2;

        mCanvas.drawLine(mFinalLeftX, mFinalTopY, mFinalRightX, mFinalBottomY, mLinePaint);
        mCanvas.drawLine(mFinalRightX, mFinalTopY, mFinalLeftX, mFinalBottomY, mLinePaint);

    }

    public void rePlay() {
        mBoundFactor = 0;
        mDrawIndicator = TRANSLATION_LINE;
        postInvalidate();
    }

}
