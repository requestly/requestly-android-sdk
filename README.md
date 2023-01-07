<p align="center"><img src="https://user-images.githubusercontent.com/16779465/194505910-b6a7be70-df20-4b1a-9730-06a48cdd75ac.png" alt="Requestly Logo" width="50%"/></p>

<h2 align="center">Chrome like developer tool for Android debug builds</h2>

<p align="center">
    <a href="https://github.com/requestly/requestly-android-sdk/releases"><img alt="GitHub release" src="https://img.shields.io/github/release/requestly/requestly-android-sdk.svg"></a>
    <a href="https://search.maven.org/search?q=io.requestly%20-library"><img alt="Maven" src="https://img.shields.io/maven-metadata/v.svg?label=maven-central&color=green&logo=apachemaven&metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fio%2Frequestly%2Frequestly-android%2Fmaven-metadata.xml"></a>
    <a href="https://bit.ly/requestly-slack"><img alt="Maven" src="https://img.shields.io/badge/slack-@requestly--community-purple.svg?logo=slack"></a>
</p>

<p align="center">
    <img height="500" alt="Api Debugger" src="./assets/quick-preview.png">
</p>

Requestly Android SDK lets you debug your android apps without needing you to setup any proxies or install any certificates everytime. It makes easy to identify & debug your Android Apps faster and save your time.

- [Try Now](#try-now)
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
- [Acknowledgments](#acknowledgments)

## Try Now
### ↓ Click on Screenshots to try Apps ↓
<a href="https://appetize.io/embed/ctumozxsaz6cj4wox36vjc7jwy?device=pixel6pro&osVersion=12.0&scale=75&deviceColor=black" target="_blank">
    <img width="250" alt="Try Now" src="https://user-images.githubusercontent.com/16779465/202981047-81ae477c-9ff8-49ed-9c03-0140bd9bb537.png">
</a>
<a href="https://appetize.io/embed/s3w5vwumwjtvj4kn2xsez5b4cy?device=pixel6pro&osVersion=12.0&scale=75&deviceColor=black" target="_blank">
    <img width="250" alt="Try Now" src="https://user-images.githubusercontent.com/16779465/202981055-63253e28-0a7e-4ebc-889e-672f12a38dbc.png">
</a>
<a href="https://appetize.io/embed/hngjeexnhwrcm3acdclsqkye6e?device=pixel6pro&osVersion=12.0&scale=75&deviceColor=black" target="_blank">
    <img width="250" alt="Try Now" src="https://user-images.githubusercontent.com/16779465/202981457-7c5f57fe-3216-4f9e-b55e-89c9f71c514a.png">
</a>

### OR
- [Dowload CryptoDemo Apk](https://github.com/requestly/android-debugger-examples/releases/latest/download/cryptodemo-debug.apk)
- [Download Infinity Reddit Apk](https://github.com/requestly/android-debugger-examples/releases/latest/download/infinity-reddit-debug.apk)
- [Download Pokedex Apk](https://github.com/requestly/android-debugger-examples/releases/latest/download/pokedex-debug.apk)

## Installation
The best way to install the Requestly Android SDK is with a build system like Gradle. This ensures you can easily upgrade to the latest versions.

RQInterceptor is distributed through [Maven Central](https://search.maven.org/search?q=io.requestly%20-library). To use it you need to add the following Gradle dependency to your build.gradle file of you android app module (NOT the root file)

```
dependencies {
    debugImplementation "io.requestly:requestly-android:2.4.8"
    releaseImplementation "io.requestly:requestly-android-noop:2.4.8"
    debugImplementation "io.requestly:requestly-android-okhttp:2.4.8"
    releaseImplementation "io.requestly:requestly-android-okhttp-noop:2.4.8"
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

## Acknowledgments
Special Thanks to [chuckerteam](https://github.com/chuckerteam/chucker) for maintaining such an awesome project [Chucker](https://github.com/chuckerteam/chucker) because of which requestly-android-sdk was possible.
