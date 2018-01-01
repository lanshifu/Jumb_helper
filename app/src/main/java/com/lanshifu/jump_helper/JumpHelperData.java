package com.lanshifu.jump_helper;

import cn.bmob.v3.BmobObject;

/**
 * Created by lanshifu on 2018/1/1.
 */

public class JumpHelperData extends BmobObject {

    private String name;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String message;

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    private Boolean enable;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    private String model;

    public Number getCount() {
        return count;
    }

    public void setCount(Number count) {
        this.count = count;
    }

    private Number count;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
