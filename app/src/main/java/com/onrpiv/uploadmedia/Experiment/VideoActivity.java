package com.onrpiv.uploadmedia.Experiment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.Camera.CameraFragment;
import com.onrpiv.uploadmedia.Utilities.RealPathUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * author: sarbajit mukherjee
 * Created by sarbajit mukherjee on 09/07/2020.
 */

public class VideoActivity extends AppCompatActivity{
    private Button pickVideo, generateFrames, recordVideo;
    public static final int REQUEST_PICK_VIDEO = 3;
    private static final int REQUEST_VIDEO_CAPTURE = 300;
    public ProgressDialog pDialog;
    private VideoView mVideoView;
    private TextView mBufferingTextView;
    private String videoPath;
    private String userName;
    private String fps = "30";

    // Current playback position (in milliseconds).
    private int mCurrentPosition = 0;

    // Tag for the instance state bundle.
    private static final String PLAYBACK_TIME = "play_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the transferred data from source activity.
        Intent userNameIntent = getIntent();
        userName = userNameIntent.getStringExtra("UserName");

        recordVideo = (Button) findViewById(R.id.recordVideo);
        pickVideo = (Button) findViewById(R.id.pickVideo);
        generateFrames = (Button) findViewById(R.id.generateFrames);
        generateFrames.setEnabled(false);

        recordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Camera supports high speed capture
                if (hasHighSpeedCapability() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    FragmentManager fragManager = getSupportFragmentManager();

                    final String requestKey = "highSpeedKey";
                    fragManager.setFragmentResultListener(requestKey, VideoActivity.this, new FragmentResultListener() {
                        @Override
                        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                            videoCaptured(Uri.parse(result.getString("uri")));
                            fps = result.getString("fps");
                            generateFrames.setEnabled(true);
                        }
                    });

                    fragManager.beginTransaction().replace(R.id.video_layout_container, CameraFragment.newInstance(requestKey)).commit();

                // Camera doesn't support high speed capture
                } else {
                    Intent videoCaptureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    if (videoCaptureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(videoCaptureIntent, REQUEST_VIDEO_CAPTURE);
                    }
                }
            }
        });

        pickVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickVideoIntent.setType("video/*");
                startActivityForResult(pickVideoIntent, REQUEST_PICK_VIDEO);
            }
        });

        generateFrames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoPath != null){
                    generateFrames(view);
                }else{
                    Toast.makeText(VideoActivity.this, "Please select a video", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mVideoView = (VideoView) findViewById(R.id.videoview);
        mBufferingTextView = (TextView) findViewById(R.id.buffering_textview);

        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(PLAYBACK_TIME);
        }

        // Set up the media controller widget and attach it to the video view.
        MediaController controller = new MediaController(this);
        controller.setMediaPlayer(mVideoView);
        mVideoView.setMediaController(controller);

        initDialog();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mVideoView.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Media playback takes a lot of resources, so everything should be
        // stopped and released at this time.
        releasePlayer();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current playback position (in milliseconds) to the
        // instance state bundle.
        outState.putInt(PLAYBACK_TIME, mVideoView.getCurrentPosition());
    }

    private void initializePlayer(Uri uri) {
        // Show the "Buffering..." message while the video loads.
        mBufferingTextView.setVisibility(VideoView.VISIBLE);
        if (uri != null){
            mVideoView.setVideoURI(uri);
        }
        // Listener for onPrepared() event (runs after the media is prepared).
        mVideoView.setOnPreparedListener(
                new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {

                        // Hide buffering message.
                        mBufferingTextView.setVisibility(VideoView.INVISIBLE);

                        // Restore saved position, if available.
                        if (mCurrentPosition > 0) {
                            mVideoView.seekTo(mCurrentPosition);
                        } else {
                            // Skipping to 1 shows the first frame of the video.
                            mVideoView.seekTo(1);
                        }

                        // Start playing!
                        mVideoView.start();
                    }
                });

        // Listener for onCompletion() event (runs after media has finished
        // playing).
        mVideoView.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        Toast.makeText(VideoActivity.this,
                                R.string.toast_message,
                                Toast.LENGTH_SHORT).show();

                        // Return the video position to the start.
                        mVideoView.seekTo(0);
                    }
                });
    }

    private void releasePlayer() {
        mVideoView.stopPlayback();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_VIDEO_CAPTURE) {
                if (data != null) {
                    videoCaptured(data.getData());
                }
            }
            if (requestCode == REQUEST_PICK_VIDEO) {
                if (data != null) {
                    videoSelected(data.getData());
                }
            }
            generateFrames.setEnabled(true);
        }
        else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "Sorry, there was an error!", Toast.LENGTH_LONG).show();
        }
    }

    private void videoCaptured(Uri video) {
        Toast.makeText(this, "Video content URI: " + video,
                Toast.LENGTH_LONG).show();

        videoPath = RealPathUtil.getRealPath(VideoActivity.this, video);
        initializePlayer(video);
        recordVideo.setBackgroundColor(Color.parseColor("#00CC00"));
        pickVideo.setBackgroundColor(Color.parseColor("#00CC00"));
    }

    private void videoSelected(Uri video) {
        Toast.makeText(this, "Video content URI: " + video,
                Toast.LENGTH_LONG).show();

        videoPath = RealPathUtil.getRealPath(VideoActivity.this, video);
        initializePlayer(video);
        pickVideo.setBackgroundColor(Color.parseColor("#00CC00"));
    }

    /**
     * Command for extracting images from video
     */
    private void generateFrames(View view){
        String fileExtn = ".png";

        double startMs= 0.0;
        double endMs= 1000.0;

        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        String filePrefix = "EXTRACT_" + timeStamp + "_";

        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/PIV_Frames_" + userName);

        // Then we create the storage directory if does not exists
        if (!storageDirectory.exists()) storageDirectory.mkdir();

        File jpegFile = new File(storageDirectory, filePrefix + "%03d" + fileExtn);

        /* https://ffmpeg.org/ffmpeg.html
        ffmpeg command line options
        -y  overwrite any existing files
        -i  input video path
        -an blocks all audio streams of a file from being mapped for any output
        -r  force the frame rate of output file
        -ss where to start processing.
            The value is a time duration See more https://ffmpeg.org/ffmpeg-utils.html#Time-duration.
        -t  total duration or when to stop processing.
            The value is a time duration. See more https://ffmpeg.org/ffmpeg-utils.html#Time-duration.
         */
        String[] complexCommand = {"-y", "-i", videoPath, "-an", "-r", fps, "-ss", "" + startMs / 1000, "-t", "" + (endMs - startMs) / 1000, jpegFile.getAbsolutePath()};
        /*   Remove -r 1 if you want to extract all video frames as images from the specified time duration.*/
        execFFmpegBinary(complexCommand);
    }


    /**
     * Executing ffmpeg binary
     */
    private void execFFmpegBinary(final String[] command) {
            FFmpeg.executeAsync(command, new ExecuteCallback() {
                @Override
                public void apply(long executionId, int returnCode) {
                    if (returnCode == Config.RETURN_CODE_SUCCESS) {
                    Toast.makeText(VideoActivity.this, "Frames Generation Completed", Toast.LENGTH_SHORT).show();

                    RealPathUtil.deleteIfTempFile(VideoActivity.this, videoPath);
                    generateFrames.setBackgroundColor(Color.parseColor("#00CC00"));

                    } else if (returnCode == Config.RETURN_CODE_CANCEL) {
                        Log.d("FFMPEG", "Frame extraction cancelled!");
                    } else {
                        Toast.makeText(VideoActivity.this, "Frames Generation FAILED", Toast.LENGTH_SHORT).show();
                        Log.e("FFMPEG", "Async frame extraction failed with return code = " + returnCode);
                    }
                }
            });
    }

    protected void initDialog() {

        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(true);
    }

    private boolean hasHighSpeedCapability() {
        final CameraManager camManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        boolean hasHighFPS = false;
        try {
            int[] capabilities = camManager.getCameraCharacteristics(camManager.getCameraIdList()[0]).get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
            assert capabilities != null;
            for (int capability : capabilities) {
                if (capability == CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO) {
                    hasHighFPS = true;
                    break;
                }
            }
        } catch (IllegalArgumentException | CameraAccessException e) {
            e.printStackTrace();
        }
        return hasHighFPS;
    }
}

