package com.example.fcmobilehub;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class OverlayService extends Service {
    private WindowManager wm;
    private View bubble;
    private View panel;
    private WindowManager.LayoutParams bubbleParams;
    private WindowManager.LayoutParams panelParams;

    @Override public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 23 &&
            !android.provider.Settings.canDrawOverlays(this)) {
            stopSelf();
            return;
        }
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        createBubble();
    }

    private int dp(float n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
    }

    private int overlayType() {
        return Build.VERSION.SDK_INT >= 26
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private void createBubble() {
        TextView b = text("FC", 15, Color.WHITE, true);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(Color.rgb(190, 35, 55));
        circle.setStroke(dp(2), Color.WHITE);
        b.setBackground(circle);
        b.setElevation(dp(8));

        bubbleParams = new WindowManager.LayoutParams(
            dp(56), dp(56), overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        bubbleParams.gravity = Gravity.TOP | Gravity.START;
        bubbleParams.x = dp(18);
        bubbleParams.y = dp(180);

        b.setOnTouchListener(new View.OnTouchListener() {
            int startX, startY;
            float downX, downY;
            boolean dragged;

            @Override public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = bubbleParams.x;
                        startY = bubbleParams.y;
                        downX = e.getRawX();
                        downY = e.getRawY();
                        dragged = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = e.getRawX() - downX;
                        float dy = e.getRawY() - downY;
                        if (Math.abs(dx) > dp(5) || Math.abs(dy) > dp(5)) dragged = true;
                        if (dragged) {
                            bubbleParams.x = startX + (int) dx;
                            bubbleParams.y = startY + (int) dy;
                            wm.updateViewLayout(bubble, bubbleParams);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!dragged) togglePanel();
                        return true;
                }
                return false;
            }
        });

        bubble = b;
        wm.addView(bubble, bubbleParams);
    }

    private void togglePanel() {
        if (panel != null) {
            closePanel();
        } else {
            openPanel();
        }
    }

    private void openPanel() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(12), dp(14), dp(12));
        box.setBackground(rounded(Color.rgb(20, 24, 34), 18));
        box.setElevation(dp(12));

        TextView heading = text("FC MOBILE HUB", 16, Color.WHITE, true);
        heading.setPadding(0, 0, 0, dp(10));
        box.addView(heading, new LinearLayout.LayoutParams(-1, dp(38)));

        TextView power = text("POWER SHOT  ·  ANTRENMAN", 14, Color.WHITE, true);
        power.setBackground(rounded(Color.rgb(190, 35, 55), 12));
        box.addView(power, new LinearLayout.LayoutParams(-1, dp(46)));
        power.setOnClickListener(v ->
            Toast.makeText(this, "Power Shot için oyun içi şut kontrollerini kullan. Bu düğme yalnızca yardımcı menüdür.", Toast.LENGTH_LONG).show());

        TextView launch = text("FC MOBILE'I AÇ", 14, Color.WHITE, true);
        launch.setBackground(rounded(Color.rgb(48, 57, 75), 12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(44));
        lp.topMargin = dp(8);
        box.addView(launch, lp);
        launch.setOnClickListener(v -> {
            Intent i = getPackageManager().getLaunchIntentForPackage("com.ea.gp.fifamobile");
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            } else {
                Toast.makeText(this, "FC Mobile bulunamadı.", Toast.LENGTH_SHORT).show();
            }
        });

        TextView close = text("KAPAT", 13, Color.rgb(205, 210, 220), true);
        box.addView(close, new LinearLayout.LayoutParams(-1, dp(36)));
        close.setOnClickListener(v -> closePanel());

        panelParams = new WindowManager.LayoutParams(
            dp(240), WindowManager.LayoutParams.WRAP_CONTENT, overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        panelParams.gravity = Gravity.TOP | Gravity.START;
        panelParams.x = Math.max(dp(8), bubbleParams.x - dp(90));
        panelParams.y = bubbleParams.y + dp(66);

        panel = box;
        wm.addView(panel, panelParams);
    }

    private void closePanel() {
        if (panel != null) {
            try { wm.removeView(panel); } catch (Exception ignored) {}
            panel = null;
        }
    }

    @Override public void onDestroy() {
        closePanel();
        if (wm != null && bubble != null) {
            try { wm.removeView(bubble); } catch (Exception ignored) {}
        }
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
