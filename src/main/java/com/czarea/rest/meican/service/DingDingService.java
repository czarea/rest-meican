package com.czarea.rest.meican.service;

import com.czarea.rest.meican.ding.Message;
import javax.websocket.SendResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author zhouzx
 */
@Service
public class DingDingService {

    @Value("#{'https://oapi.dingtalk.com/robot/send?access_token='.concat('${dingding.token}')}")
    private String url;

    private final RestTemplate restTemplate;

    public DingDingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public SendResult send(Message message) {
        return restTemplate.postForObject(url, message, SendResult.class);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
