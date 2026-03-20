package com.m2sdl.dop.projet;

public class Deplacement {
    private float dx;
    private float dy;
    private static final float FRICTION = 0.98f;
    private static final float VITESSE_MIN = 0.5f;

    public Deplacement() {
        this.dx = 0f;
        this.dy = 0f;
    }

    public void lancer(float angleDeg, float puissance) {
        double angleRad = Math.toRadians(angleDeg);
        dx = (float)(Math.cos(angleRad) * puissance);
        dy = (float)(Math.sin(angleRad) * puissance);
    }

    public void appliquerFriction() {
        dx *= FRICTION;
        dy *= FRICTION;
        if (Math.abs(dx) < VITESSE_MIN && Math.abs(dy) < VITESSE_MIN) {
            dx = 0f;
            dy = 0f;
        }
    }

    public boolean estArretee() {
        return dx == 0f && dy == 0f;
    }
    public void appliquerImpulsion(float ix, float iy) {
        dx += ix;
        dy += iy;
    }
    public void toucherMur()     { dx *= -0.8f; }
    public void toucherPlafond() { dy *= -0.8f; }

    public float getDeplacementX() { return dx; }
    public float getDeplacementY() { return dy; }
}