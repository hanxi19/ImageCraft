#!/usr/bin/env python3
import sys
import json
import argparse
import os
from tencentcloud.common import credential
from tencentcloud.common.profile.client_profile import ClientProfile
from tencentcloud.common.profile.http_profile import HttpProfile
from tencentcloud.common.exception.tencent_cloud_sdk_exception import TencentCloudSDKException
from tencentcloud.tmt.v20180321 import tmt_client, models


class LinePreservingTranslator:
    def __init__(self, secret_id, secret_key, region="ap-beijing"):
        """初始化翻译器"""
        self.cred = credential.Credential(secret_id.strip(), secret_key.strip())
        self.client = self._init_client(region)

    def _init_client(self, region):
        """初始化腾讯云客户端"""
        http_profile = HttpProfile()
        http_profile.endpoint = "tmt.tencentcloudapi.com"
        client_profile = ClientProfile()
        client_profile.httpProfile = http_profile
        return tmt_client.TmtClient(self.cred, region, client_profile)

    def translate_line(self, text, target_lang):
        """翻译单行文本"""
        try:
            req = models.TextTranslateRequest()
            req.from_json_string(json.dumps({
                "SourceText": text,
                "Source": "auto",
                "Target": target_lang,
                "ProjectId": 0
            }))
            resp = self.client.TextTranslate(req)
            return resp.TargetText
        except TencentCloudSDKException as e:
            print(f"翻译失败: {str(e)}", file=sys.stderr)
            return text  # 失败时返回原文


def main():
    parser = argparse.ArgumentParser(
        description='保留行格式的文本翻译工具',
        formatter_class=argparse.RawTextHelpFormatter
    )
    parser.add_argument('--input_file', required=True,
                        help='包含待翻译文本的文件路径\n示例: E:\path\to\input.txt')
    parser.add_argument('--target', required=True,
                        help='目标语言代码\n示例: zh-中文, en-英文, ja-日文')
    parser.add_argument('--output_path', required=True,
                        help='翻译结果输出目录\n示例: E:\path\to\output')

    args = parser.parse_args()

    # 从环境变量获取SecretId和SecretKey
    secret_id = os.getenv("TENCENT_SECRET_ID")
    secret_key = os.getenv("TENCENT_SECRET_KEY")

    if not secret_id or not secret_key:
        print("错误: 必须设置TENCENT_SECRET_ID和TENCENT_SECRET_KEY环境变量", file=sys.stderr)
        sys.exit(1)

    try:
        # 读取输入文件
        with open(args.input_file, 'r', encoding='utf-8') as f:
            input_lines = f.read().splitlines()

        # 初始化翻译器
        translator = LinePreservingTranslator(secret_id, secret_key)

        # 逐行翻译并保持格式
        output_lines = []
        for line in input_lines:
            if line.strip():  # 非空行才翻译
                translated = translator.translate_line(line, args.target)
                output_lines.append(translated)
            else:
                output_lines.append("")  # 保留空行

        # 生成输出文件名和路径
        input_filename = os.path.basename(args.input_file)
        output_filename = f"{os.path.splitext(input_filename)[0]}_translate.txt"
        output_filepath = os.path.join(args.output_path, output_filename)

        # 确保输出目录存在
        os.makedirs(args.output_path, exist_ok=True)

        # 保存结果到文件
        with open(output_filepath, 'w', encoding='utf-8') as f:
            f.write('\n'.join(output_lines))

        print(f"Translation completed, result saved to: {output_filepath}")

    except Exception as e:
        print(f"Error during translation: {str(e)}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()