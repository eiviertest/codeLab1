/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.sample.cast.castconnect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Toast;

import androidx.leanback.app.VideoSupportFragment;
import androidx.leanback.app.VideoSupportFragmentGlueHost;
import androidx.leanback.media.PlaybackTransportControlGlue;
import androidx.leanback.widget.PlaybackControlsRow;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.cast.MediaError;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.tv.CastReceiverContext;
import com.google.android.gms.cast.tv.media.MediaCommandCallback;
import com.google.android.gms.cast.tv.media.MediaException;
import com.google.android.gms.cast.tv.media.MediaLoadCommandCallback;
import com.google.android.gms.cast.tv.media.MediaManager;
import com.google.android.gms.cast.tv.media.QueueUpdateRequestData;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Handles video playback with media controls.
 */
public class PlaybackVideoFragment extends VideoSupportFragment {

    private static final String LOG_TAG = "PlaybackVideoFragment";
    private static final int UPDATE_DELAY = 16;

    private SimpleExoPlayer mPlayer;
    private Movie mMovie;
    private HlsMediaSource hlsMediaSource;
    private LeanbackPlayerAdapter mPlayerAdapter;
    private PlaybackTransportControlGlue<LeanbackPlayerAdapter> mTransportControlGlue;

    private MediaSessionCompat mMediaSession;
    private MediaSessionConnector mMediaSessionConnector;
    private MyMediaMetadataProvider mMediaMetadataProvider;

    private CastReceiverContext castReceiverContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        initializePlayer();
        if (mMediaSession != null) {
            mMediaSession.setActive(true);
        }
        processIntent(getActivity().getIntent());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTransportControlGlue != null) {
            mTransportControlGlue.pause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mMediaSession != null) {
            mMediaSession.setActive(false);
        }
        releasePlayer();
    }

    private void initializePlayer() {
        if (mPlayer == null) {
            VideoSupportFragmentGlueHost glueHost =
                    new VideoSupportFragmentGlueHost(PlaybackVideoFragment.this);

            mPlayer = ExoPlayerFactory.newSimpleInstance(getContext());
            mPlayerAdapter = new LeanbackPlayerAdapter(getContext(), mPlayer, UPDATE_DELAY);
            mPlayerAdapter.setRepeatAction(PlaybackControlsRow.RepeatAction.INDEX_NONE);
            mMediaMetadataProvider = new MyMediaMetadataProvider();

            mTransportControlGlue = new PlaybackTransportControlGlue<>(getContext(), mPlayerAdapter);
            mTransportControlGlue.setHost(glueHost);
            mTransportControlGlue.setSeekEnabled(true);

            mMediaSession = new MediaSessionCompat(getContext(), LOG_TAG);
            mMediaSessionConnector = new MediaSessionConnector(mMediaSession);
            mMediaSessionConnector.setPlayer(mPlayer);
            mMediaSessionConnector.setMediaMetadataProvider(mMediaMetadataProvider);

            castReceiverContext = CastReceiverContext.getInstance();
            if (castReceiverContext != null) {
                MediaManager mediaManager = castReceiverContext.getMediaManager();
                mediaManager.setSessionCompatToken(mMediaSession.getSessionToken());
                mediaManager.setMediaLoadCommandCallback(new MyMediaLoadCommandCallback());
                mediaManager.setMediaCommandCallback(new MyMediaCommandCallback());

                // Use MediaStatusInterceptor to process the MediaStatus before sending out.
                mediaManager.setMediaStatusInterceptor(mediaStatusWriter -> {
                    try {
                        mediaStatusWriter.setCustomData(new JSONObject("{myData: 'CustomData'}"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private void releasePlayer() {
        if (mMediaSession != null) {
            mMediaSession.release();
        }
        if (castReceiverContext != null) {
            castReceiverContext.getMediaManager().setSessionCompatToken(null);
        }
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
            mPlayerAdapter = null;
            mTransportControlGlue = null;
            hlsMediaSource = null;
        }
    }

    private void play(Movie movie) {
        mTransportControlGlue.setTitle(movie.getTitle());
        mTransportControlGlue.setSubtitle(movie.getDescription());
        prepareMediaForPlaying(Uri.parse(movie.getVideoUrl()));
        mTransportControlGlue.play();
    }

    private void prepareMediaForPlaying(Uri mediaSourceUri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                getContext(), Util.getUserAgent(getContext(), "castconnect"));

        hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaSourceUri);

        mPlayer.prepare(hlsMediaSource);
    }

    public void processIntent(Intent intent) {
        MediaManager mediaManager = CastReceiverContext.getInstance().getMediaManager();
        if (mediaManager.onNewIntent(intent)) {
            return;
        }

        // Clears all overrides in the modifier.
        mediaManager.getMediaStatusModifier().clear();

        if (intent.hasExtra(PlaybackActivity.MOVIE)) {
            mMovie = (Movie) intent.getSerializableExtra(PlaybackActivity.MOVIE);
            play(mMovie);
        }
    }

    class MyMediaMetadataProvider implements MediaSessionConnector.MediaMetadataProvider {
        @Override
        public MediaMetadataCompat getMetadata(Player player) {
            MediaMetadataCompat.Builder mediaMetadata = new MediaMetadataCompat.Builder();
            if (mMovie != null) {
                mediaMetadata.putString(
                        MediaMetadataCompat.METADATA_KEY_TITLE, mMovie.getTitle());
                mediaMetadata.putString(
                        MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, mMovie.getTitle());
                mediaMetadata.putString(
                        MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE,
                        mMovie.getDescription());
                mediaMetadata.putString(
                        MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mMovie.getVideoUrl());
                mediaMetadata.putString(
                        MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                        mMovie.getCardImageUrl());
            }
            mediaMetadata.putLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION, mTransportControlGlue.getDuration());

            return mediaMetadata.build();
        }
    }

    private class MyMediaLoadCommandCallback extends MediaLoadCommandCallback {
        @Override
        public Task<MediaLoadRequestData> onLoad(String senderId, MediaLoadRequestData mediaLoadRequestData) {
            Toast.makeText(getActivity(), "onLoad()", Toast.LENGTH_SHORT).show();

            if (mediaLoadRequestData == null) {
                // Throw MediaException to indicate load failure.
                return Tasks.forException(new MediaException(
                        new MediaError.Builder()
                                .setDetailedErrorCode(MediaError.DetailedErrorCode.LOAD_FAILED)
                                .setReason(MediaError.ERROR_REASON_INVALID_REQUEST)
                                .build()));
            }

            return Tasks.call(() -> {
                play(convertLoadRequestToMovie(mediaLoadRequestData));

                // Update media metadata and state
                MediaManager mediaManager = castReceiverContext.getMediaManager();
                mediaManager.setDataFromLoad(mediaLoadRequestData);

                // Use MediaStatusModifier to provide additional information for Cast senders.
                mediaManager.getMediaStatusModifier()
                        .setMediaCommandSupported(MediaStatus.COMMAND_QUEUE_NEXT, true)
                        .setIsPlayingAd(false);

                mediaManager.broadcastMediaStatus();

                // Return the resolved MediaLoadRequestData to indicate load success.
                return mediaLoadRequestData;
            });
        }
    }

    private Movie convertLoadRequestToMovie(MediaLoadRequestData mediaLoadRequestData) {
        if (mediaLoadRequestData == null) {
            return null;
        }
        MediaInfo mediaInfo = mediaLoadRequestData.getMediaInfo();
        if (mediaInfo == null) {
            return null;
        }

        String videoUrl = mediaInfo.getContentId();
        if (mediaInfo.getContentUrl() != null) {
            videoUrl = mediaInfo.getContentUrl();
        }

        MediaMetadata metadata = mediaInfo.getMetadata();
        Movie movie = new Movie();
        movie.setVideoUrl(videoUrl);
        if (metadata != null) {
            movie.setTitle(metadata.getString(MediaMetadata.KEY_TITLE));
            movie.setDescription(metadata.getString(MediaMetadata.KEY_SUBTITLE));
            movie.setCardImageUrl(metadata.getImages().get(0).getUrl().toString());
        }
        return movie;
    }

    private class MyMediaCommandCallback extends MediaCommandCallback {
        @Override
        public Task<Void> onQueueUpdate(String senderId, QueueUpdateRequestData queueUpdateRequestData) {
            Toast.makeText(getActivity(), "onQueueUpdate()", Toast.LENGTH_SHORT).show();

            // Queue Prev / Next
            if (queueUpdateRequestData.getJump() != null) {
                Toast.makeText(getActivity(),
                        "onQueueUpdate(): Jump = " + queueUpdateRequestData.getJump(),
                        Toast.LENGTH_SHORT).show();
            }

            return super.onQueueUpdate(senderId, queueUpdateRequestData);
        }
    }
}