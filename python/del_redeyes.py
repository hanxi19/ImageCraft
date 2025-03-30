import math
import argparse
import cv2
import numpy as np

global img, ans
global point1, point2

readroad = "C:\\Users\\17204\\Desktop\\image\\red.jpeg"
saveroad = "C:\\Users\\17204\\Desktop\\image\\red.jpeg"
picname = 'cateye.jpg'
input="C:\\Users\\17204\\Desktop\\image\\red.jpeg"
output= "C:\\Users\\17204\\Desktop\\image\\del_red.jpeg"
# picname ='12031565.jpg'
# picname = 'child-red eye.jpg'

def main():
    global img, ans
    img = cv2.imread(input)
    ans = img.copy()
    cv2.namedWindow('image')
    cv2.setMouseCallback('image', on_mouse)
    cv2.imshow('image', ans)
    cv2.waitKey(0)

def rgb2hsi(R, G, B):
    m = R - (G + B) / 2
    n = cv2.multiply(R - G, R - G) + cv2.multiply(R - B, G - B)
    theta = np.zeros([R.shape[0], R.shape[1]])
    eps = 0.000001  # 定义一个微小量，避免出现分母为0至分式错误
    # 下面按照公式进行变换
    for i in range(R.shape[0]):
        for j in range(R.shape[1]):
            theta[i, j] = math.acos(m[i, j] / math.sqrt(n[i, j] + eps))
    H = theta.copy()
    mid = B > G
    H[mid] = 2 * math.pi - theta[mid] # 给HSI空间中的H赋值
    S = np.zeros([R.shape[0], R.shape[1]])
    for i in range(R.shape[0]):
        for j in range(R.shape[1]):
            S[i, j] = 1 - (3 * min(R[i, j], G[i, j], B[i, j]) / (R[i, j] + G[i, j] + B[i, j] + eps)) # 给HSI空间中的S赋值
    I = (R + G + B) / 3  # 给HSI空间中的I赋值
    return H, S, I


def hsi2rgb(H, S, I):
    mid = math.pi * 2 / 3  # 首先将120度转换为弧度制便于判定
    R = np.zeros([H.shape[0], H.shape[1]])  # 初始化R,G,B矩阵
    G = np.zeros([H.shape[0], H.shape[1]])
    B = np.zeros([H.shape[0], H.shape[1]])
    for i in range(H.shape[0]):
        for j in range(H.shape[1]):
            # 以下按情况进行分类讨论
            if H[i, j] <= mid:
                R[i, j] = I[i, j] * (1 + S[i, j] * math.cos(H[i, j]) / math.cos(0.5 * mid - H[i, j]))
                B[i, j] = I[i, j] * (1 - S[i, j])
                G[i, j] = 3 * I[i, j] - (R[i, j] + B[i, j])
            elif (H[i, j] <= mid * 2) and H[i, j] > mid:
                H[i, j] = H[i, j] - mid
                G[i, j] = I[i, j] * (1 + S[i, j] * math.cos(H[i, j]) / math.cos(0.5 * mid - H[i, j]))
                R[i, j] = I[i, j] * (1 - S[i, j])
                B[i, j] = 3 * I[i, j] - (R[i, j] + G[i, j])
            elif (H[i, j] <= mid * 3) and H[i, j] > mid * 2:
                H[i, j] = H[i, j] - mid * 2
                B[i, j] = I[i, j] * (1 + S[i, j] * math.cos(H[i, j]) / math.cos(0.5 * mid - H[i, j]))
                G[i, j] = I[i, j] * (1 - S[i, j])
                R[i, j] = 3 * I[i, j] - (B[i, j] + G[i, j])
    return R, G, B


def on_mouse(event, x, y, flags, param):
    global img, ans, point1, point2
    img1 = img.copy()
    if event == cv2.EVENT_LBUTTONDOWN:  # 左键点击
        point1 = (x, y) # 获取一个角点坐标
        cv2.circle(img1, point1, 10, (0, 255, 0), 1)
        cv2.imshow('image', img1)
    elif event == cv2.EVENT_MOUSEMOVE and (flags & cv2.EVENT_FLAG_LBUTTON):  # 按住左键拖曳
        cv2.rectangle(img1, point1, (x, y), (255, 0, 0), 1)
        cv2.imshow('image', img1)
    elif event == cv2.EVENT_LBUTTONUP:  # 左键释放
        point2 = (x, y) # 获取对角角点坐标
        cv2.rectangle(img1, point1, point2, (0, 0, 255), 1)
        cv2.imshow('image', img1)
        img2 = ans.astype(np.int32) # 转换数据类型方便进行运算
        # 注意读入的图像三层分别对应RGB空间的B,G,R
        H, S, I = rgb2hsi(img2[:, :, 2], img2[:, :, 1], img2[:, :, 0])
        min_x = min(point1[0], point2[0]) # 获取待处理区域信息
        min_y = min(point1[1], point2[1])
        width = abs(point1[0] - point2[0])
        height = abs(point1[1] - point2[1])
        # 按照红眼定义在指定区域进行去红眼处理
        for i in range(min_y, min_y + height + 1):
            for j in range(min_x, min_x + width + 1):
                # m,n=H[i, j],S[i, j]
                if (H[i, j] < math.pi / 4) or H[i, j] > math.pi * 7 / 4:
                    if S[i, j] > 0.25:
                        S[i, j] = 0
        R, G, B = hsi2rgb(H, S, I)
        # 构造去红眼的结果影像
        mid = np.zeros([R.shape[0], R.shape[1], 3])
        mid[:, :, 0] = B
        mid[:, :, 1] = G
        mid[:, :, 2] = R
        ans = mid.astype(np.uint8) # 转换数据类型方便结果展示与保存
        cv2.imshow('image1', ans)
        cv2.imwrite(output,ans)

def remove_redeye_with_coords(x, y, w, h, n):
    """
    根据归一化坐标去除红眼。
    :param x: 左上角 x 坐标（归一化）
    :param y: 左上角 y 坐标（归一化）
    :param w: 宽度（归一化）
    :param h: 高度（归一化）
    :param n: 图像的宽高比归一化系数（通常为1）
    """
    global img, ans
    img2 = ans.astype(np.int32)  # 转换数据类型方便进行运算
    H, S, I = rgb2hsi(img2[:, :, 2], img2[:, :, 1], img2[:, :, 0])

    # 将归一化坐标转换为实际像素坐标
    height, width = img.shape[:2]
    min_x = int(x * width)
    min_y = int(y * height)
    rect_width = int(w * width)
    rect_height = int(h * height)

    # 按照红眼定义在指定区域进行去红眼处理
    for i in range(min_y, min_y + rect_height + 1):
        for j in range(min_x, min_x + rect_width + 1):
            if (H[i, j] < math.pi / 4) or H[i, j] > math.pi * 7 / 4:
                if S[i, j] > 0.25:
                    S[i, j] = 0

    R, G, B = hsi2rgb(H, S, I)
    # 构造去红眼的结果影像
    mid = np.zeros([R.shape[0], R.shape[1], 3])
    mid[:, :, 0] = B
    mid[:, :, 1] = G
    mid[:, :, 2] = R
    ans = mid.astype(np.uint8)  # 转换数据类型方便结果展示与保存
    cv2.imshow('image1', ans)
    cv2.imwrite(output, ans)

def to_normalized_coords(x1, y1, x2, y2, img):
    """
    将图片的两点像素坐标转换为归一化坐标。
    :param x1: 左上角 x 坐标（像素）
    :param y1: 左上角 y 坐标（像素）
    :param x2: 右下角 x 坐标（像素）
    :param y2: 右下角 y 坐标（像素）
    :param img: 图像对象（用于获取宽度和高度）
    :return: 归一化坐标 (x, y, w, h, n)
    """
    # 获取图片宽度和高度
    img_height, img_width = img.shape[:2]

    # 计算左上角坐标和宽高
    x = min(x1, x2)
    y = min(y1, y2)
    w = abs(x2 - x1)
    h = abs(y2 - y1)

    # 归一化坐标
    norm_x = x / img_width
    norm_y = y / img_height
    norm_w = w / img_width
    norm_h = h / img_height
    n = 1  # 通常归一化系数为 1

    return norm_x, norm_y, norm_w, norm_h, n

# if __name__ == '__main__':
#     # 示例调用 remove_redeye_with_coords 函数
#     # 假设归一化坐标为 (0.3, 0.3, 0.2, 0.2, 1)
#     img = cv2.imread(input)
#     ans = img.copy()
#     x,y,w,h,n=to_normalized_coords(67,116,147,149,img)
#     remove_redeye_with_coords(x,y,w,h,n)

if __name__ == '__main__':
    # 使用 argparse 解析命令行参数
    parser = argparse.ArgumentParser(description="去除图片中的红眼")
    parser.add_argument('--x', type=float, required=True, help="左上角 x 坐标（归一化）")
    parser.add_argument('--y', type=float, required=True, help="左上角 y 坐标（归一化）")
    parser.add_argument('--w', type=float, required=True, help="宽度（归一化）")
    parser.add_argument('--h', type=float, required=True, help="高度（归一化）")
    parser.add_argument('--n', type=float, default=1, help="归一化系数（通常为1）")
    parser.add_argument('--input', type=str, required=True, help="输入图像路径")
    parser.add_argument('--output', type=str, required=True, help="输出图像路径")

    args = parser.parse_args()

    # 调用去红眼函数
    remove_redeye_with_coords(args.x, args.y, args.w, args.h, args.n, args.input, args.output)

