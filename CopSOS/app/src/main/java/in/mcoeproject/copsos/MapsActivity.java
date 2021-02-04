//Activity for displaying the live location of the cop and assigned citizen.

package in.mcoeproject.copsos;

//Importing all required libraries.
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, RoutingListener {

    //Declaring all required views and objects.
    private GoogleMap mMap;
    private String assignedCitizenID, complaint_id, citizenName, uid, profile_image_url, navCoordinatesUri;
    private Location mLastLocation;
    private GeoFire geoFireCopAssigned;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference copAssignedRef, assignedCitizenRef, databaseReference1, databaseReference2, assignedCitizenLocationRef, removeCompDetailsRef;
    private ValueEventListener assignedCitizenLocationRefListener, cancelledComplaintListener, reportedComplaintListener, setAssignedCitizenInfoListener;
    private LocationRequest mLocationRequest;
    private TextView txt_name, txt_phone, txt_estDistance;
    private ImageView img_user_profile, img_call_icon, img_refresh, img_nav_icon, img_forward_icon;
    private boolean showRoute=false, complaintCompleted=false;
    private double LocationLat, LocationLon, distance;
    private List<Polyline> polylines= new ArrayList<>();
    private static final int[] COLORS = new int[]{R.color.colorAccent};
    private LatLng copLatLng;
    private Marker citizenMarker, copMarker;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Obtaining all views of the layout file.
        txt_name = findViewById(R.id.txt_name);
        txt_phone = findViewById(R.id.txt_phone);
        img_user_profile = findViewById(R.id.img_user_profile);
        img_call_icon = findViewById(R.id.img_call_icon);
        img_refresh = findViewById(R.id.img_refresh_icon);
        img_nav_icon = findViewById(R.id.img_nav_icon);
        txt_estDistance = findViewById(R.id.txt_estDistance);
        img_forward_icon = findViewById(R.id.img_forward_icon);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Obtaining the assigned citizen id and complaint id from previous activity.
        assignedCitizenID = getIntent().getStringExtra("assignedCitizenID");
        complaint_id = getIntent().getStringExtra("complaint_id");

        sharedPreferences=getApplicationContext().getSharedPreferences("valuesFile", 0);
        editor=sharedPreferences.edit();

        //Displaying the new complaint notification using NotificationHelper class.
        NotificationHelper.displayNotification(this, "New Complaint", "Tap to get details of assigned citizen", 02);

        //setting the assigned citizen details to layout views.
        setAssignedCitizenInfo();

        //Listening for the complaint cancellation and completion.
        complaintCancelledCompletedListener();

        //Listening for the current complaint reported status.
        addListenerForReportedComplaint();
    }

    //Method for listening for current complaint cancellation and completion.
    private void complaintCancelledCompletedListener() {

        //Adding value event listener for listening for citizen id removal from cop object in firebase db.
        assignedCitizenRef = FirebaseDatabase.getInstance().getReference().child("actors").child("cops").child(uid).child("citizenID");
        cancelledComplaintListener= new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                } else {

                    //If citizen id is removed from cop object.

                    //Erasing all lines from the map and stopping live location updates and removing location from Firebase..
                    erasePolyLines();
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MapsActivity.this);
                    try {
                        geoFireCopAssigned.removeLocation("cop_location");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //Removing all assigned listeners.
                    removeListeners();

                    //Removing complaint details and assigned citizen id from Shared Preferences file.
                    editor.remove("complaint_id");
                    editor.remove("assignedCitizenUid");
                    editor.apply();

                    //If the complaint is completed, redirect to ComplaintCompleted Activity.
                    if (complaintCompleted) {
                        NotificationHelper.clearAllNotifications(MapsActivity.this);
                        NotificationHelper.displayNotification(MapsActivity.this, "Complaint Completed", "Congratulations.", 04);
                        Intent intent = new Intent(MapsActivity.this, ComplaintCompleted.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        finish();
                        startActivity(intent);
                    } else {

                        //Else redirect to ComplaintCancelled Activity.
                        Intent intent = new Intent(MapsActivity.this, ComplaintCancelled.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        NotificationHelper.clearAllNotifications(MapsActivity.this);
                        NotificationHelper.displayNotification(MapsActivity.this, "Cancelled Complaint", "Sorry, your last complaint was cancelled by the citizen", 05);
                        finish();
                        startActivity(intent);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        assignedCitizenRef.addValueEventListener(cancelledComplaintListener);
    }

    //Method for listening for current complaint reported status.
    private void addListenerForReportedComplaint() {

        //Adding value event listener for listening for complaint added to ongoing_reported_complaints object in Firebase db.
         databaseReference2 = FirebaseDatabase.getInstance().getReference("ongoing_reported_complaints").child(complaint_id);
         reportedComplaintListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //If complaint is reported

                    //Stopping the live location updates and removing the location from Firebase db.
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MapsActivity.this);
                    geoFireCopAssigned.removeLocation(uid);

                    //Removing the citizen id and complaint id from cop details from Firebase db.
                    DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("actors").child("cops").child(uid);
                    databaseReference.child("citizenID").removeValue();
                    databaseReference.child("complaint_id").removeValue();
                    NotificationHelper.clearAllNotifications(MapsActivity.this);

                    //erasing the routing lines from Map.
                    erasePolyLines();

                    //Removing the assignedCitizenId form Shared Preferences file.
                    editor.remove("assignedCitizenUid");
                    editor.apply();

                    //Removing all added listeners.
                    removeListeners();

                    //Redirecting to ComplaintReported Activity
                    Intent intent = new Intent(MapsActivity.this, ComplaintReported.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("complaint_id", complaint_id);
                    finish();
                    startActivity(intent);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseReference2.addValueEventListener(reportedComplaintListener);
    }

    //Method for obtaining the current assigned details from Firebase db.
    public void setAssignedCitizenInfo(){

       //Adding listener for getting the citizen details.
       databaseReference1= FirebaseDatabase.getInstance().getReference("actors")
                .child("citizens")
                .child(assignedCitizenID);
         setAssignedCitizenInfoListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Obtaining and setting citizen name and phone.
                citizenName=dataSnapshot.child("name").getValue().toString();
                txt_name.setText(citizenName);
                txt_phone.setText(dataSnapshot.child("phone").getValue().toString());

                //Assigning the user profile image if it exists.
                if(dataSnapshot.child("profile_image_url").exists())
                {
                    profile_image_url=dataSnapshot.child("profile_image_url").getValue().toString();
                    //Setting the user profile image using Glide library using the image url.
                    Glide.with(getApplication()).load(profile_image_url).into(img_user_profile);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        databaseReference1.addValueEventListener(setAssignedCitizenInfoListener);
    }

    //Method for initializing the google api client service for accessing GPS Location service.
    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    //Method automatically called when the mapp is ready for processing.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Displaying assigned citizen location on map.
        getAssignedCitizenLocation();
    }

    //Method for showing routes between cop and citizen.
    public void doRouting(View view) {

        //Setting the map focus to cops's current location.
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

        //Performing the routing using Google Directions API.
        Routing routing = new Routing.Builder()
                .key(BuildConfig.GOOGLE_MAPS_API_KEY)
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                //Setting the two end points location coordinates for routing.
                .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new LatLng(LocationLat, LocationLon))
                .build();
        routing.execute();

    }

    //Method for obtaining the assigned citizen's live location.
    public void getAssignedCitizenLocation() {

        //Getting the citizen live location from the ongoing_complaints object of Firebase db.
        assignedCitizenLocationRef = FirebaseDatabase.getInstance().getReference("ongoing_complaints")
                .child(complaint_id).child("citizen_location").child("l");
        assignedCitizenLocationRefListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    //Storing the obtained location in a List.
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    LocationLat = 0;
                    LocationLon = 0;

                    //Getting the citizen Latitude and longitude from the List.
                    if (map.get(0) != null) {
                        LocationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        LocationLon = Double.parseDouble(map.get(1).toString());
                    }

                    //Declaring and initializing a LatLng object for map marker.
                    LatLng citizenLocation = new LatLng(LocationLat, LocationLon);

                    //Removing any previous marker.
                    if (citizenMarker != null) {
                        citizenMarker.remove();
                    }

                    //Adding a new marker for citizen location using the icon from mipmap resources folder.
                    citizenMarker = mMap.addMarker(new MarkerOptions().position(citizenLocation).icon(bitmapDescriptorFromVector(MapsActivity.this, R.mipmap.user_marker_icon)).title("Citizen"));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        assignedCitizenLocationRef.addValueEventListener(assignedCitizenLocationRefListener);
    }

    //Method for returning the BitmapDescriptor image of the vector image for the marker.
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    //Method for getting the updated live location of the cop using Google API client.
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

    //Method called automatically when the cop location changes.
    @Override
    public void onLocationChanged(Location location) {

        //Adding the updated cop location to Firebase db.
        mLastLocation = location;
        copAssignedRef = FirebaseDatabase.getInstance().getReference("ongoing_complaints")
                .child(String.valueOf(complaint_id));
        geoFireCopAssigned= new GeoFire(copAssignedRef);
        geoFireCopAssigned.setLocation("cop_location", new GeoLocation(location.getLatitude(), location.getLongitude()));

        //Adding the updated marker for cop location on map.
        copLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        //Removing any previous marker.
        if(copMarker!=null){
            copMarker.remove();
        }

        //Adding the new marker for cop location with the cop icon from mipmap folder.
        copMarker = mMap.addMarker(new MarkerOptions().position(copLatLng).icon(bitmapDescriptorFromVector(MapsActivity.this, R.mipmap.cop_marker_icon)).title("Citizen"));

        //Showing the route for first time after the location is updated.
        if(showRoute==false){
            showRoute=true;

            doRouting(null);
        }
    }

    //Method called automatically when the routing failed.
    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    //Method called when the routing succeeds.
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        //Removing any previous poly lines.
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        //Drawing the new poly lines on map for updates routes.
        polylines = new ArrayList<>();
        for (int i = 0; i <route.size(); i++) {
            int colorIndex = i % COLORS.length;
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            //Getting the estimated distance between cop and citizen.
            //Converting the distance to Kms unit.
            distance=route.get(i).getDistanceValue()/1000f;
            DecimalFormat df = new DecimalFormat("####0.00");
            txt_estDistance.setText("Estimated Distance: "+df.format(distance)+"Kms");

            //Showing the notification of Estimated distance.
            NotificationHelper.displayNotification(this, "Citizen Details", citizenName+" is "+ df.format(distance)+"Kms away from you.", 03);
            if(distance<0.10){
                showCitizenFoundDialog();
            }
        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    //Method for erasing the poly lines from the map.
    private  void erasePolyLines(){
        for(Polyline line:polylines){
            line.remove();
        }
        polylines.clear();
    }

    //Method for showing the alert dialog for citizen found confirmation.
    private void showCitizenFoundDialog(){
        new AlertDialog.Builder(this)
                .setTitle("Citizen Found?")
                .setMessage("Have You Found The Citizen?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //If Cop confirms citizen found
                        complaintCompleted();

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //If citizen not found, then dismiss the dialog box.
                        dialogInterface.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    //Method for complaint completed process.
    private void complaintCompleted(){

        //Moving the complaint details from ongoing_complaints to completed_complaints object of Firebase db.
        final DatabaseReference fromPath=FirebaseDatabase.getInstance().getReference("ongoing_complaints").child(complaint_id);
        final DatabaseReference toPath=FirebaseDatabase.getInstance().getReference("completed_complaints").child(complaint_id);

        fromPath.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Adding complaint completed date and time.
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm:ss");
                DateFormat dfDate = new SimpleDateFormat("yyyy/MM/dd");
                String currTime = mdformat.format(calendar.getTime());
                String currDate=dfDate.format(Calendar.getInstance().getTime());

                //Adding all complaint details to a Map object.
                Map<String, Object> complaintsDetails=new HashMap<>() ;
                complaintsDetails.put("complaint_id", dataSnapshot.child("complaint_id").getValue());
                complaintsDetails.put("Citizen_uid", dataSnapshot.child("Citizen_uid").getValue());
                complaintsDetails.put("assigned_cop_ID", dataSnapshot.child("cop_ID").getValue());
                complaintsDetails.put("complaint_solved_date", currDate);
                complaintsDetails.put("complaint_solved_time", currTime);
                complaintsDetails.put("complaint_solved_loc_lon", mLastLocation.getLongitude());
                complaintsDetails.put("complaint_solved_loc_lng", mLastLocation.getLatitude());
                complaintsDetails.put("complaint_create_date", dataSnapshot.child("complaint_create_date").getValue());
                complaintsDetails.put("complaint_create_loc_lat", dataSnapshot.child("complaint_create_loc_lat").getValue());
                complaintsDetails.put("complaint_create_loc_lng", dataSnapshot.child("complaint_create_loc_lng").getValue());
                complaintsDetails.put("complaint_create_time", dataSnapshot.child("complaint_create_time").getValue());

                //Adding the map object to completed_complaints object of Firebase db.
                toPath.updateChildren(complaintsDetails);

                //Removing the complaint from ongoing_complaints object of Firebase db.
                removeCompDetailsRef=FirebaseDatabase.getInstance().getReference("ongoing_complaints").child(complaint_id);
                removeCompDetailsRef.removeValue();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        //Setting the complaintCompleted boolean to true for further processing the complaint in complaintCancelledCompletedListener method.
        complaintCompleted=true;

    }

    //Method for removing all added listeners.
    private void removeListeners(){
        assignedCitizenRef.removeEventListener(cancelledComplaintListener);
        databaseReference2.removeEventListener(reportedComplaintListener);
        databaseReference1.removeEventListener(setAssignedCitizenInfoListener);
        assignedCitizenLocationRef.removeEventListener(assignedCitizenLocationRefListener);
    }

    //Method called when phone icon pressed.
    public void phoneIconPressed(View view) {

        //Opening the phone dial app on user device for making call to citizen.
        Intent callIntent = new Intent(Intent.ACTION_DIAL);

        //Setting the phone number automatically in user phone dailer.
        callIntent.setData(Uri.parse("tel:" + txt_phone.getText()));
        startActivity(callIntent);
    }

    //Method to be called when navigation icon pressed for navigating to the citizen.
    public void mapsNavigationIconPressed(View view) {

        //Adding citizen location coordinates as uri to the intent.
        navCoordinatesUri = "google.navigation:q=" + LocationLat + "," + LocationLon;
        Uri navUri = Uri.parse(navCoordinatesUri);

        //Starting the google maps navigation to the citizen location.
        Intent navIntent = new Intent(Intent.ACTION_VIEW, navUri);
        navIntent.setPackage("com.google.android.apps.maps");
        startActivity(navIntent);
    }

    //Method to be called when forward complaint icon pressed.
    public void forwardCompliantIconPressed(View view) {

        //Creating an alert dialog for confirming the cop about complaint forwarding
        new AlertDialog.Builder(MapsActivity.this).setCancelable(true)
                .setTitle("Forward Complaint!")
                .setMessage("Forward to the nearest Police Station?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //If user confirms to forward complaint,
                        Toast.makeText(MapsActivity.this, "FORWARDING", Toast.LENGTH_SHORT).show();

                        //Calling method to forward complaint.
                        forwardCurrentComplaint();

                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //If user pressed "NO" then dismissing the alert dialog.
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    //Method to perform operations required for forwarding the complaint.
    private void forwardCurrentComplaint() {

        //Stopping the live location updates of cop and removing the saved location from Firebase db.
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MapsActivity.this);
        geoFireCopAssigned.removeLocation(uid);

        //Removing all added listeners and erasing the routing poly lines from the map.
        removeListeners();
        erasePolyLines();

        //Removing citizenID and complaint_id from current cop object from Firebase db.
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("actors").child("cops").child(uid);
        databaseReference.child("citizenID").removeValue();
        databaseReference.child("complaint_id").removeValue();

        //Removing the assigned citizen id from the cops shared preferences file.
        editor.remove("assignedCitizenUid");
        editor.apply();


        //Clearing all current notifications.
        NotificationHelper.clearAllNotifications(MapsActivity.this);

        //Redirecting to the ComplaintForwarded Activity.
        Intent intent = new Intent(MapsActivity.this, ComplaintForwarded.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //Passing complaint_id to the ComplaintForwarded Activity.
        intent.putExtra("complaint_id", complaint_id);
        finish();
        startActivity(intent);

    }


}
