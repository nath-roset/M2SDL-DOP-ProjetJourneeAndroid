package com.m2sdl.dop.projet.bs;

import android.graphics.Canvas;
import android.view.SurfaceHolder;
import com.m2sdl.dop.projet.views.GameView;

public class GameThread extends Thread {

    private final SurfaceHolder surfaceHolder;
    private final GameView gameView;
    private volatile boolean running = false;
    private static final int TARGET_FPS = 60;
    private static final long FRAME_TIME = 1000 / TARGET_FPS;

    public GameThread(SurfaceHolder surfaceHolder, GameView gameView) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gameView      = gameView;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        while (running) {
            long debut = System.currentTimeMillis();
            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    synchronized (surfaceHolder) {
                        gameView.update();
                        gameView.draw(canvas);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            long elapsed = System.currentTimeMillis() - debut;
            long sleep   = FRAME_TIME - elapsed;
            if (sleep > 0) {
                try { Thread.sleep(sleep); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
    }
}