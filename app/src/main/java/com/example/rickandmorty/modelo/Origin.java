package com.example.rickandmorty.modelo;

import java.io.Serializable;

public class Origin implements Serializable {
    private String name;
    private String url;

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
