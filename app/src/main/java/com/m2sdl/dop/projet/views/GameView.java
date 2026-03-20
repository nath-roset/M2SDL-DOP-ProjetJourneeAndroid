package com.m2sdl.dop.projet.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.m2sdl.dop.projet.Bulborb;
import com.m2sdl.dop.projet.Deplacement;
import com.m2sdl.dop.projet.bs.GameThread;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread thread;
    private int largeurBalle = 50;
    private int x = largeurBalle + 100;
    private int y= largeurBalle + 100;

    private Deplacement deplacement;


    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        thread = new GameThread(getHolder(), this);
        setFocusable(true);
        this.deplacement = new Deplacement();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
            } catch (InterruptedException e) {
                Log.e(Bulborb.TAG, e.getMessage());
            }
            retry = false;
        }
    }

    /**
     * Met à jour l'état de la vue
     */
    public void update() {
        if(x == largeurBalle || x == getWidth()-largeurBalle){
            deplacement.toucherMur();
        }
        if(y == largeurBalle || y == getHeight()-largeurBalle){
            deplacement.toucherPlafond();
        }
        x+= (int) deplacement.getDeplacementX();
        y+= (int) deplacement.getDeplacementY();
    }

    /**
     * Dessine le nouvel état après mis à jour
     *
     * @param canvas The Canvas to which the View is rendered.
     */
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor(Color.WHITE);
            Paint paint = new Paint();
            paint.setColor(Color.rgb(250, 0, 0));
            canvas.drawCircle(x, y,  50,  paint);
        }
    }
}
