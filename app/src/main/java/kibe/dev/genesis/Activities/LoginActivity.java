package kibe.dev.genesis.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import kibe.dev.genesis.R;

public class LoginActivity extends AppCompatActivity {

    private EditText userMail, userPassword;
    private Button loginBtn;
    private ProgressBar loginProgress;
    private FirebaseAuth mAuth;
    private Intent HomeActivity;
    private ImageView loginPhoto;
    private TextView signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userMail = findViewById(R.id.login_email);
        userPassword = findViewById(R.id.login_password);
        loginBtn = findViewById(R.id.login_btn);
        loginProgress = findViewById(R.id.progressBar);
        loginPhoto = findViewById(R.id.login_photo);
        mAuth = FirebaseAuth.getInstance();
        HomeActivity = new Intent(this, kibe.dev.genesis.Activities.HomeActivity.class);
        signUp = findViewById(R.id.createAcc);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUp = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(signUp);
                finish();
            }
        });

        loginProgress.setVisibility(View.INVISIBLE);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginProgress.setVisibility(View.VISIBLE);
                loginBtn.setVisibility(View.INVISIBLE);

                final String mail = userMail.getText().toString();
                final String password = userPassword.getText().toString();

                if (mail.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please verify all fields", Toast.LENGTH_SHORT).show();
                    loginBtn.setVisibility(View.VISIBLE);
                    loginProgress.setVisibility(View.INVISIBLE);
                } else {
                    signIn(mail,password);
                }
            }
        });


    }

    private void signIn(String mail, String password) {

        mAuth.signInWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {


                    loginProgress.setVisibility(View.INVISIBLE);
                    loginBtn.setVisibility(View.VISIBLE);
                    updateUI();
                } else {
                    Toast.makeText(LoginActivity.this, "Fill all fields correctly````````+", Toast.LENGTH_SHORT).show();
                    loginBtn.setVisibility(View.VISIBLE);
                    loginProgress.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    private void updateUI() {

        startActivity(HomeActivity);
        finish();

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            updateUI();
        }
    }
}
