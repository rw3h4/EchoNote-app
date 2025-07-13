package org.rw3h4.echonote.ui.voice.record;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.rw3h4.echonote.R;
import org.rw3h4.echonote.data.local.model.Category;
import org.rw3h4.echonote.ui.custom.WaveformView;
import org.rw3h4.echonote.viewmodel.RecordVoiceNoteViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RecordVoiceNoteActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE_RECORD_AUDIO = 21;

    private enum State {
        READY_TO_RECORD,
        RECORDING,
        STOPPED
    }

    private State currentState = State.READY_TO_RECORD;
    private boolean isRecording = false;

    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private long recordingStartTime;
    private Handler timerHandler = new Handler(Looper.getMainLooper());

    private RecordVoiceNoteViewModel viewModel;
    private WaveformView waveformView;
    private TextView timerTextView, fileSizeTextView;
    private FloatingActionButton buttonRecordStop,buttonDiscard, buttonSave;
    private ImageButton buttonClose;
    private TextInputEditText titleInput;
    private AutoCompleteTextView categoryInput;
    private View recordingOverlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_record_voice_note);

        viewModel = new ViewModelProvider(this).get(RecordVoiceNoteViewModel.class);

        initializeViews();
        setupClickListeners();
        observeViewModel();
        setUiState(State.READY_TO_RECORD);
    }

    private void initializeViews() {
        waveformView = findViewById(R.id.waveform_view);
        timerTextView = findViewById(R.id.text_recording_timer);
        buttonRecordStop = findViewById(R.id.button_record_stop);
        buttonDiscard = findViewById(R.id.button_discard);
        buttonSave = findViewById(R.id.button_save);
        buttonClose = findViewById(R.id.button_close);
        titleInput = findViewById(R.id.note_title_input);
        categoryInput = findViewById(R.id.voice_note_category_input);
        recordingOverlay = findViewById(R.id.recording_overlay);
        fileSizeTextView = findViewById(R.id.text_file_size);

    }

    private void setupClickListeners() {
        buttonRecordStop.setOnClickListener(v -> toggleRecording());
        buttonSave.setOnClickListener(v -> saveVoiceNote());
        buttonDiscard.setOnClickListener(v -> discardAndFinish());
        buttonClose.setOnClickListener(v -> handleClose());
    }

    private void observeViewModel() {
        viewModel.allCategories.observe(this, this::setupCategorySuggestions);
        viewModel.getSaveFinished().observe(this, isFinished -> {
            if (isFinished != null && isFinished) {
                Toast.makeText(this, "Voice note saved", Toast.LENGTH_SHORT).show();
                finish();
                viewModel.onSaveComplete();
            }
        });
    }

    private void setupCategorySuggestions(List<Category> categories) {
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            if (!category.getName().equalsIgnoreCase("None")) {
                categoryNames.add(category.getName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categoryNames);
        categoryInput.setAdapter(adapter);
    }

    private void toggleRecording() {
        if (isRecording) {
            stopRecording();
        } else {
            if (checkPermissions()) {
                startRecording();
            } else {
                requestPermissions();
            }
        }
    }

    private void startRecording() {
        try {
            audioFilePath = getRecordingFilePath();
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            setUiState(State.RECORDING);
            waveformView.clearWaveform();
            recordingStartTime = System.currentTimeMillis();
            timerHandler.post(waveformRunnable);
        } catch (IOException e) {
            Toast.makeText(this, "Recording failed to start", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (!isRecording) return;
        isRecording = false;

        timerHandler.removeCallbacks(waveformRunnable);
        try {
            mediaRecorder.stop();
            mediaRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mediaRecorder = null;
        setUiState(State.STOPPED);
        displayFileSize();
    }

    private void saveVoiceNote() {
        String title = Objects.requireNonNull(titleInput.getText()).toString().trim();
        if (title.isEmpty()) {
            title = "Voice Note " + new SimpleDateFormat("yyyy-MM-dd HH:mm",
                    Locale.getDefault()).format(new Date());
        }

        String categoryName = categoryInput.getText().toString().trim();
        long duration = System.currentTimeMillis() - recordingStartTime;

        viewModel.saveVoiceNote(title, categoryName, audioFilePath, duration);
    }

    private void discardAndFinish() {
        if (audioFilePath != null) {
            new File(audioFilePath).delete();
        }
        finish();
    }

    private void handleClose() {
        if (currentState == State.STOPPED) {
            new AlertDialog.Builder(this)
                    .setTitle("Discard Recording?")
                    .setMessage("Are sure you want to discard this recording?")
                    .setPositiveButton("Discard", (
                            dialog, which
                    ) -> discardAndFinish())
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            finish();
        }
    }

    private final Runnable waveformRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecording && mediaRecorder != null) {
                long elapsedTime = System.currentTimeMillis() -  recordingStartTime;
                timerTextView.setText(formatDuration(elapsedTime));
                waveformView.addAmplitude(mediaRecorder.getMaxAmplitude());
                timerHandler.postDelayed(this, 100);
            }
        }
    };

    private void setUiState(State state) {
        currentState = state;
        switch (state) {
            case READY_TO_RECORD:
                buttonRecordStop.setImageResource(R.drawable.ic_mic);
                buttonRecordStop.setEnabled(true);
                buttonRecordStop.setAlpha(1f);
                buttonSave.setVisibility(View.GONE);
                buttonDiscard.setVisibility(View.GONE);
                animateOverlay(false);
                break;
            case RECORDING:
                buttonRecordStop.setImageResource(R.drawable.ic_stop);
                buttonSave.setVisibility(View.GONE);
                buttonDiscard.setVisibility(View.GONE);
                animateOverlay(true);
                break;
            case STOPPED:
                buttonRecordStop.setEnabled(false);
                buttonRecordStop.setAlpha(0.5f);
                buttonSave.setVisibility(View.VISIBLE);
                buttonDiscard.setVisibility(View.VISIBLE);
                animateOverlay(false);
                break;
        }
    }

    private void animateOverlay(boolean show) {
        recordingOverlay.setVisibility(View.VISIBLE);
        float startAlpha = show ? 0f : 1f;
        float endAlpha = show ? 1f : 0f;
        Animation animation = new AlphaAnimation(startAlpha, endAlpha);
        animation.setDuration(300);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (!show) {
                    recordingOverlay.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationStart(Animation animation) {}
        });

        recordingOverlay.startAnimation(animation);
    }

    private void displayFileSize() {
        File file = new File(audioFilePath);
        if (file.exists()) {
            fileSizeTextView.setText(formatFileSize(file.length()));
            fileSizeTextView.setVisibility(View.VISIBLE);
        }
    }

    private String getRecordingFilePath() {
        File dir = getExternalFilesDir(null);  // App-specific storage
        return new File(dir, "VoiceNote_" + System.currentTimeMillis() + ".m4a").getAbsolutePath();
    }

    private String formatDuration(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMG".charAt(exp - 1);
        return String.format(Locale.US, "%.1f %cB", bytes / Math.pow(1024, exp), pre);
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_CODE_RECORD_AUDIO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE_RECORD_AUDIO && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        } else {
            Toast.makeText(this, "Audio recording permission denied",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        timerHandler.removeCallbacks(waveformRunnable);
    }
}