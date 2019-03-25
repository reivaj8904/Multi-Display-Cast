package es.munix.multidisplaycast.helpers;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import es.munix.multidisplaycast.CastControlsActivity;
import es.munix.multidisplaycast.R;
import es.munix.multidisplaycast.services.CastReceiver;
import es.munix.multidisplaycast.services.ControlsService;

/**
 * Created by munix on 3/11/16.
 */

public class NotificationsHelper {
    public static final String CHANNEL = "cast_notification";

    public void cancelNotification(Context context) {
        if (context != null) {
            ContextCompat.startForegroundService(context, new Intent(context, ControlsService.class)
                    .setAction("cancel"));
        }
    }

    public byte[] getBitmap(Context context, @NonNull String link) {
        try {
            Bitmap largeIcon = ((BitmapDrawable) Glide.
                    with(context).
                    load(link).
                    submit(100, 100).
                    get()).getBitmap();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            largeIcon.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(new NotificationChannel(CHANNEL, "Controles de CAST", NotificationManager.IMPORTANCE_MIN));
    }

    public void updateButton(Context context, Boolean isPaused) {
        ContextCompat.startForegroundService(context, new Intent(context, ControlsService.class)
                .setAction("togglePause")
                .putExtra("isPaused", isPaused));
    }

    public void showNotification(final Context context, final String title, final String subtitle, final String icon) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannel(context);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(context, ControlsService.class)
                        .putExtra("title",title)
                        .putExtra("subtitle",subtitle)
                        .putExtra("image", getBitmap(context, icon));
                ContextCompat.startForegroundService(context, intent);
            }
        });
    }
}
