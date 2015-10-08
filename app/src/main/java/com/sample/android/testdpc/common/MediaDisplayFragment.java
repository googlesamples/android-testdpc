/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sample.android.testdpc.common;

import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import com.sample.android.testdpc.R;

/**
 * This fragment helps to display Media (e.g. image, video)
 */
public class MediaDisplayFragment extends Fragment {
    private static final String ARG_DISPLAY_REQUEST = "argDisplayRequest";
    private static final String ARG_MEDIA_URI = "argMediaUri";

    public static final int REQUEST_DISPLAY_IMAGE = 1;
    public static final int REQUEST_DISPLAY_VIDEO = 2;

    private int mDisplayRequest;
    private Uri mMediaUri;

    public static MediaDisplayFragment newInstance(int displayRequest, Uri mediaUri) {
        MediaDisplayFragment fragment = new MediaDisplayFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DISPLAY_REQUEST, displayRequest);
        args.putString(ARG_MEDIA_URI, mediaUri.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDisplayRequest = getArguments().getInt(ARG_DISPLAY_REQUEST);
            mMediaUri = Uri.parse(getArguments().getString(ARG_MEDIA_URI));
        }
        getActivity().getActionBar().setTitle(R.string.display_media);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_media_display, container, false);
        switch (mDisplayRequest) {
            case REQUEST_DISPLAY_IMAGE:
                ImageView imageView = (ImageView) view.findViewById(R.id.image);
                imageView.setImageURI(mMediaUri);
                imageView.setVisibility(View.VISIBLE);
                break;
            case REQUEST_DISPLAY_VIDEO:
                final VideoView videoView = (VideoView) view.findViewById(R.id.video);
                videoView.setVideoURI(mMediaUri);
                videoView.setVisibility(View.VISIBLE);
                videoView.requestFocus();
                Button playButton = (Button) view.findViewById(R.id.play_button);
                playButton.setVisibility(View.VISIBLE);
                playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        videoView.start();
                    }
                });
                Button stopButton = (Button) view.findViewById(R.id.stop_button);
                stopButton.setVisibility(View.VISIBLE);
                stopButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        videoView.pause();
                    }
                });
                break;
        }

        Button deleteButton = (Button) view.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getContentResolver().delete(mMediaUri, null, null);
                getActivity().getFragmentManager().popBackStack();
            }
        });
        return view;
    }

}
