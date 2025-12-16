package com.zbuntu.terminal.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.zbuntu.terminal.BootstrapManager;
import com.zbuntu.terminal.pty.TerminalSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Service to manage terminal sessions
 */
public class TermuxService extends Service {
    private static final String TAG = "TermuxService";
    
    private final IBinder mBinder = new LocalBinder();
    private final List<TerminalSession> mSessions = new ArrayList<>();
    private BootstrapManager mBootstrapManager;
    
    public class LocalBinder extends Binder {
        public TermuxService getService() {
            return TermuxService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        mBootstrapManager = new BootstrapManager(this);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    /**
     * Create a new terminal session
     */
    public TerminalSession createSession(TerminalSession.TerminalSessionClient client, int rows, int columns) {
        String shellPath = mBootstrapManager.getShellPath();
        String cwd = mBootstrapManager.getHomeDir().getAbsolutePath();
        String[] args = new String[] { shellPath };
        String[] env = mBootstrapManager.buildEnvironment();
        
        TerminalSession session = new TerminalSession(shellPath, cwd, args, env, client);
        mSessions.add(session);
        
        session.start(rows, columns);
        
        return session;
    }
    
    /**
     * Remove a session
     */
    public void removeSession(TerminalSession session) {
        mSessions.remove(session);
        if (session.isRunning()) {
            session.close();
        }
    }
    
    /**
     * Get bootstrap manager
     */
    public BootstrapManager getBootstrapManager() {
        return mBootstrapManager;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        
        // Close all sessions
        for (TerminalSession session : mSessions) {
            if (session.isRunning()) {
                session.close();
            }
        }
        mSessions.clear();
    }
}
