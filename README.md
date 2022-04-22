# Requestly Android Interceptor
Requestly Android Interceptor lets you debug your android apps without needing you to setup any proxies or install any certificates everytime. All it requires is a one time setup of 3 lines of code.

## Installation

The best way to install the Requestly Android SDK is with a build system like Gradle. This ensures you can easily upgrade to the latest versions.

RQInterceptor is distributed through [Maven Central](https://search.maven.org/artifact/io.requestly.rqinterceptor/library). To use it you need to add the following Gradle dependency to your build.gradle file of you android app module (NOT the root file)

```
dependencies {
  debugImplementation "io.requestly.rqinterceptor:library:1.+"
  releaseImplementation "io.requestly.rqinterceptor:library-no-op:1.+"
}
```

## Configuration
To configure the Interceptor, you need to initialize the RQCollector with your SDK Key ([Steps to generate](https://docs.requestly.io/mobile-interceptor/tutorial#generate-sdk-key)) and then add rqInterceptor as the last interceptor on okHttpClient

### okHttp
```
val collector = RQCollector(context=appContext, sdkKey="<your-sdk-key>")

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
