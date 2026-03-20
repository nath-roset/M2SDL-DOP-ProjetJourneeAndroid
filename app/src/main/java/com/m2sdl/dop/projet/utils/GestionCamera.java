package com.m2sdl.dop.projet.utils;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.m2sdl.dop.projet.views.GameView;

import org.opencv.android.OpenCVLoader;
import java.io.File;
import java.io.IOException;

public class GestionCamera {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri photoUri;
    private GameView view;

    public GestionCamera(GameView view) {
        this.view = view;
    }

    public void startGestionCamera() {
        if (!OpenCVLoader.initLocal()) {
            throw new RuntimeException("Échec init OpenCV");
        }

        lancerAppareilPhoto();
    }

    private void lancerAppareilPhoto() {
        File photoFile = creerFichierPhoto();
        photoUri = FileProvider.getUriForFile(view.getContext(), view.getContext().getPackageName() + ".provider", photoFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        Activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            traiterPhotoEtLancerJeu();
        }
    }

    private void traiterPhotoEtLancerJeu() {
        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(
                    view.getContext().getContentResolver(), photoUri);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // gameView = new GameView(this, bitmap); => ENVOYER LA BITMAP AU GAMEVIEW
        view.setBitmap(bitmap);
        // TODO - CLOSE CAMERA
    }

    private File creerFichierPhoto() {
        File dir = view.getContext().getExternalFilesDir("photos");
        return new File(dir, "tableau_" + System.currentTimeMillis() + ".jpg");
    }
}
