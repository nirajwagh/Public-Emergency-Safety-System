//Activity for displaying the map and live location of citizen and cop.

package in.mcoeProject.sos;

//Importing all required libraries.
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("deprecation")
public class mapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener, RoutingListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String assignedCopID;
    private DatabaseReference copLocationRef;
    private DatabaseReference complaintForwardRef;
    private DatabaseReference databaseReference1;
    private DatabaseReference complaintCompletedRef;
    private ValueEventListener copLocationRefListener, setAssignedCopInfoListener, complaintCompletedListener, complaintForwardedListener;
    private GeoFire geoFire;
    private String uid, profile_image_url;
    private Marker copMarker, citizenMarker;
    private TextView txt_name, txt_phone, txt_id, txt_estTime;
    private ImageView img_user_profile;
    private List<Polyline> polylines;
    private boolean showRoute=false, complaintCompleted=false;
    private double locationLat=0;
    private double locationLon=0;
    private String complaint_id;
    private String copName;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //Calling the method for building the Google API client for accessing the location of the user.
        buildGoogleApiClient();

        //Obtaining all required views of the activity.
        txt_name=findViewById(R.id.txt_name);
        txt_phone=findViewById(R.id.txt_phone);
        txt_id=findViewById(R.id.txt_id);
        img_user_profile=findViewById(R.id.img_user_profile);
        txt_estTime=findViewById(R.id.txt_estTime);

        //Accessing and initializing the Google maps fragment.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Initializing the PolyLines object for displaying the routes on map using lines.
        polylines = new ArrayList<>();

        //Obtaining the user id from the Firebase Authentication service.
        uid=FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Accessing the cop id and complaint id passed by previous activity.
        assignedCopID=getIntent().getStringExtra("assignedCopID");
        complaint_id=getIntent().getStringExtra("complaint_id");

        //Setting the assigned cop info in the activity layout views.
        setAssignedCopInfo();

        //Adding details to Shared Preference file.
        addDetailsToSharedPreference();

        //Adding a listener to be triggered if the complaint is completed.
        addComplaintCompletedListener();

        //Adding a listener to be triggered if the complaint is forwarded by the assigned cop.
        addComplaintForwardedListener();
    }

    //onMapReady automatically called when the google map is ready for processing.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Getting assigned cop location and assigning marker to the google map.
        getCopLocation();
    }

    //Method for storing data locally on user device using Shared Preference.
    private void addDetailsToSharedPreference(){

        //Using shared preference for storing data in storedValuesFile.xml file locally on user device.
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("storedValuesFile", 0);

        //Initializing the editor for modifying the shared preference file.
        editor= sharedPreferences.edit();

        //Adding the assigned cop id and complaint id to the shared preference file.
        editor.putString("assignedCopUid", assignedCopID);
        editor.putString("complaint_id", complaint_id);
        editor.apply();

    }

    //Method for reporting to the nearest police station.
    private void reportNearestPoliceStation(String typeOfRequest) {

        //Removing the assigned listeners for stopping further listening.
        removeListeners();

        //Removing current assigned cop data from the Firebase db.
        removeDatabaseValues(typeOfRequest);

        //Getting user current GPS location for passing to reporting cop activity.
        double currLocationLat = mLastLocation.getLatitude();
        double currLocationLng = mLastLocation.getLongitude();

        //Removing citizen location from Firebase db and stopping GPS location updates.
        geoFire.removeLocation("citizen_location");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,mapActivity.this);
        removeListeners();

        //Intent for redirecting to LocatingPoliceStation activity.
        Intent intent=new Intent(mapActivity.this, LocatingPoliceStation.class);

        //Adding data to the intent.
        intent.putExtra("currLocationLat", currLocationLat);
        intent.putExtra("currLocationLng", currLocationLng);
        intent.putExtra("isAlreadySearching", false);
        intent.putExtra("complaint_id", String.valueOf(complaint_id) );
        intent.putExtra("typeOfRequest", typeOfRequest);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        //Finishing the current activity and starting the LocatingPoliceStation activity.
        finish();
        startActivity(intent);

    }

    //Method for removing data from the Firebase db,
    private void removeDatabaseValues(String typeOfRequest) {

        //If user has reported the cop on his behalf, then add the reported complaint details to Firebase db.
        if(typeOfRequest.equals("reported")){
            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("ongoing_reported_complaints").child(String.valueOf(complaint_id));
            databaseReference.child("complaint_id").setValue(String.valueOf(complaint_id));
        }

        //Removing the currently assigned cop_ID and cop_location from the Firebase db ongling complaints.
        DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("ongoing_complaints").child(String.valueOf(complaint_id));
        databaseReference2.child("cop_ID").removeValue();
        databaseReference2.child("cop_location").removeValue();

        //Removing the assigned cop id from the citizen object of Firebase db.
        databaseReference2 =FirebaseDatabase.getInstance().getReference("actors").child("citizens").child(uid);
        databaseReference2.child("assigned_cop_id").removeValue();

    }

    //Adding listener to trigger when complaint is forwarded by the assigned cop.
    private void addComplaintForwardedListener() {

        complaintForwardRef= FirebaseDatabase.getInstance().getReference("ongoing_forwarded_complaints")
                .child(complaint_id);
       complaintForwardedListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    //Forwarding to the nearest police station using the reportNearestPoliceStation method and type of request as "forwarded".
                    reportNearestPoliceStation("forwarded");

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };

       //Finally, adding the listener.
       complaintForwardRef.addValueEventListener(complaintForwardedListener);
    }

    //Method for setting the assigned cop info to the activity layout views.
    private void setAssignedCopInfo() {

        //Adding the assigned cop id to Firebase db ongoing complaints object.
        DatabaseReference addCompIdRef = FirebaseDatabase.getInstance().getReference("ongoing_complaints")
                .child(complaint_id);
        addCompIdRef.child("cop_ID").setValue(assignedCopID);

        //Obtaining the assigned cop details from the Firebase db.
        databaseReference1= FirebaseDatabase.getInstance().getReference("actors")
                .child("cops")
                .child(assignedCopID);
        setAssignedCopInfoListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Assigning the obtained cop details to the layout views.
                copName=dataSnapshot.child("name").getValue().toString();
                txt_name.setText(copName);
                txt_phone.setText(dataSnapshot.child("phone").getValue().toString());
                txt_id.setText(dataSnapshot.child("id").getValue().toString());

                //If profile image is uploaded by the cop, then image assigned using Glide library and image url
                if(dataSnapshot.child("profile_image_url").exists())
                {
                    profile_image_url=dataSnapshot.child("profile_image_url").getValue().toString();

                    //Glide library is used for assigning images from url to imageView.
                    Glide.with(getApplication()).load(profile_image_url).into(img_user_profile);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        databaseReference1.addValueEventListener(setAssignedCopInfoListener);
    }

    //Method for cancelling the current complaint request.
    public void cancelRequestButtonPressed(View v){

        //Stopping the current live location updates and removing location of citizen from Firebase db.
        geoFire.removeLocation("citizen_location");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,mapActivity.this);

        //Calling the cancelRequest method to cancel the request.
        cancelRequest();
    }

    //Method to cancel the current complaint.
    private void cancelRequest(){

        //Stopping the current live location updates and removing location of citizen from Firebase db.
        geoFire.removeLocation("citizen_location");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,mapActivity.this);

        //Removing the event listener for getting cop live location.
        copLocationRef.removeEventListener(copLocationRefListener);

        //removing ongoing complaint details form Firebase db and Shared Preferences.
        removeOngoingComplaintDetails();

        //If complaint completed, then notify and redirect to ComplaintCompleted activity.
        if(complaintCompleted){

            Intent intent=new Intent(mapActivity.this, ComplaintCompleted.class);

            //Displaying notification about complaint completion.
            NotificationHelper.clearAllNotifications(this);
            NotificationHelper.displayNotification(getApplicationContext(), "Complaint Solved", "Your complaint Id was "+complaint_id, 03);

            //Adding complaint id and cop uid to intent and starting the ComplaintCompleted activity.
            intent.putExtra("complaint_id", complaint_id+"");
            intent.putExtra("cop_uid", assignedCopID);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(intent);

        }else{

            //Complaint is cancelled by the citizen. Show notification and redirect to Request activity.
            NotificationHelper.clearAllNotifications(mapActivity.this);
            NotificationHelper.clearAllNotifications(mapActivity.this);
            NotificationHelper.displayNotification(getApplicationContext(), "Complaint Cancelled", "You cancelled your last complaint.", 05);
            Intent intent=new Intent(mapActivity.this, Request.class);
            intent.putExtra("cancelRequest", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(intent);

        }

    }

    //Method for removing ongoing complaint data from Firebase db and shared preferences.
    private void removeOngoingComplaintDetails() {

        //Removing citizen id and complaint if from assigned cop object from Firebase db .
        DatabaseReference copObjectRef=FirebaseDatabase.getInstance().getReference().child("actors").child("cops").child(assignedCopID);
        copObjectRef.child("citizenID").removeValue();
        copObjectRef.child("complaint_id").removeValue();

        //Removing assigned cop id and complaint id from the citizens objects current user from Firebase db.
        DatabaseReference removeCopIdRef=FirebaseDatabase.getInstance().getReference("actors").child("citizens").child(uid);
        removeCopIdRef.child("assigned_cop_id").removeValue();
        removeCopIdRef.child("complaint_id").removeValue();

        //Removing the ongoing complaint object from Firebase db.
        DatabaseReference removeOngoingComplaint=FirebaseDatabase.getInstance().getReference("ongoing_complaints").child(String.valueOf(complaint_id));
        removeOngoingComplaint.removeValue();

        //Removing complaint id and assigned cop uid from the shared preferences file.
        editor.remove("complaint_id");
        editor.remove("assignedCopUid");
        editor.apply();
    }

    //Method for getting assigned cop location.
    private void getCopLocation() {

        //Accessing the live location of assigned cop using ongoing_complaints object of Firebase db.
        copLocationRef = FirebaseDatabase.getInstance().getReference("ongoing_complaints")
                .child(String.valueOf(complaint_id)).child("cop_location").child("l");
        copLocationRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List map=(List<Object>) dataSnapshot.getValue();
                    locationLat=0;
                    locationLon=0;
                    if(map.get(0)!=null){
                        locationLat=Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1)!=null){
                        locationLon=Double.parseDouble(map.get(1).toString());
                    }

                    //Converting the lat lng double double values to LatLang object.
                    LatLng copLocation= new LatLng(locationLat, locationLon);

                    //Removing any preexisting maps marker.
                    if(copMarker!=null){
                        copMarker.remove();
                    }

                    //Adding market to for the cop location on the map and assigning marker icon from mipmap folder.
                    copMarker= mMap.addMarker(new MarkerOptions().position(copLocation).title("cop").icon(bitmapDescriptorFromVector(mapActivity.this, R.mipmap.cop_marker_icon)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };

        copLocationRef.addValueEventListener(copLocationRefListener);
    }

    //Method to return a Bitmap image from the passed vector image, for assigning icon to map marker.
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        //Returning the bitmap image of the passed vector resource id.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    //Method for showing routes between cop and citizen on google map.
    private void doRouting() {

        getCopLocation();
        //Moving the map focus to the citizen current location.
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

        //Performing routing and specifying both end points using ".waypoints()".
        Routing routing = new Routing.Builder()
                .key(BuildConfig.GOOGLE_MAPS_API_KEY) //API key required for google Directions API.
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(locationLat, locationLon),new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()) )
                .build();
        routing.execute();
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

    //Method automatically called when user location changes.
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        //Uploading the citizen's current location to Firebase db every time location changes.
        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("ongoing_complaints")
                .child(String.valueOf(complaint_id));
        geoFire=new GeoFire(databaseReference1);
        geoFire.setLocation("citizen_location", new GeoLocation(location.getLatitude(),location.getLongitude()));

        //Initializing a LatLng object with user current location for adding the citizen marker to map.
        LatLng citizenLocation = new LatLng(location.getLatitude(), location.getLongitude());

        //Removing the previous citizen marker if exists.
        if(citizenMarker!=null){
            citizenMarker.remove();
        }

        //Adding the new citizen marker to the map with new location.
        citizenMarker= mMap.addMarker(new MarkerOptions().position(citizenLocation).title("cop").icon(bitmapDescriptorFromVector(mapActivity.this, R.mipmap.user_marker_icon)));

        //Showing the routes between cop and citizen for the first time.
        if(!showRoute){
            showRoute=true;
            doRouting();
        }
    }

    //Initializing user location updates at periodic intervals.
    @SuppressLint("MissingPermission")
    @Override
    public void onConnected( Bundle bundle) {
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

    //Method called automatically when the activity is stopped.
    @Override
    protected void onStop() {
        super.onStop();

        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("actors").child("users").child(userID).child("location");
            databaseReference.setValue(null);
        }
    }

    //Method called automatically when routing fails.
    @Override
    public void onRoutingFailure(RouteException e) {
        if(e == null) {

            //Display a toast message for failed routing.
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {
    }

    //Method called automatically when routing success.
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        //Removing any previous poly lines.
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        //Displaying the route on map using polylines.
        polylines = new ArrayList<>();
        for (int i = 0; i <route.size(); i++) {
            PolylineOptions polyOptions = new PolylineOptions();

            //setting color for the route
            polyOptions.color(getResources().getColor(R.color.colorRoute));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            //Obtaining the estimated time required for covering the route.
            int minutes=route.get(i).getDurationValue()/60;
            String time="Estimated Arrival Time: "+minutes+"mins";
            txt_estTime.setText(time);

            //Displaying notification for the estimated time required.
            NotificationHelper.displayNotification(getApplicationContext(), "Arriving Soon", copName+" is on the way, will arrive in "+minutes+" minutes", 02);

        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    //Method for adding listener for completed complaint.
    private void addComplaintCompletedListener(){
        complaintCompletedRef=FirebaseDatabase.getInstance().getReference("completed_complaints").child(String.valueOf(complaint_id));
        complaintCompletedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    complaintCompleted=true;
                    //Calling the cancelRequest method to complete the request and perform other operations as needed.
                    cancelRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        complaintCompletedRef.addValueEventListener(complaintCompletedListener);

    }

    //Method to remove all assigned listeners.
    void removeListeners(){
        databaseReference1.removeEventListener(setAssignedCopInfoListener);
        complaintCompletedRef.removeEventListener(complaintCompletedListener);
        copLocationRef.removeEventListener(copLocationRefListener);
        complaintForwardRef.removeEventListener(complaintForwardedListener);
    }

    //Method for making a phone call to the assigned cop on call icon pressed by user.
    public void phoneCallToAssignedCop(View view) {

        //Intent for redirecting to the Phone app dial screen.
        Intent callIntent=new Intent(Intent.ACTION_DIAL);

        //Automatically setting the cop phone number in the phone app.
        callIntent.setData(Uri.parse("tel:"+txt_phone.getText()));
        startActivity(callIntent);
    }

    //Method to be called when refresh icon pressed.
    public void refreshIconPressed(View view) {

        //calling the routing method.
        doRouting();
    }

    //Method to be called when Report icon presed.
    public void reportIconPressed(View view) {

        //Creating and showing dialog box for confirmation by the user for reporting the assigned cop.
        new AlertDialog.Builder(mapActivity.this).setCancelable(true)
                .setTitle("Report !")
                .setMessage("Report to the nearest Police Station?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //calling the method to report to nearest police station on user confirmation.
                        reportNearestPoliceStation("reported");
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //Dismissing the dialog box is user cancels reporting of cop.
                        dialogInterface.dismiss();
                    }
                }).show();
    }
}
