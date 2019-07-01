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
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

    @Scheduled(cron = "0 0 13 * * 1,2,4")
    private void clear() {
        meiCanApi.today();
        meiCanApi.setCookies(new HashMap<>());

    }

    @Scheduled(cron = "0 0/10 14-16 * * 1,2,4")
    private void order() throws ParseException {
        User user;
        logger.info("点餐小能手开始工作咯☻");
        List<User> users = userRepository.findAll();
        for (int i = 0; i < users.size(); i++) {
            user = users.get(i);
            meiCanApi.today();
            logger.info("开始提醒：{} 点餐！", user.getName());
            if (meiCanApi.getCookies().get(user.getEmail()) == null || meiCanApi.getCookies().get(user.getEmail()).isEmpty()) {
                meiCanApi.getCookies(user.getEmail(), user.getPassword());
            }
            OrderDetail orderDetail = null;
            try {
                orderDetail = meiCanApi.hasOrder(user.getEmail());
            } catch (Exception e) {
                logger.error("有可能登陆失败！");
            }
            boolean hasOrder = false;
            try {
                if (orderDetail != null && orderDetail.getDateList().get(0).getCalendarItemList().get(0).getCorpOrderUser() != null) {
                    hasOrder = true;
                }
            } catch (Exception e) {
                logger.error("", e);
                continue;
            }
            if (hasOrder) {
                logger.info("{} 已经下单，忽略！", user.getName());
                continue;
            }
            List<Dish> dishes = meiCanApi.getDishes();
            if (dishes == null || dishes.isEmpty()) {
                meiCanApi.getDishsFromMeiCan(user.getEmail(), user.getTabUniqueId());
                dishes = meiCanApi.getDishes();
            }

            logger.info("推送钉钉消息！");
            orderingRemind(dishes, user);

            Date now = new Date();
            Date beginOrder = DateUtils.parseDate(DateFormatUtils.format(now, "yyyy-MM-dd 15:00:00"), "yyyy-MM-dd HH:mm:ss");
            if (now.after(beginOrder)) {
                logger.info("到了当天15：00，小助手开始直接点餐了！");
                List<Order> orders = orderRepository
                    .findAllByUserId(user.getId(), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
                    .getContent();

                List<Dish> dishList = meiCanApi.getDishes();
                WeightedRandomBag weightedRandomBag = new WeightedRandomBag();
                dishList.forEach(item -> {
                    orders.forEach(order -> {
                        if (order.getDishId().equalsIgnoreCase(item.getId())) {
                            item.setWeight(item.getWeight() + 1);
                        }
                    });
                    weightedRandomBag.addEntry(item, item.getWeight());
                });

                Dish selected = (Dish) weightedRandomBag.getRandom();

                meiCanApi.order(selected.getId(), user.getEmail(),user.getTabUniqueId());

                Order order = new Order();
                order.setDishId(selected.getId());
                order.setDish(selected.getName());
                order.setUserId(user.getId());
                order.setCreateTime(new Date());
                orderRepository.save(order);
                orderedRemind(selected, user);
            } else {
                logger.info("未到15:00，不开始自动点餐！");
            }

        }
    }

    private void orderedRemind(Dish selected, User user) {
        StringBuffer message = new StringBuffer();
        message.append("美餐小助手完成一次点餐了哦^_^\n")
            .append("给主人：" + user.getName() + "订了:")
            .append(selected.getName() + "\n")
            .append("给个赞吧^_^ \n");
        At at = new At(Arrays.asList(user.getPhone()),false);
        TextMessage textMessage = new TextMessage(message.toString(), true);
        textMessage.setAt(at);
        dingDingService.send(textMessage);
    }

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
        At at = new At(Arrays.asList(user.getPhone()),false);
        TextMessage textMessage = new TextMessage(message.toString(), false);
        textMessage.setAt(at);
        dingDingService.send(textMessage);
    }

}
