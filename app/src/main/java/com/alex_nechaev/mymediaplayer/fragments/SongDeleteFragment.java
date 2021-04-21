package com.alex_nechaev.mymediaplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alex_nechaev.mymediaplayer.R;

public class SongDeleteFragment extends Fragment {

    public static SongDeleteFragment newInstance(){
        SongDeleteFragment songDeleteFragment = new SongDeleteFragment();
        Bundle bundle = new Bundle();
        songDeleteFragment.setArguments(bundle);
        return songDeleteFragment;
    }

    public interface OnSongDeleteListener{
        void onYesPressed();
        void onNoPressed();
    }

    OnSongDeleteListener callback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this,callback);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            callback = (OnSongDeleteListener)context;
        }catch (ClassCastException ex){
            throw new ClassCastException("The calling activity must implement OnSongDeleteListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.delete_query_fragment,container,false);

        ImageButton yesBtn = view.findViewById(R.id.dialog_yes_btn);
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onYesPressed();
            }
        });

        ImageButton noBtn = view.findViewById(R.id.dialog_no_btn);
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onNoPressed();
            }
        });

        return view;
    }
}
