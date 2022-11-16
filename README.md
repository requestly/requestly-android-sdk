<p align="center"><img src="https://user-images.githubusercontent.com/16779465/194505910-b6a7be70-df20-4b1a-9730-06a48cdd75ac.png" alt="Requestly Logo" width="50%"/></p>

<h2 align="center">Chrome like developer tool for Android debug builds</h2>

<p align="center">
    <a href="https://github.com/requestly/requestly-android-sdk/releases"><img alt="GitHub release" src="https://img.shields.io/github/release/requestly/requestly-android-sdk.svg"></a>
    <a href="https://search.maven.org/search?q=io.requestly%20-library"><img alt="Maven" src="https://img.shields.io/badge/-Maven-green?logo=apachemaven"></a>
</p>

<p align="center">
    <img height="500" alt="Api Debugger" src="./assets/quick-preview.png">
</p>

Requestly Android SDK lets you debug your android apps without needing you to setup any proxies or install any certificates everytime. It makes easy to identify & debug your Android Apps faster and save your time.

- [Installation](#installation)
- [SDK Initialization](#sdk-initialization)
- [API Debugger Initialization](#api-debugger-initialization)
  - [okHttp](#okhttp)
  - [Retrofit](#retrofit)
- [Features](#features)
  - [API Debugger](#api-debugger)
  - [Analytics Event Debugger](#analytics-event-debugger)
  - [Logs Debugger](#logs-debugger)
  - [Host Switcher](#host-switcher)
- [Try Now](#try-now)
- [Acknowledgments](#acknowledgments)
  
## Installation
The best way to install the Requestly Android SDK is with a build system like Gradle. This ensures you can easily upgrade to the latest versions.

RQInterceptor is distributed through [Maven Central](https://search.maven.org/search?q=io.requestly%20-library). To use it you need to add the following Gradle dependency to your build.gradle file of you android app module (NOT the root file)

```
dependencies {
    debugImplementation "io.requestly:requestly-android:2.4.3"
    releaseImplementation "io.requestly:requestly-android-noop:2.4.3"
    debugImplementation "io.requestly:requestly-android-okhttp:2.4.3"
    releaseImplementation "io.requestly:requestly-android-okhttp-noop:2.4.3"
}
```

## SDK Initialization
Initialize the Requestly SDK in your Application class onCreate method.

```
class App : Application(){
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Requestly SDK like this
        Requestly.Builder(this, [optional "<your-sdk-key>"])
            .build()
    }
}
```
> `sdk-key` is optional. You can use local devtool features without sdk-key. <br/>
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

### Analytics Event Debugger
Debug & Validate your Analytics Events directly from your App. The SDK provides a simple API to send your events.

```kotlin
RequestlyEvent.send(<eventName: String>, <eventData:Map<String, Any>>)
```

<img width="400" alt="Events Debugger" src="./assets/events-debugger.png">

### Logs Debugger
Debug your Logs directly from your App. No need to connect your device to your computer to know what's happening inside your app.

<img width="400" alt="Logs Debugger" src="./assets/logs-debugger.png">

### Host Switcher
Switch between production and staging APIs easily in your Android debug builds. Eg. api.requestly.io → staging.requestly.io

<img width="400" alt="Logs Debugger" src="./assets/host-switcher.jpeg">

## Try Now

<a href="https://appetize.io/embed/ctumozxsaz6cj4wox36vjc7jwy?device=pixel6pro&osVersion=12.0&scale=75&deviceColor=black"><img width="300" alt="Try Now" src="https://user-images.githubusercontent.com/16779465/202178671-41f03b5d-a4c8-44c8-a7ff-0acbc0fd9451.png"></a>

## Acknowledgments
Special Thanks to chuckerteam for maintaining such an awesome project because of which rq-interceptor was possible
https://github.com/chuckerteam/chucker
