# RQ Interceptor

https://docs.requestly.io/interceptor/android-interceptor


## Add Gradle Dependency

RQInterceptor is distributed through Maven Central. To use it you need to add the following Gradle dependency to your build.gradle file of you android app module (NOT the root file)

```
dependencies {
    debugImplementation "io.requestly.rqinterceptor:library:1.0.0"
    releaseImplementation "io.requestly.rqinterceptor:library-no-op:1.0.0"
}
```


## Add RQInterceptor to OkHttp Client
```
val collector = RQCollector(
    context = appContext,
    sdkKey = "<your-sdk-key>",
)
val rqInterceptor = RQInterceptor.Builder(appContext)
    .collector(collector)
    .build()

val client = OkHttpClient.Builder()
    .addInterceptor(rqInterceptor)
    .build()
```

## Acknowledgments
Special Thanks to chuckerteam for maintaining such an awesome project because of which rq-interceptor was possible
https://github.com/chuckerteam/chucker
