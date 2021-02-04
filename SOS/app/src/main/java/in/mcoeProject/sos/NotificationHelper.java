//Java class for displaying notifications.

package in.mcoeProject.sos;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class NotificationHelper {

    private static NotificationCompat.Builder builder;
    private static NotificationManagerCompat manager;

    public static void displayNotification(Context context, String title, String message, int id) {

            Intent intent = new Intent(context, splash.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    100,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT

            );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("copAssigned", "myNotification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        builder = new NotificationCompat.Builder(context, "copAssigned")
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_icon)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentText(message)
        .setColor(ContextCompat.getColor(context, R.color.colorAccent));

        manager = NotificationManagerCompat.from(context);
        manager.notify(id, builder.build());
    }

    public static void clearAllNotifications(Context context){
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
}
