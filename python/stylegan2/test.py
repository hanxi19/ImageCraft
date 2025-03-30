import subprocess

output = r'E:\develop\pythonproject\stylegan2\stylegan2-ada-pytorch\output'
model_cifar10 = r'E:\develop\pythonproject\stylegan2\stylegan2-ada-pytorch\weights\cifar10.pkl'
seeds = 100
trunc=0.1

model=model_cifar10

# 定义命令行参数
command = [
    "python",  # 调用 Python 解释器
    "generate.py",  # 要执行的脚本
    f"--outdir={output}",  # 输出目录
    f"--seeds={seeds}",  # 种子
    f"--trunc={trunc}",  # 截断值
    f"--network={model}"  # 网络模型路径
]

# 执行命令
try:
    result = subprocess.run(command, check=True, text=True, capture_output=True)
    print("Output:", result.stdout)
    print("Errors:", result.stderr)
except subprocess.CalledProcessError as e:
    print("Error occurred:", e)