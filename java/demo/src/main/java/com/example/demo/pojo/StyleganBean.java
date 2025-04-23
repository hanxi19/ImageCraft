package com.example.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StyleganBean {
    private int seed;
    private float trunc;
    private int network;
    private String resImgUrl;
    private String fileName;
}
