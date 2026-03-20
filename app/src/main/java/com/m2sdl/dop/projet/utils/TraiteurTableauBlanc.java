package com.m2sdl.dop.projet.utils;

import android.graphics.Bitmap;

import com.m2sdl.dop.projet.BoiteDeColision;

import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

public class TraiteurTableauBlanc {

    // Par seuil de luminosité
    private static final int SEUIL_NOIRCEUR = 60;
    private static final int SURFACE_MIN = 500;

    public static List<BoiteDeColision> detecter(
            Bitmap bitmap, int viewWidth, int viewHeight) {

        List<BoiteDeColision> boites = new ArrayList<>();

        // 1. Bitmap (RGBA) → Mat RGBA
        Mat matRgba = new Mat();
        Utils.bitmapToMat(bitmap, matRgba);

        // 2. RGBA → Niveaux de gris
        Mat matGris = new Mat();
        Imgproc.cvtColor(matRgba, matGris, Imgproc.COLOR_RGBA2GRAY);

        // 3. Seuillage inverse
        Mat masque = new Mat();
        Imgproc.threshold(matGris, masque,
                SEUIL_NOIRCEUR,
                255,
                Imgproc.THRESH_BINARY_INV);

        // 4. Morphologie
        Mat noyau = Imgproc.getStructuringElement(
                Imgproc.MORPH_RECT, new Size(9, 9));
        Imgproc.morphologyEx(masque, masque, Imgproc.MORPH_CLOSE, noyau);
        Imgproc.morphologyEx(masque, masque, Imgproc.MORPH_DILATE, noyau);

        // 5. Détection des contours
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(masque, contours,
                new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // 6. Mise à l'échelle
        float scaleX = (float) viewWidth  / bitmap.getWidth();
        float scaleY = (float) viewHeight / bitmap.getHeight();

        // 7. BoiteDeColision
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            if (rect.width * rect.height < SURFACE_MIN) continue;

            int left   = (int) (rect.x                 * scaleX);
            int top    = (int) (rect.y                 * scaleY);
            int right  = (int) ((rect.x + rect.width)  * scaleX);
            int bottom = (int) ((rect.y + rect.height) * scaleY);

            boites.add(new BoiteDeColision(left, top, right, bottom));
        }

        matRgba.release();
        matGris.release();
        masque.release();
        noyau.release();

        return boites;
    }
}