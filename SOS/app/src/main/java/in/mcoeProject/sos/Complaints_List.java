//Activity for displaying all the complaints of the user.

package in.mcoeProject.sos;

//Importing all required libraries.
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Complaints_List extends AppCompatActivity {

    //Declaring all required data members.
    private String uid, copName, copId, copPhone, complaintCreatedDate, complaintCreatedTime, complaintId, copProfileUrl;

    //Declaring a list for passing the complaints data to the Recycler view adapter
    private List<ComplaintsDataClass> complaintsData;

    //Declaring recycler view for displaying the complaints.
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaints_list);

        recyclerView=findViewById(R.id.recycler);

        //Setting Linear layout for the recyclerview.
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Initializing the list as ArrayList.
        complaintsData=new ArrayList<>();

        //Obtaining the user id of the user from the Firebase authentication service.
        uid= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        //Calling the method for accessing all complaint details of the user from the Firebase db.
        getComplaintDetails();
    }

    //Method for obtaining all complaints of the user form the Firebase db.
    private void getComplaintDetails() {

        //Database reference for obtaining the reference for the current user complaints from the Firebase db.
        final DatabaseReference complaintDetailsRef= FirebaseDatabase.getInstance().getReference("actors").child("citizens")
                .child(uid).child("complaints");

        //Value event listener for obtaining all complaints of user.
        complaintDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    //Accessing the complaint objects one by one from the datasnapshot using for loop.
                    for(DataSnapshot complaint : dataSnapshot.getChildren()){

                        copName= Objects.requireNonNull(complaint.child("cop_name").getValue()).toString();
                        copId=complaint.child("cop_id").getValue().toString();
                        copPhone=complaint.child("cop_phone").getValue().toString();
                        complaintCreatedDate=complaint.child("complaint_create_date").getValue().toString();
                        complaintCreatedTime=complaint.child("complaint_create_time").getValue().toString();
                        complaintId=complaint.child("complaint_id").getValue().toString();
                        try {
                            copProfileUrl=complaint.child("cop_image_url").getValue().toString();
                        } catch (Exception e) {
                            copProfileUrl=(null);

                            e.printStackTrace();
                        }

                        //Adding the complaints to the list in each iteration of the for loop.
                        complaintsData.add(new ComplaintsDataClass(copName, copId, copPhone, complaintCreatedDate, complaintCreatedTime, complaintId, copProfileUrl));

                    }

                    //Setting the adapter to the recyclerview.
                    recyclerView.setAdapter(new ComplaintsAdapter(complaintsData, Complaints_List.this));
                }else{

                    //Displaying a toast message is no complaint data is found.
                    Toast.makeText(Complaints_List.this, "No Data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Method to be called when the back button is pressed.
    public void backButtonPressed(View view) {

        //Intent for redirecting to the Request activity on back button pressed.
        Intent intent=new Intent(Complaints_List.this, Request.class);
        finish();
        startActivity(intent);
    }
}
