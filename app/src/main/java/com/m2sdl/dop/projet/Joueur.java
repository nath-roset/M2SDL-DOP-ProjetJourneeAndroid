package com.m2sdl.dop.projet;

public class Joueur {
    private String nom;
    private int coups;
    private int couleur;

    public Joueur(String nom, int couleur) {
        this.nom    = nom;
        this.coups  = 0;
        this.couleur = couleur;
    }

    public void ajouterCoup()   { coups++; }
    public int  getCoups()      { return coups; }
    public String getNom()      { return nom; }
    public int getCouleur()     { return couleur; }
    public void reset()         { coups = 0; }
}