package com.zhanghui.core.constant;

/**
 * @author: ZhangHui
 * @date: 2020/10/28 14:35
 * @version：1.0
 */
public class OperateResult<T> {

    private T t;

    public void set(T t) {
        this.t = t;
    }

    public T get() {
        return this.t;
    }

    public boolean isSuccess() {
        return t == null;
    }
}
