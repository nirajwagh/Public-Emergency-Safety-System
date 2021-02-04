//Activity for assigning manual complaint by the cop.

package in.mcoeproject.copsos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;

public class ManualComplaint extends AppCompatActivity {

    //Declaring all required objects.
    private EditText txt_enter_complaint_id;
    private String complaint_id = "", assignedCitizenID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_complaint);

        //Obtaining the edit text view.
        txt_enter_complaint_id = findViewById(R.id.txt_enter_complaint_id);

    }

    //Method for validating the entered complaint number.
    public void assignComplaintManually(View v) {

        complaint_id = txt_enter_complaint_id.getText().toString();
        if (complaint_id.isEmpty()) {
            Toast.makeText(this, complaint_id, Toast.LENGTH_SHORT).show();
            txt_enter_complaint_id.setError("Required");
        } else if (complaint_id.length() != 6) {
            Toast.makeText(this, complaint_id, Toast.LENGTH_SHORT).show();
            txt_enter_complaint_id.setError("Enter 6 Digit ID");
        } else {

            //Getting the citizen id of the complaint.
            getCitizenIdForComplaint();

            //Finding complaint on Firebase db.
            findComplaintOnFirebaseDb();
        }
    }

    //Method for searching the complaint on Firebase db
    private void findComplaintOnFirebaseDb() {

        /* The complaint can be either "reported" or "forwarded". So we have to search for both.
         */

        //1. Searching for Reported complaint in Firebase db.

        //Database reference for searching the ongoing_reported_complaints object in Firebase db for REPORTED complaint.
        DatabaseReference ongoingReportedComplaintRef = FirebaseDatabase.getInstance().getReference("ongoing_reported_complaints").child(complaint_id);

        //Getting ongoing_reported_complaints object data using ListenerForSingleValueEvent.
        ongoingReportedComplaintRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    //Updating the complaint details in Firebase db by passing "reported" as type of complaint argument.
                    updateComplaintInFirebaseDb("reported");

                    //Adding the assigned complaint id and citizen id to Shared Preferences file.
                    addDetailsToSharedPreferences();

                } else {

                    //2. Searching for Forwarded complaint in Firebase db.

                    //If complaint not found under reported section, then searching for complaint in FORWARDED section.
                    DatabaseReference ongoingForwardedComplaintRef =FirebaseDatabase.getInstance().getReference("ongoing_forwarded_complaints")
                            .child(complaint_id);
                    ongoingForwardedComplaintRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){

                                //Updating the complaint details in Firebase db by passing "forwarded" as type of complaint argument.
                                updateComplaintInFirebaseDb("forwarded");

                                //Adding the assigned complaint id and citizen id to Shared Preferences file.
                                addDetailsToSharedPreferences();

                            }else{

                                //If complaint not found in Firebase db, then reporting the user by setting the error.
                                txt_enter_complaint_id.setError("No Complaint Found");
                                txt_enter_complaint_id.setText("");
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //Method for obtaining the citizen id of the assigned complaint.
    private void getCitizenIdForComplaint(){

        //Database reference for obtaining complaint citizen id using ongoing_complaints object of Firebase db and complaint_id.
        DatabaseReference getCitizenId = FirebaseDatabase.getInstance().getReference("ongoing_complaints").child(complaint_id);
        getCitizenId.addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 if(dataSnapshot.exists()){
                     //Obtaining the uid of citizen who created the complaint.
                     assignedCitizenID = dataSnapshot.child("Citizen_uid").getValue().toString();
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });

    }

    //Method for updating the complaint details in Firebase db.
    private void updateComplaintInFirebaseDb(final String typeOfComplaint) {

        //Obtaining the current cop user uid.
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Adding the new assigned cop id to the complaint in ongoing_complaints object in Firebase db.
        DatabaseReference ongoingComplaintRef = FirebaseDatabase.getInstance().getReference("ongoing_complaints").child(complaint_id);
        ongoingComplaintRef.child("cop_ID").setValue(uid);

        //Adding the complaint id and assigned citizen id under cop details in Firebase db.
        DatabaseReference copReference = FirebaseDatabase.getInstance().getReference().child("actors").child("cops").child(uid);
        HashMap map = new HashMap();
        map.put("citizenID", assignedCitizenID);
        map.put("complaint_id", complaint_id);
        copReference.updateChildren(map);

        //Adding the complaint id and assigned_cop_id under the assigned citizen object in Firebase db.
        DatabaseReference setCopIdRef = FirebaseDatabase.getInstance().getReference("actors").child("citizens").child(assignedCitizenID);
        setCopIdRef.child("assigned_cop_id").setValue(uid);
        setCopIdRef.child("complaint_id").setValue(complaint_id);


        //The complaint is now assigned to the cop, hence the complaint must be removed from the ongoing_reported_complaints or ongoing_forwarded_complaints object of Firebase db.

        //Removing the complaint from "ongoing_reported_complaints".
        if (typeOfComplaint.equals("reported")) {
            DatabaseReference removeOngoingReportedComplaintRef = FirebaseDatabase.getInstance().getReference("ongoing_reported_complaints");
            removeOngoingReportedComplaintRef.child(complaint_id).removeValue();
        } else {

            //Removing the complaint from "ongoing_forwarded_complaints".
            DatabaseReference removeOngoingForwardedComplaintRef = FirebaseDatabase.getInstance().getReference("ongoing_forwarded_complaints");
            removeOngoingForwardedComplaintRef.child(complaint_id).removeValue();
        }

        //Redirecting to MapsActivity for displaying the live locations.
        Intent intent = new Intent(ManualComplaint.this, MapsActivity.class);
        intent.putExtra("assignedCitizenID", assignedCitizenID);
        intent.putExtra("complaint_id", complaint_id);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
        startActivity(intent);

    }

    //Method to add the assigned citizen uid and complaint id in shared preferences file.
    private void addDetailsToSharedPreferences() {

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("valuesFile", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("assignedCitizenUid", assignedCitizenID);
        editor.putString("complaint_id", complaint_id);
        editor.apply();

    }
}
