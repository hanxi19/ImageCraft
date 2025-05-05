#!/usr/bin/env python3
import sys
import json
import argparse
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
    parser.add_argument('--secret_id', required=True,
                        help='腾讯云SecretId\n可从环境变量TENCENT_SECRET_ID读取')
    parser.add_argument('--secret_key', required=True,
                        help='腾讯云SecretKey\n可从环境变量TENCENT_SECRET_KEY读取')

    args = parser.parse_args()

    # 读取输入文件
    try:
        with open(args.input_file, 'r', encoding='utf-8') as f:
            input_lines = f.read().splitlines()
    except Exception as e:
        print(f"无法读取文件: {str(e)}", file=sys.stderr)
        sys.exit(1)

    # 初始化翻译器
    translator = LinePreservingTranslator(args.secret_id, args.secret_key)

    # 逐行翻译并保持格式
    output_lines = []
    for line in input_lines:
        if line.strip():  # 非空行才翻译
            translated = translator.translate_line(line, args.target)
            output_lines.append(translated)
        else:
            output_lines.append("")  # 保留空行

    # 输出结果
    print('\n'.join(output_lines))


if __name__ == "__main__":
    main()