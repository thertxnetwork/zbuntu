# Contributing to Zbuntu Terminal

Thank you for your interest in contributing to Zbuntu Terminal! This document provides guidelines for contributing to the project.

## Code of Conduct

Please be respectful and constructive in all interactions. We aim to maintain a welcoming and inclusive environment.

## How to Contribute

### Reporting Bugs

1. Check if the bug has already been reported in GitHub Issues
2. Create a new issue with:
   - Clear description of the bug
   - Steps to reproduce
   - Expected vs actual behavior
   - Device/emulator information
   - Android version
   - App version
   - Relevant logs or screenshots

### Suggesting Features

1. Check if the feature has been requested
2. Create a new issue with:
   - Clear description of the feature
   - Use cases and benefits
   - Potential implementation approach
   - Any relevant examples from other apps

### Submitting Code

1. **Fork the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/zbuntu.git
   ```

2. **Create a branch**
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/your-bug-fix
   ```

3. **Make your changes**
   - Follow the coding style (see below)
   - Add comments for complex logic
   - Update documentation if needed
   - Add tests if applicable

4. **Test your changes**
   - Build the project successfully
   - Test on multiple devices/emulators
   - Test different Android versions
   - Verify no regressions

5. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat: add feature description"
   # or
   git commit -m "fix: fix bug description"
   ```

6. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

7. **Create a Pull Request**
   - Provide a clear description
   - Link related issues
   - Include screenshots if UI changes
   - Wait for review

## Coding Style

### Java Code

- Follow standard Java naming conventions
- Use camelCase for variables and methods
- Use PascalCase for classes
- Add JavaDoc for public methods
- Keep methods focused and concise
- Maximum line length: 120 characters

Example:
```java
/**
 * Creates a new terminal session.
 * 
 * @param client The session client for callbacks
 * @param rows Terminal height in rows
 * @param columns Terminal width in columns
 * @return A new TerminalSession instance
 */
public TerminalSession createSession(TerminalSessionClient client, int rows, int columns) {
    // Implementation
}
```

### C/C++ Code

- Follow Linux kernel style for C code
- Use snake_case for functions and variables
- Add comments for complex operations
- Check return values and handle errors
- Free allocated memory
- Close file descriptors

Example:
```c
/**
 * Create subprocess and return PTY file descriptor
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
    // Implementation
}
```

### XML Resources

- Use lowercase with underscores for IDs
- Group related attributes
- Add comments for complex layouts

Example:
```xml
<TextView
    android:id="@+id/status_text"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="8dp"
    android:text="@string/status_initializing"
    android:textColor="@color/text_primary"
    android:textSize="12sp" />
```

## Commit Message Format

Use conventional commits format:

```
<type>: <description>

[optional body]

[optional footer]
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Adding tests
- `chore`: Build process or tooling changes

Examples:
```
feat: add text selection support
fix: resolve PTY creation crash on Android 14
docs: update build instructions
refactor: simplify terminal rendering logic
```

## Testing

### Manual Testing

Before submitting:
1. Test on at least one physical device
2. Test on at least one emulator
3. Test both debug and release builds
4. Verify no crashes or ANRs
5. Check memory usage
6. Test different screen sizes

### Areas to Test

- Bootstrap installation
- Terminal session creation
- Text input/output
- Key event handling
- Process lifecycle
- Memory leaks
- Performance

## Documentation

Update documentation when:
- Adding new features
- Changing APIs
- Modifying build process
- Adding dependencies
- Changing architecture

Files to update:
- `README.md` - User-facing documentation
- `ARCHITECTURE.md` - Technical details
- `BUILD.md` - Build instructions
- Code comments - Implementation details

## Dependencies

When adding new dependencies:
1. Justify the need
2. Check license compatibility
3. Verify maintenance status
4. Consider APK size impact
5. Update build configuration

## Performance

Consider performance impact:
- Avoid blocking UI thread
- Use efficient data structures
- Minimize memory allocations
- Optimize rendering
- Profile if needed

## Security

Security considerations:
- Validate all inputs
- Handle permissions properly
- Avoid hardcoded secrets
- Use secure network connections
- Follow Android security best practices

## Review Process

1. Automated checks will run on PR
2. Maintainers will review code
3. Address feedback and comments
4. Make requested changes
5. Once approved, PR will be merged

## Questions?

If you have questions:
- Check existing documentation
- Search GitHub issues
- Create a new issue
- Tag with "question" label

## License

By contributing, you agree that your contributions will be licensed under the same license as the project.

## Thank You!

Your contributions help make Zbuntu Terminal better for everyone!
