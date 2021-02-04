//Activity for notifying the cop about the current reported complaint.

package in.mcoeproject.copsos;

//Importing all required libraries.
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ComplaintReported extends AppCompatActivity {

    //Declaraing required layout view.
    private EditText txt_cop_reason;

    //Declaring SharedPreferences object for working with the Shared Preferences file.
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_reported);

        txt_cop_reason=findViewById(R.id.txt_cop_reason);

        //Add complaint reported status to Shared Preferences file.
        addComplaintReportedToSharedPref();

        //Play complaint reported sound and vibration.
        playSoundAndVibration();

    }

    //Adding complaint reported status to shared preference valuesFile.
    private void addComplaintReportedToSharedPref() {

        //Initializing the SharedPreferences object to work with valuesFile.
        sharedPreferences=this.getSharedPreferences("valuesFile", 0);
        editor=sharedPreferences.edit();

        //setting the isReported value to true.
        editor.putBoolean("isReported", true);
        editor.apply();

    }

    //Method for playing sound and vibration.
    private void playSoundAndVibration() {

        VibrationHelper.vibrate(this, 1);

        //Playing the "alert_msg" audio file from raw folder.
        MediaPlayer mediaPlayer=MediaPlayer.create(this, R.raw.alert_mgs);
        mediaPlayer.start();
    }

    //Method to be called when the submit button pressed.
    public void submitCopReason(View v){

        //Checking if the user entered reason is empty or not.
        String cop_reason= txt_cop_reason.getText().toString();
        if(!cop_reason.isEmpty()){

            //If the entered reason by user is not empty, then add the details to Firebase db.

            //Obtaining the complaint id passed by previous activity.
            String complaint_id=getIntent().getStringExtra("complaint_id");

            //Obtaining the user uid from the Firebase Auth service.
            String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();

            //Uploading the complaint reported details to Firebase db.
            DatabaseReference addCopReportedRef= FirebaseDatabase.getInstance().getReference("reported_cops").child(uid+" "+complaint_id);
            addCopReportedRef.child("cop_reason").setValue(cop_reason);
            addCopReportedRef.child("complaint_id").setValue(complaint_id);
            addCopReportedRef.child("cop_uid").setValue(uid);

            //Setting the values of isReported in SHared Preferences file to false.
            editor.putBoolean("isReported", false);
            editor.remove("complaint_id");
            editor.apply();

            //Stopping the vibration and redirecting to Status activity.
            VibrationHelper.stopVibrate(this);
            Intent intent=new Intent(ComplaintReported.this, Status.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(intent);
        }else{

            //If no reason is entered by cop, then  prompt to enter the reason.
            txt_cop_reason.setError("Please enter a reason.");
        }
    }
}
