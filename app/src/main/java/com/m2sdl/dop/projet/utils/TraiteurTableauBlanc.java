package com.m2sdl.dop.projet.utils;

import android.graphics.Bitmap;

import com.m2sdl.dop.projet.BoiteDeColision;

import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

public class TraiteurTableauBlanc {
    private static final Scalar BLEU_BAS  = new Scalar(175, 40, 70);
    private static final Scalar BLEU_HAUT = new Scalar(270, 100, 100);
    private static final int SURFACE_MIN  = 500;

    /**
     * Analyse le bitmap et retourne les boîtes de collision
     * correspondant aux traits bleus détectés.
     *
     * @param bitmap      Photo du tableau blanc
     * @param viewWidth   Largeur de la GameView (pour mise à l'échelle)
     * @param viewHeight  Hauteur de la GameView
     */
    public static List<BoiteDeColision> detecter(
            Bitmap bitmap, int viewWidth, int viewHeight) {

        List<BoiteDeColision> boites = new ArrayList<>();

        // 1. Bitmap → Mat BGR
        Mat matBgr = new Mat();
        Utils.bitmapToMat(bitmap, matBgr);

        // 2. BGR → HSV
        Mat matHsv = new Mat();
        Imgproc.cvtColor(matBgr, matHsv, Imgproc.COLOR_BGR2HSV);

        // 3. Masque sur la plage bleue
        Mat masque = new Mat();
        Core.inRange(matHsv, BLEU_BAS, BLEU_HAUT, masque);

        // 4. Morphologie : fermeture pour combler les trous dans les traits
        Mat noyau = Imgproc.getStructuringElement(
                Imgproc.MORPH_RECT, new Size(9, 9));
        Imgproc.morphologyEx(masque, masque, Imgproc.MORPH_CLOSE, noyau);
        Imgproc.morphologyEx(masque, masque, Imgproc.MORPH_DILATE, noyau);

        // 5. Détection des contours
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(masque, contours,
                new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // 6. Facteurs de mise à l'échelle bitmap → écran
        float scaleX = (float) viewWidth  / bitmap.getWidth();
        float scaleY = (float) viewHeight / bitmap.getHeight();

        // 7. Convertir chaque contour en BoiteDeColision
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);

            if (rect.width * rect.height < SURFACE_MIN) continue;

            int left   = (int) (rect.x                * scaleX);
            int top    = (int) (rect.y                * scaleY);
            int right  = (int) ((rect.x + rect.width) * scaleX);
            int bottom = (int) ((rect.y + rect.height)* scaleY);

            boites.add(new BoiteDeColision(left, top, right, bottom));
        }

        // Libération mémoire
        matBgr.release(); matHsv.release();
        masque.release(); noyau.release();

        return boites;
    }
}