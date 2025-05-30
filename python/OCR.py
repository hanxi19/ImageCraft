import argparse
import os
from paddleocr import PaddleOCR


def extract_text_from_image(image_path, lang='ch'):
    """
    Extract text from an image using PaddleOCR
    Parameters:
        image_path: Path to the image
        lang: Language type ('ch'-Chinese, 'en'-English, 'japan'-Japanese, etc.)
    Returns:
        Recognized text in original line format
    """
    # Initialize PaddleOCR
    ocr = PaddleOCR(use_doc_orientation_classify=False,
    use_doc_unwarping=False,
    use_textline_orientation=True, lang=lang)

    # Perform OCR recognition
    result = ocr.ocr(image_path)

    # Organize recognition results by line
    lines = []

    # if result and len(result) > 0:
    #     # Sort by y-coordinate to maintain line order
    #     print(result[0])
    #     sorted_lines = sorted(result[0], key=lambda line: line[0][0][1])

    #     current_line_y = None
    #     current_line_text = []

    #     for line in sorted_lines:
    #         _, (text, _) = line
    #         y_pos = line[0][0][1]

    #         # If y-coordinate changes beyond a certain threshold, consider it a new line
    #         if current_line_y is None or abs(y_pos - current_line_y) > 20:
    #             if current_line_text:
    #                 lines.append(' '.join(current_line_text))
    #                 current_line_text = []
    #             current_line_y = y_pos
    #         current_line_text.append(text)

    #     if current_line_text:
    #         lines.append(' '.join(current_line_text))

    # return '\n'.join(lines)
    if result and len(result) > 0 and isinstance(result[0], dict):
        rec_texts = result[0].get('rec_texts', [])
        rec_boxes = result[0].get('rec_boxes', [])
        # 按y坐标排序
        sorted_items = sorted(
            list(zip(rec_boxes, rec_texts)),
            key=lambda item: item[0][1] if len(item[0]) > 0 else 0
        )
        lines = [text for box, text in sorted_items]
        return '\n'.join(lines)
    else:
        return ""


def ensure_directory_exists(directory):
    """Ensure the directory exists, create it if not"""
    if not os.path.exists(directory):
        os.makedirs(directory)


def main():
    # Set command-line arguments
    parser = argparse.ArgumentParser(description='Use PaddleOCR to recognize text in an image')
    parser.add_argument('--image', required=True, help='Path to the image to be recognized')
    parser.add_argument('--lang', default='ch',
                        help='Recognition language: ch-Chinese (default), en-English, japan-Japanese, french-French, german-German, korean-Korean')
    parser.add_argument('--output_path', required=True,
                        help='Path to the directory where the result file will be saved')

    args = parser.parse_args()

    try:
        # Extract text
        result = extract_text_from_image(args.image, args.lang)

        # Ensure the output directory exists
        ensure_directory_exists(args.output_path)

        # Generate output file name
        image_name = os.path.basename(args.image)
        output_filename = f"{os.path.splitext(image_name)[0]}_result.txt"
        output_filepath = os.path.join(args.output_path, output_filename)

        # Save result to file
        with open(output_filepath, 'w', encoding='utf-8') as f:
            f.write(result)

        print(f"Recognition completed, result saved to: {output_filepath}")

    except Exception as e:
        print(f"Error during recognition: {str(e)}")


if __name__ == '__main__':
    import sys
    sys.argv = [
        'Ocr.py',
        '--image', r'E:\develop\web\ImageCraft\image\ocr\ori_img\00000007.jpg',
        '--lang', 'ch',
        '--output_path', r'E:\develop\web\ImageCraft\image\ocr\res_Txt'
    ]
    main()