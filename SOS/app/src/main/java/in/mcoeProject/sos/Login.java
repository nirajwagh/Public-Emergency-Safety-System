//Login activity for logging in the citizen.

package in.mcoeProject.sos;

//Importing all the required libraries.
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

public class Login extends AppCompatActivity {

    private EditText txt_email1, txt_pass1;

    //FirebaseAuth object for Firebase user authentication.
    private FirebaseAuth firebaseAuth;
    private String email,pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Obtaining all required views of the activity layout.
        txt_email1=findViewById(R.id.txt_email1);
        txt_pass1=findViewById(R.id.txt_pass1);
        firebaseAuth=FirebaseAuth.getInstance();

    }

    //Method for verifying the user entered details.
    public boolean verifyEnteredDetails(){

        //Accessing the inputted email and password by the user.
        email= txt_email1.getText().toString();
        pass= txt_pass1.getText().toString();

        //Checking the validity of the inputted email.
        if(TextUtils.isEmpty(email) || ! android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ){
            txt_email1.setError("Enter valid email address");
            txt_email1.requestFocus();
        }else if(pass.length()<6){
            //If password length is less than 6.
            txt_pass1.setError("Enter valid Password");
            txt_pass1.requestFocus();
        }else {
            return true;
        }
        return false;
    }

    //Method for signing in the user when sign in button pressed.
    public void signInButtonPressed(View v){

        //If the details are valid as per the proper format.
        if(verifyEnteredDetails()){

            //Signing in the user using the FirebaseAuth object.
            firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        //If sign in successful then display the Success message to user.
                        Toast.makeText(Login.this, "Login Success", Toast.LENGTH_SHORT).show();

                        //Intent for redirecting to the Request activity.
                        Intent intent=new Intent(Login.this, Request.class );
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else{

                        //If login failed, display "Failed" message.
                        Toast.makeText(Login.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    //Method to handle the Register button press.
    public void registerButtonPressed(View v){

        //Redirecting to the Register activity.
        Intent intent=new Intent(Login.this, Register.class);
        startActivity(intent);
    }
}
