package com.m2sdl.dop.projet;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.List;

public class AnimationIntro {
    public enum Type { DEBUT, FIN }

    private Type type;
    private int frame = 0;
    private int largeur, hauteur;
    private List<Joueur> joueurs;
    private static final int DUREE_DEBUT  = 20;
    private static final int DUREE_FIN    = 180;

    public AnimationIntro(Type type, int largeur, int hauteur, List<Joueur> joueurs) {
        this.type    = type;
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.joueurs = joueurs;
    }

    public void update() { frame++; }

    public boolean estTerminee() {
        return frame >= (type == Type.DEBUT ? DUREE_DEBUT : DUREE_FIN);
    }

    public void draw(Canvas canvas) {
        float progression = (float) frame / (type == Type.DEBUT ? DUREE_DEBUT : DUREE_FIN);
        Paint p = new Paint();
        p.setAntiAlias(true);

        if (type == Type.DEBUT) {
            float alpha = 1f - progression;
            p.setColor(Color.argb((int)(255 * alpha), 8, 12, 28));
            canvas.drawRect(0, 0, largeur, hauteur, p);

            float scale  = 0.6f + progression * 0.4f;
            float tAlpha = (float) Math.sin(progression * Math.PI);
            p.setColor(Color.argb((int)(255 * tAlpha), 0, 255, 180));
            p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(180f);
            canvas.save();
            canvas.scale(scale, scale, largeur / 2f, hauteur / 2f);
            canvas.drawText("GO !", largeur / 2f, hauteur / 2f + 60f, p);
            canvas.restore();

        } else {
            int alpha = (int)(255 * Math.min(progression * 1.5f, 1f));
            p.setColor(Color.argb(Math.min(alpha, 220), 8, 12, 28));
            canvas.drawRect(0, 0, largeur, hauteur, p);

            float burst = Math.min(progression * 3f, 1f);
            int[] confettiColors = {
                    Color.rgb(0,255,180), Color.rgb(0,180,255),
                    Color.rgb(255,215,0), Color.rgb(255,100,50)
            };
            for (int i = 0; i < 20; i++) {
                double angle = Math.toRadians(i * 18);
                float dist   = burst * 600f;
                float cx     = largeur / 2f + (float) Math.cos(angle) * dist;
                float cy     = hauteur / 2f + (float) Math.sin(angle) * dist;
                int pAlpha   = (int)(180 * (1f - burst * 0.7f));
                p.setColor(Color.argb(pAlpha,
                        Color.red(confettiColors[i % confettiColors.length]),
                        Color.green(confettiColors[i % confettiColors.length]),
                        Color.blue(confettiColors[i % confettiColors.length])));
                canvas.drawCircle(cx, cy, 16f * (1f - burst * 0.4f), p);
            }

            if (progression > 0.3f) {
                float contentAlpha = Math.min((progression - 0.3f) / 0.25f, 1f);

                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.argb((int)(200 * contentAlpha), 12, 18, 38));
                canvas.drawRoundRect(
                        new RectF(60, hauteur * 0.18f, largeur - 60, hauteur * 0.85f),
                        40f, 40f, p);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(2f);
                p.setColor(Color.argb((int)(200 * contentAlpha), 0, 255, 180));
                canvas.drawRoundRect(
                        new RectF(60, hauteur * 0.18f, largeur - 60, hauteur * 0.85f),
                        40f, 40f, p);
                p.setStyle(Paint.Style.FILL);

                p.setTextAlign(Paint.Align.CENTER);
                p.setTextSize(110f);
                p.setColor(Color.argb((int)(255 * contentAlpha), 255, 215, 0));
                canvas.drawText("DANS LE TROU !", largeur / 2f, hauteur * 0.32f, p);

                p.setColor(Color.argb((int)(120 * contentAlpha), 0, 255, 180));
                p.setStrokeWidth(1.5f);
                p.setStyle(Paint.Style.STROKE);
                canvas.drawLine(100, hauteur * 0.38f, largeur - 100, hauteur * 0.38f, p);
                p.setStyle(Paint.Style.FILL);

                if (joueurs != null) {
                    int vainqueur = 0;
                    for (int i = 1; i < joueurs.size(); i++) {
                        if (joueurs.get(i).getCoups() < joueurs.get(vainqueur).getCoups())
                            vainqueur = i;
                    }

                    for (int i = 0; i < joueurs.size(); i++) {
                        Joueur j      = joueurs.get(i);
                        boolean gagne = (i == vainqueur);
                        float yLine   = hauteur * 0.48f + i * 160f;

                        if (gagne && joueurs.size() > 1) {
                            p.setColor(Color.argb((int)(40 * contentAlpha),
                                    Color.red(j.getCouleur()),
                                    Color.green(j.getCouleur()),
                                    Color.blue(j.getCouleur())));
                            canvas.drawRoundRect(
                                    new RectF(80, yLine - 70f, largeur - 80, yLine + 70f),
                                    20f, 20f, p);
                        }

                        p.setTextSize(70f);
                        p.setTextAlign(Paint.Align.LEFT);
                        p.setColor(Color.argb((int)(255 * contentAlpha),
                                Color.red(j.getCouleur()),
                                Color.green(j.getCouleur()),
                                Color.blue(j.getCouleur())));
                        canvas.drawText(
                                (gagne && joueurs.size() > 1 ? "🏆 " : "     ") + j.getNom(),
                                120f, yLine + 10f, p);

                        p.setTextAlign(Paint.Align.RIGHT);
                        p.setTextSize(80f);
                        int coups = j.getCoups();
                        canvas.drawText(coups + (coups > 1 ? " coups" : " coup"),
                                largeur - 120f, yLine + 10f, p);

                        if (gagne && joueurs.size() > 1) {
                            p.setTextSize(45f);
                            p.setTextAlign(Paint.Align.CENTER);
                            p.setColor(Color.argb((int)(180 * contentAlpha), 255, 215, 0));
                            canvas.drawText("GAGNANT", largeur / 2f, yLine + 65f, p);
                        }
                    }
                }

                p.setTextAlign(Paint.Align.CENTER);
                p.setTextSize(48f);
                p.setColor(Color.argb((int)(140 * contentAlpha), 150, 170, 200));
                canvas.drawText("Tap pour continuer", largeur / 2f, hauteur * 0.91f, p);
            }
        }
    }
}
