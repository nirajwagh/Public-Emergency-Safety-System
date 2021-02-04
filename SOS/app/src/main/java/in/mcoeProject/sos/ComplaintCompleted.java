//Activity for displaying the complaint completed message on user screen.

package in.mcoeProject.sos;

//Importing all required libraries
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ComplaintCompleted extends AppCompatActivity {

    //Declaring all variables required for the activity.
    private String complaintId, copUid, profile_image_url = null;
    private TextView txt_complaint_id;
    private TextView txt_cop_name;
    private TextView txt_cop_id;
    private TextView txt_cop_phone;
    private ImageView img_cop_profile;
    private String copName, copIdNo, copPhone, uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_completed);

        //Accessing all required views from the activity layout.
        txt_complaint_id = findViewById(R.id.txt_complaint_id);
        txt_cop_name = findViewById(R.id.name);
        txt_cop_id = findViewById(R.id.txt_cop_id);
        txt_cop_phone = findViewById(R.id.txt_cop_phone);
        TextView txt_title = findViewById(R.id.txt_title);
        img_cop_profile = findViewById(R.id.img_cop_profile);

        //Getting user id from the firebase realtime database.
        uid= FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Getting the complaint_id and cop_uid passed by the previous activity.
        complaintId = getIntent().getStringExtra("complaint_id");
        copUid = getIntent().getStringExtra("cop_uid");

        //Declaring and initializing MediaPlayer object for playing the media file "R.raw.completed" for complaint completed.
        MediaPlayer mediaPlayer = MediaPlayer.create(ComplaintCompleted.this, R.raw.completed);
        //playing the media.
        mediaPlayer.start();

        //calling the function for obtaining the complaint details.
        getComplaintDetails();

        //Calling the Method to save the completed complaint details to Google Firebase realtime DB.
        saveComplaintDetailsTOCloud();

    }

    //Method for saving the completed complaint data to Firebase DB.
    private void saveComplaintDetailsTOCloud() {

        //Getting the Firebase DB reference for accessing the DB.
        DatabaseReference saveComplainRef=FirebaseDatabase.getInstance().getReference("completed_complaints").child(complaintId);

        //Adding a Listener to the Firebase reference to obtain the db data for complaint details.
        saveComplainRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    //Accessing previously stored complaint data from Firebase db.
                    String complaintCreatedDate=dataSnapshot.child("complaint_create_date").getValue().toString();
                    String complaintCreatedTime =dataSnapshot.child("complaint_create_time").getValue().toString();
                    String complaintSolvedDate=dataSnapshot.child("complaint_solved_date").getValue().toString();
                    String complaintSolvedTime=dataSnapshot.child("complaint_solved_time").getValue().toString();

                    //Creating a new db reference for uploading all completed complaint details to firebase db.
                    DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("actors")
                            .child("citizens").child(uid).child("complaints").child(complaintId);

                    //Adding the completed complaint data to the firebase db using the created db reference.
                    databaseReference.child("cop_uid").setValue(copUid);
                    databaseReference.child("cop_name").setValue(copName);
                    databaseReference.child("cop_id").setValue(copIdNo);
                    databaseReference.child("cop_phone").setValue(copPhone);
                    databaseReference.child("complaint_create_date").setValue(complaintCreatedDate);
                    databaseReference.child("complaint_create_time").setValue(complaintCreatedTime);
                    databaseReference.child("complaint_solved_date").setValue(complaintSolvedDate);
                    databaseReference.child("complaint_solved_time").setValue(complaintSolvedTime);
                    databaseReference.child("complaint_id").setValue(complaintId);

                    //uploading the cop profile image url to Firebase db if it exists otherwise null value assigned.
                    if(profile_image_url!=null){
                        databaseReference.child("cop_image_url").setValue(profile_image_url);
                    }else{
                        databaseReference.child("cop_image_url").setValue(null);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //Method to obtain the complaint details from the Firebase db.
    private void getComplaintDetails() {

        //Getting the Firebase db reference for accessing the assigned cop details.
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("actors").child("cops").child(copUid);
        //Initializing a vale event listener for getting the assigned cop details.
        //Obtaining the assigned cop details.
        ValueEventListener complaintDetailsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Obtaining the assigned cop details.
                txt_complaint_id.setText(complaintId);
                txt_cop_name.setText(copName = dataSnapshot.child("name").getValue().toString());
                txt_cop_id.setText(copIdNo = dataSnapshot.child("id").getValue().toString());
                txt_cop_phone.setText(copPhone = dataSnapshot.child("phone").getValue().toString());

                //Accessing and assigning the Profile image if exists.
                if (dataSnapshot.child("profile_image_url").exists()) {
                    profile_image_url = dataSnapshot.child("profile_image_url").getValue().toString();

                    //Glide library used for accessing and assigning the image from the url.
                    Glide.with(getApplication()).load(profile_image_url).into(img_cop_profile);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseReference.addValueEventListener(complaintDetailsListener);
    }

    //Method to finish the activity on "Done" button pressed.
    public void doneButtonPressed(View v) {
        //Intent for directing to Request activity
        Intent intent = new Intent(ComplaintCompleted.this, Request.class);
        finish();
        startActivity(intent);
    }
}
