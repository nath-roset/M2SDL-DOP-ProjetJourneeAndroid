package com.m2sdl.dop.projet;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import java.util.List;

public class Balle {
    private int rayon = 20;
    private float x = 100;
    private float y = 350;
    private Deplacement deplacement;
    private int gameViewWidth;
    private int gameViewHeight;
    private List<BoiteDeColision> boites;
    private int couleurJoueur = Color.WHITE;
    private final Paint paintBalle = new Paint();
    private final Paint paintTrajectoire = new Paint();

    public Balle() {
        this.deplacement = new Deplacement();

        paintBalle.setAntiAlias(true);

        paintTrajectoire.setAntiAlias(true);
        paintTrajectoire.setColor(Color.argb(180, 255, 255, 255));
        paintTrajectoire.setStyle(Paint.Style.STROKE);
        paintTrajectoire.setStrokeWidth(3f);
        paintTrajectoire.setPathEffect(new DashPathEffect(new float[]{15f, 10f}, 0));
    }

    public void update(int gameViewHeight, int gameViewWidth, List<BoiteDeColision> boites) {
        this.gameViewHeight = gameViewHeight;
        this.gameViewWidth  = gameViewWidth;
        this.boites         = boites;
        if (!deplacement.estArretee()) {
            calculerDeplacementBalle();
            deplacement.appliquerFriction();
        }
    }

    public void lancer(float angleDeg, float puissance) {
        deplacement.lancer(angleDeg, puissance);
    }

    public boolean estArretee() {
        return deplacement.estArretee();
    }

    public void dessinerTrajectoire(Canvas canvas, float angleDeg, float puissance) {
        float simX  = x;
        float simY  = y;
        double rad  = Math.toRadians(angleDeg);
        float simDx = (float)(Math.cos(rad) * puissance);
        float simDy = (float)(Math.sin(rad) * puissance);

        float porteeMax = rayon * 2 * 8.5f;
        float distanceParcourue = 0f;

        int maxSteps = 400;

        for (int step = 0; step < maxSteps; step++) {

            float nx = simX + simDx;
            float ny = simY + simDy;

            boolean rebond = false;

            if (nx - rayon < 0) {
                nx = rayon; simDx *= -0.8f; rebond = true;
            } else if (nx + rayon > gameViewWidth) {
                nx = gameViewWidth - rayon; simDx *= -0.8f; rebond = true;
            }
            if (ny - rayon < 0) {
                ny = rayon; simDy *= -0.8f; rebond = true;
            } else if (ny + rayon > gameViewHeight) {
                ny = gameViewHeight - rayon; simDy *= -0.8f; rebond = true;
            }

            for (BoiteDeColision b : boites) {
                float ppX = Math.max(b.getBordGauche(), Math.min(nx, b.getBordDroit()));
                float ppY = Math.max(b.getBordHaut(),   Math.min(ny, b.getBordBas()));
                float ddx = nx - ppX;
                float ddy = ny - ppY;
                if (ddx * ddx + ddy * ddy < (float) rayon * rayon) {
                    if (Math.abs(ddx) > Math.abs(ddy)) simDx *= -0.8f;
                    else                               simDy *= -0.8f;
                    rebond = true;
                    break;
                }
            }

            float stepDist = (float) Math.sqrt(
                    (nx - simX) * (nx - simX) + (ny - simY) * (ny - simY)
            );
            distanceParcourue += stepDist;

            simX = nx;
            simY = ny;

            float progression = Math.min(distanceParcourue / porteeMax, 1f);

            float rayonPoint = rayon * 0.25f * (1f - progression);
            int alpha         = (int)(220 * (1f - progression));

            if (rayonPoint > 0.5f && alpha > 5) {
                Paint p = new Paint();
                p.setAntiAlias(true);
                p.setColor(Color.argb(alpha, 90, 200, 100));
                canvas.drawCircle(simX, simY, rayonPoint, p);
            }

            simDx *= 0.98f;
            simDy *= 0.98f;

            if (distanceParcourue >= porteeMax) break;
            if (Math.abs(simDx) + Math.abs(simDy) < 0.5f) break;
        }
    }

    private void calculerDeplacementBalle() {
        float dx = deplacement.getDeplacementX();
        float dy = deplacement.getDeplacementY();

        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float epaisseurMin = epaisseurMinBoites();
        float pasMax = Math.min(rayon, epaisseurMin) / 2f;
        int etapes = (int) Math.ceil(distance / pasMax);

        float stepX = dx / etapes;
        float stepY = dy / etapes;

        for (int i = 0; i < etapes; i++) {
            x += stepX;
            y += stepY;
            collisionBordEcran();
            collisionObstacles();
        }
    }
    public void resoudreCollisionAvec(Balle autre) {
        float dx   = autre.getX() - x;
        float dy   = autre.getY() - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        float distMin = rayon + autre.rayon;

        if (dist >= distMin || dist == 0) return;

        // Séparer les deux balles
        float overlap = (distMin - dist) / 2f;
        float nx = dx / dist;
        float ny = dy / dist;

        x      -= nx * overlap;
        y      -= ny * overlap;
        autre.x += nx * overlap;
        autre.y += ny * overlap;

        // Échanger les vitesses sur l'axe de collision (rebond élastique)
        float dvx = autre.deplacement.getDeplacementX() - deplacement.getDeplacementX();
        float dvy = autre.deplacement.getDeplacementY() - deplacement.getDeplacementY();
        float dot  = dvx * nx + dvy * ny;

        if (dot > 0) return; // déjà en train de s'éloigner

        float impulse = dot * 0.85f;
        deplacement.appliquerImpulsion( impulse * nx,  impulse * ny);
        autre.deplacement.appliquerImpulsion(-impulse * nx, -impulse * ny);
    }

    private float epaisseurMinBoites() {
        float min = rayon;
        for (BoiteDeColision b : boites) {
            float largeur = b.getBordDroit() - b.getBordGauche();
            float hauteur = b.getBordBas()   - b.getBordHaut();
            min = Math.min(min, Math.min(largeur, hauteur));
        }
        return Math.max(min, 1f);
    }

    private void collisionObstacles() {
        boolean rebondiX = false;
        boolean rebondiY = false;

        for (BoiteDeColision b : boites) {
            float plusProcheX = Math.max(b.getBordGauche(), Math.min(x, b.getBordDroit()));
            float plusProcheY = Math.max(b.getBordHaut(),   Math.min(y, b.getBordBas()));

            float dx = x - plusProcheX;
            float dy = y - plusProcheY;
            float d2 = dx * dx + dy * dy;

            if (d2 > rayon * rayon) continue;

            float distance = (float) Math.sqrt(d2);
            if (distance == 0) continue;

            float penetration = rayon - distance;
            x += (dx / distance) * penetration;
            y += (dy / distance) * penetration;

            boolean collisionHorizontale = Math.abs(dx) > Math.abs(dy);
            if (collisionHorizontale && !rebondiX) {
                deplacement.toucherMur();
                rebondiX = true;
            } else if (!collisionHorizontale && !rebondiY) {
                deplacement.toucherPlafond();
                rebondiY = true;
            }
        }
    }

    private void collisionBordEcran() {
        if (x - rayon < 0) {
            x = rayon; deplacement.toucherMur();
        } else if (x + rayon > gameViewWidth) {
            x = gameViewWidth - rayon; deplacement.toucherMur();
        }
        if (y - rayon < 0) {
            y = rayon; deplacement.toucherPlafond();
        } else if (y + rayon > gameViewHeight) {
            y = gameViewHeight - rayon; deplacement.toucherPlafond();
        }
    }

    public float getX()    { return x; }
    public float getY()    { return y; }
    public int getRayon()  { return rayon; }

    public void draw(Canvas canvas) {
        Paint p = new Paint();
        p.setAntiAlias(true);

        if (deplacement.estArretee()) {
            p.setColor(Color.argb(60, 255, 255, 255));
            canvas.drawCircle(x, y, rayon * 1.8f, p);
            p.setColor(couleurJoueur);
            canvas.drawCircle(x, y, rayon, p);
        } else {
            p.setColor(Color.WHITE);
            canvas.drawCircle(x, y, rayon, p);
        }

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2f);
        p.setColor(Color.argb(120, 255, 255, 255));
        canvas.drawCircle(x, y, rayon, p);
    }

    public void setCouleur(int couleur) { this.couleurJoueur = couleur; }
    public void setPosition(float px, float py) {
        this.x = px;
        this.y = py;
    }


}