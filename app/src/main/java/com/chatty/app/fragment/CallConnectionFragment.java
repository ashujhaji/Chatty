package com.chatty.app.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chatty.app.util.Constant;
import com.chatty.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CallConnectionFragment extends Fragment {

    private static String TAG = CallConnectionFragment.class.getSimpleName();
    private boolean isChatActive = false;
    private String chatId = "";
    private DatabaseReference mDatabaseRef;
    private SharedPreferences sharedPref;
    private String keyprefVideoCallEnabled;
    private String keyprefScreencapture;
    private String keyprefCamera2;
    private String keyprefResolution;
    private String keyprefFps;
    private String keyprefCaptureQualitySlider;
    private String keyprefVideoBitrateType;
    private String keyprefVideoBitrateValue;
    private String keyprefVideoCodec;
    private String keyprefAudioBitrateType;
    private String keyprefAudioBitrateValue;
    private String keyprefAudioCodec;
    private String keyprefHwCodecAcceleration;
    private String keyprefCaptureToTexture;
    private String keyprefNoAudioProcessingPipeline;
    private String keyprefAecDump;
    private String keyprefOpenSLES;
    private String keyprefDisableBuiltInAec;
    private String keyprefDisableBuiltInAgc;
    private String keyprefDisableBuiltInNs;
    private String keyprefEnableLevelControl;
    private String keyprefDisplayHud;
    private String keyprefTracing;
    private String keyprefRoomServerUrl;
    private String keyprefRoom;
    private String keyprefRoomList;
    private ArrayList<String> roomList;
    private ArrayAdapter<String> adapter;
    private String keyprefEnableDataChannel;
    private String keyprefOrdered;
    private String keyprefMaxRetransmitTimeMs;
    private String keyprefMaxRetransmits;
    private String keyprefDataProtocol;
    private String keyprefNegotiated;
    private String keyprefDataId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_call_connection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        checkForAvailableChat();
    }

    private void checkForAvailableChat() {
        DatabaseReference mRef = mDatabaseRef.child("ongoing_calls");
        mRef.orderByChild("status")
                .equalTo("waiting").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() > 0) {
                    //Chat room available
                    for (DataSnapshot chat : snapshot.getChildren()) {
                        chatId = chat.getKey();
                        String roomId = chat.child("room_id").getValue().toString();
                        addToChat(chatId,roomId);
                        break;
                    }
                } else {
                    //No chat room available. Create new one
                    createNewChat();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createNewChat() {
        DatabaseReference mRef = mDatabaseRef.child("ongoing_calls");
        chatId = UUID.randomUUID().toString();
        final String roomId = Constant.getRoomId();
        Map<String, String> message = new HashMap<>();
        message.put("room_id",roomId);
        message.put("status", "waiting");
        mRef.child(chatId).setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listenForStatusChange(chatId,roomId);
                Log.d(TAG, "chat created");
            }
        });
    }

    private void removeCall(String chatId) {
        if (!chatId.isEmpty()) {
            DatabaseReference mRef = mDatabaseRef.child("ongoing_calls").child(chatId);
            Map<String, String> message = new HashMap<>();
            message.put("status", "finish");
            mRef.setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
//                    getActivity().getSupportFragmentManager().popBackStackImmediate();
                }
            });
        }
    }

    private void listenForStatusChange(final String chat_Id,final String roomId) {
        DatabaseReference mRef = mDatabaseRef.child("ongoing_calls").child(chat_Id).child("status");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    String status = snapshot.getValue().toString();
                    if (status.contentEquals("waiting")) {
                        Log.d(TAG, "waiting");
                    } else if (status.contentEquals("ongoing")) {
                        isChatActive = true;
                        sendForCallConnection(roomId);
                        //  listenForMessages(chatId);
                    } else if (status.contentEquals("finish")) {
                        isChatActive = false;
                        Log.d(TAG, "finish");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "User added");
            }
        });
    }

    private void addToChat(final String chatId,final String roomId) {
        DatabaseReference mRef = mDatabaseRef.child("ongoing_calls").child(chatId);
        Map<String, String> message = new HashMap<>();
        message.put("status", "ongoing");
        message.put("room_id",roomId);
        mRef.setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                isChatActive = true;
                sendForCallConnection(roomId);
                Log.d(TAG, "chat created");
            }
        });
    }

    private void sendForCallConnection(String roomId) {
        // Get setting keys.
        PreferenceManager.setDefaultValues(getContext(), R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        keyprefVideoCallEnabled = getString(R.string.pref_videocall_key);
        keyprefScreencapture = getString(R.string.pref_screencapture_key);
        keyprefCamera2 = getString(R.string.pref_camera2_key);
        keyprefResolution = getString(R.string.pref_resolution_key);
        keyprefFps = getString(R.string.pref_fps_key);
        keyprefCaptureQualitySlider = getString(R.string.pref_capturequalityslider_key);
        keyprefVideoBitrateType = getString(R.string.pref_maxvideobitrate_key);
        keyprefVideoBitrateValue = getString(R.string.pref_maxvideobitratevalue_key);
        keyprefVideoCodec = getString(R.string.pref_videocodec_key);
        keyprefHwCodecAcceleration = getString(R.string.pref_hwcodec_key);
        keyprefCaptureToTexture = getString(R.string.pref_capturetotexture_key);
        keyprefAudioBitrateType = getString(R.string.pref_startaudiobitrate_key);
        keyprefAudioBitrateValue = getString(R.string.pref_startaudiobitratevalue_key);
        keyprefAudioCodec = getString(R.string.pref_audiocodec_key);
        keyprefNoAudioProcessingPipeline = getString(R.string.pref_noaudioprocessing_key);
        keyprefAecDump = getString(R.string.pref_aecdump_key);
        keyprefOpenSLES = getString(R.string.pref_opensles_key);
        keyprefDisableBuiltInAec = getString(R.string.pref_disable_built_in_aec_key);
        keyprefDisableBuiltInAgc = getString(R.string.pref_disable_built_in_agc_key);
        keyprefDisableBuiltInNs = getString(R.string.pref_disable_built_in_ns_key);
        keyprefEnableLevelControl = getString(R.string.pref_enable_level_control_key);
        keyprefDisplayHud = getString(R.string.pref_displayhud_key);
        keyprefTracing = getString(R.string.pref_tracing_key);
        keyprefRoomServerUrl = getString(R.string.pref_room_server_url_key);
        keyprefRoom = getString(R.string.pref_room_key);
        keyprefRoomList = getString(R.string.pref_room_list_key);
        keyprefEnableDataChannel = getString(R.string.pref_enable_datachannel_key);
        keyprefOrdered = getString(R.string.pref_ordered_key);
        keyprefMaxRetransmitTimeMs = getString(R.string.pref_max_retransmit_time_ms_key);
        keyprefMaxRetransmits = getString(R.string.pref_max_retransmits_key);
        keyprefDataProtocol = getString(R.string.pref_data_protocol_key);
        keyprefNegotiated = getString(R.string.pref_negotiated_key);
        keyprefDataId = getString(R.string.pref_data_id_key);


    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        removeCall(chatId);
    }
}
