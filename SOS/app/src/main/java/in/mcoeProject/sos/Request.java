//Activity for requesting for SOS service.

package in.mcoeProject.sos;

//Importing all required libraries.
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

public class Request extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    //Declaring all the required views and objects.
    private Button btn_sos, btn_cancel, btn_gps, btn_permission;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    final int My_REQUEST_CODE = 1;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private String uid, copFoundID = "";
    private GeoFire geoFire;
    private Boolean locationSet, cancelRequest = false, copSearch;
    private int radius = 1, complaint_id;
    private boolean copFound;
    private GeoQuery geoQuery;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseReference2;
    private LottieAnimationView anim_allow_gps, anim_gps;
    private TextView txt_searching;
    private boolean isGpsPermissionGranted=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        //Obtaining all required views.
        btn_permission = findViewById(R.id.btn_permission);
        btn_sos = findViewById(R.id.btn_sos);
        btn_cancel = findViewById(R.id.btn_cancel);
        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav_bar);
        btn_gps = findViewById(R.id.btn_gps);
        anim_allow_gps = findViewById(R.id.anim_allow_gps);
        anim_gps = findViewById(R.id.anim_gps);
        txt_searching = findViewById(R.id.txt_searching);

        //Setting the visibility of all animation and some required views as invisible.
        btn_cancel.setVisibility(View.INVISIBLE);
        anim_allow_gps.setVisibility(View.INVISIBLE);
        btn_gps.setVisibility(View.INVISIBLE);
        btn_sos.setVisibility(View.INVISIBLE);
        btn_permission.setVisibility(View.INVISIBLE);
        anim_gps.setVisibility(View.INVISIBLE);
        txt_searching.setVisibility(View.INVISIBLE);

        //Boolean used for setting the complaint create location.
        locationSet = false;

        //Obtaining the current user UID from Firebase.
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Setting the slider navigation bar header name as user name.
        setNavHeaderName();

        //Setting the listener for slider menu items.
        setNavListeners();

    }

    //Method for opening the slider menu bar when the user presses the menu hamburger icon.
    public void openMenu(View v){
        drawerLayout.openDrawer(Gravity.LEFT);
    }

    //Method called when the enable gps button presses, opens the app setting activity to enable gps manually.
    public void enableGpsButtonPressed(View v){
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
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

    //Method for checking if the gps service is enabled or not.
    public boolean checkForGps(){

        //Obtaining the system LOCATION_SERVICE.
        LocationManager manager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Checking if gps is enabled or not.
        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

            //If gps is not enabled, then show the alert dialog for enabling the gps.
            buildAlertForNoGps();
            btn_gps.setVisibility(View.VISIBLE);
            return false;
        }else{

            //If gps is enabled, then make the enable gps button invisible.
            btn_gps.setVisibility(View.INVISIBLE);
            return true;
        }
    }

    //Method for building an alert dialog for asking the user to enable gps.
    public void buildAlertForNoGps() {

        //Building the alert dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //If user presses Yes, then open the app setting activity for enabling the gps manually by the user.
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //If user press No, then dismiss the dialog.
                        dialogInterface.cancel();
                    }
                }).show();
    }

    //Method for checking whether locations permissions granted to the app or not.
    public void checkForPermission() {

        if(ContextCompat.checkSelfPermission(Request.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            //If location permission granted then set the boolean to true and make the allow gps buttom invisible.
            isGpsPermissionGranted=true;
            btn_permission.setVisibility(View.INVISIBLE);
        } else {

            //If location permission not granted, then prompt the user to grant the permissions.
            if (ActivityCompat.shouldShowRequestPermissionRationale(Request.this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                //Alert dialog for asking for permissions
                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("Location permission is needed for accessing current location")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                //If user press OK, the ask for permissions.
                                ActivityCompat.requestPermissions(Request.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, My_REQUEST_CODE);

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                //If user press cancel, then dismiss the dialog box and make the allow gps button visible.
                                isGpsPermissionGranted=false;
                                btn_permission.setVisibility(View.VISIBLE);
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();
            } else {

                //Ask for permissions without showing the dialog.
                ActivityCompat.requestPermissions(Request.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, My_REQUEST_CODE);
            }
        }
    }

    //Method called automatically when the user gives or denies the permissions in the permission dialog box.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Switch statement for verifying the requestCode passed while requesting the permissions.
        switch (requestCode) {
            case My_REQUEST_CODE: {

                //If the permission is granted then make the allow gps button invisible and set the boolean to true.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    btn_permission.setVisibility(View.INVISIBLE);
                    isGpsPermissionGranted=true;
                } else {

                    //If permissions not granted then show the toast message to the user for providing the permissions.
                    Toast.makeText(this, "Please Provide The Location Permission", Toast.LENGTH_SHORT).show();

                    //setting the boolean false and making the allow gps button visible so that user can give the permissions manually.
                    isGpsPermissionGranted=false;
                    btn_permission.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    //Method for cancelling the current SOS request.
    private void cancelRequest() {

        //Removing the current stored location from Firebase db and stopping the live location updates.
        try {
            //Stopping all listeners from listening.
            geoQuery.removeAllListeners();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {

            //Removing citizen location from the Firebase db.
            geoFire.removeLocation("citizen_location");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Stopping the live location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, Request.this);

        //Removing the current complaint from the ongoing_complaints object of Firebase db.
        try {
            DatabaseReference removeCompDetailsRef = FirebaseDatabase.getInstance().getReference("ongoing_complaints").child(String.valueOf(complaint_id));
            removeCompDetailsRef.removeValue();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Toggling the visibilities of appropriate views and setting the searching radius to the initial value '1'.
        btn_cancel.setVisibility(View.INVISIBLE);
        anim_gps.setVisibility(View.INVISIBLE);
        btn_sos.setVisibility(View.VISIBLE);
        txt_searching.setVisibility(View.INVISIBLE);
        copSearch = false;
        radius = 1;

    }

    //Method for getting the nearest cop to the citizen, using the location of all on duty cops from Firebase db.
    public void getNearestCop() {

        //Database reference for all on_duty cops from Firebase db.
        databaseReference = FirebaseDatabase.getInstance().getReference("cops_onduty");

        //Using GeoFire.queryAtLocation method for searching for nearest cop by incrementing the radius every time.
        GeoFire geoFire = new GeoFire(databaseReference);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), radius);

        //Removing previous listeners.
        geoQuery.removeAllListeners();

        //Adding a new listener for geo query.
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                //If a cop is found, then add the complaint id and citizen id to cops object in firebase db and cop id and complaint id to citizen object.
                if (!copFound && copSearch) {
                    copSearch = false;
                    copFound = true;

                    //The found cop ID.
                    copFoundID = key;

                    //Adding the citizen id and complaint id to cop object in Firebase db.
                    DatabaseReference copReference = FirebaseDatabase.getInstance().getReference().child("actors").child("cops").child(copFoundID);
                    HashMap map = new HashMap();
                    map.put("citizenID", uid);
                    map.put("complaint_id", complaint_id);
                    copReference.updateChildren(map);

                    //Adding the cop id and complaint id to the assigned object in Firebase db.
                    DatabaseReference citizenReference = FirebaseDatabase.getInstance().getReference().child("actors").child("citizens").child(uid);
                    HashMap map1 = new HashMap();
                    map1.put("assigned_cop_id", copFoundID);
                    map1.put("complaint_id", complaint_id);
                    citizenReference.updateChildren(map1);

                    //Stopping further location updates.
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, Request.this);
                    geoQuery.removeAllListeners();

                    //Showing notification about the cop assigned.
                    NotificationHelper.displayNotification(getApplicationContext(), "Cop Assigned", "Click To Open The App", 01);

                    //Starting the mapActivity for live location of both, cop and citizen.
                    Intent intent = new Intent(Request.this, mapActivity.class);
                    intent.putExtra("assignedCopID", copFoundID);
                    intent.putExtra("complaint_id", String.valueOf(complaint_id));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    finish();
                    startActivity(intent);

                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            //Method called if the key is not found.
            @Override
            public void onGeoQueryReady() {

                //If the key/cop is not found, then increase the radius if search.
                if (!copFound && copSearch) {

                    //Incrementing the radius by 1 km.
                    radius++;
                    Toast.makeText(Request.this, "radius: " + radius, Toast.LENGTH_SHORT).show();

                    //If radius== 10, then notify the user about no cop found near and prompt with options
                    // like cancel complaint, report to nearest police station and search again.
                    if (radius == 10) {

                        //if no cop is found in 10km radius.
                        noCopFound();
                    } else {

                        //If cop not found but radius is less than 10, then search again with the incremented radius.
                        getNearestCop();
                    }
                }
            }
            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    //Method id no cop found within 10 km radius of citizen.
    private void noCopFound() {

        //Stop all listeners from listening.
        geoQuery.removeAllListeners();

        //Play alert sound alert_mgs from the raw folder for no cop found.
        MediaPlayer mediaPlayer=MediaPlayer.create(this, R.raw.alert_mgs);
        mediaPlayer.start();

        //Show alert dialog to user and show options.
        new AlertDialog.Builder(this)
                .setTitle("No Cop Found !")
                .setMessage("Search again or report nearest Police Station?")
                .setPositiveButton("Search Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //If user presses "Search Again", then set the radius to 1 and search again.
                        radius = 1;
                        getNearestCop();

                    }
                })
                .setNegativeButton("Report", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //If user presses report, then report to the nearest police station to the citizen.

                        //Get the current location coordinates of the citizen using the mLastLocation object.
                        Double currLocationLat = mLastLocation.getLatitude();
                        Double currLocationLng = mLastLocation.getLongitude();

                        //Adding the complaint to ongoing_reported_complaints object of firebase db.
                        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("ongoing_reported_complaints").child(String.valueOf(complaint_id));
                        databaseReference.child("complaint_id").setValue(String.valueOf(complaint_id));

                        //Stopping the live location update service.
                        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, Request.this);

                        //Starting the LocatingPoliceStation Activity.
                        Intent intent = new Intent(Request.this, LocatingPoliceStation.class);
                        intent.putExtra("currLocationLat", currLocationLat);
                        intent.putExtra("currLocationLng", currLocationLng);
                        intent.putExtra("complaint_id", String.valueOf(complaint_id));
                        intent.putExtra("typeOfRequest", "reported");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        finish();
                        startActivity(intent);
                    }
                })
                .setNeutralButton("Cancel Request", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //Cancel the request is user presses Cancel Request option.
                        cancelRequest();
                    }
                })
                .setCancelable(false)
                .show();
    }

    //Method for displaying user name in slider navigation header.
    public void setNavHeaderName() {

        //obtaining user name from Firebase db.
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            //Database reference for obtaining user name.
            databaseReference = FirebaseDatabase.getInstance().getReference("actors")
                    .child("citizens").child(uid).child("name");

            //Using ListenerForSingleValueEvent for getting username.
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = (String) dataSnapshot.getValue();
                    View headerView = navigationView.getHeaderView(0);

                    //Setting the username to slider header.
                    TextView header_name = headerView.findViewById(R.id.txt_header_name);
                    header_name.setText(name);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    //Method to set listeners to slider menu items.
    public void setNavListeners() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {

                    //Listener for logout button press.
                    case R.id.menu_logout: {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(Request.this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        break;
                    }
                    //Listener for Profile button press.
                    case R.id.menu_profile: {
                        startActivity(new Intent(Request.this, Profile_Activity.class));
                        break;
                    }

                    ////Listener for Complaints button press.
                    case R.id.menu_Complaints: {
                        startActivity(new Intent(Request.this, Complaints_List.class));
                        break;
                    }
                }
                return false;
            }
        });
    }

    //Method for confirming the sos request button press action.
    public void sosRequest(View v) {

        //Checking if the gps is enabled.
        if (!checkForGps()) {
            btn_gps.setVisibility(View.VISIBLE);
            buildAlertForNoGps();
        } else {

            //If gps is enabled, then show alert to the citizen to confirm the sos request action.
            locationSet = false;
            new AlertDialog.Builder(this, R.style.myDialogTheme)
                    .setTitle("Confirm SOS Request")
                    .setMessage("Are you sure you want to call cops?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            //If the user presses YES, then save the complaint details to Firebase db
                            //and build the google api client for live location updates and searching for cop.
                            saveComplaintToCloud();
                            copSearch = true;
                            btn_sos.setVisibility(View.INVISIBLE);
                            anim_gps.setVisibility(View.VISIBLE);
                            btn_cancel.setVisibility(View.VISIBLE);
                            txt_searching.setVisibility(View.VISIBLE);
                            locationSet = false;
                            buildGoogleApiClient();
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            //Is user presses NO, then dismiss the dialog box.
                            dialogInterface.dismiss();
                        }
                    }).create()
                    .show();
        }
    }

    //Build the google api client for obtaining the location services of the device.
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    //Initializing user location updates at periodic intervals.
    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
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
        mLastLocation = location;

        //Adding the citizen updated location to ongoing_complaints object of Firebase db.
        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("ongoing_complaints")
                .child(String.valueOf(complaint_id));
        geoFire = new GeoFire(databaseReference1);
        geoFire.setLocation("citizen_location", new GeoLocation(location.getLatitude(), location.getLongitude()));

        //If the location is set for first time, then set the complaint created coordinates to the ongoing_complaints object and find the nearest cop.
        if (!locationSet) {

            locationSet = true;
            databaseReference2.child("complaint_create_loc_lat").setValue(mLastLocation.getLatitude());
            databaseReference2.child("complaint_create_loc_lng").setValue(mLastLocation.getLongitude());
            getNearestCop();

        }
    }

    //Method for saving the complaint to Firebase db after creating a random complaint id and adding complaint created time and date.
    private void saveComplaintToCloud() {

        //Getting the complaint created date and time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm:ss");
        DateFormat dfDate = new SimpleDateFormat("yyyy/MM/dd");
        String currTime = mdformat.format(calendar.getTime());
        String currDate = dfDate.format(Calendar.getInstance().getTime());

        //generating a random complaint number.
        Random random = new Random();
        complaint_id = random.nextInt(900000) + 100000;

        //Uploading the complaint details to Firebase db.
        databaseReference2 = FirebaseDatabase.getInstance().getReference("ongoing_complaints").child(String.valueOf(complaint_id));
        databaseReference2.child("complaint_id").setValue(complaint_id);
        databaseReference2.child("Citizen_uid").setValue(uid);
        databaseReference2.child("complaint_create_date").setValue(currDate);
        databaseReference2.child("complaint_create_time").setValue(currTime);
    }

    //Method called when the activity resumes, check if gps is enabled and gps permissions granted.
    @Override
    protected void onResume() {
        super.onResume();

        //Checking for the permissions granted status.
        checkForPermission();

        //If gps is no enabled, then make the animation visible and hide the SOS button.
        if (!checkForGps() || !isGpsPermissionGranted) {
            btn_sos.setVisibility(View.INVISIBLE);
            anim_allow_gps.setVisibility(View.VISIBLE);
        } else {

            //If the gps permission is granted and enabled, then hide the gps enable animation and make the sos button visible.
            anim_allow_gps.setVisibility(View.INVISIBLE);
            btn_sos.setVisibility(View.VISIBLE);

            //Checking if the complaint is to be cancelled.
            cancelRequest = getIntent().getBooleanExtra("cancelRequest", false);

            //If complaint is to be cancelled, then reset all the values.
            if (cancelRequest) {
                copFoundID = "";
                copFound = false;
                radius = 1;
            }
        }
    }
}
