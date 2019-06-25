package com.czarea.rest.meican.ding;


/**
 * @author zhouzx
 */
public abstract class Message {
    private String msgtype;

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }
}
