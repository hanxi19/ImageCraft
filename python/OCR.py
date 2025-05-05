import argparse
from paddleocr import PaddleOCR


def extract_text_from_image(image_path, lang='ch'):
    """
    使用 PaddleOCR 提取图片中的文字
    参数:
        image_path: 图片路径
        lang: 语言类型 ('ch'-中文, 'en'-英文, 'japan'-日文等)
    返回:
        保持原始行格式的识别文本
    """
    # 初始化 PaddleOCR
    ocr = PaddleOCR(use_angle_cls=True, lang=lang)

    # 进行 OCR 识别
    result = ocr.ocr(image_path, cls=True)

    # 按行组织识别结果
    lines = []
    if result and len(result) > 0:
        # 按y坐标排序，保持行顺序
        sorted_lines = sorted(result[0], key=lambda line: line[0][0][1])

        current_line_y = None
        current_line_text = []

        for line in sorted_lines:
            _, (text, _) = line
            y_pos = line[0][0][1]

            # 如果y坐标变化超过一定阈值，视为新行
            if current_line_y is None or abs(y_pos - current_line_y) > 20:
                if current_line_text:
                    lines.append(' '.join(current_line_text))
                    current_line_text = []
                current_line_y = y_pos
            current_line_text.append(text)

        if current_line_text:
            lines.append(' '.join(current_line_text))

    return '\n'.join(lines)


def main():
    # 设置命令行参数
    parser = argparse.ArgumentParser(description='使用PaddleOCR识别图片中的文字')
    parser.add_argument('--image', required=True, help='要识别的图片路径')
    parser.add_argument('--lang', default='ch',
                        help='识别语言: ch-中文(默认), en-英文, japan-日文, french-法文, german-德文, korean-韩文')

    args = parser.parse_args()

    try:
        # 提取文字
        result = extract_text_from_image(args.image, args.lang)

        # 打印结果
        print("识别结果(保持行格式):")
        print(result)

        # 保存结果到文件(可选)
        output_path = args.image.rsplit('.', 1)[0] + '_result.txt'
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(result)
        print(f"\n结果已保存到: {output_path}")

    except Exception as e:
        print(f"识别过程中出错: {str(e)}")


if __name__ == '__main__':
    main()