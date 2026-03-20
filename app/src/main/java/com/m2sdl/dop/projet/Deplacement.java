package com.m2sdl.dop.projet;

public class Deplacement {
    private float dx;
    private float dy;
    private float vitesse;

    public Deplacement() {
        this.dx = 0.5f;
        this.dy = 0.5f;
        this.vitesse = 40f;
    }

    public float getDeplacementY(){
        return dy*vitesse;
    }
    public float getDeplacementX(){
        return dx*vitesse;
    }

    public void toucherMur(){
        dx = dx * -1f;
    }
    public void toucherPlafond(){
        dy = dy * -1f;
    }

}
