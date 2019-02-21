package com.hsc.vince.androidmvp.net;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.hsc.vince.androidmvp.constant.BoreConstants;
import com.hsc.vince.androidmvp.net.converter.ApiException;
import com.toucheart.library.util.LogUtils;
import com.toucheart.library.util.ToastUtils;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

import retrofit2.HttpException;
import rx.Subscriber;

/**
 * <p>作者：Night  2018/4/11 11:15
 * <p>邮箱：codinghuang@163.com
 * <p>作用：
 * <p>描述：网络请求回调层处理，RxJava的回调
 *
 * @param <M>
 *         作为订阅者的消费类型
 */
public abstract class ApiCallback<M> extends Subscriber<M> {
    /*** 未知错误*/
    private static final long UNKOWN_EXCEPTION = -101L;
    /*** 网络错误*/
    private static final long HTTP_EXCEPTION = -100L;
    /*** 自定义忽略错误code码*/
    private Long[] mIgnoreArray = new Long[]{200L,};

    /** 回调 */
    public ApiCallback() {
    }

    /*** 请求成功的回调
     * @param model 回调的数据*/
    public abstract void onSuccess(M model);

    /*** 请求失败的回调
     * @param code 错误码
     * @param errorMsg 错误信息*/
    public abstract void onFailed(long code, String errorMsg);

    /***请求完成的回调（无论成功或者失败都会走onFinish方法）*/
    public abstract void onFinish();

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onNext(M m) {
        //请求成功集中处理
        onSuccess(m);
    }

    @Override
    public void onError(Throwable t) {
        try {
            //请求错误集中处理(自定义错误处理)
            if (t instanceof HttpException) {
                //网络错误
                HttpException httpException = (HttpException) t;
                int code = httpException.code();
                String msg = httpException.getMessage();
                if (code == 404 || code == 500 || code == 503) {
                    //无法连接到服务器   自定义操作（Toast提示或者全局提示框）
                    LogUtils.e("无法连接到服务器");
                }

                onFailed(code, msg);
            } else if (t instanceof ApiException) {
                //ApiException类自定义处理错误处理
                ApiException apiException = (ApiException) t;
                dealApiException(apiException);

                String msg = apiException.msg();
                long code = apiException.code();

                toastException(code, msg);
                onFailed(code, msg);
            } else if (t instanceof UnknownHostException
                    || t instanceof SocketTimeoutException) {
                //服务器错误
                onFailed(HTTP_EXCEPTION, t.getMessage());
            } else {
                toastException(0L, t.getMessage());
                onFailed(UNKOWN_EXCEPTION, "");
            }

            onFinish();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            t.printStackTrace();
        }
    }

    @Override
    public void onCompleted() {
        //请求完成集中处理
        onFinish();
    }

    /**
     * @param apiException
     *         处理的自定义错误
     */
    private void dealApiException(ApiException apiException) {
        if (apiException.loginOverDue() || apiException.isTokenExpied()) {
            LogUtils.d("登录过期,请重新登录");
        } else if (apiException.isMaintain()) {
            //维护期
            showMaintainDialog(apiException.getMessage());
        }
    }

    /**
     * @param message
     *         维护逻辑处理
     */
    private void showMaintainDialog(String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            //调用dialog 展示维护信息
            LogUtils.e(message);
        });
    }

    /*****
     * @param code    code
     * @param message msg*/
    private void toastException(Long code, String message) {
        if (Looper.myLooper() == null || notNeedToast(code)) {
            return;
        }

        if (!TextUtils.isEmpty(message) && !BoreConstants.isUnittest) {
            ToastUtils.showShort(message);
        }
    }

    /**
     * @param code
     *         错误码
     * @return 是否需要提示
     */
    private boolean notNeedToast(Long code) {

        return Arrays.asList(mIgnoreArray).contains(code);
    }
}
