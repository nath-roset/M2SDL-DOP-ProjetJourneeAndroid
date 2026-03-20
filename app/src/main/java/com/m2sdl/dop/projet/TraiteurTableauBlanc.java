package com.m2sdl.dop.projet;

import android.graphics.Bitmap;
import com.m2sdl.dop.projet.BoiteDeColision;
import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

public class TraiteurTableauBlanc {

    private static final int SEUIL_NOIRCEUR = 75;
    private static final int SURFACE_MIN    = 500;

    public static List<BoiteDeColision> detecter(
            Bitmap bitmap, int viewWidth, int viewHeight) {

        List<BoiteDeColision> boites = new ArrayList<>();

        Mat matRgba = new Mat();
        Utils.bitmapToMat(bitmap, matRgba);

        Mat matGris = new Mat();
        Imgproc.cvtColor(matRgba, matGris, Imgproc.COLOR_RGBA2GRAY);

        Mat masque = new Mat();
        Imgproc.threshold(matGris, masque,
                SEUIL_NOIRCEUR, 255, Imgproc.THRESH_BINARY_INV);

        Mat noyau = Imgproc.getStructuringElement(
                Imgproc.MORPH_RECT, new Size(9, 9));
        Imgproc.morphologyEx(masque, masque, Imgproc.MORPH_CLOSE,  noyau);
        Imgproc.morphologyEx(masque, masque, Imgproc.MORPH_DILATE, noyau);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(masque, contours,
                new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        float scaleX = (float) viewWidth  / bitmap.getWidth();
        float scaleY = (float) viewHeight / bitmap.getHeight();

        for (MatOfPoint contour : contours) {
            Rect bBox = Imgproc.boundingRect(contour);
            if (bBox.width * bBox.height < SURFACE_MIN) continue;

            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            MatOfPoint2f approx2f  = new MatOfPoint2f();
            double epsilon = 0.01 * Imgproc.arcLength(contour2f, true);
            Imgproc.approxPolyDP(contour2f, approx2f, epsilon, true);

            Point[] points  = approx2f.toArray();
            int     epaisseur = 12;

            for (int i = 0; i < points.length; i++) {
                Point p1 = points[i];
                Point p2 = points[(i + 1) % points.length];

                int x1 = (int)(Math.min(p1.x, p2.x) * scaleX);
                int y1 = (int)(Math.min(p1.y, p2.y) * scaleY);
                int x2 = (int)(Math.max(p1.x, p2.x) * scaleX);
                int y2 = (int)(Math.max(p1.y, p2.y) * scaleY);

                if (x2 - x1 < epaisseur) { x1 -= epaisseur/2; x2 += epaisseur/2; }
                if (y2 - y1 < epaisseur) { y1 -= epaisseur/2; y2 += epaisseur/2; }

                boites.add(new BoiteDeColision(x1, y1, x2, y2));
            }

            contour2f.release();
            approx2f.release();
        }

        matRgba.release();
        matGris.release();
        masque.release();
        noyau.release();

        return boites;
    }
}