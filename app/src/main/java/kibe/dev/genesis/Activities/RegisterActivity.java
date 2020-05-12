package kibe.dev.genesis.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import kibe.dev.genesis.R;

public class RegisterActivity extends AppCompatActivity {

    CircleImageView imgUserPhoto;
    static int PReqCode = 1;
    static int REQUESTCODE = 1;
    Uri pickedImgUri;
    String myUrl = "";

    private EditText userName, userMail, userPassword, userPassword2;
    private ProgressBar loadingProgress;
    private Button regBtn;
    private TextView logIn;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //init Views

        imgUserPhoto = findViewById(R.id.regUserPhoto);
        userName = findViewById(R.id.regName);
        userMail = findViewById(R.id.regEmail);
        userPassword = findViewById(R.id.regPassword);
        userPassword2 = findViewById(R.id.regPassword2);
        loadingProgress = findViewById(R.id.progressBar);
        regBtn = findViewById(R.id.regBtn);
        loadingProgress.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        logIn = findViewById(R.id.logIn);
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });


        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                regBtn.setVisibility(View.INVISIBLE);
                loadingProgress.setVisibility(View.VISIBLE);
                final String name = userName.getText().toString();
                final String email = userMail.getText().toString();
                final String password = userPassword.getText().toString();
                final String password2 = userPassword2.getText().toString();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || password2.isEmpty() || !password.equals(password2)) {
                    Toast.makeText(RegisterActivity.this, "Verify all fields", Toast.LENGTH_SHORT).show();
                    regBtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);
                } else if (password.length() <6 || password2.length() <6) {
                    Toast.makeText(RegisterActivity.this, "Password must be 6 characters", Toast.LENGTH_SHORT).show();
                } else {
                    // when everything is well
                    CreateUserAccount(name,email,password);
                }


            }
        });

        imgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT>=32) {
                    checkAndRequestForPermission();

                } else {

                    CropImage.activity()
                            .setAspectRatio(1, 1)
                            .start(RegisterActivity.this);
                    //openGallery();
                }

            }
        });

    }

    private void CreateUserAccount(final String username, final String email, String password) {

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User account created successfully
                            //Toast.makeText(RegisterActivity.this, "Account Created.", Toast.LENGTH_SHORT).show();
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            String userid = firebaseUser.getUid();

                            mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("username", "@" + username.toLowerCase());
                            hashMap.put("email", email);
                            hashMap.put("bio", "");
                            hashMap.put("imageUrl", pickedImgUri.toString());

                            mDatabaseRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        loadingProgress.setVisibility(View.INVISIBLE);
                                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);

                                    }
                                }
                            });
                            
                            updateUserInfo(username, pickedImgUri,mAuth.getCurrentUser());
                            
                            
                        } else {
                            // Failed to create account
                            Toast.makeText(RegisterActivity.this, "Failed to create account",Toast.LENGTH_SHORT).show();
                            regBtn.setVisibility(View.VISIBLE);
                            loadingProgress.setVisibility(View.INVISIBLE);
                        }
                    }
                });


    }

    //Update users photo and name
    private void updateUserInfo(final String name, Uri pickedImgUri, final FirebaseUser currentUser) {
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference().child("Users_photos");
        final StorageReference imgFilePath = mStorageRef.child(pickedImgUri.getLastPathSegment());
        imgFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                // Image uploaded successfully
                imgFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();

                        currentUser.updateProfile(profileUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this, "Registration complete", Toast.LENGTH_SHORT).show();
                                            updateUI();
                                        }
                                    }
                                });
                    }
                });


            }
        });
    }

    private void updateUI() {

        Intent homeActivity = new Intent(getApplicationContext(),HomeActivity.class);
        startActivity(homeActivity);
        finish();
    }


    private void openGallery() {
        //TODO: open gallery intent and wait for user to pick image !

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,REQUESTCODE);
    }

    private void checkAndRequestForPermission() {

        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(RegisterActivity.this, "Please accept the required permission", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(RegisterActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode);
            }
        } else {
            openGallery();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            pickedImgUri = result.getUri();

            //when the user has picked an image
            //pickedImgUri = data.getData();
            imgUserPhoto.setImageURI(pickedImgUri);

        }
    }
}
