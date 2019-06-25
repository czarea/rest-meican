package com.czarea.rest.meican.dto;

import java.util.List;

public class RestaurantDTO {
    private boolean noMore;
    private List<Restaurant> restaurantList;

    public static class Restaurant {
        private String name;
        private double longitude;
        private double latitude;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        @Override
        public String toString() {
            return "Restaurant{" +
                "name='" + name + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
        }
    }

    public boolean isNoMore() {
        return noMore;
    }

    public void setNoMore(boolean noMore) {
        this.noMore = noMore;
    }

    public List<Restaurant> getRestaurantList() {
        return restaurantList;
    }

    public void setRestaurantList(List<Restaurant> restaurantList) {
        this.restaurantList = restaurantList;
    }

    @Override
    public String toString() {
        return "RestaurantDTO{" +
            "noMore=" + noMore +
            ", restaurantList=" + restaurantList +
            '}';
    }
}
