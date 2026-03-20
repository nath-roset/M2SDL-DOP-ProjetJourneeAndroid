package com.m2sdl.dop.projet.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class MenuView {
    private int largeur, hauteur;
    private float[] ballX, ballY, ballDx, ballDy, ballR;
    private int[] ballColor;
    private float angle = 0f;
    private static final int NB_BALLES = 14;

    public static final int TOUCH_AUCUN   = 0;
    public static final int TOUCH_START   = 1;
    public static final int TOUCH_MULTI   = 2;
    public static final int TOUCH_PHOTO   = 3;

    public MenuView(int largeur, int hauteur) {
        this.largeur = largeur;
        this.hauteur = hauteur;
        java.util.Random rnd = new java.util.Random();
        ballX  = new float[NB_BALLES]; ballY  = new float[NB_BALLES];
        ballDx = new float[NB_BALLES]; ballDy = new float[NB_BALLES];
        ballR  = new float[NB_BALLES]; ballColor = new int[NB_BALLES];
        int[] palette = {
                Color.rgb(0,255,180), Color.rgb(0,180,255),
                Color.rgb(180,0,255), Color.rgb(255,100,0)
        };
        for (int i = 0; i < NB_BALLES; i++) {
            ballX[i]  = rnd.nextFloat() * largeur;
            ballY[i]  = rnd.nextFloat() * hauteur;
            ballDx[i] = (rnd.nextFloat() - 0.5f) * 5f;
            ballDy[i] = (rnd.nextFloat() - 0.5f) * 5f;
            ballR[i]  = 15f + rnd.nextFloat() * 35f;
            ballColor[i] = palette[rnd.nextInt(palette.length)];
        }
    }

    public void update() {
        angle = (angle + 0.4f) % 360f;
        for (int i = 0; i < NB_BALLES; i++) {
            ballX[i] += ballDx[i]; ballY[i] += ballDy[i];
            if (ballX[i] - ballR[i] < 0 || ballX[i] + ballR[i] > largeur) ballDx[i] *= -1;
            if (ballY[i] - ballR[i] < 0 || ballY[i] + ballR[i] > hauteur) ballDy[i] *= -1;
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawColor(Color.rgb(8, 12, 28));

        Paint p = new Paint();
        p.setAntiAlias(true);

        for (int i = 0; i < NB_BALLES; i++) {
            int r = Color.red(ballColor[i]);
            int g = Color.green(ballColor[i]);
            int b = Color.blue(ballColor[i]);
            p.setColor(Color.argb(25, r, g, b));
            canvas.drawCircle(ballX[i], ballY[i], ballR[i] * 2.5f, p);
            p.setColor(Color.argb(60, r, g, b));
            canvas.drawCircle(ballX[i], ballY[i], ballR[i], p);
        }

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(0.8f);
        float cx = largeur / 2f, cy = hauteur * 0.32f;
        for (int r = 60; r < 700; r += 90) {
            float pulse = (float)Math.sin(Math.toRadians(angle * 1.5f + r)) * 15f;
            p.setColor(Color.argb(15, 0, 255, 180));
            canvas.drawCircle(cx, cy, r + pulse, p);
        }
        p.setStyle(Paint.Style.FILL);

        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(200f);
        p.setColor(Color.argb(30, 0, 255, 180));
        float bounce = (float)Math.sin(Math.toRadians(angle * 2)) * 15f;
        canvas.drawText("BULBORG", cx, cy + bounce + 10f, p);
        p.setTextSize(190f);
        p.setColor(Color.rgb(0, 255, 180));
        canvas.drawText("BULBORG", cx, cy + bounce, p);

        p.setTextSize(55f);
        p.setColor(Color.argb(180, 150, 230, 200));
        canvas.drawText("mini golf 2D", cx, cy + 110f + bounce, p);

        float btnW = 560f, btnH = 130f, btnR = 65f;
        float yStart = hauteur * 0.56f;
        float yMulti = yStart + 180f;

        float pulseS = (float)Math.sin(Math.toRadians(angle * 3)) * 0.03f + 1f;
        canvas.save();
        canvas.scale(pulseS, pulseS, cx, yStart + btnH / 2f);
        drawNeonButton(canvas, cx - btnW/2f, yStart, btnW, btnH, btnR,
                Color.rgb(0,255,180), Color.rgb(8,12,28), "START", p);
        canvas.restore();

        drawNeonButton(canvas, cx - btnW/2f, yMulti, btnW, btnH, btnR,
                Color.rgb(0,180,255), Color.rgb(8,12,28), "MULTIJOUEUR", p);

        float btnPhotoSize = 110f;
        float btnPhotoX = largeur - btnPhotoSize - 60f;
        float btnPhotoY = hauteur * 0.88f;
        drawNeonButton(canvas, btnPhotoX, btnPhotoY, btnPhotoSize, btnPhotoSize, btnPhotoSize/2f,
                Color.rgb(180, 0, 255), Color.rgb(8,12,28), "CAM", p);

        p.setColor(Color.argb(100, 150, 150, 180));
        p.setTextSize(42f);
        canvas.drawText("Placez la balle puis la sortie", cx, hauteur * 0.85f, p);
    }

    private void drawNeonButton(Canvas canvas, float x, float y, float w, float h,
                                float r, int couleurNeon, int couleurFond,
                                String label, Paint p) {
        RectF rect = new RectF(x, y, x + w, y + h);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.argb(40,
                Color.red(couleurNeon), Color.green(couleurNeon), Color.blue(couleurNeon)));
        canvas.drawRoundRect(new RectF(x-8,y-8,x+w+8,y+h+8), r+8, r+8, p);
        p.setColor(Color.argb(15,
                Color.red(couleurNeon), Color.green(couleurNeon), Color.blue(couleurNeon)));
        canvas.drawRoundRect(new RectF(x-20,y-20,x+w+20,y+h+20), r+20, r+20, p);
        p.setColor(couleurFond);
        canvas.drawRoundRect(rect, r, r, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3f);
        p.setColor(couleurNeon);
        canvas.drawRoundRect(rect, r, r, p);
        p.setStyle(Paint.Style.FILL);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(w > 200f ? 72f : 38f);
        p.setColor(couleurNeon);
        canvas.drawText(label, x + w/2f, y + h * 0.65f, p);
    }

    public int detectTouch(float tx, float ty) {
        float cx   = largeur / 2f;
        float btnW = 560f, btnH = 130f;
        float yStart = hauteur * 0.56f;
        float yMulti = yStart + 180f;
        float btnPhotoSize = 110f;
        float btnPhotoX = largeur - btnPhotoSize - 60f;
        float btnPhotoY = hauteur * 0.88f;

        if (tx > cx-btnW/2f && tx < cx+btnW/2f && ty > yStart && ty < yStart+btnH)
            return TOUCH_START;
        if (tx > cx-btnW/2f && tx < cx+btnW/2f && ty > yMulti && ty < yMulti+btnH)
            return TOUCH_MULTI;
        if (tx > btnPhotoX && tx < btnPhotoX+btnPhotoSize && ty > btnPhotoY && ty < btnPhotoY+btnPhotoSize)
            return TOUCH_PHOTO;
        return TOUCH_AUCUN;
    }
}