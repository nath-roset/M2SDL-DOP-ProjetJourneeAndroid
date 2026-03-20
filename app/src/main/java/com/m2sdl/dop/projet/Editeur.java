package com.m2sdl.dop.projet;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.ArrayList;
import java.util.List;

public class Editeur {

    public enum Outil { AJOUTER, SUPPRIMER }

    private Outil outilActif = Outil.AJOUTER;
    private List<BoiteDeColision> boites;

    private float startX, startY;
    private float currentX, currentY;
    private boolean enDessin = false;
    private static final int EPAISSEUR_MIN = 20;

    public Editeur(List<BoiteDeColision> boites) {
        this.boites = boites;
    }

    public void setOutil(Outil outil) { this.outilActif = outil; }
    public Outil getOutil()           { return outilActif; }

    public void touchDown(float x, float y) {
        if (outilActif == Outil.AJOUTER) {
            startX   = x; startY   = y;
            currentX = x; currentY = y;
            enDessin = true;
        } else {
            supprimerBoiteEn(x, y);
        }
    }

    public void touchMove(float x, float y) {
        if (outilActif == Outil.AJOUTER && enDessin) {
            currentX = x;
            currentY = y;
        }
    }

    public void touchUp(float x, float y) {
        if (outilActif == Outil.AJOUTER && enDessin) {
            currentX = x;
            currentY = y;
            validerBoite();
            enDessin = false;
        }
    }

    private void validerBoite() {
        int left   = (int) Math.min(startX, currentX);
        int top    = (int) Math.min(startY, currentY);
        int right  = (int) Math.max(startX, currentX);
        int bottom = (int) Math.max(startY, currentY);

        if (right - left < EPAISSEUR_MIN) {
            left  -= EPAISSEUR_MIN / 2;
            right += EPAISSEUR_MIN / 2;
        }
        if (bottom - top < EPAISSEUR_MIN) {
            top    -= EPAISSEUR_MIN / 2;
            bottom += EPAISSEUR_MIN / 2;
        }
        boites.add(new BoiteDeColision(left, top, right, bottom));
    }

    private void supprimerBoiteEn(float x, float y) {
        for (int i = boites.size() - 1; i >= 0; i--) {
            BoiteDeColision b = boites.get(i);
            if (x >= b.getBordGauche() && x <= b.getBordDroit()
                    && y >= b.getBordHaut() && y <= b.getBordBas()) {
                boites.remove(i);
                return;
            }
        }
    }

    public void annuler() {
        if (!boites.isEmpty()) boites.remove(boites.size() - 1);
    }

    public void draw(Canvas canvas) {
        if (enDessin && outilActif == Outil.AJOUTER) {
            Paint p = new Paint();
            p.setAntiAlias(true);

            int left   = (int) Math.min(startX, currentX);
            int top    = (int) Math.min(startY, currentY);
            int right  = (int) Math.max(startX, currentX);
            int bottom = (int) Math.max(startY, currentY);

            p.setColor(Color.argb(60, 80, 120, 200));
            canvas.drawRoundRect(new RectF(left, top, right, bottom), 6, 6, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2f);
            p.setColor(Color.argb(200, 80, 120, 200));
            canvas.drawRoundRect(new RectF(left, top, right, bottom), 6, 6, p);
        }
    }

    public void drawUI(Canvas canvas, int w, int h) {
        Paint p = new Paint();
        p.setAntiAlias(true);

        // Fond toolbar
        p.setColor(Color.argb(200, 8, 12, 28));
        canvas.drawRoundRect(new RectF(20, h - 180, w - 20, h - 20), 20, 20, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.5f);
        p.setColor(Color.argb(100, 80, 120, 200));
        canvas.drawRoundRect(new RectF(20, h - 180, w - 20, h - 20), 20, 20, p);
        p.setStyle(Paint.Style.FILL);

        // Bouton AJOUTER
        boolean ajouterActif = outilActif == Outil.AJOUTER;
        p.setColor(ajouterActif
                ? Color.argb(180, 0, 200, 120)
                : Color.argb(80, 30, 40, 70));
        canvas.drawRoundRect(new RectF(40, h - 160, w / 2f - 10, h - 40), 14, 14, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2f);
        p.setColor(ajouterActif ? Color.rgb(0, 255, 150) : Color.argb(80, 80, 120, 200));
        canvas.drawRoundRect(new RectF(40, h - 160, w / 2f - 10, h - 40), 14, 14, p);
        p.setStyle(Paint.Style.FILL);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(48f);
        p.setColor(ajouterActif ? Color.rgb(0, 255, 150) : Color.argb(150, 150, 170, 200));
        canvas.drawText("+ Ajouter", w / 4f + 20, h - 85, p);

        // Bouton SUPPRIMER
        boolean suppActif = outilActif == Outil.SUPPRIMER;
        p.setColor(suppActif
                ? Color.argb(180, 200, 60, 60)
                : Color.argb(80, 30, 40, 70));
        canvas.drawRoundRect(new RectF(w / 2f + 10, h - 160, w - 40, h - 40), 14, 14, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2f);
        p.setColor(suppActif ? Color.rgb(255, 80, 80) : Color.argb(80, 80, 120, 200));
        canvas.drawRoundRect(new RectF(w / 2f + 10, h - 160, w - 40, h - 40), 14, 14, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(suppActif ? Color.rgb(255, 80, 80) : Color.argb(150, 150, 170, 200));
        canvas.drawText("- Effacer", w * 3 / 4f - 20, h - 85, p);

        // Bouton annuler (haut droite)
        p.setColor(Color.argb(160, 8, 12, 28));
        canvas.drawRoundRect(new RectF(w - 200, 30, w - 30, 120), 16, 16, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.5f);
        p.setColor(Color.argb(120, 255, 180, 0));
        canvas.drawRoundRect(new RectF(w - 200, 30, w - 30, 120), 16, 16, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(255, 180, 0));
        p.setTextSize(40f);
        canvas.drawText("↩ Annuler", w - 115, 87, p);
    }

    public boolean touchUI(float x, float y, int w, int h) {
        // Toolbar bas
        if (y > h - 180 && y < h - 20) {
            if (x > 40 && x < w / 2f - 10)       { outilActif = Outil.AJOUTER;    return true; }
            if (x > w / 2f + 10 && x < w - 40)   { outilActif = Outil.SUPPRIMER;  return true; }
        }
        // Bouton annuler
        if (x > w - 200 && x < w - 30 && y > 30 && y < 120) {
            annuler();
            return true;
        }
        return false;
    }
}