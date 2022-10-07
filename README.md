<p align="center"><img src="https://user-images.githubusercontent.com/16779465/194505910-b6a7be70-df20-4b1a-9730-06a48cdd75ac.png" alt="Requestly Logo" width="50%"/></p>

<h2 align="center">Chrome like developer tool for Android debug builds</h2>

[![GitHub release](https://img.shields.io/github/release/requestly/requestly-android-sdk.svg)](https://github.com/requestly/requestly-android-sdk/releases)
[![Maven](https://img.shields.io/badge/-Maven-green?logo=apachemaven)](https://search.maven.org/search?q=io.requestly%20-library)

Requestly Android SDK lets you debug your android apps without needing you to setup any proxies or install any certificates everytime. It makes easy to identify & debug your Android Apps faster and save your time.

- [Installation](#installation)
- [SDK Initialization](#sdk-initialization)
- [API Debugger Initialization](#api-debugger-initialization)
  - [okHttp](#okhttp)
  - [Retrofit](#retrofit)
- [Features](#features)
  - [API Debugger](#api-debugger)
  - [Analytics Event Debugger](#analytics-event-debugger)
  - [Logs Debugger](#logs-debugger-coming-soon)
- [Acknowledgments](#acknowledgments)
  
## Installation
The best way to install the Requestly Android SDK is with a build system like Gradle. This ensures you can easily upgrade to the latest versions.

RQInterceptor is distributed through [Maven Central](https://search.maven.org/search?q=io.requestly%20-library). To use it you need to add the following Gradle dependency to your build.gradle file of you android app module (NOT the root file)

```
dependencies {
    debugImplementation "io.requestly:requestly-android:2.2.0"
    releaseImplementation "io.requestly:requestly-android-noop:2.2.0"
    debugImplementation "io.requestly:requestly-android-okhttp:2.2.0"
    releaseImplementation "io.requestly:requestly-android-okhttp-noop:2.2.0"
}
```

## SDK Initialization
Initialize the Requestly SDK in your Application class onCreate method.

```
class App : Application(){
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Requestly SDK like this
        Requestly.Builder(this, "<your-sdk-key>")
            .build()
    }
}
```

To get the sdk key, you need to create an app. Follow the steps [here](https://docs.requestly.io/android/tutorial/create-app) to create an app.

## API Debugger Initialization
To configure the Interceptor, you need to initialize the RQCollector and then add rqInterceptor as the last interceptor to your okHttpClient

### okHttp
```
val collector = RQCollector(context=appContext)

val rqInterceptor = RQInterceptor.Builder(appContext)
    .collector(collector)
    .build()
    
val client = OkHttpClient.Builder()
    .addInterceptor(rqInterceptor)
    .build()
```

### Retrofit
```
Retrofit.Builder()
    .baseUrl(APIUtils.API_BASE_URI)
    .client(okHttpClient) // okHttpClient with RQInterceptor
    .build();
```

## Features

### API Debugger
Lets you view and modify HTTP traffic. It comes with these capabilities to:
- **InApp Inspector** : Directly view your HTTP request from your phone.
- **Modify API**: Modify Response, Redirect Request, Delay Request and many more.

<img width="400" alt="Api Debugger" src="./assets/api-debugger.png">

<br />


### Analytics Event Debugger
Debug & Validate your Analytics Events directly from your App. The SDK provides a simple API to send your events.

```kotlin
RequestlyEvent.send(<eventName: String>, <eventData:Map<String, Any>>)
```

<img width="400" alt="Events Debugger" src="./assets/events-debugger.png">

<br />

### Logs Debugger (Coming Soon)
Debug your Logs directly from your App. No need to connect your device to your computer to know what's happening inside your app.

<img width="400" alt="Logs Debugger" src="./assets/logs-debugger.png">

<br />

## Acknowledgments
Special Thanks to chuckerteam for maintaining such an awesome project because of which rq-interceptor was possible
https://github.com/chuckerteam/chucker
