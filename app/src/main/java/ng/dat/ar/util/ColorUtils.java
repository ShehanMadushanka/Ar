package ng.dat.ar.util;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;

/**
 * Created by Shehan Madushanka on 12/21/2017.
 * Project: ar-location-based-android-master
 */

public class ColorUtils {

    private Paint paint;
    private PorterDuffColorFilter filter;
    private PorterDuffColorFilter filter2;
    private PorterDuffColorFilter filter3;


    public void setColor(Colors color) {

        filter = new PorterDuffColorFilter(Color.CYAN, PorterDuff.Mode.SRC_IN);
        filter2 = new PorterDuffColorFilter(Color.MAGENTA, PorterDuff.Mode.SRC_IN);
        filter3 = new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);

        switch (color) {
            case BLUE:
                paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.WHITE);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                paint.setTextSize(40);
                break;
            case CYAN:
                break;
            case GREEN:
                break;
            case WHITE:
                paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.WHITE);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                paint.setTextSize(40);
                break;

        }
    }

    public static enum Colors {
        WHITE, GREEN, CYAN, BLUE, BITMAP
    }
}
