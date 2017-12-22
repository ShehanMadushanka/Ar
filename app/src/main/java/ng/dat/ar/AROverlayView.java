package ng.dat.ar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import ng.dat.ar.helper.LocationHelper;
import ng.dat.ar.model.ARPoint;

/**
 * Created by ntdat on 1/13/17.
 */

public class AROverlayView extends View {

    private final Bitmap bitmap;
    Canvas canvas;
    Context context;
    String distanceStr;
    String[] array = new String[10];
    ColorFilter filter;
    ColorFilter filter2;
    ColorFilter filter3;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location currentLocation;
    private List<ARPoint> arPoints;
    private float[][] pointsXY;
    private View view;
    private OnPointClickListener listener;

    public AROverlayView(Context context, OnPointClickListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        arPoints = new ArrayList<ARPoint>() {{
            add(new ARPoint("Loc 1", 6.7026043, 80.3627333, -30.5508425094059));
            add(new ARPoint("Saman Dewalaya", 6.6899268, 80.380158, -30.8420074));
            add(new ARPoint("Kahangama", 6.7003906, 80.3637062, -30.8420074));
            add(new ARPoint("Rathnapura", 6.704006, 80.3671733, -30.8420074));
            add(new ARPoint("Pothgul Viharaya", 6.6807346, 80.38044, -30.8420074));
            add(new ARPoint("Karangoda Vidyalaya", 6.6835611, 80.3735109, -30.8420074));
            add(new ARPoint("Cisco Tower", 6.9076323, 79.9448937, -30.8420074));
            add(new ARPoint("Mondy", 6.9021151, 79.9459666, -30.8420074));
            add(new ARPoint("Dialog Arcade", 6.9160039, 79.9437779, -30.8420074));
            add(new ARPoint("SLT Public", 6.892868, 79.9293659, -30.8420074));

            //6.7026043,80.3627333 || 6.6899268,80.380158 || 6.7003906,80.3637062 || 6.704006,80.3671733 || 6.6807346,80.38044 || 6.6835611,80.3735109
        }};

//        Drawable sourceDrawable = getResources().getDrawable(R.drawable.ic_location);
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_location);
        filter = new PorterDuffColorFilter(Color.CYAN, PorterDuff.Mode.SRC_IN);
        filter2 = new PorterDuffColorFilter(Color.MAGENTA, PorterDuff.Mode.SRC_IN);
        filter3 = new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
        view = findViewById(R.id.main_layout_id);
    }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
        this.invalidate();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        this.canvas = canvas;

        pointsXY = new float[arPoints.size()][5];

        if (currentLocation == null) {
            return;
        }

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(40);

        Paint paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setStrokeWidth(5);
        paint1.setColor(Color.GREEN);

        Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setColor(Color.WHITE);
        paint2.setTextSize(30);

        Paint paint3 = new Paint();
        paint3.setAntiAlias(true);
        paint3.setFilterBitmap(true);
        paint3.setDither(true);
        paint3.setColorFilter(filter);

        Paint paint4 = new Paint();
        paint4.setAntiAlias(true);
        paint4.setFilterBitmap(true);
        paint4.setDither(true);
        paint4.setColorFilter(filter2);

        Paint paint5 = new Paint();
        paint5.setAntiAlias(true);
        paint5.setFilterBitmap(true);
        paint5.setDither(true);
        paint5.setColorFilter(filter3);

        //center
        int x0 = canvas.getWidth() / 2;
        int y0 = canvas.getHeight() / 2;
        int dx = canvas.getWidth() / 3;
        int dy = canvas.getHeight() / 3;

        for (int i = 0; i < arPoints.size(); i++) {
            float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
            float[] pointInECEF = LocationHelper.WSG84toECEF(arPoints.get(i).getLocation());
            float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);
            array[i] = getDistance(arPoints.get(i));

            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

            if (cameraCoordinateVector[2] < 0 && Float.parseFloat(getDistance(arPoints.get(i))) < 50f) {
                float x = (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * canvas.getWidth();
                float y = (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * canvas.getHeight();

                pointsXY[i][0] = x;
                pointsXY[i][1] = y;

                canvas.drawText(arPoints.get(i).getName(), x - (5 * arPoints.get(i).getName().length()), y - 30, paint);
                canvas.drawText(arPoints.get(i).getDistance() + " km", x - (15 * arPoints.get(i).getDistance().length()), y - 1, paint2);
                canvas.drawLine(540, 1250, x, y, paint1);

                if (Float.parseFloat(getDistance(arPoints.get(i))) < 200f) {
                    canvas.drawBitmap(Bitmap.createScaledBitmap(bitmap, 200, 200, false), x - (15 * arPoints.get(i).getDistance().length()), y - 250, paint4);
                } else if (Float.parseFloat(getDistance(arPoints.get(i))) < 3f) {
                    canvas.drawBitmap(Bitmap.createScaledBitmap(bitmap, 150, 150, false), x - (15 * arPoints.get(i).getDistance().length()), y - 200, paint5);
                } else {
                    canvas.drawBitmap(Bitmap.createScaledBitmap(bitmap, 100, 100, false), x - (15 * arPoints.get(i).getName().length()), y - 150, paint3);
                }

            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        super.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();

        for (int i = 0; i < pointsXY.length; i++) {
            float xPoint = pointsXY[i][0];
            float yPoint = pointsXY[i][1];
            if (x > xPoint - 100 && x < xPoint + 100) {
                if (y > yPoint - 100 && y < yPoint + 100) {
//                    Toast.makeText(context, "Distance: " + array[i], Toast.LENGTH_SHORT).show();
                    listener.onClick(array[i], arPoints.get(i).getName());
                }
//                else {
//                    listener.onClick("", "");
//                }
            }
//            else {
//                if ()
//
//                listener.onClick("", "");
//            }
        }
        return false;
    }

    private void drawLine(float x, float y) {

        Paint paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setStrokeWidth(5);
        paint1.setColor(Color.GREEN);

        canvas.drawLine(540, 1250, x, y, paint1);
    }

    public String getDistance(ARPoint point) {

        float[] results = new float[2];
        android.location.Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), point.getLocation().getLatitude(), point.getLocation().getLongitude(), results);
        float distanceFloat = Float.parseFloat(new DecimalFormat("##.##").format(results[0]));

        if (distanceFloat > 1000) {
            Float final_distance = Float.parseFloat(new DecimalFormat("##.##").format(distanceFloat / 1000));
            distanceStr = Float.toString(final_distance);
        } else {
            distanceStr = Float.toString(Math.round(distanceFloat));
        }

        point.setDistance(distanceStr);
        return distanceStr;
    }

    public interface OnPointClickListener {
        void onClick(String distance, String name);
    }
}
