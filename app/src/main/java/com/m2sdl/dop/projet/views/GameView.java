package com.m2sdl.dop.projet.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.m2sdl.dop.projet.Balle;
import com.m2sdl.dop.projet.BoiteDeColision;
import com.m2sdl.dop.projet.Bulborb;
import com.m2sdl.dop.projet.Deplacement;
import com.m2sdl.dop.projet.bs.GameThread;

import java.util.ArrayList;
import java.util.List;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread thread;
    private Balle balle;
    private float touchStartX, touchStartY;
    private static final float PUISSANCE_MAX = 30f;
    private float touchCurrentX, touchCurrentY;
    private boolean viserEnCours = false;
    private List<BoiteDeColision> boites;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        thread = new GameThread(getHolder(), this);
        setFocusable(true);
        this.balle = new Balle();
        this.boites = new ArrayList<>();
        // Bordures
        boites.add(new BoiteDeColision(0,   300,  20,   2200)); // gauche
        boites.add(new BoiteDeColision(880, 300,  900,  2200)); // droite
        boites.add(new BoiteDeColision(100, 300,  900,  340));  // haut (entrée à gauche 0-100)
        boites.add(new BoiteDeColision(0,   2160, 800,  2200)); // bas (sortie à droite 800-900)

        // Niveau 1
        boites.add(new BoiteDeColision(20,  340,  300,  380));  // plafond couloir gauche
        boites.add(new BoiteDeColision(280, 340,  300,  700));  // mur descente droite

        // Niveau 2
        boites.add(new BoiteDeColision(20,  700,  450,  740));  // sol gauche
        boites.add(new BoiteDeColision(450, 700,  900,  740));  // sol droite
        boites.add(new BoiteDeColision(680, 740,  700,  1100)); // mur descente droite

        // Niveau 3
        boites.add(new BoiteDeColision(20,  1100, 500,  1140)); // sol gauche
        boites.add(new BoiteDeColision(480, 1140, 500,  1500)); // mur descente centre
        boites.add(new BoiteDeColision(500, 1100, 900,  1140)); // sol droite
        boites.add(new BoiteDeColision(680, 1140, 700,  1500)); // mur descente droite

        // Niveau 4
        boites.add(new BoiteDeColision(20,  1500, 680,  1540)); // sol long
        boites.add(new BoiteDeColision(680, 1540, 700,  1900)); // mur descente droite
        boites.add(new BoiteDeColision(100, 1900, 900,  1940)); // sol final
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
        if (canvas == null) return;

        canvas.drawColor(Color.WHITE);
        for (BoiteDeColision b : boites) b.draw(canvas);

        if (viserEnCours && balle.estArretee()) {
            float deltaX  = touchStartX - touchCurrentX;
            float deltaY  = touchStartY - touchCurrentY;
            float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            float puissance = Math.min(distance / 20f, PUISSANCE_MAX);
            float angle = (float) Math.toDegrees(Math.atan2(deltaY, deltaX));
            balle.dessinerTrajectoire(canvas, angle, puissance);
        }

        balle.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (balle.estArretee()) {
                    touchStartX   = event.getX();
                    touchStartY   = event.getY();
                    touchCurrentX = event.getX();
                    touchCurrentY = event.getY();
                    viserEnCours  = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (viserEnCours) {
                    touchCurrentX = event.getX();
                    touchCurrentY = event.getY();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (viserEnCours) {
                    float deltaX  = touchStartX - event.getX();
                    float deltaY  = touchStartY - event.getY();
                    float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                    float puissance = Math.min(distance / 20f, PUISSANCE_MAX);
                    float angle = (float) Math.toDegrees(Math.atan2(deltaY, deltaX));
                    balle.lancer(angle, puissance);
                    viserEnCours = false;
                }
                break;
        }
        return true;
    }
}
