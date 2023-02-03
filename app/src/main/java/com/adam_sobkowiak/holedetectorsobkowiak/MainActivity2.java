package com.adam_sobkowiak.holedetectorsobkowiak;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class MainActivity2 extends Activity {

    private Button clear_button;
    private Button submit_button;

    public Paint paint;
    public Path path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main2);

        setContentView(new Draw(this));

        //Czyszczenie canvasu
        clear_button = findViewById(R.id.clear_button);
        clear_button.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick (View v){
                clearCanvas();
            }
        });
        //Potwierdzenie rysunku
        submit_button = findViewById(R.id.submit_button);
        submit_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v){
                LoadBack();
            }
        });

    }

    private class Draw extends View {
        public Draw(Context Context) {
            super(Context);
            paint = new Paint();
            path = new Path();
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(8f);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(x, y);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    path.lineTo(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    return false;
            }
            invalidate();
            return true;
        }
    }
    public void clearCanvas(){
        path.reset();
    }
    private void LoadBack(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}