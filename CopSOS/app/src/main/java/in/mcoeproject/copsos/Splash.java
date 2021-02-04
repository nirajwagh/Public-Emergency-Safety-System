//Splash screen activity.

package in.mcoeproject.copsos;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Splash extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Getting the current Firebase user from FirebaseAuth service.
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();

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

                    //If user is not logged in, then redirect to MainActivity activity.
                    Intent intent=new Intent(Splash.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

            }
        }, 2500);  //Execution after 2.5 seconds.

    }

    //Method to check the user's current activity status.
    private void checkUserComplaintStatus() {

        //Shared preferences for accessing the data stored in storedValuesFile file.
        sharedPreferences=getApplicationContext().getSharedPreferences("valuesFile", 0);
        editor=sharedPreferences.edit();

        //Obtaining the complaint id from shared preference file.
        String complaint_id=sharedPreferences.getString("complaint_id", null);

        if(complaint_id==null){

            //If no complaint is active, then redirect to Status activity.
            Intent intent=new Intent(Splash.this, Status.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{

            //Checking if the user has reported or forwarded the complaint.
            Boolean isReported=sharedPreferences.getBoolean("isReported", false);
            Boolean isForwarded=sharedPreferences.getBoolean("isForwarded", false);
            if(isForwarded){
                Intent intent=new Intent(Splash.this, ComplaintForwarded.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("complaint_id", complaint_id);
                startActivity(intent);
                finish();

            }else if(isReported){
                Intent intent=new Intent(Splash.this, ComplaintReported.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("complaint_id", complaint_id);
                startActivity(intent);
                finish();
            }else{

                //If cop has a complaint ongoing and not reported nor forwarded, then redirect to MapsActivity activity.
                String assignedCitizenUid=sharedPreferences.getString("assignedCitizenUid", null);

                Intent intent=new Intent(Splash.this, MapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("assignedCitizenID", assignedCitizenUid);
                intent.putExtra("complaint_id", complaint_id);
                startActivity(intent);
                finish();
            }
        }
    }

}
