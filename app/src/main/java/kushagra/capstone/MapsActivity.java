package kushagra.capstone;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends ActionBarActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private int LOCATION_PERMISSION_CODE=1;
    private Polygon current_polygon;
    Button send_button;
    Button start_button;
    DatabaseReference databaseReference;
    DatabaseReference copterReference;
    DatabaseReference imageReference;
    DatabaseReference defaultReference;
    DatabaseReference logReference;
    DatabaseReference statusReference;
    private ArrayList<LatLng> marker_positions;
    private ArrayList<LatLng> image_markers;
    int marker_count=0;
    private FusedLocationProviderClient mFusedLocationClient;
    private EditText latitude;
    private EditText longitude;
    Marker quadcopter_marker;
    double location_latitude;
    double location_longitude;
    static String LOGMESSAGE="KPLOG: ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        marker_positions = new ArrayList<LatLng>(); //User selected area markers location list
        image_markers = new ArrayList<LatLng>();    //Image markers location list
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        send_button = (Button)findViewById(R.id.send_button);
        FirebaseDatabase database = FirebaseDatabase.getInstance(); //Get firebase reference
        databaseReference = database.getReference("area");  //Reference to selected area in firebase database
        copterReference = database.getReference("copter_location"); //Reference to copter_location in firebase database
        imageReference = database.getReference("aerial_images");    //Reference to aerial images in firebase database
        defaultReference = database.getReference(); //Reference to default in firebase database
        logReference = database.getReference("Log");    //Reference to log in firebase database
        statusReference = database.getReference("status");  //Reference to status in firebase database
        latitude = (EditText)findViewById(R.id.latitude);
        longitude = (EditText)findViewById(R.id.longitude);
        start_button = (Button)findViewById(R.id.start_button);
        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // add event listener to status in firebase
                statusReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
//                      Remove this listener, used only once
                        statusReference.removeEventListener(this);

                        String currentValue = dataSnapshot.getValue().toString();
                        if(currentValue.equals("airborne")){
                            showToastMessage("airborne",500);
                        }
                        else{
                            statusReference.setValue("airborne");
                            imageReference.removeValue();
                            Toast.makeText(getApplicationContext(),"Quad set to Fly",Toast.LENGTH_SHORT).show();
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        statusReference.removeEventListener(this);
                    }
                });
            }
        });
        // add event listener to copter locations in firebase
        ValueEventListener copter_location_listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Copter_location post = dataSnapshot.getValue(Copter_location.class);
                if(post!=null) {
                    latitude.setText("" + post.getLatitude());
                    longitude.setText("" + post.getLongitude());
                    if(quadcopter_marker==null) {
                        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.copter_icon);
                        Bitmap b=bitmapdraw.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                        location_latitude=post.getLatitude();
                        location_longitude=post.getLongitude();
                        quadcopter_marker = mMap.addMarker(new MarkerOptions().position(new LatLng(location_latitude,location_longitude)).draggable(false).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                        quadcopter_marker.setTitle("Quadcopter");
                    }
                    else
                        quadcopter_marker.setPosition(new LatLng(post.getLatitude(),post.getLongitude()));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this,"No authentication",Toast.LENGTH_LONG).show();
            }
        };
        copterReference.addValueEventListener(copter_location_listener);
        // add event listener to log in firebase
        final ValueEventListener log_listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String s = dataSnapshot.getValue(String.class);
                showToastMessage(s,1000);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this,"No authentication",Toast.LENGTH_SHORT).show();
            }
        };
        logReference.addValueEventListener(log_listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_view,menu);
        return super.onCreateOptionsMenu(menu);
    }
    // Menu to select type of map
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.mapTypeNone:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mapTypeNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeTerrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeSatellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    // After map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        }
        else
        {
            update_location();
        }
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Marker marker=mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
                marker.setTitle("("+latLng.latitude+","+latLng.longitude+")");
                marker.setTag(marker_count++);
                marker_positions.add(latLng);
                create_polygon(true);
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(){

            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.clear();
                marker_positions.clear();
                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.copter_icon);
                Bitmap b=bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                quadcopter_marker = mMap.addMarker(new MarkerOptions().position(new LatLng(location_latitude,location_longitude)).draggable(false).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                quadcopter_marker.setTitle("Quadcopter");
                marker_count=0;

            }
        });
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                return;
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                Integer tag =(Integer)marker.getTag();
                marker_positions.set(tag,marker.getPosition());
                create_polygon(false);
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Integer tag =(Integer)marker.getTag();
                marker_positions.set(tag,marker.getPosition());
                create_polygon(true);
            }
        });
        // When any marker is clicked
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                    // Check if image marker is selected
                if(marker.getTag()==null && !marker.getTag().toString().split(":")[0].equals("imageMarker"))
                    return false;
                Toast.makeText(MapsActivity.this,"Downloading...",Toast.LENGTH_LONG).show();
                String fileName = marker.getTag().toString().split(":")[1]+".jpg";
                //Alert box to display image
                AlertDialog.Builder ImageDialog = new AlertDialog.Builder(MapsActivity.this);
                // Image view in alert box for image
                ImageView showImage = new ImageView(MapsActivity.this);
                // Download image from specified URL
                new DownloadImage(showImage,MapsActivity.this).execute("http://104.196.253.182:8080/static/uploads/"+fileName);
                ImageDialog.setView(showImage);
                ImageDialog.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
                ImageDialog.show();
                return false;
            }
        });
        // Send the area to the firebase
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(marker_positions.size()>0) {
                    List<Location_object> location_objects = new ArrayList<Location_object>();
                    for(LatLng x : marker_positions) {
                        location_objects.add(new Location_object(x.latitude,x.longitude));
                    }
                    databaseReference.setValue(location_objects);
                    Toast.makeText(MapsActivity.this,"Area sent",Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(MapsActivity.this,"Select a location",Toast.LENGTH_LONG).show();
                }
            }
        });

//      Get the coordinates of image markers
        ValueEventListener image_reference_listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null)
                    return;
                Log.d("debug",dataSnapshot.getValue().toString());
                // Add blue markers to the map where image is taken
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    Image_info image_info = dataSnapshot1.getValue(Image_info.class);
                    image_info.name = dataSnapshot1.getKey();
                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(image_info.getLatitude(),image_info.getLongitude())).draggable(false).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    marker.setTag("imageMarker:"+image_info.name);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this,"No authentication",Toast.LENGTH_LONG).show();
            }
        };
        imageReference.addValueEventListener(image_reference_listener);

    }
    // Creating polygon of selected area
    private void create_polygon(boolean ready) {
        // Remove current polygon
        if(current_polygon!=null)
            current_polygon.remove();
        // Make polygon of selected marker
        PolygonOptions polygonOptions=new PolygonOptions().addAll(marker_positions);
        // Width of polygon
        polygonOptions.strokeWidth(3);
        // Colour the polygon
        if(!ready) {
            polygonOptions.strokeColor(Color.GREEN);
        }
        else {
            polygonOptions.strokeColor(Color.MAGENTA);
            polygonOptions.fillColor(Color.RED);
        }
        current_polygon=mMap.addPolygon(polygonOptions);
    }
    // In case permission is not given for location access (mainly for Android >= 6)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==LOCATION_PERMISSION_CODE)
        {
            boolean PERMISSION_GRANTED;
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PERMISSION_GRANTED=true;
            } else {
                PERMISSION_GRANTED=false;
            }
            if(PERMISSION_GRANTED)
                update_location();
        }
    }
    // Update location
    private void update_location() {
        try {
            mMap.setMyLocationEnabled(true);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                        .zoom(17)
                                        .bearing(90)
                                        .tilt(40)
                                        .build();
                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            }
                        }
                    });
        } catch (SecurityException e) {
            Toast.makeText(this, "Permission not available", Toast.LENGTH_LONG).show();
        }
    }
    // A simple function to display toast for specified duration less Toast.LENGTH_SHORT (2500)
    public void showToastMessage(String text, int duration){
        final Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, duration);
    }
}
