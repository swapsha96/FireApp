package in.swapsha96.fireapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "123";
    private static final int LOCATION = 100;
    private SlidingUpPanelLayout mLayout;
    DrawerLayout mDrawerLayout;
    MessagesAdapter messagesAdapter;
    TextView mDisplayName, mEmail;
    CardView addMessageCard;
    EditText sendMessage;
    Button send;

    ProgressDialog progressDialog;

    private static String uid = "";
    String markerUser;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private GoogleMap mMap;
    ChildEventListener listen;
    LatLng coordinates = new LatLng(0, 0);
    Map<String, Marker> markers = new HashMap<>();
    boolean isFocused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        progressDialog = ProgressDialog.show(ProfileActivity.this, "FireApp", "Preparing...");

        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        //DrawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //NavigationView
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                switch (menuItem.getTitle().toString()) {
                    case "My Profile" :
                        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                        mDisplayName.setText("Loading...");
                        mEmail.setText("Loading...");
                        databaseReference.child(uid).child("details").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                UserDetails details = dataSnapshot.getValue(UserDetails.class);
                                mDisplayName.setText(details.getDisplayName());
                                mEmail.setText(details.getEmail());
                                addMessageCard.setVisibility(View.GONE);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        break;
                    case "Sign Out" :
                        databaseReference.child(uid).child("status").setValue("offline");
                        mAuth.signOut();
                        if (mAuth.getCurrentUser() == null) {
                            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                            finish();
                        }
                        break;
                }
                return true;
            }
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
        //FloatingActionButton
//        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Snackbar.make(findViewById(R.id.drawer_layout), "I'm a Snackbar", Snackbar.LENGTH_LONG).setAction("Action", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Toast.makeText(ProfileActivity.this, "Snackbar Action", Toast.LENGTH_LONG).show();
//                    }
//                }).show();
//            }
//        });
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
//        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.i(TAG, "onPanelStateChanged " + newState);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
//        ImageView img = (ImageView) findViewById(R.id.img);
//        img.setImageBitmap(Bitmap.createBitmap(mUser.getPhotoUrl()));
//
//        Glide.with(this).load(fileInputStream).placeholder(R.drawable.cast_ic_mini_controller_pause_large).into(img);
//        Toast.makeText(ProfileActivity.this,xx.toString(), Toast.LENGTH_LONG).show();
        uid = mUser.getUid();
        markerUser = uid;
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.child(uid).child("details").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(UserDetails.class) == null) {
                    UserDetails details = new UserDetails(uid);
                    databaseReference.child(uid).child("details").setValue(details);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final Intent updateLocation = new Intent(ProfileActivity.this, UpdateLocation.class);
        updateLocation.putExtra("uid", uid);
        startService(updateLocation);

        mEmail = (TextView) findViewById(R.id.email);
        mDisplayName = (TextView) findViewById(R.id.display_name);
        addMessageCard = (CardView) findViewById(R.id.add_message_card);
        sendMessage = (EditText) findViewById(R.id.send_message);
        send = (Button) findViewById(R.id.send_button);
//        TextView displayNameView = (TextView) findViewById(R.id.drawer_display_name);

        String email = mUser.getEmail();
        String displayName = mUser.getDisplayName();
        mEmail.setText(email);
        mDisplayName.setText(displayName);
        addMessageCard.setVisibility(View.GONE);
//        displayNameView.setText(displayName);
        listen = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                updateLocation(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                updateLocation(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                updateLocation(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                updateLocation(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Double lat = location.getLatitude();
                Double lng = location.getLongitude();
                coordinates = new LatLng(lat, lng);
                if(isFocused) {
                    if(markers.get(uid) != null)
                        markers.get(uid).setPosition(coordinates);
                }
                else {
                    if(markers.get(uid) == null)
                    markers.put(uid, mMap.addMarker(new MarkerOptions().position(coordinates)));
                    markers.get(uid).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates,18));
                    progressDialog.dismiss();
                    isFocused = true;
                }

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        };
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            finish();
            return;
        }
        locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = sendMessage.getText().toString();
                sendMessage.setText("");
                databaseReference.child(markerUser).child("messages").push().setValue(new UserMessage(uid, markerUser,message));
            }
        });

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ProfileActivity.this));
        messagesAdapter = new MessagesAdapter(ProfileActivity.this, uid, markerUser);
        recyclerView.setAdapter(messagesAdapter);
        registerForContextMenu(recyclerView);

        databaseReference.child(uid).child("status").setValue("online");
        databaseReference.child(uid).child("status").onDisconnect().setValue("offline");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.setBuildingsEnabled(true);
//        mMap.getUiSettings().setZoomControlsEnabled(true);
        databaseReference.addChildEventListener(listen);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                for(Map.Entry<String, Marker> mMarker : markers.entrySet()) {
                    if(Objects.equals(marker,mMarker.getValue())) {
                        markerUser = mMarker.getKey();
//                        if(markerUser.equals(uid)) {
//                            addMessageCard.setVisibility(View.GONE);
//                        }
//                        else {
//                            addMessageCard.setVisibility(View.VISIBLE);
//                        }
                        addMessageCard.setVisibility(View.VISIBLE);
                        mDisplayName.setText("Loading...");
                        mEmail.setText("Loading...");
                        databaseReference.child(markerUser).child("details").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                UserDetails details = dataSnapshot.getValue(UserDetails.class);
                                mDisplayName.setText(details.getDisplayName());
                                mEmail.setText(details.getEmail());
                                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        messagesAdapter.clearMessages();
                        databaseReference.child(markerUser).child("messages").addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                messagesAdapter.addMessage(dataSnapshot);
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                messagesAdapter.changeMessage(dataSnapshot);
                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {
                                messagesAdapter.removeMessage(dataSnapshot);
                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
//            case R.id.action_settings:
//                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void updateLocation(DataSnapshot dataSnapshot) {
        String user = dataSnapshot.getKey();
        if(!dataSnapshot.hasChild("coordinates"))
            return;
        if(!dataSnapshot.hasChild("status")) {
            databaseReference.child(user).child("status").setValue("offline");
            return;
        }
        if (dataSnapshot.child("coordinates").child("lat").getValue(Double.class) == null)
            return;
        Double lat = dataSnapshot.child("coordinates").child("lat").getValue(Double.class);
        Double lng = dataSnapshot.child("coordinates").child("lng").getValue(Double.class);
        LatLng latLng = new LatLng(lat, lng);
        if(markers.get(user) != null)
            markers.get(user).setPosition(latLng);
        else
            markers.put(user, mMap.addMarker(new MarkerOptions().position(latLng)));
        if(dataSnapshot.child("status").getValue(String.class).equals("online"))
            markers.get(user).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        else
            markers.get(user).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
    }
}
