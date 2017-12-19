package ng.dat.ar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.opengl.Matrix;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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

    Context context;
    String distanceStr;
    String[] array = new String[6];
    Button b;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location currentLocation;
    private List<ARPoint> arPoints;
    private float[][] pointsXY;
    private final Bitmap bitmap;
    private final Rect rectangle;


    public AROverlayView(Context context) {
        super(context);
        this.context = context;

        arPoints = new ArrayList<ARPoint>() {{
//            add(new ARPoint("Loc 1", 6.91642923980966, 79.8887523036408, -30.5508425094059));
//            add(new ARPoint("Loc 2", 6.92642923980966, 79.86687523036408, -30.8420074));
//            add(new ARPoint("Loc 3", 6.93642923980966, 79.8687523036408, -30.8420074));
//            add(new ARPoint("Loc 4", 6.94642923980966, 79.8687523036408, -30.8420074));
//            add(new ARPoint("Loc 5", 6.95642923980966, 79.86687523036408, -30.8420074));
            add(new ARPoint("Loc 5", 6.9076323, 79.9448937, -30.8420074));
            add(new ARPoint("Loc 5", 6.9076536, 79.944765, -30.8420074));
            add(new ARPoint("Loc 5", 6.9021151, 79.9459666, -30.8420074));
            add(new ARPoint("Loc 5", 6.9160039, 79.9437779, -30.8420074));
            add(new ARPoint("Loc 5", 6.9325084, 79.966771, -30.8420074));
            add(new ARPoint("Loc 5", 6.892868, 79.9293659, -30.8420074));

            //6.9076323,79.9448937 || 6.9076536,79.944765,15z || 6.9021151,79.9459666,15 || 6.9160039,79.9437779,15 || 6.9325084,79.966771 || 6.9052548,79.9286577 || 6.9030426,79.9266639 || 6.892868,79.9293659
        }};

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_location);
        rectangle = new Rect(0,0,100,100);
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

//        b = new Button(getContext());
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//        b.setLayoutParams(params);
//        b.setGravity(Gravity.CENTER_HORIZONTAL);
//        b.setTextSize(32);
//        b.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(context, "Button clicked!", Toast.LENGTH_SHORT).show();
//            }
//        });
//        b.draw(canvas);

        pointsXY = new float[arPoints.size()][5];

        if (currentLocation == null) {
            return;
        }

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(60);

        Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setColor(Color.CYAN);
        paint2.setTextSize(30);

        Paint paint3 = new Paint();
        paint3.setAntiAlias(true);
        paint3.setFilterBitmap(true);
        paint3.setDither(true);


        //center
        int x0 = canvas.getWidth()/2;
        int y0 = canvas.getHeight()/2;
        int dx = canvas.getWidth()/3;
        int dy = canvas.getHeight()/3;
        //draw guide box
        canvas.drawRect(x0-dx, y0-dy, x0+dx, y0+dy, paint2);

//        Rect rect = new Rect();
//        rect.set(100, 100, 300, 300);
//
//        //Make a new view and lay it out at the desired Rect dimensions
//        Button view = new Button(getContext());
//        view.setText("This is a custom drawn textview");
//        view.setBackgroundColor(Color.TRANSPARENT);
//        view.setGravity(Gravity.CENTER);
//        view.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(context, "Clicked!", Toast.LENGTH_SHORT).show();
//            }
//        });
//        //Measure the view at the exact dimensions (otherwise the text won't center correctly)
//        int widthSpec = View.MeasureSpec.makeMeasureSpec(rect.width(), View.MeasureSpec.EXACTLY);
//        int heightSpec = View.MeasureSpec.makeMeasureSpec(rect.height(), View.MeasureSpec.EXACTLY);
//        view.measure(widthSpec, heightSpec);
//
//        //Lay the view out at the rect width and height
//        view.layout(0, 0, rect.width(), rect.height());
//
//        //Translate the Canvas into position and draw it
//        canvas.save();
//        canvas.translate(rect.centerX(), rect.centerY());
//        view.draw(canvas);

        for (int i = 0; i < arPoints.size(); i++) {
            float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
            float[] pointInECEF = LocationHelper.WSG84toECEF(arPoints.get(i).getLocation());
            float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);
            array[i] = getDistance(arPoints.get(i));

            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

            // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
            // if z > 0, the point will display on the opposite
            if (cameraCoordinateVector[2] < 0 && Float.parseFloat(array[i]) < 50f) {
                float x = (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * canvas.getWidth();
                float y = (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * canvas.getHeight();

                pointsXY[i][0] = x;
                pointsXY[i][1] = y;

//                canvas.drawText(arPoints.get(i).getName(), x - (15 * arPoints.get(i).getName().length()), y - 80, paint);
                canvas.drawText(arPoints.get(i).getDistance() + " km", x - (15 * arPoints.get(i).getDistance().length()), y - 60, paint2);
                canvas.drawBitmap(bitmap, x - (15 * arPoints.get(i).getName().length()), y - 40, paint3);
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
                    Toast.makeText(context, "Distance: " + array[i], Toast.LENGTH_SHORT).show();
                }
            }
        }
        return false;
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
}
