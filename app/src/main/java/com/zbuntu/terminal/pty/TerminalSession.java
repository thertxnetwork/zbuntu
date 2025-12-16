package com.zbuntu.terminal.pty;

import android.util.Log;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

/**
 * Manages a terminal session with a subprocess
 */
public class TerminalSession {
    private static final String TAG = "TerminalSession";

    private final String mShellPath;
    private final String mCwd;
    private final String[] mArgs;
    private final String[] mEnv;
    private final TerminalSessionClient mClient;

    private int mTerminalFd = -1;
    private int mProcessId = -1;
    private InputStream mTerminalInputStream;
    private OutputStream mTerminalOutputStream;
    private Thread mReaderThread;
    private boolean mRunning = false;

    public interface TerminalSessionClient {
        void onTextChanged(TerminalSession session);
        void onSessionFinished(TerminalSession session);
        void onBell(TerminalSession session);
        void onClipboardText(TerminalSession session, String text);
    }

    public TerminalSession(String shellPath, String cwd, String[] args, String[] env, TerminalSessionClient client) {
        mShellPath = shellPath;
        mCwd = cwd;
        mArgs = args;
        mEnv = env;
        mClient = client;
    }

    /**
     * Start the terminal session
     */
    public void start(int rows, int columns) {
        if (mRunning) {
            Log.w(TAG, "Session already running");
            return;
        }

        int[] processId = new int[1];
        mTerminalFd = JNI.createSubprocess(mShellPath, mCwd, mArgs, mEnv, processId, rows, columns);

        if (mTerminalFd == -1) {
            Log.e(TAG, "Failed to create subprocess");
            if (mClient != null) {
                mClient.onSessionFinished(this);
            }
            return;
        }

        mProcessId = processId[0];
        mRunning = true;

        try {
            FileDescriptor terminalFd = createFileDescriptor(mTerminalFd);
            mTerminalInputStream = new FileInputStream(terminalFd);
            mTerminalOutputStream = new FileOutputStream(terminalFd);

            // Start reader thread
            mReaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    readLoop();
                }
            });
            mReaderThread.setName("Terminal-Reader-" + mProcessId);
            mReaderThread.start();

        } catch (Exception e) {
            Log.e(TAG, "Failed to create streams", e);
            cleanup();
        }
    }

    /**
     * Write data to terminal
     */
    public void write(byte[] data) {
        if (mTerminalOutputStream != null) {
            try {
                mTerminalOutputStream.write(data);
                mTerminalOutputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Error writing to terminal", e);
            }
        }
    }

    /**
     * Read loop for terminal output
     */
    private void readLoop() {
        byte[] buffer = new byte[4096];
        try {
            while (mRunning) {
                int read = mTerminalInputStream.read(buffer);
                if (read == -1) {
                    break;
                }
                if (read > 0) {
                    // Process output
                    processOutput(buffer, read);
                    if (mClient != null) {
                        mClient.onTextChanged(this);
                    }
                }
            }
        } catch (IOException e) {
            if (mRunning) {
                Log.e(TAG, "Error reading from terminal", e);
            }
        } finally {
            cleanup();
        }
    }

    /**
     * Process terminal output
     */
    private void processOutput(byte[] buffer, int length) {
        // This is where terminal emulation would process VT100 codes
        // For now, we just store the raw output
        // In a full implementation, this would feed into a TerminalEmulator
        Log.d(TAG, "Received " + length + " bytes from terminal");
    }

    /**
     * Close the terminal session
     */
    public void close() {
        mRunning = false;
        cleanup();
    }

    /**
     * Cleanup resources
     */
    private void cleanup() {
        mRunning = false;

        try {
            if (mTerminalInputStream != null) {
                mTerminalInputStream.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing input stream", e);
        }

        try {
            if (mTerminalOutputStream != null) {
                mTerminalOutputStream.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing output stream", e);
        }

        if (mTerminalFd != -1) {
            JNI.close(mTerminalFd);
            mTerminalFd = -1;
        }

        if (mClient != null) {
            mClient.onSessionFinished(this);
        }
    }

    /**
     * Create a FileDescriptor from an integer fd
     */
    private FileDescriptor createFileDescriptor(int fd) throws Exception {
        FileDescriptor fileDescriptor = new FileDescriptor();
        Field descriptorField = FileDescriptor.class.getDeclaredField("descriptor");
        descriptorField.setAccessible(true);
        descriptorField.setInt(fileDescriptor, fd);
        return fileDescriptor;
    }

    public boolean isRunning() {
        return mRunning;
    }

    public int getProcessId() {
        return mProcessId;
    }
}
