package in.swapsha96.fireapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserDetails {
    public String uid = null;
    public String displayName = null;
    public String email = null;

    public FirebaseDatabase firebaseDatabase;
    public DatabaseReference databaseReference;
    public FirebaseAuth mAuth;
    public FirebaseUser user;

    public UserDetails() {

    }

    public UserDetails(String uid) {
        this.uid = uid;
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        this.displayName = user.getDisplayName();
        this.email = user.getEmail();
    }
    
    String getDisplayName() {
        return this.displayName;
    }

    String getEmail() {
        return this.email;
    }
}
