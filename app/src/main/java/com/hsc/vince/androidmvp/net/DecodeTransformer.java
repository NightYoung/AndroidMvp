package com.hsc.vince.androidmvp.net;


import com.hsc.vince.androidmvp.constant.BoreConstants;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * <p>作者：黄思程  2018/4/11 16:20
 * <p>邮箱：codinghuang@163.com
 * <p>作用：
 * <p>描述：Observa装饰类
 */
public class DecodeTransformer<T> implements Observable.Transformer<T, T> {


    @Override
    public Observable<T> call(Observable<T> tObservable) {
        Observable<T> newObservable;
        if (BoreConstants.isUnittest) {
            newObservable = tObservable.subscribeOn(Schedulers.immediate())
                    .observeOn(Schedulers.immediate());
        } else {
            newObservable = tObservable
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            //.delay(2000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())//模拟延迟,用于观察加载框等效果
            ;
        }
        return newObservable;
    }
}
