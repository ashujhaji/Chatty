package lets.digi.talk.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import lets.digi.talk.R;
import lets.digi.talk.util.AdHelper;

public class AlertFragment extends Fragment {
    View.OnClickListener listener;
    AlertFragment(View.OnClickListener listener){
        this.listener = listener;
    }

    private UnifiedNativeAdView nativeAdView;
    private TextView heading,body,negative,positive;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alert,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nativeAdView = view.findViewById(R.id.nativeUnifiedAd);
        heading = view.findViewById(R.id.heading);
        body = view.findViewById(R.id.body_text);
        negative = view.findViewById(R.id.negative);
        positive = view.findViewById(R.id.positive);


        AdHelper.loadAd(getContext(), nativeAdView);

        Bundle bundle = this.getArguments();
        if (bundle!=null){
            heading.setText(bundle.getString("heading"));
            body.setText(bundle.getString("body"));
            positive.setOnClickListener(listener);
        }


        negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStackImmediate();
            }
        });
    }

    public static AlertFragment getInstance(String heading,String body,View.OnClickListener listener){
        AlertFragment fragment = new AlertFragment(listener);
        Bundle bundle = new Bundle();
        bundle.putString("heading",heading);
        bundle.putString("body",body);
        fragment.setArguments(bundle);
        return fragment;
    }

}
