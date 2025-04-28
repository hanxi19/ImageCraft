package com.example.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedeyesBean {
    private String oriImageUrl;
    private String resImageUrl;
    private List<List<Double>> eyes = new ArrayList<>();

    public String convertEyesToString() {
        return eyes.stream()
                .map(rect -> {
                    if (rect.size() != 4) {
                        throw new IllegalArgumentException("每个矩形框必须包含4个元素 (x, y, w, h)");
                    }
                    // 添加第5个元素 n=1
                    return String.format("%.4f,%.4f,%.4f,%.4f,1", rect.get(0), rect.get(1), rect.get(2), rect.get(3));
                })
                .collect(Collectors.joining(";")); // 使用分号分隔不同的矩形框
    }
}
