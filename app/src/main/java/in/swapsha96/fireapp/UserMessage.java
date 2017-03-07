package in.swapsha96.fireapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UserMessage {
    public String from = null;
    public String to = null;
    public String message = null;
    public String date = null;
    public String time = null;

    public FirebaseDatabase firebaseDatabase;
    public DatabaseReference databaseReference;
    public FirebaseAuth mAuth;
    public FirebaseUser mUser;

    public UserMessage() {

    }

    public UserMessage(String from, String to, String message) {
        this.from = from;
        this.to = to;
        this.message = message;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat simpleDateFormatTime = new SimpleDateFormat("HH:mm:ss");
        this.date = simpleDateFormatDate.format(c.getTime());
        this.time = simpleDateFormatTime.format(c.getTime());
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
    }

    public String getFrom() {
        return this.from;
    }
    public String getTo() {
        return this.to;
    }
    public String getMessage() {
        return this.message;
    }
    public String getDate() {
        return this.date;
    }
    public String getTime() {
        return this.time;
    }
}
