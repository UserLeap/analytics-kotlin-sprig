# Analytics-Kotlin Sprig
Add Sprig `device-mode` support to your application with this destination

## Adding the dependency
The Sprig Segment destination is distributed via Maven Central.
1. Add `mavenCentral()` to your project's root `build.gradle` `repositories` block
2. In your application module's `build.gradle` file, add the dependency:
```groovy
dependencies {
    implementation "com.userleap:segment-analytics-kotlin-destination:$version"
}
```
*Note the Sprig library itself will be installed as an additional dependency.*

## Using the destination in your App

Select your Android Source from `My Sources` dashboard on segment, and get the value for `Write Key` from `Settings -> API Keys`

Add the Sprig destination where Segment is configured. Either in your Application class:

```kotlin
analytics = Analytics(BuildConfig.SEGMENT_WRITE_KEY, this) {
    // Automatically track Lifecycle events
    trackApplicationLifecycleEvents = true
    flushAt = 3
    flushInterval = 10
}.add(SprigDestination(application = this))
```

or in an Activity class:
```kotlin
analytics = Analytics(BuildConfig.SEGMENT_WRITE_KEY, this) {
    // Automatically track Lifecycle events
    trackApplicationLifecycleEvents = true
    flushAt = 3
    flushInterval = 10
}.add(SprigDestination(application = applicationContext))
```

Your events will now begin to flow to Sprig in device mode.

When you track an event and receive a survey, the survey will show on the top activity.

To record attributes, you can make use of the traits parameter when you call the [identify](https://segment.com/docs/connections/sources/catalog/libraries/mobile/kotlin-android/#identify) function.
Download an [example](https://github.com/UserLeap/analytics-kotlin-sprig/tree/main/example/) app for reference.

## Support

Please use Github issues, Pull Requests, or feel free to reach out to our [support team](https://segment.com/help/).
