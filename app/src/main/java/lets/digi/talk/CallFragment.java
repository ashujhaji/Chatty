package lets.digi.talk;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

/**
 * Fragment for call control.
 */
public class CallFragment extends Fragment {
    private View controlView;
    private ImageButton disconnectButton;
    private ImageView toggleMuteButton;
    private ImageView toggleSpeaker;
    private TextView captureFormatText;
    private SeekBar captureFormatSlider;
    private OnCallEvents callEvents;
    private boolean videoCallEnabled = true;

    /**
     * Call control interface for container activity.
     */
    public interface OnCallEvents {
        void onCallHangUp();

        void onCaptureFormatChange(int width, int height, int framerate);

        boolean onToggleMic();

        boolean onToggleSpeaker();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        controlView = inflater.inflate(R.layout.fragment_call, container, false);

        // Create UI controls.
        disconnectButton = (ImageButton) controlView.findViewById(R.id.button_call_disconnect);
        toggleMuteButton = controlView.findViewById(R.id.button_call_toggle_mic);
        toggleSpeaker = controlView.findViewById(R.id.button_call_toggle_speaker);
        captureFormatText = (TextView) controlView.findViewById(R.id.capture_format_text_call);
        captureFormatSlider = (SeekBar) controlView.findViewById(R.id.capture_format_slider_call);

        // Add buttons click events.
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callEvents.onCallHangUp();
            }
        });

        toggleMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean enabled = callEvents.onToggleMic();
                toggleMuteButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), enabled ? R.drawable.ic_mic : R.drawable.ic_mic_mute, null));
//                toggleMuteButton.setAlpha(enabled ? 1.0f : 0.3f);
            }
        });

        toggleSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enabled = callEvents.onToggleSpeaker();
                toggleSpeaker.setImageDrawable(ResourcesCompat.getDrawable(getResources(), enabled ? R.drawable.ic_sound : R.drawable.ic_mute, null));
//                toggleSpeaker.setAlpha(enabled ? 1.0f : 0.3f);
            }
        });

        return controlView;
    }

    @Override
    public void onStart() {
        super.onStart();

        boolean captureSliderEnabled = false;
        Bundle args = getArguments();
        if (args != null) {
            String contactName = args.getString(CallActivity.EXTRA_ROOMID);
            videoCallEnabled = args.getBoolean(CallActivity.EXTRA_VIDEO_CALL, true);
            captureSliderEnabled = videoCallEnabled
                    && args.getBoolean(CallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, false);
        }
        if (captureSliderEnabled) {
            captureFormatSlider.setOnSeekBarChangeListener(
                    new CaptureQualityController(captureFormatText, callEvents));
        } else {
            captureFormatText.setVisibility(View.GONE);
            captureFormatSlider.setVisibility(View.GONE);
        }
    }

    // TODO(sakal): Replace with onAttach(Context) once we only support API level 23+.
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callEvents = (OnCallEvents) activity;
    }
}
