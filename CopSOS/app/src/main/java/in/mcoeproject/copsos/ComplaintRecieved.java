//Activity for notifying the cop about new complaint received.

package in.mcoeproject.copsos;

//Importing all required libraries
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

public class ComplaintRecieved extends AppCompatActivity {

    //Declaring required variables.
    private String assignedCitizenID, complaint_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_recieved);

        //Adding received complaint details to shared preferences file.
        addDetailsToSharedPreferences();

        //Playing new complaint received sound and vibration.
        playSoundAndVibration();

    }

    //Method to play new complaint received alert sound and vibration.
    private void playSoundAndVibration() {

        //Playing the sound "assigned" from raw folder.
        MediaPlayer mediaPlayer=MediaPlayer.create(this, R.raw.assigned);
        mediaPlayer.start();

        //Playing custom vibration pattern using the VibrationHelper class.
        VibrationHelper.vibrate(this, 1);
    }

    //Method for adding the received complaint details to shared preferences file.
    private void addDetailsToSharedPreferences() {

        //Obtaining the assigned citizen id and complaint id passed by the previous activity.
        assignedCitizenID = getIntent().getStringExtra("assignedCitizenID");
        complaint_id = getIntent().getStringExtra("complaint_id");

        //String the details to valuesFile shared preferences file.
        SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("valuesFile", 0);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("assignedCitizenUid", assignedCitizenID);
        editor.putString("complaint_id", complaint_id);
        editor.apply();
    }

    //Method for redirecting to the MapsActivity for displaying live locations.
    public void getDetailsButtonPressed(View v){

        //Stopping the vibration.
        VibrationHelper.stopVibrate(this);

        //Initializing intent for redirecting to MapsActivity.
        Intent intent=new Intent(ComplaintRecieved.this, MapsActivity.class);

        //Passing complaint details to the MapsActivity and redirecting.
        intent.putExtra("assignedCitizenID", assignedCitizenID);
        intent.putExtra("complaint_id", complaint_id);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
