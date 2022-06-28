# Requestly Android SDK
Requestly Android SDK lets you debug your android apps without needing you to setup any proxies or install any certificates everytime. All it requires is a one time setup of 5 lines of code.

## Installation
The best way to install the Requestly Android SDK is with a build system like Gradle. This ensures you can easily upgrade to the latest versions.

RQInterceptor is distributed through [Maven Central](https://search.maven.org/search?q=io.requestly%20-library). To use it you need to add the following Gradle dependency to your build.gradle file of you android app module (NOT the root file)

```
dependencies {
    debugImplementation "io.requestly:requestly-android:2.0.0"
    releaseImplementation "io.requestly:requestly-android-noop:2.0.0"
    debugImplementation "io.requestly:requestly-android-okhttp:2.0.0"
    releaseImplementation "io.requestly:requestly-android-okhttp-noop:2.0.0"
}
```

## Initialization
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

## Interceptor Initialization
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
## Acknowledgments
Special Thanks to chuckerteam for maintaining such an awesome project because of which rq-interceptor was possible
https://github.com/chuckerteam/chucker
