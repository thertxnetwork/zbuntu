package com.zbuntu.terminal;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Manages the Termux bootstrap package installation
 */
public class BootstrapManager {
    private static final String TAG = "BootstrapManager";
    
    // Bootstrap URLs for different architectures
    private static final String BOOTSTRAP_BASE_URL = "https://github.com/termux/termux-packages/releases/latest/download/";
    
    private final Context mContext;
    private final File mFilesDir;
    private final File mHomeDir;
    private final File mUsrDir;
    
    public interface BootstrapListener {
        void onProgress(int percent, String message);
        void onComplete();
        void onError(String error);
    }
    
    public BootstrapManager(Context context) {
        mContext = context;
        mFilesDir = context.getFilesDir();
        mHomeDir = new File(mFilesDir, "home");
        mUsrDir = new File(mFilesDir, "usr");
    }
    
    /**
     * Check if bootstrap is already installed
     */
    public boolean isBootstrapInstalled() {
        File binDir = new File(mUsrDir, "bin");
        File sh = new File(binDir, "sh");
        return sh.exists() && sh.canExecute();
    }
    
    /**
     * Get the architecture string for downloads
     */
    private String getArchitecture() {
        String arch = System.getProperty("os.arch");
        if (arch == null) {
            arch = android.os.Build.SUPPORTED_ABIS[0];
        }
        
        if (arch.contains("aarch64") || arch.contains("armv8")) {
            return "aarch64";
        } else if (arch.contains("arm")) {
            return "arm";
        } else if (arch.contains("x86_64") || arch.contains("amd64")) {
            return "x86_64";
        } else if (arch.contains("x86") || arch.contains("i686")) {
            return "i686";
        }
        
        Log.w(TAG, "Unknown architecture: " + arch + ", defaulting to aarch64");
        return "aarch64";
    }
    
    /**
     * Setup bootstrap directories
     */
    private void setupDirectories() throws Exception {
        if (!mFilesDir.exists()) {
            if (!mFilesDir.mkdirs()) {
                throw new Exception("Failed to create files directory");
            }
        }
        
        if (!mHomeDir.exists()) {
            if (!mHomeDir.mkdirs()) {
                throw new Exception("Failed to create home directory");
            }
        }
        
        if (!mUsrDir.exists()) {
            if (!mUsrDir.mkdirs()) {
                throw new Exception("Failed to create usr directory");
            }
        }
        
        // Create .hushlogin to suppress login messages
        File hushLogin = new File(mHomeDir, ".hushlogin");
        if (!hushLogin.exists()) {
            hushLogin.createNewFile();
        }
    }
    
    /**
     * Install bootstrap package
     */
    public void installBootstrap(final BootstrapListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listener.onProgress(0, "Setting up directories...");
                    setupDirectories();
                    
                    String arch = getArchitecture();
                    String bootstrapFileName = "bootstrap-" + arch + ".zip";
                    String downloadUrl = BOOTSTRAP_BASE_URL + bootstrapFileName;
                    
                    listener.onProgress(10, "Downloading bootstrap for " + arch + "...");
                    File bootstrapZip = downloadBootstrap(downloadUrl, listener);
                    
                    listener.onProgress(60, "Extracting bootstrap...");
                    extractBootstrap(bootstrapZip, listener);
                    
                    listener.onProgress(90, "Setting permissions...");
                    setPermissions();
                    
                    // Clean up
                    bootstrapZip.delete();
                    
                    listener.onProgress(100, "Bootstrap installation complete");
                    listener.onComplete();
                    
                } catch (Exception e) {
                    Log.e(TAG, "Bootstrap installation failed", e);
                    listener.onError("Installation failed: " + e.getMessage());
                }
            }
        }).start();
    }
    
    /**
     * Download bootstrap package
     */
    private File downloadBootstrap(String urlString, BootstrapListener listener) throws Exception {
        File bootstrapZip = new File(mFilesDir, "bootstrap.zip");
        
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        
        int fileLength = connection.getContentLength();
        
        InputStream input = connection.getInputStream();
        OutputStream output = new FileOutputStream(bootstrapZip);
        
        byte[] buffer = new byte[4096];
        long total = 0;
        int count;
        
        while ((count = input.read(buffer)) != -1) {
            total += count;
            if (fileLength > 0) {
                int progress = 10 + (int) ((total * 50) / fileLength);
                listener.onProgress(progress, "Downloading: " + (total / 1024) + " KB");
            }
            output.write(buffer, 0, count);
        }
        
        output.flush();
        output.close();
        input.close();
        
        return bootstrapZip;
    }
    
    /**
     * Extract bootstrap package
     */
    private void extractBootstrap(File bootstrapZip, BootstrapListener listener) throws Exception {
        ZipInputStream zis = new ZipInputStream(new java.io.FileInputStream(bootstrapZip));
        ZipEntry entry;
        byte[] buffer = new byte[4096];
        
        while ((entry = zis.getNextEntry()) != null) {
            String entryName = entry.getName();
            File outputFile = new File(mUsrDir, entryName);
            
            if (entry.isDirectory()) {
                outputFile.mkdirs();
            } else {
                // Ensure parent directory exists
                outputFile.getParentFile().mkdirs();
                
                FileOutputStream fos = new FileOutputStream(outputFile);
                int count;
                while ((count = zis.read(buffer)) != -1) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
            }
            
            zis.closeEntry();
        }
        
        zis.close();
    }
    
    /**
     * Set proper permissions for bootstrap files
     */
    private void setPermissions() {
        setExecutablePermissions(new File(mUsrDir, "bin"));
        setExecutablePermissions(new File(mUsrDir, "libexec"));
    }
    
    private void setExecutablePermissions(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    file.setExecutable(true, false);
                    file.setReadable(true, false);
                } else if (file.isDirectory()) {
                    setExecutablePermissions(file);
                }
            }
        }
    }
    
    /**
     * Get the home directory
     */
    public File getHomeDir() {
        return mHomeDir;
    }
    
    /**
     * Get the usr directory
     */
    public File getUsrDir() {
        return mUsrDir;
    }
    
    /**
     * Get environment variables for terminal session
     */
    public String[] buildEnvironment() {
        return new String[] {
            "TERM=xterm-256color",
            "HOME=" + mHomeDir.getAbsolutePath(),
            "PATH=" + mUsrDir.getAbsolutePath() + "/bin",
            "PREFIX=" + mUsrDir.getAbsolutePath(),
            "TMPDIR=" + mUsrDir.getAbsolutePath() + "/tmp",
            "SHELL=" + mUsrDir.getAbsolutePath() + "/bin/sh",
            "LANG=en_US.UTF-8"
        };
    }
    
    /**
     * Get shell path
     */
    public String getShellPath() {
        File bash = new File(mUsrDir, "bin/bash");
        if (bash.exists()) {
            return bash.getAbsolutePath();
        }
        return new File(mUsrDir, "bin/sh").getAbsolutePath();
    }
}
