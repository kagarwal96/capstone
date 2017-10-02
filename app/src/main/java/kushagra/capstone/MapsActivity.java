package kushagra.capstone;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private int LOCATION_PERMISSION_CODE=1;
    private Polygon current_polygon;
    Button send_button;
    DatabaseReference databaseReference;
    private ArrayList<LatLng> marker_positions;
    int marker_count=0;
    private FusedLocationProviderClient mFusedLocationClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        marker_positions = new ArrayList<LatLng>();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        send_button = findViewById(R.id.send_button);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("area");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(marker_positions.size()>0) {
                    String id = databaseReference.push().getKey();
                    List<Location_object> location_objects = new ArrayList<Location_object>();
                    for(LatLng x : marker_positions) {
                        location_objects.add(new Location_object(x.latitude,x.longitude));
                    }
                    databaseReference.child(id).setValue(location_objects);
                    Toast.makeText(MapsActivity.this,"Area sent",Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(MapsActivity.this,"Select a location",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private void create_polygon(boolean ready) {
        if(current_polygon!=null)
            current_polygon.remove();
        PolygonOptions polygonOptions=new PolygonOptions().addAll(marker_positions);
        polygonOptions.strokeWidth(3);
        if(!ready) {
            polygonOptions.strokeColor(Color.GREEN);
        }
        else {
            polygonOptions.strokeColor(Color.MAGENTA);
            polygonOptions.fillColor(Color.RED);
        }
        current_polygon=mMap.addPolygon(polygonOptions);
    }

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
}
