//Java class for handling the device vibration on notification.

package in.mcoeproject.copsos;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class VibrationHelper {
    static long[] mVibratePattern;
    static Vibrator vibrator;
    public static void  vibrate(Context context, int pattern){
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if(pattern==1){
            mVibratePattern = new long[]{0, 500, 1000, 700, 1000, 900, 1000, 1200};
        }else if(pattern==2){
            mVibratePattern = new long[]{0, 1000, 1000, 1000, 1000, 1000, 1000, 1000};
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect effect = VibrationEffect.createWaveform(mVibratePattern, -1);
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(mVibratePattern, -1);
        }
    }

    public static void stopVibrate(Context context){
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.cancel();
    }
}
