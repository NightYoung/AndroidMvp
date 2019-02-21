package com.hsc.vince.androidmvp.net;

import android.os.Build;
import android.support.annotation.NonNull;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.hsc.vince.androidmvp.BuildConfig;
import com.hsc.vince.androidmvp.constant.BoreConstants;
import com.hsc.vince.androidmvp.net.converter.DecodeConverterFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * <p>作者：Night  2018/4/11 11:53
 * <p>邮箱：codinghuang@163.com
 * <p>作用：Retrofit
 * <p>描述：网络请求单例实现
 */
public class NetClient {
    /*** 语言环境 默认zh_cn*/
    private static final String APP_LANG = "app_lang";
    /*** 客户端来源 1.web、2.iOS、3.Android*/
    private static final String SOURCE = "source";
    /*** 设备唯一标示*/
    private static final String DEVICE_SN = "sn";
    /*** 软件对外版本号*/
    private static final String APP_VERSION = "version";
    /*** 软件对内版本号*/
    private static final String VERSION_CODE = "version_code";
    /*** 设备类型 iPhone 华为 魅族 小米*/
    private static final String DEVICE_TYPE = "device_type";
    /*** 设备版本 MI 6*/
    private static final String DEVICE_VERSION = "device_version";
    /*** 设备系统版本*/
    private static final String SYS_VERSION = "device_system_version";
    /** 系统来源 */
    private static final String SOURCE_ANDROID = "3";

    /*** 请求头token的key*/
    public static final String TOKEN_KEY = "Authorization";
    /*** 请求头token的value前半部*/
    public static final String TOKEN_VALUE_START = "Authorization: bearer ";

    NetClient() {
        throw new Error("illegal operation!");
    }

    private static volatile Retrofit mInstance = null;
    private static OkHttpClient mHttpClient = null;

    /*** Retrofit 单例实现
     * @return Retrofit单例*/
    public static Retrofit newInstance() {
        if (null == mInstance) {
            synchronized (Retrofit.class) {
                if (mInstance == null) {

                    mHttpClient = getOkHttpClient();
                    mInstance = new Retrofit.Builder()
                            .baseUrl(ApiStore.API_SERVICE_URL)
                            .addConverterFactory(DecodeConverterFactory.create())
                            //.addConverterFactory(GsonConverterFactory.create())
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .client(mHttpClient)
                            .build();
                }
            }
        }

        return mInstance;
    }

    /*** 重置Retrofit*/
    public static void reset() {

        mInstance = null;
    }

    /*** 自定义Client
     * @return 自定义Client*/
    private static OkHttpClient getOkHttpClient() {
        OkHttpClient.Builder builder = getBuilder();
        if (BuildConfig.DEBUG) {
            return builder
                    .addNetworkInterceptor(new StethoInterceptor())
                    .build();
        } else {

            return builder.build();
        }
    }

    /***  获取自定义Builder
     * @return 自定义Builder*/
    @NonNull
    private static OkHttpClient.Builder getBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        //拦截器，添加统一头信息
        BaseParamsInterceptor basicParamsInterceptor =
                new BaseParamsInterceptor.Builder()
                        .addHeaderParamsMap(appHeader())
                        .addParamsMap(appBody())
                        .build();
        builder.addInterceptor(basicParamsInterceptor);

        //调试添加拦截器
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            //设置 Debug Log 模式
            builder.addInterceptor(loggingInterceptor);
        }

        //设置超时和重连
        builder.connectTimeout(15, TimeUnit.SECONDS);
        builder.readTimeout(20, TimeUnit.SECONDS);
        builder.writeTimeout(20, TimeUnit.SECONDS);
        //错误重连
        builder.retryOnConnectionFailure(true);
        return builder;
    }

    /*** 添加统一头信息
     * @return 头信息*/
    static Map<String, String> appHeader() {
        Map<String, String> property = new HashMap<>();
        property.put(TOKEN_KEY, "token information");
        //property.put("key", "value")
        //property.put("key", "value")
        return property;
    }

    /*** 统一的body
     *  @return body信息*/
    public static Map<String, String> appBody() {
        Map<String, String> preperty = new HashMap<>();

        if (BoreConstants.isUnittest) {
            preperty.put(APP_VERSION, "3.0.0");
            preperty.put(VERSION_CODE, "3.0.0");
            preperty.put(SYS_VERSION, "7.0");
            preperty.put(DEVICE_TYPE, "mi");
            preperty.put(DEVICE_SN, "wdc");
            preperty.put(SOURCE, "3");
            preperty.put(DEVICE_VERSION, "mi4");
            preperty.put(APP_LANG, "zh_cn");
        } else {
            //设置语言类型
            preperty.put(APP_LANG, "zh_cn");
            preperty.put(SOURCE, SOURCE_ANDROID);
            preperty.put(DEVICE_SN, "wdc");
            preperty.put(APP_VERSION, BuildConfig.VERSION_NAME);
            preperty.put(VERSION_CODE, String.valueOf(BuildConfig.VERSION_CODE));
            preperty.put(DEVICE_TYPE, Build.BRAND);
            preperty.put(DEVICE_VERSION, Build.MODEL);
            preperty.put(SYS_VERSION, String.valueOf(Build.VERSION.SDK_INT));
        }
        return preperty;
    }
}
