package lets.digi.talk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import lets.digi.talk.R;
import lets.digi.talk.util.AdHelper;

public class SplashActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fetchRemoteConfigData();
            Log.d("authTag", "User exist");
        } else {
            fetchRemoteConfigData();
            Log.d("authTag", "User not exist");
        }
    }

    private void loginAnonymous() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            moveToNextActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e("auth", "failed");
                        }
                    }
                });
    }

    private void fetchRemoteConfigData() {
        final FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            AdHelper.BANNER_AD_ID = mFirebaseRemoteConfig.getString("banner_ad_id");
                            AdHelper.NATIVE_AD_ID = mFirebaseRemoteConfig.getString("native_ad_id");
                            AdHelper.INTERSTITIAL_AD_ID = mFirebaseRemoteConfig.getString("interstitial_ad_id");
                        }
                        moveToNextActivity();
                    }
                });
    }

    private void moveToNextActivity() {
        //start main activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}
