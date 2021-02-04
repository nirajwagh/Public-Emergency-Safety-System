//Activity for displaying the complaint cancelled status.

package in.mcoeproject.copsos;

//Importing all required libraries.
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

public class ComplaintCancelled extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_cancelled);

        //Performing custom pattern of vibration on cop device when the complaint is cancelled by the citizen.
        //VibrationHelper java class used for vibration.
        VibrationHelper.vibrate(this, 1);

        //Playing media file alert_msg from the raw folder using MediaPlayer class.
        MediaPlayer mediaPlayer=MediaPlayer.create(this, R.raw.alert_mgs);
        mediaPlayer.start();
    }

    //Method to be called when OK button pressed.
    public void okPressed(View view) {

        //Stopping the ongoing vibration.
        VibrationHelper.stopVibrate(this);

        //Clearing all previous notifications using NotificationHelper class.
        NotificationHelper.clearAllNotifications(ComplaintCancelled.this);

        //Displaying new notification as "Complaint Cancelled".
        NotificationHelper.displayNotification(ComplaintCancelled.this, "Complaint Cancelled", "Sorry, your last complaint was cancelled by the citizen", 05);

        //Redirecting to the Status activity using Intent.
        Intent intent = new Intent(ComplaintCancelled.this, Status.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("requestCanceled", true);
        finish();
        startActivity(intent);

    }
}
