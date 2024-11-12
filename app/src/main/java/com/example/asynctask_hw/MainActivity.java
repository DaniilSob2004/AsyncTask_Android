package com.example.asynctask_hw;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final int SLEEP = 500;
    private static final int START_PROGRESS = 0;
    private static final int MAX_PROGRESS = 100;
    private static final int STEP = 5;

    private TextView statusTextView;
    private ProgressBar progressBar;
    private TextView percentTextView;
    private Button startBtn;
    private Button cancelBtn;

    private ProgressTask progressTask;
    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();  // инициализация
    }


    public void startClick(View view) {
        String textBtn = ((Button) view).getText().toString();
        if (textBtn.equals(getString(R.string.start))) {
            if (isPaused && progressTask != null) {
                resumeTask();
            }
            else {
                startTask();
            }
        }
        else {
            stopTask();
        }
    }

    public void cancelClick(View view) {
        cancelTask();
    }


    private void init() {
        statusTextView = findViewById(R.id.status_text_view);
        progressBar = findViewById(R.id.progress_bar);
        percentTextView = findViewById(R.id.percent_text_view);
        startBtn = findViewById(R.id.start_btn);
        cancelBtn = findViewById(R.id.cancel_btn);
    }

    private void setPercent(int percent) {
        String percentStr = percent + "%";
        percentTextView.setText(percentStr);
    }

    private void startTask() {
        startBtn.setText(getString(R.string.stop));
        cancelBtn.setEnabled(true);

        // запуск ProgressTask
        progressTask = new ProgressTask();
        progressTask.execute();
    }

    private void stopTask() {
        isPaused = true;
        startBtn.setText(getString(R.string.start));
        statusTextView.setText(getString(R.string.stop_status));
    }

    private void resumeTask() {
        synchronized (progressTask) {
            isPaused = false;
            progressTask.notify();  // возобновляем выполнение
        }
        startBtn.setText(getString(R.string.stop));
        statusTextView.setText(getString(R.string.running));
    }

    private void cancelTask() {
        if (progressTask != null) {
            progressTask.cancel(true);  // отменяем задачу
            progressTask = null;
        }
        isPaused = false;
        startBtn.setText(getString(R.string.start));
        cancelBtn.setEnabled(false);
        statusTextView.setText(getString(R.string.wait_status));
        progressBar.setProgress(0);
        setPercent(0);
    }


    private class ProgressTask extends AsyncTask<Void, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            statusTextView.setText(getString(R.string.pre_execute));
        }

        @Override
        protected String doInBackground(Void... voids) {
            statusTextView.setText(getString(R.string.running));

            for (int i = START_PROGRESS; i <= MAX_PROGRESS; i += STEP) {
                if (isCancelled()) {
                    return getString(R.string.stop_status);
                }

                synchronized (this) {
                    while (isPaused) {
                        try {
                            wait();
                        }
                        catch (InterruptedException e) {
                            Log.d(this.getClass().getName(), e.getMessage());
                            return null;
                        }
                    }
                }

                publishProgress(i);
                try {
                    Thread.sleep(SLEEP);
                }
                catch (InterruptedException e) {
                    Log.d(this.getClass().getName(), e.getMessage());
                }
            }
            return getString(R.string.task_completed);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            int val = values[0];
            progressBar.setProgress(val);
            setPercent(val);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.d(this.getClass().getName(), result);
            cancelTask();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            statusTextView.setText(getString(R.string.wait_status));
        }
    }
}