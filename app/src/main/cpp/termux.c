#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <termios.h>
#include <sys/ioctl.h>
#include <sys/wait.h>
#include <errno.h>
#include <android/log.h>

#define LOG_TAG "Zbuntu-Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

/**
 * Create subprocess and return PTY file descriptor
 * Returns the file descriptor of the master PTY side, or -1 on error
 */
static int create_subprocess(
    JNIEnv* env,
    const char* cmd,
    const char* cwd,
    char* const argv[],
    char** envp,
    int* pProcessId,
    int rows,
    int columns
) {
    int ptm = open("/dev/ptmx", O_RDWR | O_CLOEXEC);
    if (ptm < 0) {
        LOGE("Cannot open /dev/ptmx: %s", strerror(errno));
        return -1;
    }

    // Grant access to slave PTY
    if (grantpt(ptm) < 0 || unlockpt(ptm) < 0) {
        LOGE("grantpt/unlockpt failed: %s", strerror(errno));
        close(ptm);
        return -1;
    }

    // Get slave PTY name
    char* devname = ptsname(ptm);
    if (devname == NULL) {
        LOGE("ptsname failed: %s", strerror(errno));
        close(ptm);
        return -1;
    }

    // Set terminal size
    struct winsize sz = {
        .ws_row = (unsigned short)rows,
        .ws_col = (unsigned short)columns
    };
    ioctl(ptm, TIOCSWINSZ, &sz);

    pid_t pid = fork();
    if (pid < 0) {
        LOGE("Fork failed: %s", strerror(errno));
        close(ptm);
        return -1;
    }

    if (pid > 0) {
        // Parent process
        *pProcessId = (int)pid;
        return ptm;
    }

    // Child process
    setsid();

    // Open slave PTY
    int pts = open(devname, O_RDWR);
    if (pts < 0) {
        LOGE("Cannot open slave PTY: %s", strerror(errno));
        exit(-1);
    }

    // Redirect stdin, stdout, stderr to PTY
    dup2(pts, 0);
    dup2(pts, 1);
    dup2(pts, 2);

    // Close original PTY fds
    close(ptm);
    close(pts);

    // Clear signal handlers
    for (int i = 1; i < NSIG; i++) {
        signal(i, SIG_DFL);
    }

    // Change working directory
    if (cwd != NULL && chdir(cwd) != 0) {
        LOGE("chdir failed: %s", strerror(errno));
    }

    // Set environment variables
    if (envp != NULL) {
        for (int i = 0; envp[i] != NULL; i++) {
            char* var = strdup(envp[i]);
            char* eq = strchr(var, '=');
            if (eq != NULL) {
                *eq = '\0';
                setenv(var, eq + 1, 1);
            }
            free(var);
        }
    }

    // Execute command
    execvp(cmd, argv);
    
    // If exec fails
    LOGE("exec failed: %s", strerror(errno));
    exit(-1);
}

/**
 * JNI function to create subprocess
 */
JNIEXPORT jint JNICALL
Java_com_zbuntu_terminal_pty_JNI_createSubprocess(
    JNIEnv* env,
    jclass clazz,
    jstring cmd,
    jstring cwd,
    jobjectArray args,
    jobjectArray envVars,
    jintArray processId,
    jint rows,
    jint columns
) {
    // Convert Java strings to C strings
    const char* cmd_str = (*env)->GetStringUTFChars(env, cmd, NULL);
    const char* cwd_str = (cwd == NULL) ? NULL : (*env)->GetStringUTFChars(env, cwd, NULL);

    // Convert arguments array
    jsize args_len = (*env)->GetArrayLength(env, args);
    char** argv = (char**)malloc((args_len + 1) * sizeof(char*));
    for (int i = 0; i < args_len; i++) {
        jstring arg = (jstring)(*env)->GetObjectArrayElement(env, args, i);
        argv[i] = (char*)(*env)->GetStringUTFChars(env, arg, NULL);
    }
    argv[args_len] = NULL;

    // Convert environment variables
    char** envp = NULL;
    if (envVars != NULL) {
        jsize env_len = (*env)->GetArrayLength(env, envVars);
        envp = (char**)malloc((env_len + 1) * sizeof(char*));
        for (int i = 0; i < env_len; i++) {
            jstring env_var = (jstring)(*env)->GetObjectArrayElement(env, envVars, i);
            envp[i] = (char*)(*env)->GetStringUTFChars(env, env_var, NULL);
        }
        envp[env_len] = NULL;
    }

    // Create subprocess
    int pid = 0;
    int ptm = create_subprocess(env, cmd_str, cwd_str, argv, envp, &pid, rows, columns);

    // Store process ID
    if (processId != NULL) {
        jint* pid_ptr = (*env)->GetIntArrayElements(env, processId, NULL);
        pid_ptr[0] = pid;
        (*env)->ReleaseIntArrayElements(env, processId, pid_ptr, 0);
    }

    // Clean up
    (*env)->ReleaseStringUTFChars(env, cmd, cmd_str);
    if (cwd_str != NULL) {
        (*env)->ReleaseStringUTFChars(env, cwd, cwd_str);
    }
    for (int i = 0; i < args_len; i++) {
        jstring arg = (jstring)(*env)->GetObjectArrayElement(env, args, i);
        (*env)->ReleaseStringUTFChars(env, arg, argv[i]);
    }
    free(argv);

    if (envp != NULL) {
        jsize env_len = (*env)->GetArrayLength(env, envVars);
        for (int i = 0; i < env_len; i++) {
            jstring env_var = (jstring)(*env)->GetObjectArrayElement(env, envVars, i);
            (*env)->ReleaseStringUTFChars(env, env_var, envp[i]);
        }
        free(envp);
    }

    return ptm;
}

/**
 * JNI function to wait for subprocess completion
 */
JNIEXPORT jint JNICALL
Java_com_zbuntu_terminal_pty_JNI_waitFor(
    JNIEnv* env,
    jclass clazz,
    jint pid
) {
    int status;
    waitpid(pid, &status, 0);
    if (WIFEXITED(status)) {
        return WEXITSTATUS(status);
    } else if (WIFSIGNALED(status)) {
        return -WTERMSIG(status);
    }
    return 0;
}

/**
 * JNI function to close file descriptor
 */
JNIEXPORT void JNICALL
Java_com_zbuntu_terminal_pty_JNI_close(
    JNIEnv* env,
    jclass clazz,
    jint fd
) {
    close(fd);
}
