package cassie.jintian.com.diyview.widget;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import cassie.jintian.com.diyview.R;

/**
 * 新充电进度条
 * <p>
 * Created by Administrator on 2019/7/11.
 */

public class NewCircleProView extends View {

    //当前进度条值
    private int value = 0;
    //进度条可走最大值 默认95
    private int maxUseValue = 95;
    //进度条最大值 默认0-100
    private int maxValue = 100;

    private String hideText = "实时充电量"; //提示文字
    private float proWidth = 10; //进度条宽度
    private float proPadding = 6; //进度条和父体的padding
    private float hintTextSize = 12; //提示字体大小
    private float valueTextSize = 17; //值字体大小
    private int hintTextColor = Color.parseColor("#777777");
    private int valueTextColor = Color.BLACK;


    //Paint
    private Paint greyPaint;// 灰色的 Paint
    private Paint whitePaint; // 中心 白色圆的 Paint
    private Paint hintTextPaint;// 提示文字的 Paint
    private Paint valueTextPaint;// 进度条百分比文字的 Paint
    private Paint smallProPaint;// 小进度条 Paint
    private Paint bigProPaint; // 打进度条 Paint
    private Paint pointPaint;// 小圆点 Paint
    private Paint shadePonitPaint;//小圆的阴影 Paint

    //RectF
    private RectF smallRectF;
    private RectF bigRectF;


    private float centerX;// 整个布局的中心x
    private float centerY;//  整个布局的中心y
    private float bgCirleSize;// 大背景圆的直径
    private float whiteCirleSize;// 白色圆的直径
    private float bigProSize;// 大背景的颜色


    private float nowPro = 0;//用于动画

    private int[] proColor;
    private float[] proFloat;

    private ValueAnimator animator;

    public NewCircleProView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        @SuppressLint("Recycle") TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NewCircleProView);
        //文字尺寸和颜色
        hintTextSize = typedArray.getDimension(R.styleable.NewCircleProView_hintTextSize, hintTextSize);
        hintTextColor = typedArray.getColor(R.styleable.NewCircleProView_hintTextColor, hintTextColor);
        valueTextSize = typedArray.getDimension(R.styleable.NewCircleProView_valueTextSize, valueTextSize);
        valueTextColor = typedArray.getColor(R.styleable.NewCircleProView_valueTextColor, valueTextColor);
        //获取到文字
        if (typedArray.getString(R.styleable.NewCircleProView_hideText) != null)
            hideText = typedArray.getString(R.styleable.NewCircleProView_hideText);
        //值
        value = typedArray.getInt(R.styleable.NewCircleProView_value, value);
        //最大可用最大
        maxUseValue = typedArray.getInt(R.styleable.NewCircleProView_maxUseValue, maxUseValue);
        //最大值
        maxValue = typedArray.getInt(R.styleable.NewCircleProView_maxValue, maxValue);
        proWidth = typedArray.getDimension(R.styleable.NewCircleProView_proWidth, DensityUtil.dip2px(getContext(), proWidth));
        proPadding = typedArray.getDimension(R.styleable.NewCircleProView_proPadding, DensityUtil.dip2px(getContext(), proPadding));


    }

    private void initSize() {
        //中心点
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;

        bgCirleSize = getWidth() - proPadding;
        whiteCirleSize = bgCirleSize - proWidth;
        bigProSize = bgCirleSize / 2 - whiteCirleSize / 2;

    }

    private void initPaint() {
        //大背景圆画笔
        greyPaint = new Paint();
        greyPaint.setColor(Color.parseColor("#E9E9E9"));

        //中间白圆画笔
        whitePaint = new Paint();
        whitePaint.setColor(Color.WHITE);

        //值文字画笔
        valueTextPaint = new Paint();
        valueTextPaint.setColor(valueTextColor);

        //提示文字画笔
        hintTextPaint = new Paint();
        hintTextPaint.setColor(hintTextColor);

        //设置字体大小
        hintTextPaint.setTextSize(DensityUtil.dip2px(getContext(), hintTextSize));
        valueTextPaint.setTextSize(DensityUtil.dip2px(getContext(), valueTextSize));

        //渐变颜色
        proColor = new int[]{Color.parseColor("#E66B00"), Color.parseColor("#FAD061"),
                Color.parseColor("#4AB84F")};
        //渐变范围
        proFloat = new float[]{0f, 0.5f, 1f};
        //将颜色的起始位置调到正下方
        SweepGradient sweepGradient = new SweepGradient(centerX, centerY, proColor, proFloat);
        Matrix matrix = new Matrix();
        matrix.setRotate(90, centerX, centerX);
        sweepGradient.setLocalMatrix(matrix);

        shadePonitPaint = new Paint();

        //小进度条
        smallProPaint = new Paint();
        smallProPaint.setStyle(Paint.Style.STROKE);
        smallProPaint.setStrokeWidth(DensityUtil.dip2px(getContext(), 1));
        smallProPaint.setShader(sweepGradient);
        //计算位置
        float leftAndTop = (bgCirleSize / 2 - whiteCirleSize / 2) +
                (centerX - bgCirleSize / 2);
        float rightAndBottom = (centerX * 2) - ((centerX - bgCirleSize / 2) +
                (bgCirleSize / 2 - whiteCirleSize / 2));
        //设置弧的上下左右的点
        smallRectF = new RectF(leftAndTop, leftAndTop, rightAndBottom, rightAndBottom);

        //大进度条
        bigProPaint = new Paint();
        bigProPaint.setStyle(Paint.Style.STROKE);
        bigProPaint.setAntiAlias(true);
        //大进度条的粗细
        bigProPaint.setStrokeWidth(bigProSize);
        //设置进度条颜色
        bigProPaint.setShader(sweepGradient);
        //设置弧的上下左右的点
        bigRectF = new RectF(leftAndTop - bigProSize / 2, leftAndTop - bigProSize / 2,
                rightAndBottom + bigProSize / 2, rightAndBottom + bigProSize / 2);

        //小圆点的样式
        pointPaint = new Paint();
        pointPaint.setStrokeWidth(DensityUtil.dip2px(getContext(), 1));
        pointPaint.setShader(sweepGradient);
    }


    public NewCircleProView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initSize();
        initPaint();
        canvas.save();

        float nowValue;
        //如果没动画就直接显示出来 有的话就动态算值再动画加载出来
        if (animator == null) {
            nowValue = value;
        } else nowValue = nowPro;

        //画背景圆
        canvas.drawCircle(centerX, centerY, bgCirleSize / 2, greyPaint);
        //画中心圆
        canvas.drawCircle(centerX, centerY, whiteCirleSize / 2, whitePaint);
        //提示文字
        String sevenText = hideText;
        //百分比文字
        String valueText = value + "%";


//        if (value >= maxUseValue) {
//            valueText = maxUseValue + "%";
//        }
        //计算提示文字和数值文字的高度宽度
        float valueTextHight = measureTextHeight(valueTextPaint);
        float textPadding = DensityUtil.dip2px(getContext(), 8);
        float sevenTextWeight = getTextWidth(hintTextPaint, sevenText);
        float valueTextWeight = getTextWidth(valueTextPaint, valueText);
        //画实时充电量文字
        canvas.drawText(sevenText, centerX - sevenTextWeight / 2,
                centerY - valueTextHight / 2, hintTextPaint);
        //画值文字
        canvas.drawText(valueText, centerX - valueTextWeight / 2,
                centerY + textPadding + valueTextHight / 2, valueTextPaint);
        //画小进度条
        canvas.drawArc(smallRectF, 92, (float) (maxUseValue * 3.6), false, smallProPaint);

        //画大进度条
        if (animator==null){
            canvas.drawArc(bigRectF, 90, (float) (value * 3.6), false, bigProPaint);
        }else
        canvas.drawArc(bigRectF, 90, (float) (nowValue * 3.6), false, bigProPaint);


        //计算出小圆点的x轴y轴
        double childCenterX;
        double childCenterY;
        //因为小圆点是圆的 在渐变色里面会出现小圆点里有两个颜色 在这里进行调整
        if (value < (maxValue * 2) / 3) {
            childCenterX = centerX + ((bgCirleSize / 2) - (bgCirleSize - whiteCirleSize) / 2 +
                    bigProSize / 2) * Math.cos(Math.toRadians(96 + (float) (nowValue * 3.6)));
            childCenterY = centerY + ((bgCirleSize / 2) - (bgCirleSize - whiteCirleSize) / 2 +
                    bigProSize / 2) * Math.sin(Math.toRadians(96 + (float) (nowValue * 3.6)));
        } else {
            childCenterX = centerX + ((bgCirleSize / 2) - (bgCirleSize - whiteCirleSize) / 2 +
                    bigProSize / 2) * Math.cos(Math.toRadians(84 + (float) (nowValue * 3.6)));
            childCenterY = centerY + ((bgCirleSize / 2) - (bgCirleSize - whiteCirleSize) / 2 +
                    bigProSize / 2) * Math.sin(Math.toRadians(84 + (float) (nowValue * 3.6)));
        }

        //计算阴影的颜色
        ArgbEvaluator argbEvaluator = new ArgbEvaluator();
        int pointColor;
        if (nowValue <= 50) {
            pointColor = (int) argbEvaluator.evaluate( nowValue / 100, proColor[0], proColor[1]);
        } else
            pointColor = (int) argbEvaluator.evaluate( nowValue / 100, proColor[1], proColor[2]);

        //阴影渐变颜色
        RadialGradient radialGradient = new RadialGradient((float) childCenterX, (float) childCenterY, bigProSize + DensityUtil.dip2px(getContext(), 5),
                new int[]{pointColor, Color.TRANSPARENT}, new float[]{0.5f, 1f}, Shader.TileMode.CLAMP);
        shadePonitPaint.setShader(radialGradient);
        //画阴影
        canvas.drawCircle((float) childCenterX, (float) childCenterY, bigProSize + DensityUtil.dip2px(getContext(), 5), shadePonitPaint);
        //小圆点外面的白环 （其实是一个圆）
        canvas.drawCircle((float) childCenterX, (float) childCenterY, bigProSize + DensityUtil.dip2px(getContext(), 1), whitePaint);
//        //画小圆点
        canvas.drawCircle((float) childCenterX, (float) childCenterY, bigProSize, pointPaint);


//        pointPaint.getShader().getLocalMatrix()
    }


    /**
     * 测量文字高度
     *
     * @param paint 画笔
     * @return
     */
    public static float measureTextHeight(Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return (Math.abs(fontMetrics.ascent) - fontMetrics.descent);
    }

    /**
     * 测量文字宽度
     *
     * @param paint 画笔
     * @param str   文字
     * @return
     */
    public static int getTextWidth(Paint paint, String str) {
        int iRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) Math.ceil(widths[j]);
            }
        }
        return iRet;
    }

    public void setValue(int value) {
//        if (value <= maxUseValue)
        if (value > maxValue) this.value = maxValue;
        this.value = value;
//        invalidate();
        startAnimator();
    }

    /**
     * 设置最大量
     *
     * @param maxUseValue
     */
    public void setMaxUseValue(int maxUseValue) {
        this.maxUseValue = maxUseValue;
        invalidate();
    }


    /**
     * 启动动画
     */
    private void startAnimator() {
        //动画
        animator = ValueAnimator.ofFloat(0, value);
        animator.setDuration(1800);
        animator.setInterpolator(new OvershootInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                nowPro = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }
}
