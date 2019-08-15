package com.czarea.rest.meican.dto;

import java.util.List;

/**
 * @author zhouzx
 */
public class DishDTO {

    private List<Dish> myRegularDishList;

    private List<Dish> othersRegularDishList;

    public static class Dish {

        private String id;
        private String name;
        private int weight = 1;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "Dish{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", weight=" + weight +
                '}';
        }
    }

    public List<Dish> getMyRegularDishList() {
        return myRegularDishList;
    }

    public void setMyRegularDishList(List<Dish> myRegularDishList) {
        this.myRegularDishList = myRegularDishList;
    }

    public List<Dish> getOthersRegularDishList() {
        return othersRegularDishList;
    }

    public void setOthersRegularDishList(List<Dish> othersRegularDishList) {
        this.othersRegularDishList = othersRegularDishList;
    }
}
