package com.m2sdl.dop.projet;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class BoiteDeColision {
    private Rect rectangle;
    private Paint paintFill, paintBord, paintReflet;

    public BoiteDeColision(int left, int top, int right, int bottom) {
        rectangle = new Rect(left, top, right, bottom);

        paintFill = new Paint();
        paintFill.setAntiAlias(true);
        paintFill.setColor(Color.argb(90, 30, 40, 70));

        paintBord = new Paint();
        paintBord.setAntiAlias(true);
        paintBord.setStyle(Paint.Style.STROKE);
        paintBord.setStrokeWidth(2f);
        paintBord.setColor(Color.argb(180, 80, 120, 200));

        paintReflet = new Paint();
        paintReflet.setAntiAlias(true);
        paintReflet.setColor(Color.argb(30, 180, 210, 255));
    }

    public void draw(Canvas canvas) {
        RectF r = new RectF(rectangle);
        canvas.drawRoundRect(r, 6f, 6f, paintFill);
        canvas.drawRoundRect(r, 6f, 6f, paintBord);

        float refletH = Math.min(6f, r.height() * 0.3f);
        RectF reflet  = new RectF(r.left + 2, r.top + 2, r.right - 2, r.top + 2 + refletH);
        canvas.drawRoundRect(reflet, 4f, 4f, paintReflet);
    }

    public int getBordGauche() { return rectangle.left; }
    public int getBordDroit()  { return rectangle.right; }
    public int getBordHaut()   { return rectangle.top; }
    public int getBordBas()    { return rectangle.bottom; }
}