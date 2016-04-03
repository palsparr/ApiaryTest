package com.example.patrik.apiarytest;

import java.util.ArrayList;

/**
 * Created by Patrik on 2016-03-30.
 */
public class Product {
    String name;
    ArrayList<Integer> categoryList;

    public Product(String name, ArrayList<Integer> categoryList) {
        this.name = name;
        this.categoryList = categoryList;
    }
}
