package com.mediareceiver.pro;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {

    private MediaServer mediaServer;
    private Button startButton, stopButton, copyButton, folderButton;
    private TextView serverStatus, tunnelStatus, urlText, visitorCount, fileCount, logText;
    private ClipboardManager clipboard;
    private String publicUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupClickListeners();

        clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        updateLog("📱 Media Receiver Pro Started");
        updateLog("📍 Storage: " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/MediaReceiverPro");
        updateLog("💡 Press 'Start Server' to begin");
    }

    private void initializeViews() {
        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        copyButton = (Button) findViewById(R.id.copyButton);
        folderButton = (Button) findViewById(R.id.folderButton);

        serverStatus = (TextView) findViewById(R.id.serverStatus);
        tunnelStatus = (TextView) findViewById(R.id.tunnelStatus);
        urlText = (TextView) findViewById(R.id.urlText);
        visitorCount = (TextView) findViewById(R.id.visitorCount);
        fileCount = (TextView) findViewById(R.id.fileCount);
        logText = (TextView) findViewById(R.id.logText);

        updateButtonStates(false);
    }

    private void setupClickListeners() {
        startButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startServer();
				}
			});

        stopButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					stopServer();
				}
			});

        copyButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					copyUrlToClipboard();
				}
			});

        folderButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					openStorageFolder();
				}
			});

        urlText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!publicUrl.isEmpty()) {
						openUrlInBrowser(publicUrl);
					}
				}
			});
    }

    private void startServer() {
        if (mediaServer != null && mediaServer.isRunning()) {
            showToast("Server is already running");
            return;
        }

        updateLog("🚀 Starting server...");
        mediaServer = new MediaServer(this);
        mediaServer.startServer();
        updateButtonStates(true);
    }

    private void stopServer() {
        if (mediaServer != null) {
            mediaServer.stopServer();
        }
        updateButtonStates(false);
        updateServerStatus(false);
        updateTunnelStatus(false);
    }

    private void updateButtonStates(boolean serverRunning) {
        startButton.setEnabled(!serverRunning);
        stopButton.setEnabled(serverRunning);
        copyButton.setEnabled(serverRunning && !publicUrl.isEmpty());

        startButton.setAlpha(serverRunning ? 0.5f : 1.0f);
        stopButton.setAlpha(serverRunning ? 1.0f : 0.5f);
        copyButton.setAlpha((serverRunning && !publicUrl.isEmpty()) ? 1.0f : 0.5f);
    }

    private void copyUrlToClipboard() {
        if (!publicUrl.isEmpty()) {
            ClipData clip = ClipData.newPlainText("Public URL", publicUrl);
            clipboard.setPrimaryClip(clip);
            showToast("URL copied to clipboard");
            updateLog("📋 URL copied: " + publicUrl);
        } else {
            showToast("No public URL available");
        }
    }

    private void openStorageFolder() {
        File storageDir = FileUtils.getMediaStorageDir();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(storageDir);
        intent.setDataAndType(uri, "resource/folder");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            showToast("No file manager app found");
            updateLog("📁 Storage path: " + storageDir.getAbsolutePath());
        }
    }

    private void openUrlInBrowser(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            updateLog("🌐 Opening URL in browser: " + url);
        } catch (Exception e) {
            showToast("Cannot open browser");
            updateLog("❌ Browser error: " + e.getMessage());
        }
    }

    public void updateServerStatus(final boolean running) {
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (running) {
						serverStatus.setText("RUNNING");
						serverStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
					} else {
						serverStatus.setText("STOPPED");
						serverStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
					}
				}
			});
    }

    public void updateTunnelStatus(final boolean active) {
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (active) {
						tunnelStatus.setText("ACTIVE");
						tunnelStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
					} else {
						tunnelStatus.setText("DISABLED");
						tunnelStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
					}
				}
			});
    }

    public void updatePublicUrl(final String url) {
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					publicUrl = url;
					urlText.setText(url);
					updateButtonStates(mediaServer != null && mediaServer.isRunning());
				}
			});
    }

    public void updateVisitorCount(final int count) {
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					visitorCount.setText(String.valueOf(count));
				}
			});
    }

    public void updateFileCount(final int count) {
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					fileCount.setText(String.valueOf(count));
				}
			});
    }

    public void updateLog(final String message) {
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
					String logEntry = "[" + timestamp + "] " + message + "\n";

					String currentLog = logText.getText().toString();
					if (currentLog.length() > 5000) {
						currentLog = currentLog.substring(0, 3000) + "\n... (log truncated)\n";
					}

					logText.setText(logEntry + currentLog);
				}
			});
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
				}
			});
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaServer != null) {
            mediaServer.stopServer();
        }
    }
}
