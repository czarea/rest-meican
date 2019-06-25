package com.czarea.rest.meican.dto;

import java.util.List;

/**
 * @author zhouzx
 */
public class OrderDetail {
    private String endDate;
    private String startDate;
    private List<Data> dateList;

    public static class Data {
        private String date;
        private List<Item> calendarItemList;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public List<Item> getCalendarItemList() {
            return calendarItemList;
        }

        public void setCalendarItemList(List<Item> calendarItemList) {
            this.calendarItemList = calendarItemList;
        }

        public static class Item {
            private String title;

            private CorpOrderUser corpOrderUser;

            public static class CorpOrderUser {
                private String uniqueId;

                public String getUniqueId() {
                    return uniqueId;
                }

                public void setUniqueId(String uniqueId) {
                    this.uniqueId = uniqueId;
                }
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public CorpOrderUser getCorpOrderUser() {
                return corpOrderUser;
            }

            public void setCorpOrderUser(CorpOrderUser corpOrderUser) {
                this.corpOrderUser = corpOrderUser;
            }
        }
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public List<Data> getDateList() {
        return dateList;
    }

    public void setDateList(List<Data> dateList) {
        this.dateList = dateList;
    }
}
