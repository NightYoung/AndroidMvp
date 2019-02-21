package com.hsc.vince.androidmvp.net;

/**
 * <p>作者：Night  2019/1/14 19:35
 * <p>邮箱：codinghuang@163.com
 * <p>作用：
 * <p>描述：加密的Retrofit的单例和配置
 */
public class DecodeAppClient extends NetClient {

//    /**
//     * 应用的网络连接
//     */
//    DecodeAppClient() {
//        super();
//    }
//
//    private static volatile Retrofit instance = null;
//
//    /**
//     * 获取一个配置好的Retrofit单例
//     *
//     * @return Retrofit对象
//     */
//    public static Retrofit newInstance() {
//        if (null == instance) {
//            synchronized (Retrofit.class) {
//                if (instance == null) {
//
//                    OkHttpClient okHttpClient = getOkHttpClient();
//
//                    instance = new Retrofit.Builder()
//                            .baseUrl(ApiStores.API_SERVER_URL)
//                            .addConverterFactory(DecodeConverterFactory.create())
//                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
//                            .client(okHttpClient)
//                            .build();
//                }
//            }
//        }
//        return instance;
//    }
//
//
//    /**
//     * @return 配置后的OkHttpClient
//     */
//    private static OkHttpClient getOkHttpClient() {
//        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//
//
//        BasicParamsInterceptor basicParamsInterceptor =
//                new BasicParamsInterceptor.Builder()
//                        .addHeaderParamsMap(appHeader())
////                        .addEncryptBodyParamsMap(appBody())
//                        .addParamsMap(appBody())
//                        .build();
//
//
//        builder.addInterceptor(basicParamsInterceptor);
//
//        // Log信息拦截器
//        if (BuildConfig.DEBUG) {
//            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
//            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//            //设置 Debug Log 模式
//            builder.addInterceptor(loggingInterceptor);
//        }
//
//        //设置超时和重连
//        builder.connectTimeout(15, TimeUnit.SECONDS);
//        builder.readTimeout(20, TimeUnit.SECONDS);
//        builder.writeTimeout(20, TimeUnit.SECONDS);
//        //错误重连
//        builder.retryOnConnectionFailure(true);
//
//        return builder.build();
//    }

}
