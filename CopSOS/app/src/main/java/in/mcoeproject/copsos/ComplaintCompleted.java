//Activity for displaying the complaint completed status.

package in.mcoeproject.copsos;

//Importing all required libraries.
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;

public class ComplaintCompleted extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_completed);

        //Playing the Completed sound.
        playCompletedSound();

        //Using Handler class for delaying the execution for redirecting to the Status activity.
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //Redirecting to the Status activity.
                Intent intent=new Intent(ComplaintCompleted.this, Status.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }, 5500);   //Delay of 5.5 seconds.
    }

    //Method for playing the complaint completed sound.
    private void playCompletedSound() {

        //Playing the media sound "completed" from the raw resource folder using MediaPlayer class.
        MediaPlayer mediaPlayer=MediaPlayer.create(this, R.raw.completed);
        mediaPlayer.start();
    }

}
