package com.m2sdl.dop.projet;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

public class Balle {
    private int largeurBalle = 50;
    private int x = largeurBalle + 100;
    private int y= largeurBalle + 100;
    private Deplacement deplacement;

    public Balle() {
        this.largeurBalle = largeurBalle;
        this.x = x;
        this.y = y;
        this.deplacement = new Deplacement();
    }

    public void draw(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.rgb(250, 0, 0));
        canvas.drawCircle(x, y,  50,  paint);
    }
    public void update(int gameViewHeight, int gameViewWidth, List<BoiteDeColision> boites){
        collisionBordEcran(gameViewHeight, gameViewWidth);
        collisionObstacles(boites);
        calculerDeplacementBalle();
    }

    private void collisionObstacles(List<BoiteDeColision> boites) {
        for (var b : boites) {

            if (y + largeurBalle > b.getBordHaut() && y - largeurBalle < b.getBordBas()) {
                if (x + largeurBalle >= b.getBordGauche() && x < b.getBordGauche()) {
                    deplacement.toucherMur();
                }
                else if (x - largeurBalle <= b.getBordDroit() && x > b.getBordDroit()) {
                    deplacement.toucherMur();
                }
            }

            if (x + largeurBalle > b.getBordGauche() && x - largeurBalle < b.getBordDroit()) {
                if (y + largeurBalle >= b.getBordHaut() && y < b.getBordHaut()) {
                    deplacement.toucherPlafond();
                }
                else if (y - largeurBalle <= b.getBordBas() && y > b.getBordBas()) {
                    deplacement.toucherPlafond();
                }
            }
        }
    }

    private void collisionBordEcran(int gameViewHeight, int gameViewWidth) {
        if(x == largeurBalle || x == gameViewWidth -largeurBalle){
            deplacement.toucherMur();
        }
        if(y == largeurBalle || y == gameViewHeight -largeurBalle){
            deplacement.toucherPlafond();
        }
    }

    private void calculerDeplacementBalle() {
        x+= (int) deplacement.getDeplacementX();
        y+= (int) deplacement.getDeplacementY();
    }
}
