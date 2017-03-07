package in.swapsha96.fireapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private String TAG = "123";
    EditText mDisplayName, mEmail, mPassword;
    Button mSignUp;
    ProgressDialog progress;
    private FirebaseAuth mAuth;
    UserDetails details;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mDisplayName = (EditText) findViewById(R.id.display_name);
        mSignUp = (Button) findViewById(R.id.sign_up);
        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String displayName = mDisplayName.getText().toString().trim();
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if (TextUtils.isEmpty(displayName)) {
                    mDisplayName.setError("Enter display name.");
                    return;
                }
                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Enter email ID.");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Enter password.");
                    return;
                }
                progress = ProgressDialog.show(SignUpActivity.this, "Sign Up","Signing Up.", true);
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Hide progress dialog
                        progress.dismiss();
                        if(!task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        FirebaseUser mUser = mAuth.getCurrentUser();
                        if(mUser != null) {
                            //Add display name
                            String displayName = mDisplayName.getText().toString();
                            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(displayName).build();
                            mUser.updateProfile(userProfileChangeRequest);
                            //Email  Verification
                            String email = mUser.getEmail();
                            mUser.sendEmailVerification();
                            Toast.makeText(SignUpActivity.this, "Verification mail has been sent to " + email + ".", Toast.LENGTH_LONG).show();
                            //Add user in database
                            String uid = mUser.getUid();
                            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                            DatabaseReference user = firebaseDatabase.getReference(uid);
                            user.setValue(displayName);
                            details = new UserDetails(uid);
                            user.child("details").setValue(details);
                            //Go to main activity
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            finish();
                        }
                    }
                });
            }
        });
    }
}
