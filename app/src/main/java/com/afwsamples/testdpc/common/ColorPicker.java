/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.afwsamples.testdpc.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afwsamples.testdpc.R;

/**
 * This fragment can be used to get a color input from the user. It contains the seek bars for
 * RGB and an edit box for entering hexadecimal color value, either of them can be used to provide
 * input.
 */
public class ColorPicker extends DialogFragment implements SeekBar.OnSeekBarChangeListener,
        View.OnClickListener {
    private static final String ARG_COLOR_VALUE = "init_color";
    private static final String ARG_LISTENER_FRAGMENT_TAG = "listener_fragment_tag";
    private static final String ARG_ID = "id";

    public static final String COLOR_STRING_FORMATTER = "#%08x";

    private String mListenerTag;
    private int mCurrentColor;
    // Id given as an argument while initiating this class, this will be passed as is to the
    // listener on callback. Since there could be multiple elements initiating this, caller
    // can use this to differentiate those.
    private String mId;

    private View mTitleHeader;
    private SeekBar mRedBar;
    private SeekBar mGreenBar;
    private SeekBar mBlueBar;
    private TextView mRedBarValue;
    private TextView mGreenBarValue;
    private TextView mBlueBarValue;
    private EditText mColorValue;
    private Button mDoneButton;
    private Button mCancelButton;
    private Button mPreviewButton;

    public static ColorPicker newInstance(int initColor, String listenerTag, String id) {
        ColorPicker fragment = new ColorPicker();
        Bundle args = new Bundle();
        args.putInt(ARG_COLOR_VALUE, initColor);
        args.putString(ARG_LISTENER_FRAGMENT_TAG, listenerTag);
        args.putString(ARG_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentColor = savedInstanceState.getInt(ARG_COLOR_VALUE);
            mListenerTag = savedInstanceState.getString(ARG_LISTENER_FRAGMENT_TAG);
            mId = savedInstanceState.getString(ARG_ID);
        } else if (getArguments() != null) {
            mCurrentColor = getArguments().getInt(ARG_COLOR_VALUE,
                    getResources().getColor(R.color.teal));
            mListenerTag = getArguments().getString(ARG_LISTENER_FRAGMENT_TAG);
            mId = getArguments().getString(ARG_ID);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View rootView = LayoutInflater.from(getActivity()).inflate(
                R.layout.color_picker, null);
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(rootView)
                .setPositiveButton(R.string.color_picker_done,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                OnColorSelectListener listener = (OnColorSelectListener)
                                        getFragmentManager().findFragmentByTag(mListenerTag);
                                if (listener != null) {
                                    listener.onColorSelected(mCurrentColor, mId);
                                }
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mDoneButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                mDoneButton.setTextColor(mCurrentColor);
                mCancelButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                mCancelButton.setTextColor(mCurrentColor);
                updateViewsColor();
            }
        });

        initializeViews(rootView);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARG_COLOR_VALUE, mCurrentColor);
        outState.putString(ARG_LISTENER_FRAGMENT_TAG, mListenerTag);
        outState.putString(ARG_ID, mId);
        super.onSaveInstanceState(outState);
    }

    private void initializeViews(View rootView) {
        mTitleHeader = rootView.findViewById(R.id.title_header);

        mRedBar = (SeekBar) rootView.findViewById(R.id.red_bar);
        mGreenBar = (SeekBar) rootView.findViewById(R.id.green_bar);
        mBlueBar = (SeekBar) rootView.findViewById(R.id.blue_bar);

        mRedBarValue = (TextView) rootView.findViewById(R.id.red_bar_value);
        mGreenBarValue = (TextView) rootView.findViewById(R.id.green_bar_value);
        mBlueBarValue = (TextView) rootView.findViewById(R.id.blue_bar_value);

        mRedBar.setOnSeekBarChangeListener(this);
        mGreenBar.setOnSeekBarChangeListener(this);
        mBlueBar.setOnSeekBarChangeListener(this);

        mColorValue = (EditText) rootView.findViewById(R.id.color_value);
        mPreviewButton = (Button) rootView.findViewById(R.id.color_preview);
        mPreviewButton.setOnClickListener(this);
    }

    private void updateViewsColor() {
        mTitleHeader.setBackgroundColor(mCurrentColor);
        mDoneButton.setTextColor(mCurrentColor);
        mCancelButton.setTextColor(mCurrentColor);

        mRedBar.setProgress(Color.red(mCurrentColor));
        mGreenBar.setProgress(Color.green(mCurrentColor));
        mBlueBar.setProgress(Color.blue(mCurrentColor));

        mRedBarValue.setText(Integer.toString(Color.red(mCurrentColor)));
        mGreenBarValue.setText(Integer.toString(Color.green(mCurrentColor)));
        mBlueBarValue.setText(Integer.toString(Color.blue(mCurrentColor)));

        mColorValue.setText(String.format(COLOR_STRING_FORMATTER, mCurrentColor));
        mColorValue.setSelection(mColorValue.getText().length());
        mColorValue.getBackground().mutate().setColorFilter(mCurrentColor,
                PorterDuff.Mode.SRC_ATOP);
        mPreviewButton.setTextColor(mCurrentColor);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mCurrentColor = Color.rgb(mRedBar.getProgress(),
                    mGreenBar.getProgress(), mBlueBar.getProgress());
            updateViewsColor();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // Do nothing
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Do nothing
    }

    public interface OnColorSelectListener {
        void onColorSelected(int selectedColor, String id);
    }

    /**
     * User has tapped the update button, update the color value depending on the value in
     * the edit box. The current color value will be updated only if the edit box contains a valid
     * color, otherwise an error toast is shown to the user.
     */
    @Override
    public void onClick(View view) {
        try {
            mCurrentColor = Color.parseColor(mColorValue.getText().toString());
            updateViewsColor();
        } catch (IllegalArgumentException e) {
            Toast.makeText(getActivity(), R.string.not_valid_color, Toast.LENGTH_SHORT).show();
        }
    }
}