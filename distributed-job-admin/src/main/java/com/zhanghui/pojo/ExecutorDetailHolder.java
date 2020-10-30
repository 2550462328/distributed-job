package com.zhanghui.pojo;

/**
 * @author: ZhangHui
 * @date: 2020/10/27 15:57
 * @version：1.0
 */
public class ExecutorDetailHolder {

    private Double loadFactor;

    public Double get(){
        return this.loadFactor;
    }

    public ExecutorDetailHolder(Double loadFactor){
        this.loadFactor = loadFactor;
    }
}
