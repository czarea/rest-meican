> 源码地址：[https://github.com/czarea/rest-meican.git](https://github.com/czarea/rest-meican.git)

### 简介
使用 **RestTemplate + 定时任务 + 权重** 完成一个自动美餐点餐小助手！

### 核心处理

**登陆**

RestTemplate POST 请求模拟登陆得到ResponseEntity，最终拿到cookie信息，美餐登陆是使用POST，然后直接重定向。这里有一个小插曲，因为是重定向，所以如果是账号密码错误，美餐是把错误信息返回到cookie中了

```
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
     cookies = response.getHeaders().get("Set-Cookie");
}
```

**下单**

```
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
headers.put(HttpHeaders.COOKIE, cookies);
MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
params.add("corpAddressRemark", "");
params.add("corpAddressUniqueId", "c897a959b99a");
params.add("remarks", "[{\"dishId:" + dishId + ",\"remark\":\"\"}]");
params.add("order", "[{\"count\":1,\"dishId:" + dishId + "}]");
params.add("tabUniqueId", "c91b9fde-f182-4235-bf35-b1a132f6c0c7");
params.add("targetTime", today);
params.add("userAddressUniqueId", "c897a959b99a");
HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
return restTemplate.exchange(meiCanProperties.getOrder(), HttpMethod.POST, request, String.class).getBody();

```

**定时**

使用springboot 的schedure，在每周一、二、四的下午14点到16点定时10分钟跑一次

```
@Scheduled(cron = "0 0/10 14-16 * * 1,2,4")
```

### 权重算法

```
public class WeightedRandomBag<T extends Object> {

    private class Entry {

        double accumulatedWeight;
        T object;
    }

    private List<Entry> entries = new CopyOnWriteArrayList<>();
    private double accumulatedWeight;
    private Random rand = new Random();

    public void addEntry(T object, double weight) {
        accumulatedWeight += weight;
        Entry e = new Entry();
        e.object = object;
        e.accumulatedWeight = accumulatedWeight;
        entries.add(e);
    }

    public T getRandom() {
        double r = rand.nextDouble() * accumulatedWeight;

        for (Entry entry : entries) {
            if (entry.accumulatedWeight >= r) {
                return entry.object;
            }
        }
        return null;
    }
}
```
