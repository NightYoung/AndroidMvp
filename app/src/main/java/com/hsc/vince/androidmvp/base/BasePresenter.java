package com.hsc.vince.androidmvp.base;

import com.hsc.vince.androidmvp.net.ApiStore;
import com.hsc.vince.androidmvp.net.DecodeTransformer;
import com.hsc.vince.androidmvp.net.NetClient;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * <p>作者：黄思程  2018/4/11 16:42
 * <p>邮箱：codinghuang@163.com
 * <p>作用：
 * <p>描述：基类Presenter
 * 观察者和被观察者绑定，结束绑定操作
 */
public class BasePresenter<V> {
    protected V mMvpView;
    protected ApiStore mApiStore;
    private CompositeSubscription mCompositeDisposable;

    /*** 创建并注入View
     * @param v view*/
    public BasePresenter(V v) {

        attachView(v);
    }

    /*** 绑定View
     * @param mvpView mvpView*/
    private void attachView(V mvpView) {
        this.mMvpView = mvpView;
        mApiStore = NetClient.newInstance().create(ApiStore.class);
    }

    /***解绑View*/
    public void detachView() {
        onUnsubscribe();
        this.mMvpView = null;
    }

    /*** RXjava取消注册，以避免内存泄露*/
    public void onUnsubscribe() {
        if (mCompositeDisposable != null && !mCompositeDisposable.hasSubscriptions()) {
            mCompositeDisposable.unsubscribe();
        }
    }

    /**
     * Rxjava添加注册，使用CompositeSubscription绑定被观察者和观察者，方便管理
     *
     * @param observable 被观察者，会使用到装饰者
     * @param subscriber 观察者
     */
    protected <T> void addSubscription(Observable<T> observable, Subscriber<T> subscriber) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = new CompositeSubscription();
        }

        Observable.Transformer<T, T> transformer = new DecodeTransformer<>();
        Subscription subscription = observable.compose(transformer).subscribe(subscriber);
        mCompositeDisposable.add(subscription);
    }
}
