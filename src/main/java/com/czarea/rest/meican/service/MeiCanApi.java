package com.czarea.rest.meican.service;

import com.czarea.rest.meican.config.MeiCanProperties;
import com.czarea.rest.meican.dto.DishDTO;
import com.czarea.rest.meican.dto.DishDTO.Dish;
import com.czarea.rest.meican.dto.OrderDetail;
import com.czarea.rest.meican.dto.RestaurantDTO;
import com.czarea.rest.meican.dto.RestaurantDTO.Restaurant;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings("Duplicates")
@Service
public class MeiCanApi {

    private final RestTemplate restTemplate;
    private final MeiCanProperties meiCanProperties;

    public MeiCanApi(RestTemplate restTemplate, MeiCanProperties meiCanProperties) {
        this.restTemplate = restTemplate;
        this.meiCanProperties = meiCanProperties;
    }

    private Map<String, List<String>> cookies = new HashMap<>();
    private List<Dish> dishes;
    private List<Restaurant> restaurants;
    private String today;

    /**
     * 登陆获取cookie
     *
     * @return cookie
     */
    public void getCookies(String email, String password) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", email);
        params.add("password", password);
        params.add("redirectUrl", "");
        params.add("remember", "true");
        params.add("loginType", "username");
        params.add("openId", "");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Object> response = restTemplate.postForEntity(meiCanProperties.getLogin(), request, Object.class);
        cookies.put(email, response.getHeaders().get("Set-Cookie"));
    }

    public OrderDetail hasOrder(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.put(HttpHeaders.COOKIE, cookies.get(email));
        HttpEntity<MultiValueMap<String, String>> params = new HttpEntity<>(headers);
        Map<String, String> uriVariables = new HashMap<>(2);
        uriVariables.put("beginDate", DateFormatUtils.format(new Date(), "yyyy-MM-dd"));
        uriVariables.put("endDate", DateFormatUtils.format(new Date(), "yyyy-MM-dd"));
        return restTemplate
            .exchange(meiCanProperties.getDetail(), HttpMethod.GET, params, OrderDetail.class, uriVariables).getBody();
    }

    public void getRestaurantsFromMeiCan(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.put(HttpHeaders.COOKIE, cookies.get(email));
        HttpEntity<MultiValueMap<String, String>> params = new HttpEntity<>(headers);
        RestaurantDTO result = restTemplate.exchange(meiCanProperties.getRestaurant() + today, HttpMethod.GET, params, RestaurantDTO.class)
            .getBody();
        restaurants = result.getRestaurantList();
    }

    public void getDishsFromMeiCan(String email, String tabUniqueId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.put(HttpHeaders.COOKIE, cookies.get(email));
        Map<String, String> uriVariables = new HashMap<>(2);
        uriVariables.put("tabUniqueId", tabUniqueId);
        HttpEntity<MultiValueMap<String, String>> params = new HttpEntity<>(headers);
        DishDTO result = restTemplate.exchange(meiCanProperties.getDish() + today, HttpMethod.GET, params, DishDTO.class, uriVariables)
            .getBody();
        dishes = Stream.concat(result.getMyRegularDishList().stream(), result.getOthersRegularDishList().stream())
            .collect(Collectors.toList());
    }

    public String order(String dishId, String email, String tabUniqueId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.put(HttpHeaders.COOKIE, cookies.get(email));
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("corpAddressRemark", "");
        params.add("corpAddressUniqueId", "c897a959b99a");
        params.add("remarks", "[{\"dishId\":\"" + dishId + "\",\"remark\":\"\"}]");
        params.add("order", "[{\"count\":1,\"dishId\":\"" + dishId + "\"}]");
        params.add("tabUniqueId", tabUniqueId);
        params.add("targetTime", today);
        params.add("userAddressUniqueId", "c897a959b99a");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        return restTemplate.exchange(meiCanProperties.getOrder(), HttpMethod.POST, request, String.class).getBody();
    }

    /**
     * 删除订单
     */
    public void refund(String uniqueId, String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.put(HttpHeaders.COOKIE, cookies.get(email));
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("restoreCart", "false");
        params.add("type", "CORP_ORDER");
        params.add("uniqueId", uniqueId);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        restTemplate.exchange(meiCanProperties.getOrder(), HttpMethod.POST, request, String.class).getBody();
    }

    public void today() {
        today = new SimpleDateFormat("yyyy-MM-dd 16:00").format(new Date());
    }


    public Map<String, List<String>> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, List<String>> cookies) {
        this.cookies = cookies;
    }

    public List<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }

    public List<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(List<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

    public String getToday() {
        return today;
    }

    public void setToday(String today) {
        this.today = today;
    }
}
