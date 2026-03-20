package com.m2sdl.dop.projet;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class BoiteDeColision {
    private Rect rectangle;
    private Paint paint = new Paint();


    public BoiteDeColision(int left, int top, int right, int bottom){
        rectangle = new Rect(left, top, right, bottom);
        paint.setColor(Color.rgb(0, 0, 250));
    }


    public void draw(Canvas canvas){
        canvas.drawRect(rectangle,paint);
    }

    public int getBordGauche(){
        return rectangle.left;
    }
    public int getBordDroit(){
        return rectangle.right;
    }

    public int getBordHaut(){
        return rectangle.top;
    }

    public int getBordBas(){
        return rectangle.bottom;
    }



}
