//Splash screen activity.

package in.mcoeProject.sos;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class splash extends AppCompatActivity {

    private  String assignedCopUid, complaint_id;
    private FirebaseUser firebaseUser;
    private Intent intent;
    private SharedPreferences sharedPreferences;
    private boolean isReporting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Getting the current Firebase user from FirebaseAuth service.
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();

        //Shared preferences for accessing the data stored in storedValuesFile file.
        sharedPreferences = getApplicationContext().getSharedPreferences("storedValuesFile", 0);

        //Handler object for delaying the execution.
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //Checking if the user is logged in.
                if(firebaseUser!=null){

                    //If user logged in, then check the current complaint status, if any.
                    checkUserComplaintStatus();
                }else{
                    //If user is not logged in, then redirect to Login activity.
                    intent=new Intent(splash.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    finish();
                    startActivity(intent);
                }

            }
        }, 2500);  //Execution after 2.5 seconds.

    }

    //Method to check the user's current activity status.
    private void checkUserComplaintStatus() {

        //Obtaining the complaint id from shared preference file.
        complaint_id=sharedPreferences.getString("complaint_id", null);
        if(complaint_id==null){

            //If no complaint is active, then redirect to Request activity.
            intent=new Intent(splash.this, Request.class);
        }else {

            //Checking if the user has reported the complaint.
            isReporting = sharedPreferences.getBoolean("isReporting", false);
            if (isReporting) {

                //If user has reported the ongoing complaint, then redirect to LocatingPoliceStation activity.
                intent = new Intent(splash.this, LocatingPoliceStation.class);
                intent.putExtra("complaint_id", complaint_id);
                intent.putExtra("isAlreadySearching", true);
            }else{

                //If user has a complaint ongoing and not reported, then redirect to mapActivity activity.
                assignedCopUid=sharedPreferences.getString("assignedCopUid", null);
                intent=new Intent(splash.this, mapActivity.class);
                intent.putExtra("assignedCopID", assignedCopUid);
                intent.putExtra("complaint_id", complaint_id);
            }
        }

        //Starting the assigned activity.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
        startActivity(intent);
    }

}
