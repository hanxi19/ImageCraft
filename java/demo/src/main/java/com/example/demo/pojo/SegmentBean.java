package com.example.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SegmentBean {
    private String imageUrl;               // 原始图片路径
    private List<Double> keyPoints;       // 关键点坐标 [x1,y1,x2,y2,...]
    private String segmentationResultUrl; // 分割结果图片路径

    /**
     * 将关键点列表转换为字符串格式
     * @return "x1,y1;x2,y2;..."
     */
    public String convertPointsToString() {
        if (keyPoints == null || keyPoints.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyPoints.size(); i += 2) {
            if (i > 0) {
                sb.append(";");
            }
            sb.append(String.format("%.1f,%.1f", keyPoints.get(i), keyPoints.get(i + 1)));
        }
        return sb.toString();
    }
}