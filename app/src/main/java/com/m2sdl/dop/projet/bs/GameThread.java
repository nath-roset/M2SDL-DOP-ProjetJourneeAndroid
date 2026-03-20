package com.m2sdl.dop.projet.bs;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

import com.m2sdl.dop.projet.Bulborb;
import com.m2sdl.dop.projet.views.*;

public class GameThread extends Thread {
    private final SurfaceHolder surfaceHolder;
    private final GameView gameView;
    private boolean running;
    private Canvas canvas;

    public GameThread(SurfaceHolder surfaceHolder, GameView gameView) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        while (running) {
            canvas = null;
            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    this.gameView.update();
                    this.gameView.draw(canvas);
                    sleep(16); // busy-waiting
                }
            } catch (Exception e) {
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        Log.e(Bulborb.TAG, e.getMessage());
                    }
                }
            }
        }
    }
}
