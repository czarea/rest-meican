package com.czarea.rest.meican.ding;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * @author zhouzx
 */
@JsonInclude(Include.NON_NULL)
public class At {

    private List<String> atMobiles;
    @JsonProperty("isAtAll")
    private boolean isAtAll;

    public At(List<String> atMobiles, boolean isAtAll) {
        this.atMobiles = atMobiles;
        this.isAtAll = isAtAll;
    }

    public At(boolean isAtAll) {
        this.isAtAll = isAtAll;
    }

    public List<String> getAtMobiles() {
        return atMobiles;
    }

    public void setAtMobiles(List<String> atMobiles) {
        this.atMobiles = atMobiles;
    }

    public boolean isAtAll() {
        return isAtAll;
    }

    public void setAtAll(boolean atAll) {
        isAtAll = atAll;
    }
}
