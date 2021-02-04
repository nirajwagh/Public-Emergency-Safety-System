//Activity for notifying the complaint forwarded status.

package in.mcoeproject.copsos;

//Importing all required libraries.
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ComplaintForwarded extends AppCompatActivity {

    //Declaring all required views and class objects.
    private EditText txt_forward_reason;
    private ImageView img_reason;
    private final int My_REQUEST_CODE=1;

    //Bitmap object for converting the captured image to bitmap type.
    private Bitmap bitmap;
    private String forwardComplaintID;
    private ProgressBar progressBar1;
    private UploadTask uploadTask;
    private Button btn_submit;
    private DatabaseReference complaintForwardRef;

    //SharedPreferences object for obtaining the data stored by the app on device.
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private TextView txt_uploading_img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_forwarded);

        //Obtaining all required views from layout file.
        txt_forward_reason=findViewById(R.id.txt_forward_reason);
        img_reason=findViewById(R.id.img_reason);
        progressBar1=findViewById(R.id.progressBar1);
        btn_submit=findViewById(R.id.btn_submit);
        txt_uploading_img=findViewById(R.id.txt_uploading_img);

        //Making the required views INVISIBLE when image is not being uploaded.
        progressBar1.setVisibility(View.INVISIBLE);
        btn_submit.setVisibility(View.INVISIBLE);
        txt_uploading_img.setVisibility(View.INVISIBLE);

        //Adding forwarded complaint details to Firebase db.
        addForwardedComplaintToDB();

        //Adding complaint forwarded status to Shared Preferences file.
        addForwardedComplaintToSharedPref();

    }

    //Method for adding Forwarded complaint details to Shared Preference file in user device.
    private void addForwardedComplaintToSharedPref() {

        //Initializing SharedPreferences for obtaining the values stored in valuesFile on user device.
        sharedPreferences=this.getSharedPreferences("valuesFile", MODE_PRIVATE);
        editor=sharedPreferences.edit();

        //Adding a boolean value to valuesFile for the current status of complaint.
        editor.putBoolean("isForwarded", true);
        editor.apply();
    }

    //Method for adding Forwarded complaint details to Firebase DB.
    private void addForwardedComplaintToDB() {

        //Getting the current user UID from the Firebase Auth service.
        String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Obtaining the complaint Id passed by previous activity.
        String complaint_id=getIntent().getStringExtra("complaint_id");

        //Creating a custom forward complaint id using user UID and complaint id.
        forwardComplaintID= uid + " " + complaint_id;

        //Adding the forwarded complaint to Firebase db ongoing_forwarded_complaints object.
        complaintForwardRef= FirebaseDatabase.getInstance().getReference("ongoing_forwarded_complaints")
                .child(complaint_id);
        complaintForwardRef.child("complaint_id").setValue(complaint_id);

        //Adding the forwarded complaint to Firebase db forwarded_complaints object.
        complaintForwardRef= FirebaseDatabase.getInstance().getReference("forwarded_complaints")
                .child(forwardComplaintID);
        complaintForwardRef.child("complaint_id").setValue(complaint_id);
        complaintForwardRef.child("cop_uid").setValue(uid);
    }

    //Method to be called when the image icon pressed.
    public void uploadImageIconPressed(View view) {

        //Checking if Device camera access permission granted or not.
        if (ContextCompat.checkSelfPermission(ComplaintForwarded.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            //If camera permission granted, then open device camera app for capturing image.
            Intent intent=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

            //Starting the camera activity for Result for further processing on capturing image.
            startActivityForResult(intent, 0);
        } else {

            //If the device camera access permission not granted then prompt the user to give the camera permissions.
            if (ActivityCompat.shouldShowRequestPermissionRationale(ComplaintForwarded.this, Manifest.permission.CAMERA)) {

                //Displaying alert dialog to user for giving camera access permissions.
                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("Camera permission is needed for taking photo.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                //Asking for permissions again, when user presses OK on alert dialog to give permissions.
                                ActivityCompat.requestPermissions(ComplaintForwarded.this, new String[]{Manifest.permission.CAMERA}, My_REQUEST_CODE);

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                //Dismissing the alert dialog when users presses "Cancel".
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();
            }else{

                //Requesting the permission without showing alert.
                ActivityCompat.requestPermissions(ComplaintForwarded.this, new String[]{Manifest.permission.CAMERA}, My_REQUEST_CODE);
            }
        }
    }

    //Method called automatically when user provides action on the permissions prompt.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Checking if the prompted permissions are given or not.
        switch (requestCode){

            case My_REQUEST_CODE:{

                //If permissions given, then open the camera app.
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 0);
                }else{

                    //If permissions not given, then show toast message to allow permission.
                    Toast.makeText(this, "Please Provide Camera permissions.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //Method called automatically when the camera activity is finished.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Obtaining the captured image from the camera app.
        bitmap= (Bitmap) data.getExtras().get("data");

        //Setting the captured image to the Image view on activity.
        img_reason.setImageBitmap(bitmap);

        //Making the progress bar and uploading text visible.
        progressBar1.setVisibility(View.VISIBLE);
        txt_uploading_img.setVisibility(View.VISIBLE);

        //Uploading the captured image to Firebase Storage service using the submit method.
        submit();

    }

    //Method for converting the captured bitmap image to byte array and then uploading it to Firebase Storage service.
    public void submit(){

        //converting the bitmap image to byte array after compression.
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] b = stream.toByteArray();

        //Getting the storage reference of the firebase Storage.
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("forwarded_complaints_images").child(forwardComplaintID);

        //Trying to upload the image.
        uploadTask=storageReference.putBytes(b);

        //Adding listener to the uploading task.
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //If uploading is successful

                Toast.makeText(ComplaintForwarded.this, "Image Upload Successful", Toast.LENGTH_SHORT).show();

                //Adding the uploaded image url to Firebase db forwarded_complaints object
                DatabaseReference forwardedCompRef=FirebaseDatabase.getInstance().getReference("forwarded_complaints").child(forwardComplaintID);
                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful());
                Uri downloadUrl = urlTask.getResult();
                Map newImage=new HashMap();
                newImage.put("report_image_url", downloadUrl.toString());
                forwardedCompRef.updateChildren(newImage);

                //Making the progressbar invisible and submit button visible.
                progressBar1.setVisibility(View.INVISIBLE);
                btn_submit.setVisibility(View.VISIBLE);

            }
        });
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                //If uploading image fails, then show the message to user.
                Toast.makeText(ComplaintForwarded.this, "Image Upload Failed "+e.toString(), Toast.LENGTH_SHORT).show();
                progressBar1.setVisibility(View.INVISIBLE);
            }
        });

    }

    //Method called when the submit button is pressed after uploading the image.
    public void SubmitButtonPressed(View view) {

        //Checking if the Forward reason is entered by the user or not.
        String forwardReason=txt_forward_reason.getText().toString();
        if(forwardReason.isEmpty()){

            //If the forward reason is not entered, then prompt the user to enter it.
            txt_forward_reason.setError("Please Enter Your Reason.");
            return;
        }

        //Adding the forward reason to Firebase db forward_reason child.
        complaintForwardRef.child("forward_reason").setValue(forwardReason);

        //Updating the forwarded complaint status in Shared Preferences file.
        editor.putBoolean("isForwarded", false);
        editor.remove("complaint_id");
        editor.apply();

        //Redirecting to Status activity.
        Intent intent=new Intent(ComplaintForwarded.this, Status.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
        startActivity(intent);

    }

}
