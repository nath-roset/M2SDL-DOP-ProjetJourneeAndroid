package com.m2sdl.dop.projet;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;

import com.m2sdl.dop.projet.views.GameView;

import org.opencv.android.OpenCVLoader;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
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

        lancerAppareilPhoto();
    }

    private void lancerAppareilPhoto() {
        File photoFile = creerFichierPhoto();
        photoUri = FileProvider.getUriForFile(
                this, getPackageName() + ".provider", photoFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
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
                    getContentResolver(), photoUri);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        gameView = new GameView(this, bitmap);
        setContentView(gameView);
    }

    private File creerFichierPhoto() {
        File dir = getExternalFilesDir("photos");
        return new File(dir, "tableau_" + System.currentTimeMillis() + ".jpg");
    }
}