package com.m2sdl.dop.projet;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.List;

public class Balle {
    private int rayon = 20;
    private float x = rayon + 100;
    private float y = rayon + 100;
    private Deplacement deplacement;
    private int gameViewWidth;
    private int gameViewHeight;
    private List<BoiteDeColision> boites;

    public Balle() {
        this.deplacement = new Deplacement();
    }

    public void update(int gameViewHeight, int gameViewWidth, List<BoiteDeColision> boites) {
        this.gameViewHeight = gameViewHeight;
        this.gameViewWidth  = gameViewWidth;
        this.boites         = boites;
        calculerDeplacementBalle();
    }

    private void calculerDeplacementBalle() {
        float dx = deplacement.getDeplacementX();
        float dy = deplacement.getDeplacementY();
        //utile pour ralentir la balle quand elle approche a la prochaine it d'un bloc
        float tMin = 1f;
        BoiteDeColision boiteTouchee = null;
        boolean collisionX = false; // savoir si c'est un mur ou un plafond

        for (BoiteDeColision b : boites) {
            float[] result = tempsCollision(x, y, dx, dy, b);
            if (result != null && result[0] < tMin) {
                tMin         = result[0];
                boiteTouchee = b;
                collisionX   = result[1] != 0;
            }
        }

        if (boiteTouchee != null) {
            float offset = 0.5f;
            x = x + dx * tMin;
            y = y + dy * tMin;

            if (collisionX) {
                x += (deplacement.getDeplacementX() > 0 ? -offset : offset);
                deplacement.toucherMur();
            } else {
                y += (deplacement.getDeplacementY() > 0 ? -offset : offset);
                deplacement.toucherPlafond();
            }
        } else {
            x += dx;
            y += dy;
        }

        collisionBordEcran();
    }

    private float[] tempsCollision(float cx, float cy, float dx, float dy, BoiteDeColision b) {
        float gauche  = b.getBordGauche() - rayon;
        float droite  = b.getBordDroit()  + rayon;
        float haut    = b.getBordHaut()   - rayon;
        float bas     = b.getBordBas()    + rayon;

        float tEntreeX, tSortieX, tEntreeY, tSortieY;

        if (Math.abs(dx) < 0.001f) {
            if (cx < gauche || cx > droite) return null;
            tEntreeX = -Float.MAX_VALUE;
            tSortieX =  Float.MAX_VALUE;
        } else {
            tEntreeX = (gauche - cx) / dx;
            tSortieX = (droite - cx) / dx;
            if (tEntreeX > tSortieX) { float t = tEntreeX; tEntreeX = tSortieX; tSortieX = t; }
        }

        if (Math.abs(dy) < 0.001f) {
            if (cy < haut || cy > bas) return null;
            tEntreeY = -Float.MAX_VALUE;
            tSortieY =  Float.MAX_VALUE;
        } else {
            tEntreeY = (haut - cy) / dy;
            tSortieY = (bas  - cy) / dy;
            if (tEntreeY > tSortieY) { float t = tEntreeY; tEntreeY = tSortieY; tSortieY = t; }
        }

        float tEntree = Math.max(tEntreeX, tEntreeY);
        float tSortie = Math.min(tSortieX, tSortieY);

        if (tEntree > tSortie || tSortie < 0 || tEntree > 1f) return null;

        float t = Math.max(tEntree, 0f);
        float axeX = (tEntreeX > tEntreeY) ? 1f : 0f;

        return new float[]{ t, axeX };
    }

    private void collisionBordEcran() {
        if (x - rayon < 0) {
            x = rayon;
            deplacement.toucherMur();
        } else if (x + rayon > gameViewWidth) {
            x = gameViewWidth - rayon;
            deplacement.toucherMur();
        }
        if (y - rayon < 0) {
            y = rayon;
            deplacement.toucherPlafond();
        } else if (y + rayon > gameViewHeight) {
            y = gameViewHeight - rayon;
            deplacement.toucherPlafond();
        }
    }

    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.rgb(250, 0, 0));
        canvas.drawCircle(x, y, rayon, paint);
    }
}