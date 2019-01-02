package top.crosstun.svgclickview;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import top.crosstun.svg.SVGView;
import top.crosstun.svg.module.SVGPath;

public class MainActivity extends AppCompatActivity {

    SVGView svgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        svgView = findViewById(R.id.svg);
        svgView.setSVG(getResources().openRawResource(R.raw.taizhou));
        svgView.setOnSVGItemClickListener(new SVGView.OnSVGItemClickListener() {
            @Override
            public void onSVGItemClick(SVGPath svgPath) {
                Toast.makeText(MainActivity.this,svgPath.clas,Toast.LENGTH_SHORT).show();
            }
        });
//
//        try {
//            Thread.sleep(1*1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        List<SVGPath> paths = svgView.getPaths();

        ImageView pointView = findViewById(R.id.point1);
//        AnimationDrawable animationDrawable = (AnimationDrawable) pointView.getDrawable();
//        animationDrawable.start();

        ObjectAnimator animator1 = ObjectAnimator.ofFloat(pointView,"alpha", 1f,0f);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(pointView,"scaleX", 1f,2f);
        ObjectAnimator animator3 = ObjectAnimator.ofFloat(pointView,"scaleY", 1f,2f);

        animator1.setRepeatCount(ValueAnimator.INFINITE);//无限循环
        animator2.setRepeatCount(ValueAnimator.INFINITE);//无限循环
        animator3.setRepeatCount(ValueAnimator.INFINITE);//无限循环

        animator1.setRepeatMode(ValueAnimator.RESTART);
        animator2.setRepeatMode(ValueAnimator.RESTART);
        animator3.setRepeatMode(ValueAnimator.RESTART);


        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(1000);
        //animatorSet.play(anim);//执行当个动画
         animatorSet.playTogether(animator1,animator2,animator3);//同时执行
//        animatorSet.playSequentially(animator1,animator2,animator3);//依次执行动画
        animatorSet.start();

//        RectF rectF = new RectF();
//        paths.get(0).path.computeBounds(rectF,true);
//
//        ConstraintLayout.LayoutParams layoutParams  = (ConstraintLayout.LayoutParams) pointView.getLayoutParams();
//        layoutParams.rightMargin = (int) (rectF.left/2);
//        layoutParams.topMargin = (int) (rectF.top/2);
//        pointView.setLayoutParams(layoutParams);
    }
}
