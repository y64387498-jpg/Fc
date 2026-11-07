package com.example.fcmobilehub;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int OVERLAY_REQUEST = 4201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER);
        page.setPadding(28, 32, 28, 32);
        page.setBackgroundColor(Color.rgb(10, 13, 20));

        TextView title = new TextView(this);
        title.setText("FC MOBILE HUB");
        title.setTextColor(Color.WHITE);
        title.setTextSize(27);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("Oyuna hızlı giriş ve yüzen menü");
        subtitle.setTextColor(Color.rgb(160, 170, 185));
        subtitle.setTextSize(15);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 10, 0, 38);

        Button start = new Button(this);
        start.setText("BAŞLAT  ⚽");
        start.setTextSize(19);
        start.setAllCaps(false);
        start.setTextColor(Color.WHITE);
        start.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(190, 35, 55)));
        start.setPadding(36, 18, 36, 18);
        start.setOnClickListener(v -> startHubAndGame());

        TextView note = new TextView(this);
        note.setText("İlk kullanımda diğer uygulamaların üzerinde gösterme iznini aç.\nFC balonuna dokununca menü açılır; sürükleyerek yerini değiştirebilirsin.");
        note.setTextColor(Color.rgb(125, 137, 155));
        note.setTextSize(12);
        note.setGravity(Gravity.CENTER);
        note.setPadding(12, 34, 12, 0);

        page.addView(title);
        page.addView(subtitle);
        page.addView(start);
        page.addView(note);
        setContentView(page);
    }

    private void startHubAndGame() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent permission = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivityForResult(permission, OVERLAY_REQUEST);
            Toast.makeText(this, "İzni açıp uygulamaya geri dön, sonra BAŞLAT'a tekrar bas.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            startService(new Intent(this, OverlayService.class));
        } catch (Exception e) {
            Toast.makeText(this, "Yüzen menü başlatılamadı: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        Intent game = getPackageManager().getLaunchIntentForPackage("com.ea.gp.fifamobile");
        if (game == null) {
            Toast.makeText(this, "FC Mobile bulunamadı. Oyun yüklü mü kontrol et.", Toast.LENGTH_LONG).show();
            return;
        }
        game.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(game);
    }
}
