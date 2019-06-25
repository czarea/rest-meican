package com.czarea.rest.meican.ding;

/**
 * dingding 机器人文字提醒
 *
 * @author zhouzx
 */
public class TextMessage extends Message {

    private Text text;
    private At at;

    public TextMessage() {
        this.setMsgtype("text");
    }

    public TextMessage(String content, boolean atAll) {
        this.setMsgtype("text");
        this.setText(new Text(content));
        this.setAt(new At(atAll));
    }

    public void atAll() {
        this.setAt(new At(true));
    }


    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public At getAt() {
        return at;
    }

    public void setAt(At at) {
        this.at = at;
    }


}
