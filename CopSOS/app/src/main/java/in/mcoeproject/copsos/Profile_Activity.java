//Activity for displaying user profile details.

package in.mcoeproject.copsos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Profile_Activity extends AppCompatActivity {

    //Declaring all required view and variables.
    private TextView txt_profile_name, txt_profile_email, txt_profile_phone,txt_id_details, txt_uploading;
    private  String userId, profile_image_url;
    private DatabaseReference databaseReference;
    private NavigationView navigationView;
    private ImageView img_back_profile, profile_image;
    //Uri object for picking image from gallery.
    private Uri resultUri;
    private  ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_);

        //Obtaining all required views from the layout file.
        txt_profile_name=findViewById(R.id.txt_profile_name);
        txt_profile_email=findViewById(R.id.txt_profile_email);
        txt_profile_phone=findViewById(R.id.txt_profile_phone);
        txt_id_details=findViewById(R.id.txt_id_details);
        navigationView=findViewById(R.id.nav_bar_profile);
        img_back_profile=findViewById(R.id.img_back_profile);
        profile_image=findViewById(R.id.profile_image);
        progressBar=findViewById(R.id.progressBar);
        txt_uploading=findViewById(R.id.txt_uploading);

        //Setting the visibility of progressbar and txt_uploading
        progressBar.setVisibility(View.INVISIBLE);
        txt_uploading.setVisibility(View.INVISIBLE);

        //Setting profile details from Firebase db.
        setProfileDetails();

        //Setting name in side navigation header.
        setNavHeaderName();

        //Setting listeners to side navigation menu.
        setNavListeners();

    }

    //Method for displaying user name in slider navigation header.
    public void setNavHeaderName(){

        //obtaining user name from Firebase db.
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();

            //Database reference for obtaining user name.
            databaseReference=FirebaseDatabase.getInstance().getReference("actors")
                    .child("users").child(uid).child("name");

            //Using ListenerForSingleValueEvent for getting username.
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name= (String) dataSnapshot.getValue();
                    View headerView= navigationView.getHeaderView(0);

                    //Setting the username to slider header.
                    TextView header_name=headerView.findViewById(R.id.txt_header_name);
                    header_name.setText(name);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    //Method to set listeners to slider menu items.
    public void setNavListeners(){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){

                    //Listener for logout button press.
                    case R.id.menu_logout:{
                        FirebaseAuth.getInstance().signOut();
                        Intent intent=new Intent(Profile_Activity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        break;
                    }
                    //Listener for Profile button press.
                    case R.id.menu_profile:{
                        startActivity(new Intent(Profile_Activity.this, Profile_Activity.class));
                        break;
                    }
                }
                return false;
            }
        });
    }

    //Method for back button press.
    public void backButtonPressed(View v){
        this.finish();
    }

    //Method to open device image gallery.
    public void profileImageClicked(View view) {

        //Intent to open device image gallery.
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);

    }

    //Method to handle the chosen image.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //If image choosing is success.
        if(requestCode==1 && resultCode==RESULT_OK){

            //Getting chosen image address on device.
           final Uri imageUri=data.getData();
            resultUri=imageUri;

            //Setting the chosen image to profile image view.
            profile_image.setImageURI(imageUri);

            //Uploading the chosen image to cloud.
            saveProfileImageToCloud();
        }else{
            //Error toast message for unsuccessful image choosing.
            Toast.makeText(this, "No Image Choosen", Toast.LENGTH_SHORT).show();
        }
    }

    //Method to upload image to firebase cloud.
    private void saveProfileImageToCloud() {

        //Making the views visible.
        progressBar.setVisibility(View.VISIBLE);
        txt_uploading.setVisibility(View.VISIBLE);

        //Converting the chosen image to Bitmap.
        Bitmap bitmap=null;
        try {
            bitmap= MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Converting the image to a byte array for uploading to Firebase.
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte [] data=baos.toByteArray();

        //Creating a firebase storage reference for uploading the image file.
        final StorageReference filePath=FirebaseStorage.getInstance().getReference().child("profile_images").child(userId);

        //Uploading the image to firebase storage service.
        UploadTask uploadTask=filePath.putBytes(data);

        //On successful image upload.
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //Storing the uploaded image Firebase storage url to Firebase Realtime database.
                DatabaseReference userReference=FirebaseDatabase.getInstance().getReference().child("actors").child("cops").child(userId);
                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful());
                Uri downloadUrl = urlTask.getResult();
                Map newImage=new HashMap();
                newImage.put("profile_image_url", downloadUrl.toString());
                userReference.updateChildren(newImage);

                //Making the views invisible.
                progressBar.setVisibility(View.INVISIBLE);
                txt_uploading.setVisibility(View.INVISIBLE);

                //Displaying message for success upload.
                Toast.makeText(Profile_Activity.this, "Image Upload Successful", Toast.LENGTH_SHORT).show();

            }
        });

        //Showing upload failed using toast message.
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Profile_Activity.this, "Image Upload Failed "+e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Method for setting the user profile details.
    private void setProfileDetails() {
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            userId= FirebaseAuth.getInstance().getCurrentUser().getUid();

            //Creating a database reference for accessing the firebase db.
            databaseReference= FirebaseDatabase.getInstance().getReference("actors")
                    .child("cops")
                    .child(userId);

            //Getting the user details using ListenerForSingleValueEvent.
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    //Setting the user details to textviews.
                    txt_profile_name.setText(dataSnapshot.child("name").getValue().toString());
                    txt_profile_email.setText(dataSnapshot.child("email").getValue().toString());
                    txt_profile_phone.setText(dataSnapshot.child("phone").getValue().toString());
                    txt_id_details.setText(dataSnapshot.child("id").getValue().toString());

                    //setting the user profile image if exists.
                    if(dataSnapshot.child("profile_image_url").exists())
                    {
                        //Obtaining user profile image url.
                        profile_image_url=dataSnapshot.child("profile_image_url").getValue().toString();
                        //Using Glide library for setting image from profile_image_url to profile_image.
                        Glide.with(getApplication()).load(profile_image_url).into(profile_image);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }
}
