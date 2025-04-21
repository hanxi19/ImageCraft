package com.example.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DenoisingBean {
    private String oriImgUrl;
    private String resImgUrl;
    private int kernel;
    private int methods;
    private int bound;
}
