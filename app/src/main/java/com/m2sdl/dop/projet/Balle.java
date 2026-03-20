package com.m2sdl.dop.projet;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

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
    public void update(int gameViewHeight, int gameViewWidth){
        if(x == largeurBalle || x == gameViewWidth-largeurBalle){
            deplacement.toucherMur();
        }
        if(y == largeurBalle || y == gameViewHeight-largeurBalle){
            deplacement.toucherPlafond();
        }
        x+= (int) deplacement.getDeplacementX();
        y+= (int) deplacement.getDeplacementY();
    }
}
