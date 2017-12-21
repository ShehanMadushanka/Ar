package ng.dat.ar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ARActivity extends AppCompatActivity implements SensorEventListener, LocationListener, AROverlayView.OnPointClickListener {

    public static final int REQUEST_LOCATION_PERMISSIONS_CODE = 0;
    final static String TAG = "ARActivity";
    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 0;//1000 * 60 * 1; // 1 minute
    public Location location;
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    boolean locationServiceAvailable;
    int tar;
    AROverlayView.OnPointClickListener listener;
    ImageView icwifi;
    private SurfaceView surfaceView;
    private FrameLayout cameraContainerLayout;
    private AROverlayView arOverlayView;
    private Camera camera;
    private ARCamera arCamera;
    private TextView tvCurrentLocation;
    private SensorManager sensorManager;
    private LocationManager locationManager;
    private Button btnBackWP;
    private Button btnNextWP;
    private TextView txtDistance;
    private TextView txtName;
    private View view;
    private RelativeLayout.LayoutParams params1;
    private RelativeLayout.LayoutParams params2;
    private RelativeLayout.LayoutParams params3;
    private RelativeLayout.LayoutParams params4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        cameraContainerLayout = (FrameLayout) findViewById(R.id.camera_container_layout);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        tvCurrentLocation = (TextView) findViewById(R.id.tv_current_location);
        arOverlayView = new AROverlayView(this, this);
        icwifi = (ImageView) findViewById(R.id.img_wifi);
        btnNextWP = (Button) findViewById(R.id.btn_next_wp);
        btnBackWP = (Button) findViewById(R.id.btn_back_wp);

        btnNextWP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change();
            }
        });


        btnBackWP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change2();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        requestLocationPermission();
        requestCameraPermission();
        registerSensors();
        initAROverlayView();
    }

    @Override
    public void onPause() {
        releaseCamera();
        super.onPause();
    }

    public void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        } else {
            initARCameraView();
        }
    }

    public void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSIONS_CODE);
        } else {
            initLocationService();
        }
    }

    public void initAROverlayView() {
        if (arOverlayView.getParent() != null) {
            ((ViewGroup) arOverlayView.getParent()).removeView(arOverlayView);
        }
        cameraContainerLayout.addView(arOverlayView);
    }

    public void initARCameraView() {
        reloadSurfaceView();

        if (arCamera == null) {
            arCamera = new ARCamera(this, surfaceView);
        }
        if (arCamera.getParent() != null) {
            ((ViewGroup) arCamera.getParent()).removeView(arCamera);
        }
        cameraContainerLayout.addView(arCamera);
        arCamera.setKeepScreenOn(true);
        initCamera();
    }

    private void initCamera() {
        int numCams = Camera.getNumberOfCameras();
        if (numCams > 0) {
            try {
                camera = Camera.open();
                camera.startPreview();
                arCamera.setCamera(camera);
            } catch (RuntimeException ex) {
                Toast.makeText(this, "Camera not found", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void reloadSurfaceView() {
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }

        cameraContainerLayout.addView(surfaceView);
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            arCamera.setCamera(null);
            camera.release();
            camera = null;
        }
    }

    private void registerSensors() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrixFromVector = new float[16];
            float[] projectionMatrix = new float[16];
            float[] rotatedProjectionMatrix = new float[16];

            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, sensorEvent.values);

            if (arCamera != null) {
                projectionMatrix = arCamera.getProjectionMatrix();
            }

            Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
            this.arOverlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //do nothing
    }

    private void initLocationService() {

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            this.locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

            // Get GPS and network status
            this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isNetworkEnabled && !isGPSEnabled) {
                // cannot get location
                this.locationServiceAvailable = false;
            }

            this.locationServiceAvailable = true;

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    updateLatestLocation();
                }
            }

            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateLatestLocation();
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());

        }
    }

    private void updateLatestLocation() {
        if (arOverlayView != null && location != null) {
            arOverlayView.updateCurrentLocation(location);
            tvCurrentLocation.setText(String.format("lat: %s \nlon: %s \naltitude: %s \n",
                    location.getLatitude(), location.getLongitude(), location.getAltitude()));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        updateLatestLocation();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onClick(String distance, String name) {

        btnBackWP = (Button) findViewById(R.id.btn_back_wp);
        btnNextWP = (Button) findViewById(R.id.btn_next_wp);
        view = findViewById(R.id.main_layout_id);
        txtDistance = (TextView) view.findViewById(R.id.tv_distance);
        txtName = (TextView) view.findViewById(R.id.tv_name);
        txtDistance.setText(distance + "km");
        txtName.setText(name);

        params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params3 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params4 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params1.setMargins(20, 20, 20, 20);
        params2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params2.setMargins(20, 20, 20, 20);

        params3.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params3.addRule(RelativeLayout.ABOVE, R.id.main_layout_id);
        params3.setMargins(20, 0, 0, 0);
        params4.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params4.addRule(RelativeLayout.ABOVE, R.id.main_layout_id);
        params4.setMargins(0, 0, 20, 0);

        if (!distance.equals("") && !name.equals("")) {

            if (view.getVisibility() == View.VISIBLE) {
                Animation bottomDown = AnimationUtils.loadAnimation(ARActivity.this,
                        R.anim.bottom_down);
                view.startAnimation(bottomDown);
                bottomDown.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        btnNextWP.startAnimation(animation);
                        btnBackWP.startAnimation(animation);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        btnBackWP.setLayoutParams(params1);
                        btnNextWP.setLayoutParams(params2);
                        view.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            } else {
                Animation bottomUp = AnimationUtils.loadAnimation(ARActivity.this,
                        R.anim.bottom_up);
                view.startAnimation(bottomUp);
                view.setVisibility(View.VISIBLE);
                btnNextWP.startAnimation(bottomUp);
                btnBackWP.startAnimation(bottomUp);
                btnBackWP.setLayoutParams(params3);
                btnNextWP.setLayoutParams(params4);
            }
        } else {
            if (view.getVisibility() == View.VISIBLE) {
                Animation bottomDown = AnimationUtils.loadAnimation(ARActivity.this,
                        R.anim.bottom_down);
                view.startAnimation(bottomDown);
                bottomDown.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        btnNextWP.startAnimation(animation);
                        btnBackWP.startAnimation(animation);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        btnBackWP.setLayoutParams(params1);
                        btnNextWP.setLayoutParams(params2);
                        view.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        }

    }

    private void change() {

        @SuppressLint("RestrictedApi") final ContextThemeWrapper wrapper = new ContextThemeWrapper(this, R.style.DefaultScene);
        @SuppressLint("RestrictedApi") Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_wifi, wrapper.getTheme());
        icwifi.setImageDrawable(drawable);
//
//        theme.applyStyle(R.style.BaubleSmall, false);
//
//        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_wifi, theme);
//        icwifi.setImageDrawable(drawable);

    }

    private void change2() {

        @SuppressLint("RestrictedApi") final ContextThemeWrapper wrapper = new ContextThemeWrapper(this, R.style.UpdatedScene);
        @SuppressLint("RestrictedApi") Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_wifi, wrapper.getTheme());
        icwifi.setImageDrawable(drawable);
//
//        theme.applyStyle(R.style.BaubleSmall, false);
//
//        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_wifi, theme);
//        icwifi.setImageDrawable(drawable);

    }
}
