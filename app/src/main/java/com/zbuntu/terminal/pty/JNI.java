package com.zbuntu.terminal.pty;

/**
 * JNI bridge to native PTY functions
 */
public class JNI {
    static {
        System.loadLibrary("termux-native");
    }

    /**
     * Create a subprocess with a PTY
     * @param cmd Command to execute
     * @param cwd Working directory
     * @param args Command arguments
     * @param envVars Environment variables (KEY=VALUE format)
     * @param processId Output array to receive process ID
     * @param rows Terminal rows
     * @param columns Terminal columns
     * @return File descriptor for PTY master, or -1 on error
     */
    public static native int createSubprocess(
        String cmd,
        String cwd,
        String[] args,
        String[] envVars,
        int[] processId,
        int rows,
        int columns
    );

    /**
     * Wait for subprocess to complete
     * @param pid Process ID
     * @return Exit code
     */
    public static native int waitFor(int pid);

    /**
     * Close a file descriptor
     * @param fd File descriptor to close
     */
    public static native void close(int fd);

    /**
     * Get version information
     * @return Version string
     */
    public static native String getVersionInfo();
}
