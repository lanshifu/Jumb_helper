package com.lanshifu.jump_helper;

import cn.bmob.v3.BmobObject;

/**
 * Created by lanshifu on 2017/12/31.
 */

public class JumpHelperConfig extends BmobObject {
    private Boolean enable;
    private String message;

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
