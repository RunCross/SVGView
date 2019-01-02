package top.crosstun.svg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import top.crosstun.svg.module.SVGPath;
import top.crosstun.svg.parse.SVGParser;


public class SVGView extends View {

    private MyHandler handler;
    private List<SVGPath> paths;
    private RectF svgRect;
    private Paint mPaint;
    private int viewWidth;
    private int viewHeight;

    private Path mPath;

    private float scale = 1.5f;

    private int fillColor;
    private int bgColor;
    private int textColor;
    private int selectColor;

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    private Map<String, String> circleMap;
    private int circleColor = Color.parseColor("#F08460");
    private int[] circleAlpha = new int[]{
            255,
            212,
            170,
            127,
            85,
            42
    };
    private int[] circleColors = new int[]{
            Color.parseColor("#ff8177"),
            Color.parseColor("#ff867a"),
            Color.parseColor("#ff8c7f"),
            Color.parseColor("#f99185"),
            Color.parseColor("#cf556c"),
            Color.parseColor("#b12a5b"),
    };
    private int[] circleRadius = new int[]{
            8, 10, 12, 13, 14, 15
    };
    private int index = 0;

    public SVGView(Context context) {
        super(context);
        init();
    }

    public SVGView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        handler = new MyHandler(this);
        svgRect = new RectF();
        fillColor = Color.parseColor("#60C0DD");
        bgColor = Color.parseColor("#F6F6F6");
        textColor = Color.parseColor("#b53535");
        selectColor = Color.parseColor("#F08460");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //防止变形
        setMeasuredDimension(measureSize(1000, widthMeasureSpec),
                measureSize(1000, heightMeasureSpec));
    }


    private int measureSize(int defultSize, int measureSpce) {
        int mode = MeasureSpec.getMode(measureSpce);
        int size = MeasureSpec.getSize(measureSpce);
        int measureSize = defultSize;
        switch (mode) {
            case MeasureSpec.EXACTLY:
                measureSize = Math.max(defultSize, size);
                break;
            case MeasureSpec.AT_MOST:
                measureSize = defultSize;
                break;
        }
        return measureSize;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        colScale();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (paths == null || paths.size() < 1) {
            return;
        }
        //        Matrix mMatrix = new Matrix();
//        mMatrix.postScale(0.5f,0.5f);
        //这个set方法不可以
//        mMatrix.setScale(0.5f,0.5f);
//        canvas.concat(mMatrix);
        //上面的方法也可以
//        canvas.restore();
        canvas.scale(scale, scale);
        mPaint.setAlpha(255);
        mPaint.setColor(bgColor);
        canvas.drawRect(svgRect, mPaint);
        //绘制图和边
        for (int i = 0; i < paths.size(); i++) {
            SVGPath svgPath = paths.get(i);
            mPaint.setStyle(Paint.Style.FILL);
            if (mPath == svgPath.path) {
                mPaint.setColor(selectColor);
            } else {
                mPaint.setColor(fillColor);
            }
            canvas.drawPath(svgPath.path, mPaint);

            mPaint.setStrokeWidth(1);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(bgColor);
            canvas.drawPath(svgPath.path, mPaint);
        }

        //绘制文字
        for (int i = 0; i < paths.size(); i++) {
            SVGPath svgPath = paths.get(i);
            if (!TextUtils.isEmpty(svgPath.title)) {
                mPaint.setColor(textColor);
                mPaint.setTextSize(18);
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawText(svgPath.title,
                        (svgPath.rectF.right - svgPath.rectF.left) / 2 + svgPath.rectF.left - 8,
                        (svgPath.rectF.bottom - svgPath.rectF.top) / 2 + svgPath.rectF.top + 6,
                        mPaint
                );
            }
        }

        //绘制有问题的地区的警告
        if (circleMap != null && !circleMap.isEmpty()) {
            for (int i = 0; i < paths.size(); i++) {
                if (!TextUtils.isEmpty(circleMap.get(paths.get(i).title))) {
                    SVGPath svgPath = paths.get(i);
                    mPaint.setColor(circleColor);
                    mPaint.setAlpha(circleAlpha[index]);
                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle((svgPath.rectF.right - svgPath.rectF.left) / 2 + svgPath.rectF.left - 8,
                            (svgPath.rectF.bottom - svgPath.rectF.top) / 2 + svgPath.rectF.top + 6,
                            circleRadius[index],
                            mPaint);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (onSVGItemClickListener == null) return super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            if (paths != null)
                for (int i = 0; i < paths.size(); i++) {
                    SVGPath svgPath = paths.get(i);
                    if (isInSVGRect(svgPath.path, x / scale, y / scale)) {
                        mPath = svgPath.path;
                        postInvalidate();
                        onSVGItemClickListener.onSVGItemClick(svgPath);
                        break;
                    }
                }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            mPath = null;
            postInvalidate();
        }
        return super.onTouchEvent(event);
    }

    private void colScale() {
        if (svgRect.width() > 0 && svgRect.height() > 0 && viewWidth > 0 && viewHeight > 0) {
            float widthScale = viewWidth * 1.00f / svgRect.width();
            float heightScale = viewHeight * 1.00f / svgRect.height();
//            float widthScale = svgRect.width()/viewWidth;
//            float heightScale = svgRect.height()/viewHeight;
            scale = Math.min(widthScale, heightScale);
        }
        postInvalidate();
    }

    public void setSVG(final InputStream inputStream) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                paths = SVGParser.parse(inputStream, svgRect);
                if (paths != null && paths.size() > 0) {
                    handler.sendEmptyMessage(0);
                }
            }
        }.run();
    }

    public List<SVGPath> getPaths() {
        return paths;
    }

    Region region = new Region();

    /**
     * @param path
     * @param x
     * @param y
     * @return
     */
    private boolean isInSVGRect(Path path, float x, float y) {
        RectF r = new RectF();
        //计算控制点的边界
        path.computeBounds(r, true);
        //设置区域路径和剪辑描述的区域
        region.setPath(path, new Region((int) r.left, (int) r.top, (int) r.right, (int) r.bottom));
        return region.contains((int) x, (int) y);
    }

    /**
     * 重置svg
     */
    public void reset() {
        mPath = null;
        circleMap = null;
        handler.removeCallbacksAndMessages(null);
        postInvalidate();
    }

    public void setTraceBackList(Map<String, String> map) {
        circleMap = map;
        index = 0;
        handler.sendEmptyMessageDelayed(1, 300);
    }

    public void onResume() {
        if (circleMap != null && !circleMap.isEmpty()) {
            handler.sendEmptyMessageDelayed(1, 300);
        }
    }

    public void onPause() {
        handler.removeMessages(1);
    }

    public void onDestroy() {
        mPath = null;
        circleMap = null;
        handler.removeCallbacksAndMessages(null);
        handler = null;
        if (paths != null) {
            paths.clear();
        }
    }

    static class MyHandler extends Handler {
        WeakReference<SVGView> weakReference;

        MyHandler(SVGView svgView) {
            weakReference = new WeakReference<>(svgView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SVGView _this = weakReference.get();
            if (_this == null) {
                return;
            }
            if (msg.what == 0) {
                _this.colScale();
            } else if (msg.what == 1) {
                _this.postInvalidate();
                _this.index++;
                if (_this.index >= _this.circleColors.length) {
                    _this.index = 0;
                }
                _this.handler.sendEmptyMessageDelayed(1, 200);
            }
        }
    }

    OnSVGItemClickListener onSVGItemClickListener;

    public void setOnSVGItemClickListener(OnSVGItemClickListener onSVGItemClickListener) {
        this.onSVGItemClickListener = onSVGItemClickListener;
    }

    public interface OnSVGItemClickListener {
        void onSVGItemClick(SVGPath svgPath);
    }
}
