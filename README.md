# ImageCraft

![为 ImageCraft 项目生成 logo(1)](G:\下载\为 ImageCraft 项目生成 logo(1).png)

## 项目简介

ImageCraft 图艺工坊是一个集成化图片处理平台，整合了多种图片编辑、修复、生成和识别功能。

## 功能特性

### 基础图片编辑

- 格式转换（JPEG、PNG等）

- 图片裁剪

- 添加文字

- 图片压缩

- 多图转GIF

  ![image-20250603175414086](C:\Users\17204\AppData\Roaming\Typora\typora-user-images\image-20250603175414086.png)

### 高级功能

#### 1. 水印&目标物体消除

用户通过涂抹覆盖目标物体，系统基于生成式图像修复系统**deepfill**生成自然背景填充空缺区域。

![image-20250603172203377](C:\Users\17204\AppData\Roaming\Typora\typora-user-images\image-20250603172203377.png)

#### 2.图像语义分割

上传图片并标注目标物体关键点（keypoint），系统通过 SAM 模型实现精准语义分割，返回独立的目标物体图像或背景图像。

![image-20250603170510115](C:\Users\17204\AppData\Roaming\Typora\typora-user-images\image-20250603170510115.png)

#### 3.图片去划痕&噪点

基于**Bringing-Old-Photos-Back-to-Life**框架，系统修复图片中的划痕或噪点，还原图片的良好质感。

![image-20250603170620566](C:\Users\17204\AppData\Roaming\Typora\typora-user-images\image-20250603170620566.png)

#### 4.人像去红眼

修复由于闪光灯导致的人像红眼问题。

![image-20250603170658784](C:\Users\17204\AppData\Roaming\Typora\typora-user-images\image-20250603170658784.png)

#### 5. 图像生成

输入种子值，基于**StyleGAN2** 模型生成逼真的猫、狗、艺术图像。

![image-20250603171051340](C:\Users\17204\AppData\Roaming\Typora\typora-user-images\image-20250603171051340.png)

#### 6.OCR文字识别

基于 **PaddleOCR** 模型提取图片中的文字内容

![image-20250603171153914](C:\Users\17204\AppData\Roaming\Typora\typora-user-images\image-20250603171153914.png)

#### 7.文字翻译

完成对图片中文字的修改后，系统对接腾讯云 API，将待翻译文字数据翻译成目标语言

## 系统架构

[系统架构图]

## 快速开始

### 环境要求

- JDK 11+
- Python 3.8+
- MySQL 8.0+

### 安装步骤

1. 克隆仓库：

   ```
   git clone https://github.com/your-repo/imagecraft.git
   cd imagecraft
   ```

2. 后端设置：

   ```
   cd java
   # 配置application.properties中的数据库连接
   mvn spring-boot:run
   ```

3. 安装图片处理程序

   ```
   cd python
   conda create --prefix=.\imgc python=3.9
   conda activate imgc
   ```

   参考官方配置流程，将项目clone到/python文件夹中

   deepfill:

   https://github.com/nipponjo/deepfillv2-pytorch

   stylegan:

   https://github.com/NVlabs/stylegan2-ada-pytorch

   Bringing-Old-Photos-Back-to-Life:

   https://github.com/microsoft/Bringing-Old-Photos-Back-to-Life

   SAM:

   https://github.com/facebookresearch/segment-anything

   PaddleOcr:

   https://github.com/PaddlePaddle/PaddleOCR

   Tencent cloud:

   https://cloud.tencent.com/product/tmt?fromSource=gwzcw.9736864.9736864.9736864&utm_medium=cpc&utm_id=gwzcw.9736864.9736864.9736864&msclkid=d91987470cac15cf58d7ef79b3f8b3c5

   全部安装完成后，/python中应包含

   ```
   ├── imgc
   ├── Bringing-Old-Photos-Back-to-Life
   ├── stylegan2
   ├── deepFillv2
   ├── segment
   ├── OCR.py
   ├── segment.py
   ├── del_redeyes.py
   ├── translate.py
   ```

4. 访问应用：

   ```
   http://localhost:8081/Tourist_Homepage
   ```

## 项目结构

```
imagecraft/
├── image	# 图片存储
├── java	#后端代码
├── python	# 图片处理脚本
└── 前端	# 前端代码
```