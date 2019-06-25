package com.czarea.rest.meican.schedule;

import static org.slf4j.LoggerFactory.getLogger;

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
import java.util.Date;
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

    @Scheduled(cron = "0 */10 14-16 * * 1,2,4")
    private void order() throws ParseException {
        User user;
        logger.info("点餐小能手开始工作咯☻");
        List<User> users = userRepository.findAll();
        int i = 0;
        for (; i < users.size(); i++) {
            if (i == 0) {
                meiCanApi.today();
            }
            user = users.get(i);

            meiCanApi.getCookies(user.getEmail(), user.getPassword());
            OrderDetail orderDetail = null;
            try {
                orderDetail = meiCanApi.hasOrder();
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
            meiCanApi.getDishsFromMeiCan();
            meiCanApi.getRestaurantsFromMeiCan();
            List<Dish> dishes = meiCanApi.getDishes();

            logger.info("推送钉钉消息！");
            orderingRemind(dishes);

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

                meiCanApi.order(selected.getId());

                Order order = new Order();
                order.setDishId(selected.getId());
                order.setDish(selected.getName());
                order.setUserId(user.getId());
                order.setCreateTime(new Date());
                orderedRemind(selected);
            }

        }
    }

    private void orderedRemind(Dish selected) {
        StringBuffer message = new StringBuffer();
        message.append("驴迹美餐小助手开始发功啦^_^\n")
            .append("给主人订了！\n")
            .append(selected.getName() + "\n")
            .append("给个点赞吧^_^");
        dingDingService.send(new TextMessage(message.toString(), true));
    }

    private void orderingRemind(List<Dish> dishes) {
        StringBuffer message = new StringBuffer();
        message.append("驴迹美餐小助手温馨提示点餐时间到！！！\n")
            .append("如果已经点餐请忽略！\n\n")
            .append("可点餐列表：\n\n");
        AtomicInteger i = new AtomicInteger(1);
        dishes.forEach(item -> {
            message.append(i.get()).append(": ").append(item.getName() + "\n");
            i.getAndIncrement();
        });

        message.append("\n\n");
        dingDingService.send(new TextMessage(message.toString(), true));
    }

}
