package com.m2sdl.dop.projet.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.m2sdl.dop.projet.Balle;
import com.m2sdl.dop.projet.BoiteDeColision;
import com.m2sdl.dop.projet.Bulborb;
import com.m2sdl.dop.projet.Deplacement;
import com.m2sdl.dop.projet.bs.GameThread;
import com.m2sdl.dop.projet.utils.TraiteurTableauBlanc;

import java.util.ArrayList;
import java.util.List;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread thread;
    private Balle balle;
    private List<BoiteDeColision> boites;
    private Bitmap fondTableau;
    private boolean boitesGenerees = false;


    public GameView(Context context, Bitmap fondTableau) {
        super(context);
        getHolder().addCallback(this);
        thread = new GameThread(getHolder(), this);
        setFocusable(true);
        this.fondTableau = fondTableau;
        this.balle = new Balle();
        this.boites = new ArrayList<>();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (!boitesGenerees) {
            // Générer les boîtes maintenant qu'on connaît w/h
            boites = TraiteurTableauBlanc.detecter(fondTableau, width, height);
            boitesGenerees = true;

            // Démarrer le thread seulement après génération
            thread = new GameThread(getHolder(), this);
            thread.setRunning(true);
            thread.start();
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {

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
        balle.update(this.getHeight(),this.getWidth(),boites);

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
            if (fondTableau != null) {
                canvas.drawBitmap(fondTableau,
                        null,
                        new android.graphics.RectF(0, 0, getWidth(), getHeight()),
                        null);
            } else {
                canvas.drawColor(Color.WHITE);
            }

            balle.draw(canvas);

            for (var b:boites) {
                b.draw(canvas);

            }

        }
    }
}
