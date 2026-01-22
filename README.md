## SDK Initialization

1. **Initialize Early**: Initialize SDK in `Application.onCreate()` (Android) or `AppDelegate` (iOS)
2. **Use Service Pattern**: Use service singleton for centralized access
3. **Store References**: Store client references for reuse
4. **Handle Errors**: Wrap initialization in error handling

## Error Handling

1. **Always Handle Errors**: Wrap all SDK calls in try-catch or Result handling
2. **Check Error Types**: Handle different error types appropriately
3. **Provide User Feedback**: Show user-friendly error messages
4. **Log Errors**: Log errors for debugging
5. **Implement Retry Logic**: For transient errors, implement retry logic
6. **Use Error Callbacks**: Implement error callbacks for installation errors

## Lifecycle Management

1. **Let SDK Manage**: Don't manually manage polling/renewal unless necessary
2. **Handle Errors**: Implement error callbacks for retry notifications
3. **Monitor Status**: Check profile status periodically
4. **Push Notifications**: Use push notifications for immediate updates when available
5. **Background Tasks**: Ensure background tasks are properly configured (iOS)

## Security

1. **Secure Storage**: SDK uses encrypted storage - don't store credentials elsewhere
2. **Token Management**: Let SDK manage tokens - don't store tokens manually
3. **Credential Handling**: Never log or expose credentials
4. **Network Security**: All API calls use HTTPS

## Performance

1. **Reuse Clients**: Create clients once and reuse
2. **Async Operations**: Use async/await (iOS) or coroutines (Android)
3. **Background Operations**: Use background tasks for polling
4. **Cache Usage**: Trust SDK cache - don't duplicate caching

## Testing

1. **Test Permissions**: Test permission flows
2. **Test Errors**: Test error handling paths
3. **Test Lifecycle**: Test polling and renewal
4. **Test on Device**: Test on physical devices (especially iOS)

## Related Documentation

- [Getting Started - Android](../android/getting-started.md)
- [Getting Started - iOS](../ios/getting-started.md)
- [Error Handling](error-handling.md)
- [Lifecycle Management](lifecycle-management.md)
