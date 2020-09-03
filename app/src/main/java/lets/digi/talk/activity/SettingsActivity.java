package lets.digi.talk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import lets.digi.talk.R;
import lets.digi.talk.util.Constant;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private TextView rateUs;
    private TextView share;
    private TextView termOfUses;
    private TextView privacyPolicy;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        init();
        setToolbar();



    }


    private void init(){
        toolbar = findViewById(R.id.toolbar);
        rateUs = findViewById(R.id.rate_us);
        share = findViewById(R.id.share);
        termOfUses = findViewById(R.id.term_of_uses);
        privacyPolicy = findViewById(R.id.privacy_policy);

        rateUs.setOnClickListener(this);
        share.setOnClickListener(this);
        termOfUses.setOnClickListener(this);
        privacyPolicy.setOnClickListener(this);
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rate_us:{
                Constant.openPlaystore(this);
            }
            case R.id.share:{
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String shareText = "Getting bored? Chat with new friends and make some fun. Download the app now. https://play.google.com/store/apps/details?id=" + getPackageName();
                intent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(intent, "Share via"));
            }
            case R.id.term_of_uses:{

            }
            case R.id.privacy_policy:{

            }
        }
    }
}
