import cv2
import numpy as np
import torch
from segment_anything import SamPredictor, sam_model_registry
import argparse


class SAMSegmenter:
    def __init__(self, model_type="vit_b", checkpoint_path="sam_vit_b_01ec64.pth"):
        """
        初始化SAM分割器

        参数:
            model_type: 模型类型(vit_b/vit_l/vit_h)
            checkpoint_path: 模型权重路径
        """
        self.sam = sam_model_registry[model_type](checkpoint=checkpoint_path)
        self.predictor = SamPredictor(self.sam)

    def segment_and_save(self, image_path, points_list, output_path="masked_region.png"):
        """
        对指定图片进行分割并保存结果

        参数:
            image_path: 输入图片路径
            points_list: 目标点列表[(x1,y1),(x2,y2),...]
            output_path: 输出图片路径
        """
        # 读取图片
        image = cv2.imread(image_path)
        if image is None:
            raise ValueError(f"无法读取图片: {image_path}")

        # 转换为RGB格式
        image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

        # 准备点坐标和标签(全部设为前景点)
        point_coords = np.array(points_list)
        point_labels = np.ones(len(points_list), dtype=int)

        # 进行分割预测
        self.predictor.set_image(image_rgb)
        masks, _, _ = self.predictor.predict(
            point_coords=point_coords,
            point_labels=point_labels,
            multimask_output=False,
        )

        if len(masks) == 0:
            raise RuntimeError("分割失败，未生成有效掩码")

        # 获取最佳掩码
        mask = masks[0]

        # 保存透明背景结果
        self._save_transparent_result(image, mask, output_path)

        return mask

    def _save_transparent_result(self, image, mask, save_path):
        """
        保存透明背景结果

        参数:
            image: 原始BGR图像
            mask: 分割掩码
            save_path: 保存路径
        """
        # 创建4通道透明图像
        result = np.zeros((image.shape[0], image.shape[1], 4), dtype=np.uint8)

        # 设置掩码区域
        result[mask == 1, :3] = image[mask == 1]  # BGR通道
        result[mask == 1, 3] = 255  # Alpha通道(不透明)
        result[mask == 0, 3] = 0  # 背景透明

        # 保存结果
        cv2.imwrite(save_path, result)
        print(f"分割结果已保存至: {save_path}")


def parse_arguments():
    """
    解析命令行参数
    """
    parser = argparse.ArgumentParser(description="SAM Segmenter")
    parser.add_argument("--image", type=str, required=True, help="输入图片路径")
    parser.add_argument("--points", type=str, required=True,
                       help="目标点列表，格式为'x1,y1;x2,y2;...'")
    parser.add_argument("--output", type=str, required=True, help="输出图片路径")
    parser.add_argument("--model_type", type=str, default="vit_b",
                       help="模型类型(vit_b/vit_l/vit_h)")
    parser.add_argument("--checkpoint", type=str, default="sam_vit_b_01ec64.pth",
                       help="模型权重路径")
    return parser.parse_args()


def main():
    # 解析命令行参数
    args = parse_arguments()

    # 解析点列表
    try:
        points = [tuple(map(float, point.split(","))) for point in args.points.split(";")]
    except Exception as e:
        raise ValueError(f"点坐标格式错误: {str(e)}")

    # 初始化分割器
    segmenter = SAMSegmenter(model_type=args.model_type, checkpoint_path=args.checkpoint)

    # 执行分割并保存
    try:
        segmenter.segment_and_save(args.image, points, args.output)
        print("分割处理完成")
    except Exception as e:
        print(f"处理失败: {str(e)}")
        exit(1)


if __name__ == "__main__":
    main()
    
'''
 python --% E:\code\Pythoncode\Pycharmproject\PDF_project\src\command_py\getmask_list.py --image "E:\code\Pythoncode\Pycharmproject\PDF_project\data\source_data\SAM_test1.jpg" --points "270.2,290.1;249.4,399.2;261.7,325.2;191.5,308.1;134.6,424.8" --output "result.png" --model_type vit_b --checkpoint "E:\code\Pythoncode\Pycharmproject\PDF_project\model\sam_vit_b_01ec64.pth"
'''