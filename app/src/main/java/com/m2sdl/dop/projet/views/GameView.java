package com.m2sdl.dop.projet.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.m2sdl.dop.projet.AnimationIntro;
import com.m2sdl.dop.projet.Balle;
import com.m2sdl.dop.projet.BoiteDeColision;
import com.m2sdl.dop.projet.EtatJeu;
import com.m2sdl.dop.projet.Joueur;
import com.m2sdl.dop.projet.Sortie;
import com.m2sdl.dop.projet.bs.GameThread;
import com.m2sdl.dop.projet.utils.GestionCamera;

import java.util.ArrayList;
import java.util.List;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread thread;
    private List<Balle> balles = new ArrayList<>();
    private Sortie sortie;
    private List<BoiteDeColision> boites;
    private EtatJeu etat = EtatJeu.MENU;
    private MenuView menu;
    private AnimationIntro animation;

    private List<Joueur> joueurs = new ArrayList<>();
    private int joueurActif  = 0;
    private boolean modeMulti = false;

    private float touchStartX, touchStartY;
    private float touchCurrentX, touchCurrentY;
    private boolean viserEnCours = false;
    private static final float PUISSANCE_MAX = 30f;

    private GestionCamera gestionCamera;
    private Bitmap bitmap;

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        thread = new GameThread(getHolder(), this);
        setFocusable(true);
        this.boites = new ArrayList<>();
        initBoites();
    }

    private void initBoites() {
        boites.add(new BoiteDeColision(0,   300,  20,   2200));
        boites.add(new BoiteDeColision(880, 300,  900,  2200));
        boites.add(new BoiteDeColision(100, 300,  900,  340));
        boites.add(new BoiteDeColision(0,   2160, 800,  2200));
        boites.add(new BoiteDeColision(20,  340,  300,  380));
        boites.add(new BoiteDeColision(280, 340,  300,  700));
        boites.add(new BoiteDeColision(20,  700,  450,  740));
        boites.add(new BoiteDeColision(450, 700,  900,  740));
        boites.add(new BoiteDeColision(680, 740,  700,  1100));
        boites.add(new BoiteDeColision(20,  1100, 500,  1140));
        boites.add(new BoiteDeColision(480, 1140, 500,  1500));
        boites.add(new BoiteDeColision(500, 1100, 900,  1140));
        boites.add(new BoiteDeColision(680, 1140, 700,  1500));
        boites.add(new BoiteDeColision(20,  1500, 680,  1540));
        boites.add(new BoiteDeColision(680, 1540, 700,  1900));
        boites.add(new BoiteDeColision(100, 1900, 900,  1940));
    }

    private void initJoueurs(boolean multi) {
        joueurs.clear();
        balles.clear();
        joueurs.add(new Joueur("J1", Color.rgb(0, 255, 180)));
        Balle b1 = new Balle();
        b1.setCouleur(Color.rgb(0, 255, 180));
        balles.add(b1);
        if (multi) {
            joueurs.add(new Joueur("J2", Color.rgb(255, 100, 50)));
            Balle b2 = new Balle();
            b2.setCouleur(Color.rgb(255, 100, 50));
            balles.add(b2);
        }
        joueurActif = 0;
        modeMulti   = multi;
    }

    private Balle balleActive() {
        return balles.get(joueurActif);
    }

    private void passerJoueurSuivant() {
        joueurActif = (joueurActif + 1) % joueurs.size();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas == null) return;

        switch (etat) {
            case MENU:
                if (menu != null) menu.draw(canvas);
                break;

            case PLACEMENT_BALLE:
                dessinerJeu(canvas);
                for (int i = 0; i < joueurActif; i++) balles.get(i).draw(canvas);
                drawInstructions(canvas,
                        joueurs.size() > 1
                                ? "Placer balle " + joueurs.get(joueurActif).getNom()
                                : "Touchez pour placer la balle",
                        joueurs.get(joueurActif).getCouleur());
                break;

            case PLACEMENT_SORTIE:
                dessinerJeu(canvas);
                for (Balle b : balles) b.draw(canvas);
                drawInstructions(canvas, "Touchez pour placer la sortie",
                        Color.rgb(255, 215, 0));
                break;

            case JEU:
                dessinerJeu(canvas);
                if (sortie != null) sortie.draw(canvas);
                for (int i = balles.size() - 1; i >= 0; i--) {
                    if (i != joueurActif) balles.get(i).draw(canvas);
                }
                if (viserEnCours && balleActive().estArretee()) {
                    float dx   = touchStartX - touchCurrentX;
                    float dy   = touchStartY - touchCurrentY;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    float puissance = Math.min(dist / 20f, PUISSANCE_MAX);
                    float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
                    balleActive().dessinerTrajectoire(canvas, angle, puissance);
                }
                balleActive().draw(canvas);
                drawHUD(canvas);
                if (animation != null && !animation.estTerminee()) {
                    animation.draw(canvas);
                }
                break;

            case VICTOIRE:
                dessinerJeu(canvas);
                if (sortie != null) sortie.draw(canvas);
                for (Balle b : balles) b.draw(canvas);
                if (animation != null) animation.draw(canvas);
                break;
        }
    }

    private void dessinerJeu(Canvas canvas) {
        canvas.drawColor(Color.rgb(8, 12, 28));

        Paint grid = new Paint();
        grid.setColor(Color.argb(12, 0, 255, 180));
        grid.setStrokeWidth(1f);
        int step = 80;
        for (int x = 0; x < getWidth();  x += step) canvas.drawLine(x, 0, x, getHeight(), grid);
        for (int y = 0; y < getHeight(); y += step) canvas.drawLine(0, y, getWidth(),  y, grid);

        for (BoiteDeColision b : boites) b.draw(canvas);
    }

    private void drawInstructions(Canvas canvas, String texte, int couleur) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.argb(170, 8, 12, 28));
        canvas.drawRoundRect(new RectF(50, 80, getWidth() - 50, 210), 30, 30, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2f);
        p.setColor(Color.argb(200,
                Color.red(couleur), Color.green(couleur), Color.blue(couleur)));
        canvas.drawRoundRect(new RectF(50, 80, getWidth() - 50, 210), 30, 30, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(couleur);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(52f);
        canvas.drawText(texte, getWidth() / 2f, 165f, p);
    }

    private void drawHUD(Canvas canvas) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        for (int i = 0; i < joueurs.size(); i++) {
            Joueur j      = joueurs.get(i);
            boolean actif = (i == joueurActif);
            float yPos    = 90f + i * 120f;
            int cr = Color.red(j.getCouleur());
            int cg = Color.green(j.getCouleur());
            int cb = Color.blue(j.getCouleur());

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(actif ? 160 : 80, 8, 12, 28));
            canvas.drawRoundRect(new RectF(20, yPos - 65f, 400, yPos + 25f), 22, 22, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(actif ? 3f : 1f);
            p.setColor(actif ? j.getCouleur() : Color.argb(80, cr, cg, cb));
            canvas.drawRoundRect(new RectF(20, yPos - 65f, 400, yPos + 25f), 22, 22, p);
            if (actif) {
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.argb(20, cr, cg, cb));
                canvas.drawRoundRect(new RectF(22, yPos - 63f, 398, yPos + 23f), 20, 20, p);
            }
            p.setStyle(Paint.Style.FILL);
            p.setTextAlign(Paint.Align.LEFT);
            p.setTextSize(54f);
            p.setColor(actif ? j.getCouleur() : Color.argb(120, cr, cg, cb));
            int coups = j.getCoups();
            canvas.drawText(j.getNom() + "   " + coups
                    + (coups > 1 ? " coups" : " coup"), 45f, yPos, p);
        }
    }

    public void update() {
        switch (etat) {
            case MENU:
                if (menu != null) menu.update();
                break;

            case JEU:
                for (Balle b : balles) b.update(getHeight(), getWidth(), boites);
                if (sortie != null) {
                    sortie.update();
                    for (int i = 0; i < balles.size(); i++) {
                        Balle b = balles.get(i);
                        if (sortie.estTouchee(b.getX(), b.getY(), b.getRayon())) {
                            animation = new AnimationIntro(
                                    AnimationIntro.Type.FIN, getWidth(), getHeight(), joueurs);
                            etat = EtatJeu.VICTOIRE;
                            break;
                        }
                    }
                }
                if (animation != null && !animation.estTerminee()) animation.update();
                break;

            case VICTOIRE:
                if (animation != null) animation.update();
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float tx = event.getX();
        float ty = event.getY();

        switch (etat) {
            case MENU:
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int touch = menu.detectTouch(tx, ty);
                    if (touch == MenuView.TOUCH_START) {
                        initJoueurs(false);
                        etat = EtatJeu.PLACEMENT_BALLE;
                    } else if (touch == MenuView.TOUCH_MULTI) {
                        initJoueurs(true);
                        etat = EtatJeu.PLACEMENT_BALLE;
                    } else if (touch == MenuView.TOUCH_PHOTO) {
                        // TODO
                        gestionCamera = new GestionCamera(this);
                        initJoueurs(false);
                        etat = EtatJeu.PLACEMENT_BALLE;
                    }
                }
                break;

            case PLACEMENT_BALLE:
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    balles.get(joueurActif).setPosition(tx, ty);
                    if (modeMulti && joueurActif < joueurs.size() - 1) {
                        joueurActif++;
                    } else {
                        joueurActif = 0;
                        etat = EtatJeu.PLACEMENT_SORTIE;
                    }
                }
                break;

            case PLACEMENT_SORTIE:
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    sortie    = new Sortie(tx, ty, balles.get(0).getRayon());
                    joueurActif = 0;
                    animation = new AnimationIntro(
                            AnimationIntro.Type.DEBUT, getWidth(), getHeight(), joueurs);
                    etat = EtatJeu.JEU;
                }
                break;

            case JEU:
                if (animation != null && !animation.estTerminee()) break;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (balleActive().estArretee()) {
                            touchStartX = touchCurrentX = tx;
                            touchStartY = touchCurrentY = ty;
                            viserEnCours = true;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (viserEnCours) { touchCurrentX = tx; touchCurrentY = ty; }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (viserEnCours) {
                            float dx   = touchStartX - tx;
                            float dy   = touchStartY - ty;
                            float dist = (float) Math.sqrt(dx * dx + dy * dy);
                            if (dist > 10f) {
                                float puissance = Math.min(dist / 20f, PUISSANCE_MAX);
                                float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
                                joueurs.get(joueurActif).ajouterCoup();
                                balleActive().lancer(angle, puissance);
                                if (modeMulti) passerJoueurSuivant();
                            }
                            viserEnCours = false;
                        }
                        break;
                }
                break;

            case VICTOIRE:
                if (event.getAction() == MotionEvent.ACTION_UP
                        && animation != null && animation.estTerminee()) {
                    etat  = EtatJeu.MENU;
                    menu  = new MenuView(getWidth(), getHeight());
                    balles.clear();
                    sortie = null;
                    joueurs.forEach(Joueur::reset);
                }
                break;
        }
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        menu = new MenuView(getWidth(), getHeight());
        thread.setRunning(true);
        thread.start();
    }

    @Override public void surfaceChanged(SurfaceHolder h, int f, int w, int v) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try { thread.join(); retry = false; }
            catch (InterruptedException e) { e.printStackTrace(); }
        }
    }
}