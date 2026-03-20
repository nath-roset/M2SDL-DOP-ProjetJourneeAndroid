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
import com.m2sdl.dop.projet.Editeur;
import com.m2sdl.dop.projet.EtatJeu;
import com.m2sdl.dop.projet.Joueur;
import com.m2sdl.dop.projet.Sortie;
import com.m2sdl.dop.projet.bs.GameThread;
import com.m2sdl.dop.projet.TraiteurTableauBlanc;

import java.util.ArrayList;
import java.util.List;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    public interface OnPhotoRequestListener {
        void onPhotoRequested();
    }

    private GameThread thread;
    private List<Balle> balles = new ArrayList<>();
    private Sortie sortie;
    private List<BoiteDeColision> boites = new ArrayList<>();
    private EtatJeu etat = EtatJeu.MENU;
    private MenuView menu;
    private AnimationIntro animation;
    private Editeur editeur;
    private boolean menuBurgerOuvert = false;
    private int largeurEcran = 0, hauteurEcran = 0;

    private Bitmap fondTableau = null;
    private Bitmap fondTableauSauvegarde = null;
    private List<BoiteDeColision> boitesSauvegarde = new ArrayList<>();

    private List<Joueur> joueurs = new ArrayList<>();
    private int joueurActif = 0;
    private boolean modeMulti = false;
    private boolean modePhoto = false;

    private OnPhotoRequestListener photoListener;

    private float touchStartX, touchStartY;
    private float touchCurrentX, touchCurrentY;
    private boolean viserEnCours = false;
    private static final float PUISSANCE_MAX = 30f;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
        initBoitesDefaut();
    }

    public void setOnPhotoRequestListener(OnPhotoRequestListener listener) {
        this.photoListener = listener;
    }

    // lancerModePhotoAvecBitmap — utiliser largeurEcran/hauteurEcran
    public void lancerModePhotoAvecBitmap(Bitmap bitmap, boolean multi) {
        this.fondTableau           = bitmap;
        this.fondTableauSauvegarde = bitmap;
        this.modePhoto             = true;

        new Thread(() -> {
            // Attendre que les dimensions soient disponibles
            int w = largeurEcran > 0 ? largeurEcran : getWidth();
            int h = hauteurEcran > 0 ? hauteurEcran : getHeight();

            // Si toujours 0, attendre
            int tentatives = 0;
            while ((w == 0 || h == 0) && tentatives < 20) {
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                w = largeurEcran > 0 ? largeurEcran : getWidth();
                h = hauteurEcran > 0 ? hauteurEcran : getHeight();
                tentatives++;
            }

            final int fw = w, fh = h;
            List<BoiteDeColision> detectees =
                    TraiteurTableauBlanc.detecter(bitmap, fw, fh);

            post(() -> {
                boites           = detectees;
                boitesSauvegarde = new ArrayList<>(detectees);
                initJoueurs(multi);
                editeur = new Editeur(boites);
                etat    = EtatJeu.EDITEUR;
            });
        }).start();
    }

    private void initBoitesDefaut() {
        boites.clear();
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

    private void rejouer() {
        menuBurgerOuvert = false;
        joueurs.forEach(Joueur::reset);
        initJoueurs(modeMulti);
        sortie       = null;
        animation    = null;
        viserEnCours = false;
        etat         = EtatJeu.PLACEMENT_BALLE;
    }

    private void retourMenu() {
        modePhoto   = false;
        fondTableau = null;
        etat        = EtatJeu.MENU;
        menu        = new MenuView(getWidth(), getHeight());
        balles.clear();
        sortie       = null;
        animation    = null;
        viserEnCours = false;
        joueurs.forEach(Joueur::reset);
        initBoitesDefaut();
    }

    // ------------------------------------------------------------------ UPDATE

    public void update() {
        switch (etat) {
            case MENU:
                if (menu != null) menu.update();
                break;

            case JEU:
                for (Balle b : balles) b.update(getHeight(), getWidth(), boites);

                for (int i = 0; i < balles.size(); i++) {
                    for (int j = i + 1; j < balles.size(); j++) {
                        balles.get(i).resoudreCollisionAvec(balles.get(j));
                    }
                }

                if (sortie != null) {
                    sortie.update();
                    for (int i = 0; i < balles.size(); i++) {
                        if (sortie.estTouchee(
                                balles.get(i).getX(),
                                balles.get(i).getY(),
                                balles.get(i).getRayon())) {
                            animation = new AnimationIntro(
                                    AnimationIntro.Type.FIN,
                                    getWidth(), getHeight(), joueurs);
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

            case EDITEUR:
                break;
        }
    }

    // ------------------------------------------------------------------ DRAW

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas == null) return;

        switch (etat) {
            case MENU:
                if (menu != null) menu.draw(canvas);
                break;

            case EDITEUR:
                dessinerFond(canvas);
                if (editeur != null) {
                    editeur.draw(canvas);
                    editeur.drawUI(canvas, getWidth(), getHeight());
                }
                drawBoutonEditeur(canvas, "▶ Jouer",
                        getWidth() / 2f - 120, Color.rgb(0, 255, 150));
                drawBoutonRetourMenu(canvas);
                break;

            case PLACEMENT_BALLE:
                dessinerFond(canvas);
                for (int i = 0; i < joueurActif; i++) balles.get(i).draw(canvas);
                if (!joueurs.isEmpty()) {
                    drawInstructions(canvas,
                            joueurs.size() > 1
                                    ? "Placer balle " + joueurs.get(joueurActif).getNom()
                                    : "Touchez pour placer la balle",
                            joueurs.get(joueurActif).getCouleur());
                }
                drawBoutonRetourMenu(canvas);
                break;

            case PLACEMENT_SORTIE:
                dessinerFond(canvas);
                for (Balle b : balles) b.draw(canvas);
                drawInstructions(canvas, "Touchez pour placer la sortie",
                        Color.rgb(255, 215, 0));
                drawBoutonRetourMenu(canvas);
                break;

            case JEU:
                dessinerFond(canvas);
                if (sortie != null) sortie.draw(canvas);
                for (int i = balles.size() - 1; i >= 0; i--) {
                    if (i != joueurActif) balles.get(i).draw(canvas);
                }
                if (viserEnCours && !balles.isEmpty() && balleActive().estArretee()) {
                    float dx        = touchStartX - touchCurrentX;
                    float dy        = touchStartY - touchCurrentY;
                    float dist      = (float) Math.sqrt(dx * dx + dy * dy);
                    float puissance = Math.min(dist / 20f, PUISSANCE_MAX);
                    float angle     = (float) Math.toDegrees(Math.atan2(dy, dx));
                    balleActive().dessinerTrajectoire(canvas, angle, puissance);
                }
                if (!balles.isEmpty()) balleActive().draw(canvas);
                drawHUD(canvas);
                drawMenuBurger(canvas);
                if (animation != null && !animation.estTerminee()) animation.draw(canvas);
                break;

            case VICTOIRE:
                dessinerFond(canvas);
                if (sortie != null) sortie.draw(canvas);
                for (Balle b : balles) b.draw(canvas);
                if (animation != null) animation.draw(canvas);
                if (animation != null && animation.estTerminee()) {
                    drawBoutonsVictoire(canvas);
                }
                break;
        }
    }

    // ------------------------------------------------------------------ FOND

    private void dessinerFond(Canvas canvas) {
        if (modePhoto && fondTableau != null) {
            canvas.drawBitmap(fondTableau, null,
                    new RectF(0, 0, getWidth(), getHeight()), null);
        } else {
            canvas.drawColor(Color.rgb(8, 12, 28));
            Paint grid = new Paint();
            grid.setColor(Color.argb(12, 0, 255, 180));
            grid.setStrokeWidth(1f);
            int step = 80;
            for (int x = 0; x < getWidth();  x += step)
                canvas.drawLine(x, 0, x, getHeight(), grid);
            for (int y = 0; y < getHeight(); y += step)
                canvas.drawLine(0, y, getWidth(), y, grid);
        }
        for (BoiteDeColision b : boites) b.draw(canvas);
    }

    // ------------------------------------------------------------------ UI

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

    private void drawBoutonRetourMenu(Canvas canvas) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.argb(160, 8, 12, 28));
        canvas.drawRoundRect(new RectF(30, 30, 230, 110), 16, 16, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.5f);
        p.setColor(Color.argb(180, 200, 80, 80));
        canvas.drawRoundRect(new RectF(30, 30, 230, 110), 16, 16, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(255, 100, 100));
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(40f);
        canvas.drawText("← Menu", 130, 82, p);
    }

    private void drawBoutonsVictoire(Canvas canvas) {
        int w  = getWidth();
        int h  = getHeight();
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setTextAlign(Paint.Align.CENTER);

        float btnW = 400f, btnH = 110f;
        float cx   = w / 2f;
        float yR   = h * 0.88f;
        float yM   = h * 0.88f + 140f;

        p.setColor(Color.argb(160, 8, 12, 28));
        canvas.drawRoundRect(new RectF(cx - btnW/2, yR, cx + btnW/2, yR + btnH), 20, 20, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2f);
        p.setColor(Color.rgb(0, 255, 150));
        canvas.drawRoundRect(new RectF(cx - btnW/2, yR, cx + btnW/2, yR + btnH), 20, 20, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(0, 255, 150));
        p.setTextSize(56f);
        canvas.drawText("↺ Rejouer", cx, yR + 74f, p);

        p.setColor(Color.argb(160, 8, 12, 28));
        canvas.drawRoundRect(new RectF(cx - btnW/2, yM, cx + btnW/2, yM + btnH), 20, 20, p);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.rgb(255, 100, 100));
        canvas.drawRoundRect(new RectF(cx - btnW/2, yM, cx + btnW/2, yM + btnH), 20, 20, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(255, 100, 100));
        canvas.drawText("← Menu", cx, yM + 74f, p);
    }

    private void drawBoutonEditeur(Canvas canvas, String label, float x, int couleur) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.argb(160, 8, 12, 28));
        canvas.drawRoundRect(new RectF(x, 30, x + 240, 120), 16, 16, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2f);
        p.setColor(couleur);
        canvas.drawRoundRect(new RectF(x, 30, x + 240, 120), 16, 16, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(couleur);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(48f);
        canvas.drawText(label, x + 120, 87, p);
    }

    // ------------------------------------------------------------------ TOUCH

    private boolean touchBoutonRetourMenu(float tx, float ty) {
        return tx > 30 && tx < 230 && ty > 30 && ty < 110;
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
                        modePhoto   = false;
                        fondTableau = null;
                        initBoitesDefaut();
                        initJoueurs(false);
                        etat = EtatJeu.EDITEUR;
                        editeur = new Editeur(boites);
                    } else if (touch == MenuView.TOUCH_MULTI) {
                        modePhoto   = false;
                        fondTableau = null;
                        initBoitesDefaut();
                        initJoueurs(true);
                        etat = EtatJeu.EDITEUR;
                        editeur = new Editeur(boites);
                    } else if (touch == MenuView.TOUCH_PHOTO) {
                        if (photoListener != null) photoListener.onPhotoRequested();
                    }
                }
                break;

            case EDITEUR:
                if (editeur == null) break;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (touchBoutonRetourMenu(tx, ty)) {
                        retourMenu();
                        return true;
                    }
                    float bx = getWidth() / 2f - 120;
                    if (tx > bx && tx < bx + 240 && ty > 30 && ty < 120) {
                        editeur = null;
                        etat    = EtatJeu.PLACEMENT_BALLE;
                        return true;
                    }
                    if (editeur.touchUI(tx, ty, getWidth(), getHeight())) return true;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: editeur.touchDown(tx, ty); break;
                    case MotionEvent.ACTION_MOVE: editeur.touchMove(tx, ty); break;
                    case MotionEvent.ACTION_UP:   editeur.touchUp(tx, ty);   break;
                }
                break;

            case PLACEMENT_BALLE:
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (touchBoutonRetourMenu(tx, ty)) { retourMenu(); return true; }
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
                    if (touchBoutonRetourMenu(tx, ty)) { retourMenu(); return true; }
                    sortie      = new Sortie(tx, ty, balles.get(0).getRayon());
                    joueurActif = 0;
                    animation   = new AnimationIntro(
                            AnimationIntro.Type.DEBUT, getWidth(), getHeight(), joueurs);
                    etat = EtatJeu.JEU;
                }
                break;

            case JEU:
                if (animation != null && !animation.estTerminee()) break;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (touchMenuBurger(tx, ty)) return true;
                    if (menuBurgerOuvert) return true;
                }
                if (menuBurgerOuvert) return true; // bloquer le jeu quand menu ouvert
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!balles.isEmpty() && balleActive().estArretee()) {
                            touchStartX = touchCurrentX = tx;
                            touchStartY = touchCurrentY = ty;
                            viserEnCours = true;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (viserEnCours) {
                            touchCurrentX = tx;
                            touchCurrentY = ty;
                        }
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
                if (animation == null || !animation.estTerminee()) break;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int w      = getWidth();
                    int h      = getHeight();
                    float cx   = w / 2f;
                    float btnW = 400f, btnH = 110f;
                    float yR   = h * 0.88f;
                    float yM   = h * 0.88f + 140f;

                    if (tx > cx - btnW/2 && tx < cx + btnW/2
                            && ty > yR && ty < yR + btnH) {
                        rejouer();
                        return true;
                    }
                    if (tx > cx - btnW/2 && tx < cx + btnW/2
                            && ty > yM && ty < yM + btnH) {
                        retourMenu();
                        return true;
                    }
                }
                break;
        }
        return true;
    }

    // ------------------------------------------------------------------ SURFACE

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (menu == null) menu = new MenuView(getWidth(), getHeight());
        demarrerThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        largeurEcran = width;
        hauteurEcran = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        arreterThread();
    }

    public void onResume() { demarrerThread(); }
    public void onPause()  { arreterThread();  }

    private void demarrerThread() {
        if (thread != null && thread.isRunning()) return;
        thread = new GameThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }

    private void arreterThread() {
        if (thread == null) return;
        thread.setRunning(false);
        try { thread.join(500); } catch (InterruptedException e) { e.printStackTrace(); }
        thread = null;
    }

    private void drawMenuBurger(Canvas canvas) {
        int w = getWidth();
        Paint p = new Paint();
        p.setAntiAlias(true);

        // Bouton burger (3 traits)
        p.setColor(Color.argb(180, 8, 12, 28));
        canvas.drawRoundRect(new RectF(w - 110, 30, w - 20, 120), 14, 14, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.5f);
        p.setColor(Color.argb(200, 150, 180, 255));
        canvas.drawRoundRect(new RectF(w - 110, 30, w - 20, 120), 14, 14, p);

        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(200, 220, 255));
        p.setStrokeWidth(6f);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStyle(Paint.Style.STROKE);
        canvas.drawLine(w - 95, 58,  w - 35, 58,  p);
        canvas.drawLine(w - 95, 75,  w - 35, 75,  p);
        canvas.drawLine(w - 95, 92,  w - 35, 92,  p);

        if (menuBurgerOuvert) {
            drawMenuBurgerOuvert(canvas);
        }
    }

    private void drawMenuBurgerOuvert(Canvas canvas) {
        int w = getWidth();
        Paint p = new Paint();
        p.setAntiAlias(true);

        // Fond du menu
        p.setColor(Color.argb(220, 8, 12, 28));
        canvas.drawRoundRect(new RectF(w - 360, 130, w - 20, 460), 20, 20, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.5f);
        p.setColor(Color.argb(120, 150, 180, 255));
        canvas.drawRoundRect(new RectF(w - 360, 130, w - 20, 460), 20, 20, p);
        p.setStyle(Paint.Style.FILL);

        drawItemBurger(canvas, "← Menu",    w - 360, 150, Color.rgb(255, 100, 100));
        drawItemBurger(canvas, "↺ Rejouer", w - 360, 260, Color.rgb(0, 255, 150));
        drawItemBurger(canvas, "✎ Éditer",  w - 360, 370, Color.rgb(255, 180, 0));
    }

    private void drawItemBurger(Canvas canvas, String label, float x, float y, int couleur) {
        int w = getWidth();
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.argb(100, Color.red(couleur),
                Color.green(couleur), Color.blue(couleur)));
        canvas.drawRoundRect(new RectF(x + 10, y, w - 30, y + 90), 14, 14, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.5f);
        p.setColor(Color.argb(180, Color.red(couleur),
                Color.green(couleur), Color.blue(couleur)));
        canvas.drawRoundRect(new RectF(x + 10, y, w - 30, y + 90), 14, 14, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(couleur);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(52f);
        canvas.drawText(label, x + 10 + (w - 40 - x) / 2f, y + 62f, p);
    }

    private boolean touchMenuBurger(float tx, float ty) {
        int w = getWidth();
        // Bouton burger
        if (tx > w - 110 && tx < w - 20 && ty > 30 && ty < 120) {
            menuBurgerOuvert = !menuBurgerOuvert;
            return true;
        }
        if (menuBurgerOuvert) {
            // Menu
            if (tx > w - 360 && tx < w - 20 && ty > 150 && ty < 240) {
                menuBurgerOuvert = false; retourMenu(); return true;
            }
            // Rejouer
            if (tx > w - 360 && tx < w - 20 && ty > 260 && ty < 350) {
                menuBurgerOuvert = false; rejouer(); return true;
            }
            // Éditer — retourne en éditeur
            if (tx > w - 360 && tx < w - 20 && ty > 370 && ty < 460) {
                menuBurgerOuvert = false;
                editeur = new Editeur(boites);
                etat    = EtatJeu.EDITEUR;
                return true;
            }
            // Fermer si tap ailleurs
            menuBurgerOuvert = false;
            return false;
        }
        return false;
    }
}