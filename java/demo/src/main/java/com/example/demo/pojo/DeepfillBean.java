package com.example.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeepfillBean {
    private String segImgUrl;
    private List<Float> keyPoint = new ArrayList<>();
    private String oriImgUrl;
    private String fillImgUrl;
}
