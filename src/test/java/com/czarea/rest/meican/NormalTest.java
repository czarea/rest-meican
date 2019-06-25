package com.czarea.rest.meican;

import java.text.ParseException;
import java.util.Date;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

public class NormalTest {

    @Test
    public void test() throws ParseException {
        Date now = new Date();
        System.out.println(DateFormatUtils.format(now, "yyyy-MM-dd 15:00:00"));
        Date beginOrder = DateUtils.parseDate(DateFormatUtils.format(now, "yyyy-MM-dd 15:00:00"), "yyyy-MM-dd HH:mm:ss");
        System.out.println(beginOrder);

    }
}
