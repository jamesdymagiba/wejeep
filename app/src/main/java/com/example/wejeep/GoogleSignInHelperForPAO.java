package com.example.wejeep;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;

public class GoogleSignInHelperForPAO {
    private Activity activity;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    public GoogleSignInHelperForPAO(Activity activity) {
        this.activity = activity;
        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(activity, gso);
    }



    public interface SignInCallback {
        void onSignInSuccess(FirebaseUser user);
        void onSignInFailure(Exception e);
    }
}
