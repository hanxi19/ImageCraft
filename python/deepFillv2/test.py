import argparse
from PIL import Image
import torch
import torchvision.transforms as T
from PIL import Image

def resize_image(image_path, output_path, target_size):
    """
    将图片等比缩放到指定的分辨率，尽量保证图片质量，并删除黑色背景。
    
    :param image_path: 输入图片路径
    :param output_path: 输出图片路径
    :param target_size: 目标分辨率 (宽, 高)
    """
    with Image.open(image_path) as img:
        # 获取原始宽高
        original_width, original_height = img.size
        target_width, target_height = target_size

        # 计算等比缩放比例
        scale = min(target_width / original_width, target_height / original_height)

        # 计算新的宽高
        new_width = int(original_width * scale)
        new_height = int(original_height * scale)

        # 缩放图片
        resized_img = img.resize((new_width, new_height), Image.LANCZOS)

        # 保存结果（直接保存缩放后的图片，无黑色背景）
        resized_img.save(output_path, quality=95)  # 设置高质量保存



parser = argparse.ArgumentParser(description='Test inpainting')
parser.add_argument("--image", type=str,
                    default="examples/inpaint/case1.png", help="path to the image file")
parser.add_argument("--mask", type=str,
                    default="examples/inpaint/case1_mask.png", help="path to the mask file")
parser.add_argument("--out", type=str,
                    default="examples/inpaint/case1_out_test.png", help="path for the output file")
parser.add_argument("--checkpoint", type=str,
                    default="pretrained/states_tf_places2.pth", help="path to the checkpoint file")


def main():

    args = parser.parse_args()

    generator_state_dict = torch.load(args.checkpoint)['G']

    if 'stage1.conv1.conv.weight' in generator_state_dict.keys():
        from model.networks import Generator
    else:
        from model.networks_tf import Generator  

    use_cuda_if_available = True
    device = torch.device('cuda' if torch.cuda.is_available()
                          and use_cuda_if_available else 'cpu')

    # set up network
    generator = Generator(cnum_in=5, cnum=48, return_flow=False).to(device)

    generator_state_dict = torch.load(args.checkpoint)['G']
    generator.load_state_dict(generator_state_dict, strict=True)

    resize_image(args.image, args.image, (256, 256))

    # load image and mask
    image = Image.open(args.image)
    mask = Image.open(args.mask)
    # 等比缩放 mask 到与 image 相同的分辨率
    if mask.size != image.size:
        mask = mask.resize(image.size, Image.NEAREST)

    # prepare input
    image = T.ToTensor()(image)
    mask = T.ToTensor()(mask)

    _, h, w = image.shape
    grid = 8

    image = image[:3, :h//grid*grid, :w//grid*grid].unsqueeze(0)
    mask = mask[0:1, :h//grid*grid, :w//grid*grid].unsqueeze(0)

    print(f"Shape of image: {image.shape}")

    image = (image*2 - 1.).to(device)  # map image values to [-1, 1] range
    mask = (mask > 0.5).to(dtype=torch.float32,
                           device=device)  # 1.: masked 0.: unmasked

    image_masked = image * (1.-mask)  # mask image

    ones_x = torch.ones_like(image_masked)[:, 0:1, :, :]
    x = torch.cat([image_masked, ones_x, ones_x*mask],
                  dim=1)  # concatenate channels

    with torch.inference_mode():
        _, x_stage2 = generator(x, mask)

    # complete image
    image_inpainted = image * (1.-mask) + x_stage2 * mask

    # save inpainted image
    img_out = ((image_inpainted[0].permute(1, 2, 0) + 1)*127.5)
    img_out = img_out.to(device='cpu', dtype=torch.uint8)
    img_out = Image.fromarray(img_out.numpy())
    img_out.save(args.out)

    print(f"Saved output file at: {args.out}")


if __name__ == '__main__':
    main()
