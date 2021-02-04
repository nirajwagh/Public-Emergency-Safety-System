//Activity for Logging in the user.
package in.mcoeproject.copsos;

//Importing all required libraries.
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    //Declaring all required objects.
    private EditText txt_email1, txt_pass1;
    private String email,pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Obtaining the views from the layout file.
        txt_email1=findViewById(R.id.txt_email1);
        txt_pass1=findViewById(R.id.txt_pass1);

    }

    //Method to be called when on Sign In button pressed.
    public void signInButtonPressed(View v){

        //Obtaining the instance of Firebase authentication service.
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

        //Validating all user entered details and logging in id data is valid.
        if(verifyEnteredDetails()){

            //If entered details are valid, then log in the user using Firebase Authentication service.
            firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        //If user logging in success, then display success message and redirect to Status activity.
                        Toast.makeText(MainActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(MainActivity.this, Status.class );
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else{

                        //If Logging in failed then display the failed toast message.
                        Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    //Method to validate user entered data.
    public boolean verifyEnteredDetails(){
        email= txt_email1.getText().toString();
        pass= txt_pass1.getText().toString();

        //Validating email using android Patterns matcher and password by length.
        if(TextUtils.isEmpty(email) || ! android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ){
            txt_email1.setError("Enter valid email address");
            txt_email1.requestFocus();
        }else if(pass.length()<6){
            txt_pass1.setError("Enter valid Password");
            txt_pass1.requestFocus();
        }else {
            return true;
        }
        return false;
    }

    //Method to be called when register button pressed.
    public void registerButtonPressed(View v){

        //Redirecting to Register activity.
        Intent intent=new Intent(MainActivity.this, Register.class);
        startActivity(intent);
    }
}
