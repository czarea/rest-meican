package com.czarea.rest.meican;

import com.czarea.rest.meican.entity.Order;
import com.czarea.rest.meican.entity.User;
import com.czarea.rest.meican.repository.OrderRepository;
import com.czarea.rest.meican.repository.UserRepository;
import com.czarea.rest.meican.service.MeiCanApi;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RestMeicanApplicationTests {

    @Autowired
    private MeiCanApi meiCanApi;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void testGetUsers() {
        System.out.println(userRepository.findByStatus(0));
    }

    @Test
    public void getRestaurant() {
        meiCanApi.getCookies("zhouzx23@dingtalk.com", "123456");
        meiCanApi.getRestaurantsFromMeiCan("zhouzx23@dingtalk.com");
        System.out.println(meiCanApi.getRestaurants());
    }

    @Test
    public void getDish() {
        meiCanApi.getCookies("zhouzx23@dingtalk.com", "123456");
        meiCanApi.getDishsFromMeiCan("zhouzx23@dingtalk.com", "c91b9fde-f182-4235-bf35-b1a132f6c0c7");
        System.out.println(meiCanApi.getDishes());
    }

    @Test
    public void order() {
        meiCanApi.getCookies("zhouzx23@dingtalk.com", "123456");
        meiCanApi.getDishsFromMeiCan("zhouzx23@dingtalk.com", "c91b9fde-f182-4235-bf35-b1a132f6c0c7");
        meiCanApi.today();
        int random = RandomUtils.nextInt(0, meiCanApi.getDishes().size());
        String result = meiCanApi
            .order(meiCanApi.getDishes().get(random).getId(), "zhouzx23@dingtalk.com", "c91b9fde-f182-4235-bf35-b1a132f6c0c7");
        System.out.println(result);
    }

    @Test
    public void getDetail() {
        meiCanApi.getCookies("chenjiewen@lvjitec.com", "123456");
        meiCanApi.getDishsFromMeiCan("chenjiewen@lvjitec.com", "4b4c061f-fc5b-46d4-bd2b-2c7e9cb5ea04");
        meiCanApi.today();
        System.out.println(meiCanApi.hasOrder("chenjiewen@lvjitec.com"));
    }

    @Test
    @Rollback(false)
    public void insertUser() {
        User user = new User();
        user.setName("蔡锡彬");
        user.setPassword("123456");
        user.setEmail("xibincai@163.com");
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        userRepository.saveAndFlush(user);
    }

    @Test
    @Rollback(false)
    public void insertOrders() {
        for (int i = 0; i < 100; i++) {
            Order order = new Order();
            order.setCreateTime(new Date());
            order.setDishId(RandomStringUtils.randomAlphanumeric(5));
            order.setDish("白切鸡" + i);
            order.setUserId(1);
            order.setUpdateTime(new Date());
            orderRepository.save(order);
        }
    }

    @Test
    public void testFind() {
        List<Order> orders = orderRepository.findAllByUserId(1, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"))).getContent();
        System.out.println(orders);
    }
}

