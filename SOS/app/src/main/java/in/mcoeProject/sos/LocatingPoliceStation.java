//Activity for locating the nearest police station to the citizen.

package in.mcoeProject.sos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.DecimalFormat;
import java.util.Objects;

public class LocatingPoliceStation extends AppCompatActivity {

    private Double currLocationLat, currLocationLng;
    private DatabaseReference copAssignedRef;
    private int radius=1;
    private Boolean policeStationFound=false;
    private String policeStationName="", complaint_id, copUid, uid, typeOfRequest;
    private ValueEventListener copAssignedListener;
    private SharedPreferences.Editor editor;
    private TextView txt_status, txt_reporting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locating_police_station);

        txt_status=findViewById(R.id.txt_status);
        txt_reporting=findViewById(R.id.txt_reporting);

        //Obtaining the user id of the user from Firebase.
        uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        //Accessing the complaint id passed from previous activity.
        complaint_id=getIntent().getStringExtra("complaint_id");
        typeOfRequest=getIntent().getStringExtra("typeOfRequest");

        //Boolean to know if the app is already searching nearest police station.
        Boolean isAlreadySearching = getIntent().getBooleanExtra("isAlreadySearching", false);

        //Accessing the shared preference file
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("storedValuesFile", 0);
        editor= sharedPreferences.edit();

        //Checking if the searching process is already triggered.
        if(!isAlreadySearching){

            txt_status.setText("Reporting The Nearest Police Station");
            editor.remove("assignedCopUid");
            editor.putBoolean("isReporting", true);
            editor.apply();
            DecimalFormat df = new DecimalFormat("####0.000000");

            //Obtaining the citizen's last location passed from the pervious activity.
            currLocationLat=Double.parseDouble(df.format(getIntent().getDoubleExtra("currLocationLat",0)));
            currLocationLng=Double.parseDouble(df.format(getIntent().getDoubleExtra("currLocationLng",0)));

            //Calling the method to search for the nearest police station.
            getNearestPoliceStation();
        }else{
            //If the search is already in progress.
            String reportedPoliceStation= sharedPreferences.getString("reportedPoliceStation", null);
            txt_reporting.setText(reportedPoliceStation);
            txt_status.setText("Assigning A Cop For You");
            addCopAssignedListener();
        }
    }


    //Method for searching the nearest police station using Firebase database.
    private void getNearestPoliceStation() {

        //Database reference for accessing the data from Firebase database.
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("police_stations");

        //Initializing a GeoFire variable for searching the police station.
        GeoFire geoFire = new GeoFire(databaseReference);

        //GeoQuery variable for executing the search query.
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currLocationLat, currLocationLng), radius);
        geoQuery.removeAllListeners();

        //Geoquery even listener for searching the police station.
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!policeStationFound) {
                    policeStationFound = true;
                    policeStationName = key;

                    //Adding the assigned police station value to shared preference.
                    editor.putString("reportedPoliceStation", key);
                    editor.apply();
                    txt_reporting.setText(key);
                    txt_status.setText("Assigning A Cop For You");
                    policeStationAssigned();
                    addCopAssignedListener();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                //if Police station not found.
                if (!policeStationFound) {

                    //incrementing the radius for police station search for next iteration.
                    radius++;
                    Toast.makeText(LocatingPoliceStation.this, "radius: " + radius, Toast.LENGTH_LONG).show();

                    //Recursive call to the current function.
                    getNearestPoliceStation();
                }
            }
            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    //Method for adding the listener for a cop assigned.
    private void addCopAssignedListener() {

        //Database reference for checking if cop is assigned.
        copAssignedRef=FirebaseDatabase.getInstance().getReference("actors").child("citizens").child(uid).child("assigned_cop_id");

        copAssignedListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    //Removing the ongoing reported or forwarded complaint from Firebase db.
                    removeReportedOrForwardedComplaint();

                    //Removing all listeners
                    removeListeners();

                    copUid =dataSnapshot.getValue().toString();

                    //Setting the isReporting variable of shared preference as false.
                    editor.putBoolean("isReporting", false);
                    editor.remove("reportedPoliceStation");
                    editor.apply();

                    //Playing the sound for Cop assigned.
                    MediaPlayer mediaPlayer=MediaPlayer.create(LocatingPoliceStation.this, R.raw.assignedd);
                    mediaPlayer.start();

                    //Displaying notification for a cop assigned.
                    NotificationHelper.clearAllNotifications(LocatingPoliceStation.this);
                    NotificationHelper.displayNotification(getApplicationContext(), policeStationName, "Cop assigned for you. Tap to get Details.", 05);

                    //Intent for redirecting to the mapActivity.
                    Intent intent=new Intent(LocatingPoliceStation.this, mapActivity.class);
                    intent.putExtra("assignedCopID", copUid);
                    intent.putExtra("complaint_id", complaint_id);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    finish();
                    startActivity(intent);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        copAssignedRef.addValueEventListener(copAssignedListener);
    }

    //Method for removing the ongoing reported or forwarded complaint from Firebase db.
    private void removeReportedOrForwardedComplaint() {

        if(typeOfRequest.equals("reported")){
            //Removing the current values from the Firebase db for ongoing_reported_complaint.
            DatabaseReference removeOngoingReportedComplaintRef=FirebaseDatabase.getInstance().getReference("ongoing_reported_complaints");
            removeOngoingReportedComplaintRef.child(complaint_id).removeValue();
        }else{
            //Removing the current values from the Firebase db for ongoing_forwarded_complaints.
            DatabaseReference removeOngoingForwardedComplaintRef=FirebaseDatabase.getInstance().getReference("ongoing_forwarded_complaints");
            removeOngoingForwardedComplaintRef.child(complaint_id).removeValue();
        }
    }

    //Method for removing all listeners.
    private void removeListeners() {
        copAssignedRef.removeEventListener(copAssignedListener);
    }

    //Assigning the current complaint id to the police station in Firebase DB.
    private void policeStationAssigned() {
        DatabaseReference addToPoliceStation =FirebaseDatabase.getInstance().getReference("police_stations").child(policeStationName).child("assigned_complaints").child(complaint_id);
        addToPoliceStation.child("complaint_id").setValue(complaint_id);
        NotificationHelper.displayNotification(getApplicationContext(), policeStationName, "Please wait, assigning a Cop for you.", 04);

    }

    //Method for cancelling the current request.
    public void cancelButtonPressed(View v){

        //Creating an alert dialog for confirming the user action.
       new AlertDialog.Builder(this).setTitle("Cancel Request?")
               .setMessage("Are you sure want to cancel the request?")
               .setCancelable(true)
               .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {

                       //Removing the ongoing reported or forwarded complaint from Firebase db.
                       removeReportedOrForwardedComplaint();

                       //Removing all complaint data from Firebase db if complaint is to be cancelled.
                       DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("ongoing_complaints");
                       databaseReference.child(complaint_id).removeValue();


                       databaseReference=FirebaseDatabase.getInstance().getReference("actors").child("citizens").child(uid);
                       databaseReference.child("assigned_cod_id").removeValue();
                       databaseReference.child("complaint_id").removeValue();


                       if(!policeStationName.isEmpty()){
                           databaseReference=FirebaseDatabase.getInstance().getReference("police_stations").child(policeStationName).child("assigned_complaints");
                           databaseReference.child(complaint_id).removeValue();
                       }

                       //Displaying notification for cancelled complaint.
                       NotificationHelper.clearAllNotifications(LocatingPoliceStation.this);
                       NotificationHelper.displayNotification(getApplicationContext(), "Complaint Cancelled", "You cancelled your last complaint.", 05);

                       //Modifying the shared preference file.
                       editor.putBoolean("isReporting", false);
                       editor.remove("complaint_id");
                       try {
                           editor.remove("assignedPoliceStation");
                       } catch (Exception e) {
                           e.printStackTrace();
                       }
                       editor.apply();

                       //Intent for redirecting to the Request activity.
                       Intent intent=new Intent(LocatingPoliceStation.this, Request.class);
                       intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                       finish();
                       startActivity(intent);

                   }
               })
               .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {

                       //Dismissing the dialog alert is user dont want to cancel request
                       dialogInterface.dismiss();
                   }
               })
               .show();
    }
}
