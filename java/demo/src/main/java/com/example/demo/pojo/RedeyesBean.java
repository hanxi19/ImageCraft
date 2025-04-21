package com.example.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedeyesBean {
    private String oriImageUrl;
    private String resImageUrl;
    private List<List<Double>> eyes = new ArrayList<>();
}
