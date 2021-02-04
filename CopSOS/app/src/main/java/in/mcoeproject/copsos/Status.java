//Activity for changing the on duty status of cop

package in.mcoeproject.copsos;

//Importing all required libraries.
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suke.widget.SwitchButton;
import com.wang.avi.AVLoadingIndicatorView;


public class Status extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    //Declaring all the required views and objects.
    private TextView txt_status, txt_waiting;
    private AVLoadingIndicatorView avi;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final int My_REQUEST_CODE=1;
    private com.suke.widget.SwitchButton switchButton;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private String uid;
    private GeoFire geoFireCopOnDuty;
    private Button btn_allow_gps,btn_enable_gps, btn_manual_complaint;
    private String assignedCitizenID="", complaint_id;
    private DatabaseReference assignedCitizenRef, copReference, getComplaintIdRef;
    private ValueEventListener getAssignedCitizenListener, getComplaintIdListener;
    private Boolean copOnDuty, isRestart;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private LottieAnimationView anim_gps, anim_waiting;
    private boolean isGpsPermissionGranted=false;
    private RelativeLayout layout_background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //Obtaining all required views.
        txt_status=findViewById(R.id.txt_status);
        txt_waiting=findViewById(R.id.txt_waiting);
        btn_allow_gps=findViewById(R.id.btn_allow_gps);
        btn_enable_gps=findViewById(R.id.btn_enable_gps);
        btn_manual_complaint=findViewById(R.id.btn_manual_complaint);
        anim_gps=findViewById(R.id.anim_gps);
        anim_waiting=findViewById(R.id.anim_waiting);
        switchButton= findViewById(R.id.switch_button);
        txt_status.setText("OFF DUTY");
        drawerLayout=findViewById(R.id.drawer);
        navigationView=findViewById(R.id.nav_bar);
        layout_background=findViewById(R.id.layout_background);

        //Setting the visibility of all animation and some required views as invisible.
        btn_allow_gps.setVisibility(View.INVISIBLE);
        btn_enable_gps.setVisibility(View.INVISIBLE);
        anim_gps.setVisibility(View.INVISIBLE);
        anim_waiting.setVisibility(View.INVISIBLE);
        txt_waiting.setVisibility(View.INVISIBLE);
        btn_manual_complaint.setVisibility(View.INVISIBLE);
        switchButton.setVisibility(View.INVISIBLE);
        txt_status.setVisibility(View.INVISIBLE);

        //Obtaining the current user UID from Firebase.
        uid= FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Using SharedPreferences for working with the valuesFile file stored in cop device.
        sharedPreferences=getApplicationContext().getSharedPreferences("valuesFile", 0);
        editor=sharedPreferences.edit();

        //Setting isRestart false initially.
        isRestart=false;

        //setting the switch as unchecked.
        switchButton.setChecked(false);
        switchButton.setShadowEffect(true);
        switchButton.setEnableEffect(true);

        //Setting the checked listener to switch.
        setCheckedListenerToSwitch();

        //Adding listener for getting the assigned citizen id and complaint id when a complaint is assigned.
        getAssignedCitizen();

        //Setting the slider navigation bar header name as user name.
        setNavHeaderName();

        //Setting the listener for slider menu items.
        setNavListeners();

    }

    //Method for setting the checked listener to the switch.
    private void setCheckedListenerToSwitch() {

        //Adding on Checked changed listener to the switch for recording cop duty status.
        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {

                //If the switch is checked, then check for gps status.
                if(isChecked){

                    //If gps is turned on, then get the cop on duty using copOnDuty method.
                    if(checkForGps()){
                        copOnDuty();
                    }else{

                        //If the switch is turned off, then create an alert for enabling the gps.
                        buildAlertForNoGps();
                    }
                }
                else{

                    //If the switch is turned off, then stop the location update service, an add the status to Shared preferences file.
                    try {
                        geoFireCopOnDuty.removeLocation(uid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,Status.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //Setting the value of isOnduty to false.
                    editor.putBoolean("isOnduty", false);
                    editor.apply();

                    //Changing the activity background color to R.color.colorBackgroundOff.
                    layout_background.setBackgroundColor(getResources().getColor(R.color.colorBackgroundOff));

                    //Clear all the notifications
                    NotificationHelper.clearAllNotifications(Status.this);

                    //Make the status os OFF DUTY on screen and change the visibility of appropriate views.
                    txt_status.setText("OFF DUTY");
                    txt_waiting.setVisibility(View.INVISIBLE);
                    btn_manual_complaint.setVisibility(View.VISIBLE);
                    anim_waiting.setVisibility(View.INVISIBLE);

                }
            }
        });

    }

    //Method for getting the cop on duty.
    public void copOnDuty(){

        //Build google api client for cop live location updates.
        buildGoogleApiClient();

        //Change the activity background color to R.color.colorBackgroundOn.
        layout_background.setBackgroundColor(getResources().getColor(R.color.colorBackgroundOn));

        //Show toast message for cop on duty.
        Toast.makeText(this, "Called duty", Toast.LENGTH_SHORT).show();

        //Show notification and change the visibility of appropriate views.
        NotificationHelper.displayNotification(this, "COP SOS", "YOU ARE ON DUTY !!!", 01);
        assignedCitizenID="";
        txt_status.setText("ON DUTY");
        txt_waiting.setVisibility(View.VISIBLE);
        btn_manual_complaint.setVisibility(View.INVISIBLE);
        anim_waiting.setVisibility(View.VISIBLE);

    }

    //Method for adding the listener for getting the assigned complaint details
    public void getAssignedCitizen(){
        copReference =FirebaseDatabase.getInstance().getReference().child("actors").child("cops").child(uid);
        assignedCitizenRef=copReference.child("citizenID");
        getAssignedCitizenListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //If the citizen is added, then get the citizen id and complaint id.
                if(dataSnapshot.exists()){
                    assignedCitizenID=dataSnapshot.getValue().toString();
                    getComplaintId();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        assignedCitizenRef.addValueEventListener(getAssignedCitizenListener);
    }

    //Method for obtaining the complaint id from the cop object in Firebase db. The complaint id is added by the SOS app when the cop is assigned.
    private void getComplaintId() {

        //Database reference for getting the complaint id.
        getComplaintIdRef=copReference.child("complaint_id");

        //Using value event listener for getting the complaint id.
        getComplaintIdListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //If the complaint id is added.
                if(dataSnapshot.exists()){

                    //get the value of complaint id.
                    complaint_id=dataSnapshot.getValue().toString();

                    //Turn off the switch and remove the listeners.
                    switchButton.setChecked(false);
                    removeListeners();

                    //Start the ComplaintRecieved activity using the complaint_id.
                    Intent intent=new Intent(Status.this, ComplaintRecieved.class);
                    //Passing the assigned citizen id and complaint id to the ComplaintRecieved activity.
                    intent.putExtra("assignedCitizenID", assignedCitizenID);
                    intent.putExtra("complaint_id", complaint_id);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        //Adding the listener to the getComplaintIdRef database reference.
        getComplaintIdRef.addValueEventListener(getComplaintIdListener);
    }

    //Method for removing the added listeners for stopping the listening.
    private void removeListeners() {
        assignedCitizenRef.removeEventListener(getAssignedCitizenListener);
        getComplaintIdRef.removeEventListener(getComplaintIdListener);
    }

    //Method for checking if the gps service is enabled or not.
    public boolean checkForGps(){

        //Obtaining the system LOCATION_SERVICE.
        LocationManager manager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Checking if gps is enabled or not.
        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

            //If gps is not enabled, then show the alert dialog for enabling the gps.
            buildAlertForNoGps();
            btn_enable_gps.setVisibility(View.VISIBLE);
            return false;
        }else{

            //If gps is enabled, then make the enable gps button invisible.
            btn_enable_gps.setVisibility(View.INVISIBLE);
            return true;
        }
    }

    //Method called when the allow gps button is clicked. Opens the app permissions activity for giving the permissions manually.
    public void allowGpsButtonClicked(View v){
        Intent intent=new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);

        //Getting the current app package name for opening the settings activity.
        Uri uri=Uri.fromParts("package", getApplicationContext().getPackageName(), null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    //Method called when the enable gps button presses, opens the app setting activity to enable gps manually.
    public void enableGpsButtonPressed(View v){
       startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    //Method for building an alert dialog for asking the user to enable gps.
    public void buildAlertForNoGps(){

        //Building the alert dialog.
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //If user presses Yes, then open the app setting activity for enabling the gps manually by the user.
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //If user press No, then switch off the switch button and dismiss the dialog.
                        switchButton.setChecked(false);
                        dialogInterface.cancel();
                    }
                }).show();
    }

    //Method for opening the slider menu bar when the user presses the menu hamburger icon.
    public void openMenu(View v){
        drawerLayout.openDrawer(Gravity.LEFT);
    }

    //Method for displaying user name in slider navigation header.
    public void setNavHeaderName(){

        //obtaining user name from Firebase db.
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){

            //Database reference for obtaining user name.
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("actors")
                    .child("cops").child(uid).child("name");

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
                        Intent intent=new Intent(Status.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        break;
                    }

                    //Listener for Profile button press.
                    case R.id.menu_profile:{
                        startActivity(new Intent(Status.this, Profile_Activity.class));
                        break;
                    }
                }
                return false;
            }
        });
    }

    //Method for checking whether locations permissions granted to the app or not.
    public void checkForPermission(){
        if (ContextCompat.checkSelfPermission(Status.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //If location permission granted then set the boolean to true and make the allow gps buttom invisible.
            isGpsPermissionGranted=true;
            btn_allow_gps.setVisibility(View.INVISIBLE);
        } else {

            //If location permission not granted, then prompt the user to grant the permissions.
            if (ActivityCompat.shouldShowRequestPermissionRationale(Status.this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                //Alert dialog for asking for permissions
                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("Location permission is needed for accessing current location")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                //If user press OK, the ask for permissions.
                                ActivityCompat.requestPermissions(Status.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, My_REQUEST_CODE);

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                //If user press cancel, then dismiss the dialog box and make the allow gps button visible.
                                dialogInterface.dismiss();
                                btn_allow_gps.setVisibility(View.VISIBLE);
                                isGpsPermissionGranted=false;
                            }
                        })
                        .create()
                        .show();
            }else{

                //Ask for permissions without showing the dialog.
                ActivityCompat.requestPermissions(Status.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, My_REQUEST_CODE);
            }
        }
    }

    //Method called automatically when the user gives or denies the permissions in the permission dialog box.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Switch statement for verifying the requestCode passed while requesting the permissions.
        switch (requestCode){
            case My_REQUEST_CODE:{

                //If the permission is granted then make the allow gps button invisible and set the boolean to true.
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){

                    isGpsPermissionGranted=true;
                    btn_allow_gps.setVisibility(View.INVISIBLE);

                }else{

                    //If permissions not granted then show the toast message to the user for providing the permissions.
                    Toast.makeText(this, "Please Provide The Location Permission", Toast.LENGTH_SHORT).show();

                    //setting the boolean false and making the allow gps button visible so that user can give the permissions manually.
                    isGpsPermissionGranted=false;
                    btn_allow_gps.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    //Method for initializing the google API client for accessing the GPS location services.
    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    //Initializing user location updates at periodic intervals.
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //Method automatically called when user location changes.
    @Override
    public void onLocationChanged(Location location) {

        //Getting database reference for cops_onduty for uploading the cop location to Firebase db.
        DatabaseReference copOnDutyRef= FirebaseDatabase.getInstance().getReference("cops_onduty");

        //Uploading the location to Firebase using GeoFire setLocation function.
        geoFireCopOnDuty= new GeoFire(copOnDutyRef);
        geoFireCopOnDuty.setLocation(uid, new GeoLocation(location.getLatitude(), location.getLongitude()));
    }

    //Method called when the manual complaint button pressed.
    public void manualButtonPressed(View view) {

        //Removing the getAssignedCitizenListener from the assignedCitizenRef.
        assignedCitizenRef.removeEventListener(getAssignedCitizenListener);

        //Redirecting to the ManualComplaint activity.
        Intent intent=new Intent(Status.this, ManualComplaint.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //Method called when the switch is touched.
    public void switchButtonTouched(View view) {

        //Checking if device gps is enabled or not.
        if(!checkForGps()){

            //If gps not enabled,then show alert dialog to allow gps.
            buildAlertForNoGps();
        }

    }

    //////////////////////////////////////////////////////////

    //Method called when the activity resumes.
    @Override
    protected void onResume() {
        super.onResume();

        //Checking for gps permissions.
        checkForPermission();

        //Checking if the gps is enabled.
        Boolean isGpsEnabled=checkForGps();

        //If gps is not enabled, then showing the animation for enabling the gps and making switch invisible.
        if(!isGpsEnabled || !isGpsPermissionGranted){

            anim_gps.setVisibility(View.VISIBLE);
            btn_manual_complaint.setVisibility(View.INVISIBLE);
            switchButton.setVisibility(View.INVISIBLE);
            txt_status.setVisibility(View.INVISIBLE);
            anim_waiting.setVisibility(View.INVISIBLE);
            txt_waiting.setVisibility(View.INVISIBLE);

        }else{

            //If gps is enabled, then making the animation invisible and making switch button visible.
            switchButton.setVisibility(View.VISIBLE);
            anim_gps.setVisibility(View.INVISIBLE);
            txt_status.setVisibility(View.VISIBLE);
            btn_manual_complaint.setVisibility(View.VISIBLE);

            //Obtaining the cop's duty status from shared preferences file.
            copOnDuty=sharedPreferences.getBoolean("isOnduty", false);

            //Checking if the activity is restarted. If the activity is not restarted then set the cop on duty.d
            if(!isRestart){

                //If cop is on duty, then setting the switch button as checked.
                if(copOnDuty){
                    switchButton.setChecked(true);

                    //Calling method to set the cop on duty services.
                    copOnDuty();
                }
            }else{
                btn_manual_complaint.setVisibility(View.INVISIBLE);

            }
        }
    }

    //Method called when the activity is paused, displaying notification about alerting the cop if on duty.
    @Override
    protected void onPause() {
        super.onPause();

        if(switchButton.isChecked()){
            NotificationHelper.displayNotification(Status.this, "Alert!!!", "YOU ARE STILL ONLINE.", 06);
        }
    }

    //Method called when the activity is destroyed after going out of scope.
    @Override
    protected void onDestroy() {
        super.onDestroy();

        //If the cop is on duty, then setting isOnduty value as true in shared preferences file, and stopping the location updates temporarily.
        if(switchButton.isChecked()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,Status.this);
            editor.putBoolean("isOnduty", true);
            editor.apply();

            //Notifying the copy about on duty status.
            Toast toast=Toast.makeText(this, "You Are Still On Duty. \nOpen App To Go Off Duty", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }else{

            //If the cop is off duty, then adding the isOnduty values as false in the shared preferences file.
            editor.putBoolean("isOnduty", false);
            editor.apply();
        }

    }

    //Method called when the activity restarts after pressing the device home button.
    @Override
    protected void onRestart() {
        super.onRestart();

        //Setting isRestart variable true for not starting the location update again, because the location is already updating.
        isRestart=true;
    }


}
