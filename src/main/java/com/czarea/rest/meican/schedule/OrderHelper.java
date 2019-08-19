package com.czarea.rest.meican.schedule;

import static org.slf4j.LoggerFactory.getLogger;

import com.czarea.rest.meican.ding.At;
import com.czarea.rest.meican.ding.TextMessage;
import com.czarea.rest.meican.dto.DishDTO.Dish;
import com.czarea.rest.meican.dto.OrderDetail;
import com.czarea.rest.meican.entity.Order;
import com.czarea.rest.meican.entity.User;
import com.czarea.rest.meican.repository.OrderRepository;
import com.czarea.rest.meican.repository.UserRepository;
import com.czarea.rest.meican.service.DingDingService;
import com.czarea.rest.meican.service.MeiCanApi;
import com.czarea.rest.meican.util.WeightedRandomBag;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author zhouzx
 */
@Component
public class OrderHelper {

    private static final Logger logger = getLogger(OrderHelper.class);

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final MeiCanApi meiCanApi;
    private final DingDingService dingDingService;

    public OrderHelper(UserRepository userRepository, OrderRepository orderRepository, MeiCanApi meiCanApi,
        DingDingService dingDingService) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.meiCanApi = meiCanApi;
        this.dingDingService = dingDingService;
    }

    /**
     * 清空缓存登录信息
     */
    @Scheduled(cron = "0 0 13 * * 1,2,4")
    private void clear() {
        meiCanApi.today();
        meiCanApi.setCookies(new HashMap<>(4));
    }

    /**
     * 定时点餐，每天到15：30之后不点餐
     */
    @Scheduled(cron = "0 0/10 14-16 * * 1,2,4")
    private void order() throws Exception {
        User user;
        logger.info("点餐助手开始工作咯!!!");
        List<User> users = userRepository.findByStatus(1);
        for (int i = 0; i < users.size(); i++) {
            user = users.get(i);
            meiCanApi.today();
            Date now = new Date();
            Date beginOrderTime = DateUtils.parseDate(DateFormatUtils.format(now, "yyyy-MM-dd 15:00:00"), "yyyy-MM-dd HH:mm:ss");
            Date endOrderTime = DateUtils.parseDate(DateFormatUtils.format(now, "yyyy-MM-dd 15:30:00"), "yyyy-MM-dd HH:mm:ss");

            if (now.before(beginOrderTime)) {
                boolean remind = beforeRemind(user);
                if (remind) {
                    continue;
                }
                List<Dish> dishes = meiCanApi.getDishesFromMeiCan(user.getEmail(), user.getTabUniqueId());
                logger.info("推送钉钉消息给：{},可订餐列表为：{}", user.getName(), dishes);
                orderingRemind(dishes, user);
            } else if (now.before(endOrderTime)) {
                boolean remind = beforeRemind(user);
                if (remind) {
                    continue;
                }
                List<Dish> dishes = meiCanApi.getDishesFromMeiCan(user.getEmail(), user.getTabUniqueId());
                if (now.before(endOrderTime)) {
                    ordering(user, dishes);
                }
            } else {
                logger.info("超过15:30，还没订餐的需要自己手动订餐！！！");
            }
        }
    }

    /**
     * 14：00后开始钉钉提醒点餐
     */
    private boolean beforeRemind(User user) {
        if (meiCanApi.getCookies().get(user.getEmail()) == null || meiCanApi.getCookies().get(user.getEmail()).isEmpty()) {
            meiCanApi.getCookies(user.getEmail(), user.getPassword());
        }
        OrderDetail orderDetail = null;
        try {
            orderDetail = meiCanApi.hasOrder(user.getEmail());
        } catch (Exception e) {
            logger.error("有可能登陆失败！");
            return false;
        }
        boolean hasOrder = false;
        try {
            if (orderDetail != null && orderDetail.getDateList().get(0).getCalendarItemList().get(0).getCorpOrderUser() != null) {
                hasOrder = true;
                logger.info("{} 已经下单，忽略！", user.getName());
            }
            return hasOrder;
        } catch (Exception e) {
            logger.error("", e);
            return false;
        }
    }

    /**
     * 订餐成功钉钉提醒
     */
    private void orderedRemind(Dish selected, User user) {
        logger.info("给{}点了{}！！！", user.getName(), selected.getName());
        At at = new At(Collections.singletonList(user.getPhone()), false);
        String message = "美餐小助手完成一次点餐了哦^_^\n"
            + "给主人：" + user.getName() + "订了:"
            + selected.getName() + "\n"
            + "给个赞吧^_^ \n";
        TextMessage textMessage = new TextMessage(message, true);
        textMessage.setAt(at);
        dingDingService.send(textMessage);
    }

    /**
     * 美餐订餐
     */
    private void ordering(User user, List<Dish> dishes) {
        logger.info("到了当天15：00，小助手开始直接点餐了！");
        List<Order> orders = orderRepository
            .findAllByUserId(user.getId(), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
            .getContent();

        WeightedRandomBag<Dish> weightedRandomBag = new WeightedRandomBag<>();
        dishes.forEach(item -> {
            orders.forEach(order -> {
                if (order.getDishId().equalsIgnoreCase(item.getId())) {
                    item.setWeight(item.getWeight() + 1);
                }
            });
            weightedRandomBag.addEntry(item, item.getWeight());
        });

        boolean retry = true;
        int times = 1;
        while (retry) {
            try {
                Dish selected = weightedRandomBag.getRandom();
                times++;
                String response = meiCanApi.order(selected.getId(), user.getEmail(), user.getTabUniqueId());
                logger.info("点餐响应：{}", response);
                ObjectMapper objectMapper = new ObjectMapper();
                Map reader = objectMapper.readValue(response, Map.class);
                if (reader.get("status").equals("SUCCESSFUL")) {
                    logger.info("点餐响应结果：{}", response);
                    Order order = new Order();
                    order.setDishId(selected.getId());
                    order.setDish(selected.getName());
                    order.setUserId(user.getId());
                    order.setCreateTime(new Date());
                    orderRepository.save(order);
                    orderedRemind(selected, user);
                    break;
                } else {
                    logger.warn("第{}次给：{}点餐出错！！！", times, user.getName());
                }
            } catch (Exception e) {
                logger.error("add order error!", e);
                if (times == 5) {
                    retry = false;
                }
            }
        }
    }

    /**
     * 钉钉提醒点餐
     */
    private void orderingRemind(List<Dish> dishes, User user) {
        StringBuffer message = new StringBuffer();
        message.append("美餐小助手温馨：" + user.getName() + " 提示点餐时间到！！！\n")
            .append("可点餐列表：\n\n");
        AtomicInteger i = new AtomicInteger(1);
        dishes.forEach(item -> {
            message.append(i.get()).append(": ").append(item.getName() + "\n");
            i.getAndIncrement();
        });

        message.append("\n\n");
        At at = new At(Collections.singletonList(user.getPhone()), false);
        TextMessage textMessage = new TextMessage(message.toString(), false);
        textMessage.setAt(at);
        dingDingService.send(textMessage);
    }

}
