package com.hsc.vince.androidmvp.net;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.facebook.stetho.common.LogUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hsc.vince.androidmvp.constant.AppConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * <p>作者：Night  2018/4/11 15:45
 * <p>邮箱：codinghuang@163.com
 * <p>作用：
 * <p>描述：OkHttp拦截器
 */
public class BaseParamsInterceptor implements Interceptor {
    private static final String POST = "POST";

    private Map<String, String> queryParamsMap = new HashMap<>();
    private Map<String, String> paramsMap = new HashMap<>();
    private Map<String, String> encryptParamsMap = new HashMap<>();
    private Map<String, String> headerParamsMap = new HashMap<>();
    private List<String> headerLinesList = new ArrayList<>();

    /*** 构造器*/
    private BaseParamsInterceptor() {

    }

    /**
     * 转化RequestBody 为String
     *
     * @param request
     *         请求体
     * @return String
     */
    private static String bodyToString(final RequestBody request) {
        try (Buffer buffer = new Buffer()) {
            if (request != null)
                request.writeTo(buffer);
            else
                return "";
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        Request request = chain.request();
        Request.Builder requestBuilder = request.newBuilder();

        // process header params inject
        Headers.Builder headerBuilder = request.headers().newBuilder();
        headerParamsMap = NetClient.appHeader();
        if (headerParamsMap.size() > 0) {
            for (Object o : headerParamsMap.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                headerBuilder.add((String) entry.getKey(), (String) entry.getValue());
            }

        }

        if (!headerLinesList.isEmpty()) {
            for (String line : headerLinesList) {
                headerBuilder.add(line);
            }
        }
        requestBuilder.headers(headerBuilder.build());
        // process header params end

        // process queryParams inject whatever it's GET or POST
        if (queryParamsMap.size() > 0) {
            request = injectParamsIntoUrl(request.url().newBuilder(), requestBuilder, queryParamsMap);
        }

        // process post encrypt body inject
        if (request != null && encryptParamsMap != null && encryptParamsMap.size() > 0 && request.method().equals(POST)) {
            if (request.body() instanceof FormBody) {
                FormBody.Builder newFormBodyBuilder = new FormBody.Builder();
                HashMap<String, String> bodyMap = new HashMap<>();
                for (Object o : encryptParamsMap.entrySet()) {
                    Map.Entry entry = (Map.Entry) o;
                    bodyMap.put((String) entry.getKey(), (String) entry.getValue());
                }

                FormBody oldFormBody = (FormBody) request.body();
                if (oldFormBody != null && oldFormBody.size() > 0) {
                    for (int i = 0; i < oldFormBody.size(); i++) {
                        bodyMap.put(oldFormBody.name(i), oldFormBody.value(i));
                    }
                }
                Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                String info = "";
                try {
                    info = AESCrypt.getInstance().encrypt(gson.toJson(bodyMap));
                } catch (Exception e) {
                    LogUtil.e(e.getMessage());
                }
                newFormBodyBuilder.add(AppConstants.ENCRYPT_INFO, info);

                requestBuilder.post(newFormBodyBuilder.build());
            }

        }
        // process post body inject
        if (request != null && paramsMap != null && paramsMap.size() > 0 && request.method().equals(POST)) {
            if (request.body() instanceof FormBody) {
                FormBody.Builder newFormBodyBuilder = new FormBody.Builder();
                for (Object o : paramsMap.entrySet()) {
                    Map.Entry entry = (Map.Entry) o;
                    newFormBodyBuilder.add((String) entry.getKey(), (String) entry.getValue());
                }

                FormBody oldFormBody = (FormBody) request.body();
                int paramSize = oldFormBody.size();
                for (int i = 0; i < paramSize; i++) {
                    newFormBodyBuilder.add(oldFormBody.name(i), oldFormBody.value(i));
                }
                requestBuilder.post(newFormBodyBuilder.build());
            } else if (request.body() instanceof MultipartBody) {
                MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

                for (Object o : paramsMap.entrySet()) {
                    Map.Entry entry = (Map.Entry) o;
                    multipartBuilder.addFormDataPart((String) entry.getKey(), (String) entry.getValue());
                }

                List<MultipartBody.Part> oldParts = ((MultipartBody) request.body()).parts();
                for (MultipartBody.Part part : oldParts) {
                    multipartBuilder.addPart(part);
                }
                requestBuilder.post(multipartBuilder.build());
            }

        }
        request = requestBuilder.build();
        return chain.proceed(request);
    }

    /**
     * 判断是否能添加参数到body
     *
     * @param request
     *         请求体
     * @return boolean
     */
    private boolean canInjectIntoBody(Request request) {
        if (request == null) {
            return false;
        }
        if (!TextUtils.equals(request.method(), POST)) {
            return false;
        }
        RequestBody body = request.body();
        if (body == null) {
            return false;
        }
        MediaType mediaType = body.contentType();
        if (mediaType == null) {
            return false;
        }
        return TextUtils.equals(mediaType.subtype(), "x-www-form-urlencoded");
    }

    /**
     * 添加params到url
     *
     * @param httpUrlBuilder
     *         地址
     * @param requestBuilder
     *         请求
     * @param paramsMap
     *         键值对
     * @return Request
     */
    private Request injectParamsIntoUrl(HttpUrl.Builder httpUrlBuilder, Request.Builder requestBuilder, Map<String, String> paramsMap) {
        if (paramsMap.size() > 0) {
            for (Object o : paramsMap.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                httpUrlBuilder.addQueryParameter((String) entry.getKey(), (String) entry.getValue());
            }
            requestBuilder.url(httpUrlBuilder.build());
            return requestBuilder.build();
        }

        return null;
    }

    /**
     * BasicParamsInterceptor 构造器
     */
    public static class Builder {

        BaseParamsInterceptor interceptor;

        /*** 构造器*/
        Builder() {
            interceptor = new BaseParamsInterceptor();
        }

        /**
         * 添加键值对到请求地址
         *
         * @param key
         *         键
         * @param value
         *         值
         * @return Builder
         */
        public Builder addParam(String key, String value) {
            interceptor.paramsMap.put(key, value);
            return this;
        }

        /**
         * 添加多个键值对到请求地址
         *
         * @param paramsMap
         *         多个键值对
         * @return Builder
         */
        Builder addParamsMap(Map<String, String> paramsMap) {
            interceptor.paramsMap.putAll(paramsMap);
            return this;
        }

        /**
         * 添加请求头信息
         *
         * @param key
         *         键
         * @param value
         *         值
         * @return Builder
         */
        public Builder addHeaderParam(String key, String value) {
            interceptor.headerParamsMap.put(key, value);
            return this;
        }

        /**
         * 添加请求头信息
         *
         * @param headerParamsMap
         *         请求头信息,map形式
         * @return Builder
         */
        Builder addHeaderParamsMap(Map<String, String> headerParamsMap) {
            interceptor.headerParamsMap.putAll(headerParamsMap);
            return this;
        }

        /**
         * 添加请求头信息
         *
         * @param headerLine
         *         请求头信息，String 格式如 key:value
         * @return Builder
         */
        public Builder addHeaderLine(String headerLine) {
            int index = headerLine.indexOf(':');
            if (index == -1) {
                throw new IllegalArgumentException("Unexpected header: " + headerLine);
            }
            interceptor.headerLinesList.add(headerLine);
            return this;
        }

        /**
         * 添加请求头信息
         *
         * @param headerLinesList
         *         请求头信息集合,String 格式如 key:value
         * @return Builder
         */
        public Builder addHeaderLinesList(List<String> headerLinesList) {
            for (String headerLine : headerLinesList) {
                int index = headerLine.indexOf(':');
                if (index == -1) {
                    throw new IllegalArgumentException("Unexpected header: " + headerLine);
                }
                interceptor.headerLinesList.add(headerLine);
            }
            return this;
        }

        /**
         * 添加body的Param
         *
         * @param key
         *         键
         * @param value
         *         值
         * @return Builder
         */
        public Builder addQueryParam(String key, String value) {
            interceptor.queryParamsMap.put(key, value);
            return this;
        }

        /**
         * 添加body的Params
         *
         * @param queryParamsMap
         *         map形式的键值对
         * @return Builder
         */
        public Builder addQueryParamsMap(Map<String, String> queryParamsMap) {
            interceptor.queryParamsMap.putAll(queryParamsMap);
            return this;
        }

        /**
         * 添加加密的Params 会加密合并放入 AppConstants.ENCRYPT_INFO到body
         *
         * @param queryParamsMap
         *         键值对
         * @return Builder
         */
        public Builder addEncryptQueryParam(Map<String, String> queryParamsMap) {
            interceptor.encryptParamsMap.putAll(queryParamsMap);
            return this;
        }

        /**
         * 添加加密的Param，会加密合并放入 AppConstants.ENCRYPT_INFO到body
         *
         * @param key
         *         键
         * @param value
         *         值
         * @return Builder
         */
        public Builder addEncryptQueryParamMap(String key, String value) {
            interceptor.encryptParamsMap.put(key, value);
            return this;
        }

        /**
         * @return builder
         */
        BaseParamsInterceptor build() {
            return interceptor;
        }
    }
}
