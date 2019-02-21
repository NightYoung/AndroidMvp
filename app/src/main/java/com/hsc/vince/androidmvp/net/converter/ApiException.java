package com.hsc.vince.androidmvp.net.converter;

import com.hsc.vince.androidmvp.constant.HttpStatusConstant;

/**
 * <p>作者：Night  2019/1/14 17:34
 * <p>邮箱：codinghuang@163.com
 * <p>作用：
 * <p>描述：自定义Http错误类型
 */
public class ApiException extends RuntimeException {
    private final long errorCode;
    private final String errorMsg;
    private final String data;

    /**
     * @param errorCode
     *         errorCode
     * @param errorMsg
     *         errorMsg
     */
    public ApiException(long errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.data = "";
    }

    /**
     * @param errorCode
     *         errorCode
     * @param errorMsg
     *         errorMsg
     * @param data
     *         data
     */
    public ApiException(long errorCode, String errorMsg, String data) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.data = data;
    }

    public long code() {

        return errorCode;
    }

    public String msg() {

        return errorMsg;
    }

    public String data() {

        return data;
    }

    //自定义错误，如登录过期，账号在其他设备登录
    public boolean loginOverDue() {
        //登录过期的错误码
        return errorCode == HttpStatusConstant.LOGIN_OVER;
    }

    /**
     * @return 判断是否Token失效
     *
     * 判断错误码是否为Token失效的错误码
     */
    public boolean isTokenExpied() {
        //token失效的错误码
        return errorCode == HttpStatusConstant.TOKEN_ERROR;
    }

    /**
     * @return 判断是否处于维护期
     */
    public boolean isMaintain() {
        //处于维护期
        return errorCode == HttpStatusConstant.MAINTAIN;
    }
}
