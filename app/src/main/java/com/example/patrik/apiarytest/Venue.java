package com.example.patrik.apiarytest;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by Patrik on 2016-03-30.
 */
public class Venue {
    String name;
    double longitude;
    double latitude;
    ArrayList<Integer> categoryList;

    public Venue(String name, double longitude, double latitude, ArrayList<Integer> categoryList) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.categoryList = categoryList;
    }
}
