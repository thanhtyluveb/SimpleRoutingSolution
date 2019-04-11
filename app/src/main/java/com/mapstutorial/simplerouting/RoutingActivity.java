/*
 * Copyright (c) 2011-2019 HERE Global B.V. and its affiliate(s).
 * All rights reserved.
 * The use of this software is conditional upon having a separate agreement
 * with a HERE company for the use or utilization of this software. In the
 * absence of such agreement, the use of the software is not allowed.
 */

package com.mapstutorial.simplerouting;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.mapping.MapState;
import com.here.android.mpa.mapping.SupportMapFragment;
import com.here.android.mpa.routing.RouteManager;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.search.Address;
import com.here.android.mpa.search.AutoSuggest;
import com.here.android.mpa.search.ErrorCode;
import com.here.android.mpa.search.GeocodeRequest;
import com.here.android.mpa.search.GeocodeResult;
import com.here.android.mpa.search.Location;
import com.here.android.mpa.search.ResultListener;
import com.here.android.mpa.search.ReverseGeocodeRequest;
import com.here.android.mpa.search.TextAutoSuggestionRequest;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RoutingActivity extends FragmentActivity {
    public PositioningManager mPositioningManager;
    public PositioningManager.OnPositionChangedListener positionListener;
    private static final String LOG_TAG = RoutingActivity.class.getSimpleName();
    public Switch switchlocation;
    GeoCoordinate geoCoordinate;
    public SearchView searchView;
    GeoCoordinate vancouver;
    RoutePlan routePlan;
    ListView listViewsearch;
    public ArrayAdapter<String> arrayAdapter;
    ResultListener<List<GeocodeResult>> listener;
    public ResultListener<Location> listener1;
    public List<ViewObject> viewObjects;
    // permissions request code
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    ArrayList<String> listsearchresults = new ArrayList<>();
    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    // map embedded in the map fragment
    public Map map = null;
    public Button routebtn;

    // map fragment embedded in this activity
    private SupportMapFragment mapFragment = null;

    // TextView for displaying the current map scheme
    private TextView textViewResult = null;

    // MapRoute for this activity
    private static MapRoute mapRoute = null;
    Dialog dialog;
    RouteManager routeManager = new RouteManager();
    Button btnDriction;
    Button btnLocation;
    GeoCoordinate geoCoordinatediemden;
    Button btncancel;
    GeoCoordinate geoCoordinatecenter = new GeoCoordinate(21.0121, 105.775);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnDriction = findViewById(R.id.buttonchiduong);
        btnLocation = findViewById(R.id.buttuonlocation);
        dialog = new Dialog(this);
        dialog.setTitle("Chỉ Đường");
        dialog.setContentView(R.layout.dialog_direction);
        routebtn = findViewById(R.id.directionsbutton);
        listViewsearch = findViewById(R.id.listview);
        searchView = findViewById(R.id.searchview);
        checkPermissions();
        vancouver = new GeoCoordinate(49.2849, -123.1252);
//        listener = new GeocodeListener();

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                listViewsearch.setVisibility(View.GONE);
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                routebtn.setVisibility(View.VISIBLE);
                listViewsearch.setVisibility(View.GONE);

                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                listViewsearch.setVisibility(View.VISIBLE);
                routebtn.setVisibility(View.GONE);
                GeocodeRequest request = new GeocodeRequest(newText.toLowerCase()).setSearchArea(geoCoordinatecenter, 5000);
                request.execute(new ResultListener<List<GeocodeResult>>() {
                    @Override
                    public void onCompleted(List<GeocodeResult> geocodeResults, ErrorCode errorCode) {
                        if (geocodeResults != null) {
                            for (int i = 0; i < geocodeResults.size(); i++) {
                                Log.d("GeocodeRequest", "" + geocodeResults.get(i).getLocation().toString());
                                listsearchresults.addAll(Collections.singleton(geocodeResults.get(i).getLocation().toString()));
                                Log.d("listsearchresults", "" + listsearchresults);

                            }

                        }


                    }
                });
                arrayAdapter = new ArrayAdapter<>(getApplication(), android.R.layout.simple_list_item_1, listsearchresults);
                listViewsearch.setAdapter(arrayAdapter);
                arrayAdapter.getFilter().filter(newText.toLowerCase());

                return false;
            }

        });
        listViewsearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplication(),""+listsearchresults.get(position),Toast.LENGTH_LONG).show();


            }
        });


        ReverseGeocodeRequest request1 = new ReverseGeocodeRequest(geoCoordinatecenter);
        request1.execute(new ResultListener<Address>() {
            @Override
            public void onCompleted(Address address, ErrorCode errorCode) {
                Log.d("ReverseGeocodeRequest", "" + address);
            }
        });


    }


    // Example request listener
    class AutoSuggestionQueryListener implements ResultListener<List<AutoSuggest>> {

        @Override
        public void onCompleted(List<AutoSuggest> data, ErrorCode error) {
            for (AutoSuggest r : data) {
                try {
                    String term = "rest";
                    TextAutoSuggestionRequest request = null;
                    request = new TextAutoSuggestionRequest(term).setSearchCenter(map.getCenter());
                    if (request.execute(new AutoSuggestionQueryListener()) !=
                            ErrorCode.NONE) {
                        //Handle request error
                        //...
                    }
                } catch (IllegalArgumentException ex) {
                    //Handle invalid create search request parameters
                }
            }
        }
    }


//    class GeocodeListener implements ResultListener<List<GeocodeResult>> {
//        @Override
//        public void onCompleted(List<GeocodeResult> data, ErrorCode error) {
//            if (error != ErrorCode.NONE) {
//                Log.d("data", "" + data.size());
//
//            } else {
//                // Process result data
//                Log.d("data", "" + data);
//            }
//        }
//    }


//    class ReverseGeocodeListener implements ResultListener<Location> {
//        @Override
//        public void onCompleted(Location data, ErrorCode error) {
//            if (error != ErrorCode.NONE) {
//                // Handle error
//            } else {
//                Log.d("ReverseGeocodeListener", "" + data);
//            }
//        }
//    }

    private SupportMapFragment getSupportMapFragment() {
        return (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment);
    }


    private void initialize() {
        // Search for the map fragment to finish setup by calling init().
        mapFragment = getSupportMapFragment();
        mapFragment.setRetainInstance(true);

        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    // retrieve a reference of the map from the map fragment
                    map = mapFragment.getMap();

                    // Set the map center coordinate to the VietNAm region (no animation)
                    map.setCenter(geoCoordinatecenter, Map.Animation.NONE);
                    // Set the map zoom level to the average between min and max (no animation)
                    map.setZoomLevel((map.getMaxZoomLevel() + map.getMinZoomLevel()) / 2);


                    map.getPositionIndicator().setVisible(true);


                } else {
                    Log.e(LOG_TAG, "Cannot initialize SupportMapFragment (" + error + ")");
                }


                map.addTransformListener(new Map.OnTransformListener() {
                    @Override
                    public void onMapTransformStart() {
//                        Toast.makeText(getApplication(), "onMapTransformStart", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onMapTransformEnd(MapState mapState) {
//                        Toast.makeText(getApplication(), "onMapTransformEnd", Toast.LENGTH_LONG).show();


                    }


                });


// add listener for map gesture
                mapFragment.getMapGesture().addOnGestureListener(new MapGesture.OnGestureListener() {
                    @Override
                    public void onPanStart() {

                    }

                    @Override
                    public void onPanEnd() {
                        Toast.makeText(getApplication(), "onPanEnd", Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onMultiFingerManipulationStart() {

                    }

                    @Override
                    public void onMultiFingerManipulationEnd() {

                    }

                    @Override
                    public boolean onMapObjectsSelected(List<ViewObject> list) {
//                        viewObjects.addAll(list);
                        Log.d("onMapObjectsSelected", "list" + list.size());
                        Toast.makeText(getApplication(), "onMapObjectsSelected", Toast.LENGTH_LONG).show();
                        return false;
                    }

                    @Override
                    public boolean onTapEvent(PointF pointF) {

                        return false;
                    }

                    @Override
                    public boolean onDoubleTapEvent(PointF pointF) {

                        return false;
                    }

                    @Override
                    public void onPinchLocked() {

                    }

                    @Override
                    public boolean onPinchZoomEvent(float v, PointF pointF) {
                        return false;
                    }

                    @Override
                    public void onRotateLocked() {

                    }

                    @Override
                    public boolean onRotateEvent(float v) {
                        return false;
                    }

                    @Override
                    public boolean onTiltEvent(float v) {
                        return false;
                    }

                    @Override
                    public boolean onLongPressEvent(PointF pointF) {
                        if (geoCoordinate != null) {
                            geoCoordinatediemden = map.pixelToGeo(pointF);
                            makeMapMarker(geoCoordinatediemden);

                            Toast.makeText(getApplication(), "geoCoordinatediemden" + geoCoordinatediemden, Toast.LENGTH_LONG).show();

                        } else {
                            geoCoordinate = map.pixelToGeo(pointF);
                            makeMapMarker(geoCoordinate);

                            Toast.makeText(getApplication(), "geoCoordinate" + geoCoordinate, Toast.LENGTH_LONG).show();


                        }


                        return false;
                    }

                    @Override
                    public void onLongPressRelease() {
//                        if (routePlan != null) {
//                            routePlan.removeAllWaypoints();
//                        }
                        routebtn.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public boolean onTwoFingerTapEvent(PointF pointF) {

                        return false;
                    }
                });


                map.getPositionIndicator().setVisible(true);
                final List<String> schemes = map.getMapSchemes();

                map.setMapScheme(schemes.get(0));

//                map.setMapScheme(HYBRID_DAY);
//                map.setMapScheme(SATELLITE_DAY);
                // Array containing string values of all available map schemes
// Assume to select the 2nd map scheme in the available list

//                makeMapMarker();


                mapFragment.getMapGesture();

                switchlocation = findViewById(R.id.switchlocation);
                switchlocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (switchlocation.isChecked()) {
                            testPosition();
                            Toast.makeText(getApplication(), "checked", Toast.LENGTH_LONG).show();
                        } else {
                            if (mPositioningManager != null) {
                                mPositioningManager.removeListener(positionListener);
                            }


                        }
                    }
                });


            }
        });

//        makeMapMarker();
        textViewResult = (TextView) findViewById(R.id.title);
        textViewResult.setText(R.string.textview_routecoordinates_2waypoints);
    }

    private void testPosition() {
        // Define positioning listener

        mPositioningManager = PositioningManager.getInstance();
        mPositioningManager.start(PositioningManager.LocationMethod.GPS);
        mPositioningManager.getLocationStatus(PositioningManager.LocationMethod.GPS);
        mPositioningManager.getPosition();
        positionListener = new
                PositioningManager.OnPositionChangedListener() {
                    public void onPositionUpdated(PositioningManager.LocationMethod method,
                                                  GeoPosition position, boolean isMapMatched) {
                        // set the center only when the app is in the foreground
                        // to reduce CPU consumption
                        map.setCenter(position.getCoordinate(),
                                Map.Animation.NONE);
//                        Toast.makeText(getApplication(),"position"+position.getCoordinate(),Toast.LENGTH_LONG).show();
                    }

                    public void onPositionFixChanged(PositioningManager.LocationMethod method,
                                                     PositioningManager.LocationStatus status) {
                    }
                };
        // Register positioning listener
        mPositioningManager.addListener(new WeakReference<PositioningManager.OnPositionChangedListener>(positionListener));

    }

    private void makeMapMarker(GeoCoordinate geoCoordinate) {

// Create a custom marker image
        com.here.android.mpa.common.Image myImage =
                new com.here.android.mpa.common.Image();

        try {
            myImage.setImageResource(R.drawable.mapmarker11);
        } catch (IOException e) {
            finish();
        }

// Create the MapMarker
        MapMarker myMapMarker =
                new MapMarker(geoCoordinate, myImage);
        map.addMapObject(myMapMarker);
        myMapMarker.setDraggable(true);


// Create a gesture listener and add it to the SupportMapFragment
        MapGesture.OnGestureListener listener =
                new MapGesture.OnGestureListener.OnGestureListenerAdapter() {
                    @Override
                    public boolean onMapObjectsSelected(List<ViewObject> objects) {
                        for (ViewObject viewObj : objects) {
                            if (viewObj.getBaseType() == ViewObject.Type.USER_OBJECT) {
                                if (((MapObject) viewObj).getType() == MapObject.Type.MARKER) {
                                    // At this point we have the originally added
                                    // map marker, so we can do something with it
                                    // (like change the visibility, or more
                                    // marker-specific actions)
                                    ((MapObject) viewObj).setVisible(false);

                                }
                            }
                        }
                        // return false to allow the map to handle this callback also
                        return false;
                    }
                };

    }


    // Functionality for taps of the "Get Directions" button
    public void getDirections(View view) {
        // 1. clear previous results
        textViewResult.setText("");
        if (map != null && mapRoute != null) {
            map.removeMapObject(mapRoute);
            mapRoute = null;
        }

        // 2. Initialize RouteManager

        // 3. Select routing options
        routePlan = new RoutePlan();

        RouteOptions routeOptions = new RouteOptions();
        routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);
        routeOptions.setRouteType(RouteOptions.Type.FASTEST);
        routePlan.setRouteOptions(routeOptions);

        // 4. Select Waypoints for your routes
        // 1st point
//        dialog.show();
        routePlan.addWaypoint(geoCoordinate);

        if (geoCoordinatediemden != null) {
            routePlan.addWaypoint(geoCoordinatediemden);
            routeManager.calculateRoute(routePlan, routeManagerListener);
        }
//
//        // END: Airport, YVR

        // 5. Retrieve Routing information via RouteManagerEventListener
//        RouteManager.Error error = routeManager.calculateRoute(routePlan, routeManagerListener);
//
//
//        if (error != RouteManager.Error.NONE) {
//            Toast.makeText(getApplicationContext(),
//                    "Route calculation failed with: " + error.toString(), Toast.LENGTH_SHORT)
//                    .show();
//        }

    }

    public void chonDiemDen(View view) {

        dialog.cancel();


    }

    private RouteManager.Listener routeManagerListener = new RouteManager.Listener() {
        public void onCalculateRouteFinished(RouteManager.Error errorCode,
                                             List<RouteResult> result) {

            if (errorCode == RouteManager.Error.NONE && result.get(0).getRoute() != null) {
                // create a map route object and place it on the map
                mapRoute = new MapRoute(result.get(0).getRoute());
                map.addMapObject(mapRoute);
                // Get the bounding box containing the route and zoom in (no animation)
                GeoBoundingBox gbb = result.get(0).getRoute().getBoundingBox();
                map.zoomTo(gbb, Map.Animation.NONE, Map.MOVE_PRESERVE_ORIENTATION);

                textViewResult.setText(String.format("Route calculated with %d maneuvers.",
                        result.get(0).getRoute().getManeuvers().size()));
            } else {
                textViewResult.setText(
                        String.format("Route calculation failed: %s", errorCode.toString()));
            }
        }

        public void onProgress(int percentage) {
            textViewResult.setText(String.format("... %d percent done ...", percentage));
        }
    };


    // Set this to PositioningManager.getInstance() upon Engine Initializatio


    // Resume positioning listener on wake up
    public void onResume() {
        super.onResume();
        if (mPositioningManager != null) {
            mPositioningManager.start(
                    PositioningManager.LocationMethod.GPS_NETWORK);
        }
    }

    // To pause positioning listener
    public void onPause() {
        if (mPositioningManager != null) {
            mPositioningManager.stop();
        }
        super.onPause();
    }

    // To remove the positioning listener
    public void onDestroy() {
        if (mPositioningManager != null) {
            // Cleanup
            mPositioningManager.removeListener(
                    positionListener);
        }
        map = null;
        super.onDestroy();
    }


    private class RouteListener implements RouteManager.Listener {

        // Method defined in Listener
        public void onProgress(int percentage) {
            // Display a message indicating calculation progress
        }

        // Method defined in Listener
        public void onCalculateRouteFinished(RouteManager.Error error, List<RouteResult> routeResult) {
            // If the route was calculated successfully
            if (error == RouteManager.Error.NONE) {
                // Render the route on the map
                MapRoute mapRoute = new MapRoute(routeResult.get(0).getRoute());
                map.addMapObject(mapRoute);
            } else {
                // Display a message indicating route calculation failure
            }
        }
    }

    /**
     * Checks the dynamically controlled permissions and requests missing permissions from end user.
     */
    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted
                initialize();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_routing, menu);
        return true;
    }

}
