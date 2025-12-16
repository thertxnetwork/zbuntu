package com.zbuntu.terminal;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zbuntu.terminal.pty.TerminalSession;
import com.zbuntu.terminal.service.TermuxService;
import com.zbuntu.terminal.view.TerminalView;

/**
 * Main activity for the terminal app
 */
public class MainActivity extends AppCompatActivity implements TerminalSession.TerminalSessionClient, TerminalView.TerminalViewClient {
    private static final String TAG = "MainActivity";
    
    private TerminalView mTerminalView;
    private TextView mStatusText;
    private ProgressBar mProgressBar;
    
    private TermuxService mService;
    private TerminalSession mSession;
    private boolean mServiceBound = false;
    
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((TermuxService.LocalBinder) service).getService();
            mServiceBound = true;
            Log.d(TAG, "Service connected");
            
            // Check bootstrap installation
            checkBootstrap();
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mServiceBound = false;
            Log.d(TAG, "Service disconnected");
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mTerminalView = findViewById(R.id.terminal_view);
        mStatusText = findViewById(R.id.status_text);
        mProgressBar = findViewById(R.id.progress_bar);
        
        mTerminalView.setClient(this);
        
        // Bind to service
        Intent serviceIntent = new Intent(this, TermuxService.class);
        bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
        
        // Setup terminal view
        mTerminalView.getViewTreeObserver().addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (mSession == null && mServiceBound && mService != null) {
                        BootstrapManager bootstrap = mService.getBootstrapManager();
                        if (bootstrap.isBootstrapInstalled()) {
                            startTerminalSession();
                        }
                    }
                }
            }
        );
    }
    
    /**
     * Check if bootstrap is installed
     */
    private void checkBootstrap() {
        BootstrapManager bootstrap = mService.getBootstrapManager();
        
        if (bootstrap.isBootstrapInstalled()) {
            updateStatus("Bootstrap installed");
            startTerminalSession();
        } else {
            updateStatus("Installing bootstrap...");
            installBootstrap();
        }
    }
    
    /**
     * Install bootstrap package
     */
    private void installBootstrap() {
        BootstrapManager bootstrap = mService.getBootstrapManager();
        
        bootstrap.installBootstrap(new BootstrapManager.BootstrapListener() {
            @Override
            public void onProgress(final int percent, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setProgress(percent);
                        updateStatus(message);
                    }
                });
            }
            
            @Override
            public void onComplete() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateStatus("Bootstrap installed");
                        mProgressBar.setProgress(0);
                        startTerminalSession();
                    }
                });
            }
            
            @Override
            public void onError(final String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateStatus("Error: " + error);
                        Toast.makeText(MainActivity.this, "Installation failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    
    /**
     * Start terminal session
     */
    private void startTerminalSession() {
        if (mSession != null && mSession.isRunning()) {
            return;
        }
        
        int rows = mTerminalView.getRows();
        int columns = mTerminalView.getColumns();
        
        if (rows <= 0) rows = 24;
        if (columns <= 0) columns = 80;
        
        mSession = mService.createSession(this, rows, columns);
        mTerminalView.clear();
        mTerminalView.appendText("Starting terminal session...\n");
        updateStatus("Session active");
    }
    
    /**
     * Update status text
     */
    private void updateStatus(String status) {
        if (mStatusText != null) {
            mStatusText.setText(status);
        }
    }
    
    // TerminalSession.TerminalSessionClient implementation
    
    @Override
    public void onTextChanged(TerminalSession session) {
        // Handle text changes from terminal
        // In a full implementation, this would get the text from a buffer
    }
    
    @Override
    public void onSessionFinished(TerminalSession session) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTerminalView.appendText("\n[Session ended]\n");
                updateStatus("Session finished");
            }
        });
    }
    
    @Override
    public void onBell(TerminalSession session) {
        // Handle bell
    }
    
    @Override
    public void onClipboardText(TerminalSession session, String text) {
        // Handle clipboard
    }
    
    // TerminalView.TerminalViewClient implementation
    
    @Override
    public void onKeyDown(int keyCode, KeyEvent event) {
        if (mSession != null && mSession.isRunning()) {
            String text = mapKeyEvent(keyCode, event);
            if (text != null) {
                mSession.write(text.getBytes());
            }
        }
    }
    
    @Override
    public void onKeyUp(int keyCode, KeyEvent event) {
        // Handle key up if needed
    }
    
    @Override
    public void onTextInput(String text) {
        if (mSession != null && mSession.isRunning()) {
            mSession.write(text.getBytes());
        }
    }
    
    /**
     * Map key events to terminal codes
     */
    private String mapKeyEvent(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                return "\r";
            case KeyEvent.KEYCODE_DEL:
                return "\b";
            case KeyEvent.KEYCODE_TAB:
                return "\t";
            case KeyEvent.KEYCODE_DPAD_UP:
                return "\033[A";
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return "\033[B";
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return "\033[C";
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return "\033[D";
            default:
                if (event.getUnicodeChar() != 0) {
                    return String.valueOf((char) event.getUnicodeChar());
                }
                return null;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (mSession != null) {
            mSession.close();
        }
        
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }
}
