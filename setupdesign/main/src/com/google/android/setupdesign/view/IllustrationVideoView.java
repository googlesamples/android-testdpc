/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.setupdesign.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Animatable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.VisibleForTesting;
import com.google.android.setupcompat.util.BuildCompatUtils;
import com.google.android.setupdesign.R;
import java.io.IOException;

/**
 * A view for displaying videos in a continuous loop (without audio). This is typically used for
 * animated illustrations.
 *
 * <p>The video can be specified using {@code app:sudVideo}, specifying the raw resource to the mp4
 * video. Optionally, {@code app:sudLoopStartMs} can be used to specify which part of the video it
 * should loop back to
 *
 * <p>For optimal file size, use avconv or other video compression tool to remove the unused audio
 * track and reduce the size of your video asset: avconv -i [input file] -vcodec h264 -crf 20 -an
 * [output_file]
 */
@TargetApi(VERSION_CODES.ICE_CREAM_SANDWICH)
public class IllustrationVideoView extends TextureView
    implements Animatable,
        SurfaceTextureListener,
        OnPreparedListener,
        OnSeekCompleteListener,
        OnInfoListener,
        OnErrorListener {

  private static final String TAG = "IllustrationVideoView";

  private float aspectRatio = 1.0f; // initial guess until we know

  @Nullable // Can be null when media player fails to initialize
  protected MediaPlayer mediaPlayer;

  private @RawRes int videoResId = 0;

  private String videoResPackageName;

  @VisibleForTesting Surface surface;

  private boolean prepared;

  private boolean shouldPauseVideoWhenFinished = true;

  /**
   * The visibility of this view as set by the user. This view combines this with {@link
   * #isMediaPlayerLoading} to determine the final visibility.
   */
  private int visibility = View.VISIBLE;

  /**
   * Whether the media player is loading. This is used to hide this view to avoid a flash with a
   * color different from the background while the media player is trying to render the first frame.
   * Note: if this TextureView is not visible, it will never load the surface texture, and never
   * play the video.
   */
  private boolean isMediaPlayerLoading = false;

  public IllustrationVideoView(Context context, AttributeSet attrs) {
    super(context, attrs);
    if (!isInEditMode()) {
      init(context, attrs);
    }
  }

  private void init(Context context, AttributeSet attrs) {
    final TypedArray a =
        context.obtainStyledAttributes(attrs, R.styleable.SudIllustrationVideoView);
    final int videoResId = a.getResourceId(R.styleable.SudIllustrationVideoView_sudVideo, 0);

    // TODO: remove the usage of BuildCompatUtils#isAtLeatestS if VERSION_CODE.S is
    // support by system.
    if (BuildCompatUtils.isAtLeastS()) {
      boolean shouldPauseVideo =
          a.getBoolean(R.styleable.SudIllustrationVideoView_sudPauseVideoWhenFinished, true);
      setPauseVideoWhenFinished(shouldPauseVideo);
    }

    a.recycle();
    setVideoResource(videoResId);

    // By default the video scales without interpolation, resulting in jagged edges in the
    // video. This works around it by making the view go through scaling, which will apply
    // anti-aliasing effects.
    setScaleX(0.9999999f);
    setScaleX(0.9999999f);

    setSurfaceTextureListener(this);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = MeasureSpec.getSize(heightMeasureSpec);

    if (height < width * aspectRatio) {
      // Height constraint is tighter. Need to scale down the width to fit aspect ratio.
      width = (int) (height / aspectRatio);
    } else {
      // Width constraint is tighter. Need to scale down the height to fit aspect ratio.
      height = (int) (width * aspectRatio);
    }

    super.onMeasure(
        MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
  }

  /**
   * Set the video and video package name to be played by this view.
   *
   * @param videoResId Resource ID of the video, typically an MP4 under res/raw.
   * @param videoResPackageName The package name of videoResId.
   */
  public void setVideoResource(@RawRes int videoResId, String videoResPackageName) {
    if (videoResId != this.videoResId
        || (videoResPackageName != null && !videoResPackageName.equals(this.videoResPackageName))) {
      this.videoResId = videoResId;
      this.videoResPackageName = videoResPackageName;
      createMediaPlayer();
    }
  }

  /**
   * Set the video to be played by this view.
   *
   * @param resourceEntry the {@link com.google.android.setupdesign.util.Partner.ResourceEntry} of
   *     the video, typically an MP4 under res/raw.
   */
  public void setVideoResourceEntry(
      com.google.android.setupdesign.util.Partner.ResourceEntry resourceEntry) {
    setVideoResource(resourceEntry.id, resourceEntry.packageName);
  }

  /**
   * Set the video to be played by this view.
   *
   * @param resourceEntry the {@link com.google.android.setupcompat.partnerconfig.ResourceEntry} of
   *     the video, typically an MP4 under res/raw.
   */
  public void setVideoResourceEntry(
      com.google.android.setupcompat.partnerconfig.ResourceEntry resourceEntry) {
    setVideoResource(resourceEntry.getResourceId(), resourceEntry.getPackageName());
  }

  /**
   * Set the video to be played by this view.
   *
   * @param resId Resource ID of the video, typically an MP4 under res/raw.
   */
  public void setVideoResource(@RawRes int resId) {
    setVideoResource(resId, getContext().getPackageName());
  }

  /**
   * Sets whether the video pauses during the screen transition.
   *
   * @param paused Whether the video pauses.
   */
  public void setPauseVideoWhenFinished(boolean paused) {
    shouldPauseVideoWhenFinished = paused;
  }

  @Override
  public void onWindowFocusChanged(boolean hasWindowFocus) {
    super.onWindowFocusChanged(hasWindowFocus);
    if (hasWindowFocus) {
      start();
    } else {
      stop();
    }
  }

  /**
   * Creates a media player for the current URI. The media player will be started immediately if the
   * view's window is visible. If there is an existing media player, it will be released.
   */
  protected void createMediaPlayer() {
    if (mediaPlayer != null) {
      mediaPlayer.release();
    }
    if (surface == null || videoResId == 0) {
      return;
    }

    mediaPlayer = new MediaPlayer();

    mediaPlayer.setSurface(surface);
    mediaPlayer.setOnPreparedListener(this);
    mediaPlayer.setOnSeekCompleteListener(this);
    mediaPlayer.setOnInfoListener(this);
    mediaPlayer.setOnErrorListener(this);

    setVideoResourceInternal(videoResId, videoResPackageName);
  }

  private void setVideoResourceInternal(@RawRes int videoRes, String videoResPackageName) {
    Uri uri = Uri.parse("android.resource://" + videoResPackageName + "/" + videoRes);
    try {
      mediaPlayer.setDataSource(getContext(), uri, null);
      mediaPlayer.prepareAsync();
    } catch (IOException e) {
      Log.e(TAG, "Unable to set video data source: " + videoRes, e);
    }
  }

  protected void createSurface() {
    if (surface != null) {
      surface.release();
      surface = null;
    }
    // Reattach only if it has been previously released
    SurfaceTexture surfaceTexture = getSurfaceTexture();
    if (surfaceTexture != null) {
      setIsMediaPlayerLoading(true);
      surface = new Surface(surfaceTexture);
    }
  }

  @Override
  protected void onWindowVisibilityChanged(int visibility) {
    super.onWindowVisibilityChanged(visibility);
    if (visibility == View.VISIBLE) {
      reattach();
    } else {
      release();
    }
  }

  @Override
  public void setVisibility(int visibility) {
    this.visibility = visibility;
    if (isMediaPlayerLoading && visibility == View.VISIBLE) {
      visibility = View.INVISIBLE;
    }
    super.setVisibility(visibility);
  }

  private void setIsMediaPlayerLoading(boolean isMediaPlayerLoading) {
    this.isMediaPlayerLoading = isMediaPlayerLoading;
    setVisibility(this.visibility);
  }

  /**
   * Whether the media player should play the video in a continuous loop. The default value is true.
   */
  protected boolean shouldLoop() {
    return true;
  }

  /**
   * Release any resources used by this view. This is automatically called in
   * onSurfaceTextureDestroyed so in most cases you don't have to call this.
   */
  public void release() {
    if (mediaPlayer != null) {
      mediaPlayer.release();
      mediaPlayer = null;
      prepared = false;
    }
    if (surface != null) {
      surface.release();
      surface = null;
    }
  }

  private void reattach() {
    if (surface == null) {
      initVideo();
    }
  }

  private void initVideo() {
    if (getWindowVisibility() != View.VISIBLE) {
      return;
    }
    createSurface();
    if (surface != null) {
      createMediaPlayer();
    } else {
      // This can happen if this view hasn't been drawn yet
      Log.i(TAG, "Surface is null");
    }
  }

  protected void onRenderingStart() {}

  /* SurfaceTextureListener methods */

  @Override
  public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
    setIsMediaPlayerLoading(true);
    initVideo();
  }

  @Override
  public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {}

  @Override
  public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
    release();
    return true;
  }

  @Override
  public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {}

  /* Animatable methods */

  @Override
  public void start() {
    if (prepared && mediaPlayer != null && !mediaPlayer.isPlaying()) {
      mediaPlayer.start();
    }
  }

  @Override
  public void stop() {
    if (shouldPauseVideoWhenFinished) {
      if (prepared && mediaPlayer != null) {
        mediaPlayer.pause();
      }
    } else {
      // do not pause the media player.
    }
  }

  @Override
  public boolean isRunning() {
    return mediaPlayer != null && mediaPlayer.isPlaying();
  }

  /* MediaPlayer callbacks */

  @Override
  public boolean onInfo(MediaPlayer mp, int what, int extra) {
    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
      setIsMediaPlayerLoading(false);
      onRenderingStart();
    }
    return false;
  }

  @Override
  public void onPrepared(MediaPlayer mp) {
    prepared = true;
    mp.setLooping(shouldLoop());

    float aspectRatio = 0.0f;
    if (mp.getVideoWidth() > 0 && mp.getVideoHeight() > 0) {
      aspectRatio = (float) mp.getVideoHeight() / mp.getVideoWidth();
    } else {
      Log.w(TAG, "Unexpected video size=" + mp.getVideoWidth() + "x" + mp.getVideoHeight());
    }
    if (Float.compare(this.aspectRatio, aspectRatio) != 0) {
      this.aspectRatio = aspectRatio;
      requestLayout();
    }
    if (getWindowVisibility() == View.VISIBLE) {
      start();
    }
  }

  @Override
  public void onSeekComplete(MediaPlayer mp) {
    if (isPrepared()) {
      mp.start();
    } else {
      Log.e(TAG, "Seek complete but media player not prepared");
    }
  }

  public int getCurrentPosition() {
    return mediaPlayer == null ? 0 : mediaPlayer.getCurrentPosition();
  }

  protected boolean isPrepared() {
    return prepared;
  }

  @Override
  public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
    Log.w(TAG, "MediaPlayer error. what=" + what + " extra=" + extra);
    return false;
  }

  /**
   * Seeks to specified time position.
   *
   * @param milliseconds the offset in milliseconds from the start to seek to
   * @throws IllegalStateException if the internal player engine has not been initialized
   */
  public void seekTo(int milliseconds) {
    if (mediaPlayer != null) {
      mediaPlayer.seekTo(milliseconds);
    }
  }

  @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
  public MediaPlayer getMediaPlayer() {
    return mediaPlayer;
  }

  protected float getAspectRatio() {
    return aspectRatio;
  }
}
