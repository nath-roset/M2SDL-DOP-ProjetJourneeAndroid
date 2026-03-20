package com.m2sdl.dop.projet;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Sortie {
    private float x;
    private float y;
    private float rayon;
    private float animAngle = 0f;

    public Sortie(float x, float y, float rayonBalle) {
        this.x     = x;
        this.y     = y;
        this.rayon = rayonBalle * 1.5f;
    }

    public boolean estTouchee(float bx, float by, float rayonBalle) {
        float dx = bx - x;
        float dy = by - y;
        return Math.sqrt(dx * dx + dy * dy) < rayon;
    }

    public void update() {
        animAngle = (animAngle + 2f) % 360f;
    }

    public void draw(Canvas canvas) {
        Paint p = new Paint();
        p.setAntiAlias(true);

        p.setColor(Color.argb(60, 255, 215, 0));
        canvas.drawCircle(x, y, rayon * 2f, p);

        p.setColor(Color.rgb(255, 215, 0));
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(4f);
        canvas.drawCircle(x, y, rayon, p);

        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(0, 0, 0));
        canvas.drawCircle(x, y, rayon * 0.3f, p);

        double r1 = Math.toRadians(animAngle);
        double r2 = Math.toRadians(animAngle + 120);
        double r3 = Math.toRadians(animAngle + 240);
        float d = rayon * 0.7f;
        p.setColor(Color.rgb(255, 215, 0));
        canvas.drawCircle(x + (float)Math.cos(r1)*d, y + (float)Math.sin(r1)*d, 5f, p);
        canvas.drawCircle(x + (float)Math.cos(r2)*d, y + (float)Math.sin(r2)*d, 5f, p);
        canvas.drawCircle(x + (float)Math.cos(r3)*d, y + (float)Math.sin(r3)*d, 5f, p);
    }

    public float getX() { return x; }
    public float getY() { return y; }
}