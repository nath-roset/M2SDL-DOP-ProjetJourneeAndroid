package com.m2sdl.dop.projet.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.m2sdl.dop.projet.Balle;
import com.m2sdl.dop.projet.Bulborb;
import com.m2sdl.dop.projet.Deplacement;
import com.m2sdl.dop.projet.bs.GameThread;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread thread;
    private Balle balle;




    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        thread = new GameThread(getHolder(), this);
        setFocusable(true);
        this.balle = new Balle();
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
        balle.update(this.getHeight(),this.getWidth());
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

            balle.draw(canvas);

        }
    }
}
