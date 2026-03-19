package com.m2sdl.dop.projet;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.m2sdl.dop.projet.views.GameView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(new GameView(this));
        SharedPreferences sharedPref =
                this.getPreferences(Context.MODE_PRIVATE);
        int valeurY = sharedPref.getInt("valeurY", 0);
        valeurY = (valeurY + 100) % 400;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("valeurY", valeurY);
        editor.apply();
    }
}