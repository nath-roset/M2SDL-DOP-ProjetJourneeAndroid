package com.m2sdl.dop.projet;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Window;
import android.view.WindowManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;
import com.m2sdl.dop.projet.views.GameView;
import org.opencv.android.OpenCVLoader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {

    private static final int REQUEST_IMAGE_CAPTURE  = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2;

    private Uri photoUri;
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (!OpenCVLoader.initDebug()) {
            throw new RuntimeException("Échec init OpenCV");
        }

        gameView = new GameView(this);
        gameView.setOnPhotoRequestListener(this::demanderPermissionCamera);
        setContentView(gameView);
    }

    private void demanderPermissionCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            lancerAppareilPhoto();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.CAMERA },
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            lancerAppareilPhoto();
        }
    }

    private void lancerAppareilPhoto() {
        File photoFile = creerFichierPhoto();
        photoUri = FileProvider.getUriForFile(
                this, getPackageName() + ".provider", photoFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private File creerFichierPhoto() {
        File dir = getExternalFilesDir("photos");
        return new File(dir, "tableau_" + System.currentTimeMillis() + ".jpg");
    }

    // Source - https://stackoverflow.com/a/37958098
    // Posted by Fuyuba, modified by community. See post 'Timeline' for change history
    // Retrieved 2026-03-20, License - CC BY-SA 3.0
    private int getRotationFromCamera(Context context, Uri imageFile) {
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageFile, null);
            InputStream inputStream = context.getContentResolver()
                    .openInputStream(imageFile);
            ExifInterface exif = new ExifInterface(inputStream);
            inputStream.close();
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270: rotate = 270; break;
                case ExifInterface.ORIENTATION_ROTATE_180: rotate = 180; break;
                case ExifInterface.ORIENTATION_ROTATE_90:  rotate =  90; break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) gameView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) gameView.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            traiterPhotoEtLancerJeu();
        }
        // annulation caméra -> retour menu sans crash
    }

    private void traiterPhotoEtLancerJeu() {
        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        int rotation = getRotationFromCamera(this, photoUri);
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            bitmap = Bitmap.createBitmap(
                    bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(),
                    matrix, true);
        }

        final Bitmap bitmapFinal = bitmap;

        // Dialog pour choisir solo ou multi avant de lancer
        new android.app.AlertDialog.Builder(this)
                .setTitle("Mode de jeu")
                .setMessage("Solo ou multijoueur ?")
                .setPositiveButton("Solo",  (d, w) -> gameView.lancerModePhotoAvecBitmap(bitmapFinal, false))
                .setNegativeButton("Multi", (d, w) -> gameView.lancerModePhotoAvecBitmap(bitmapFinal, true))
                .setCancelable(false)
                .show();
    }
}