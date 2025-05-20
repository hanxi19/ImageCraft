let cropper = null;
let croppedBlob = null;
// 初始化图片选择
document.getElementById('fileInput').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = function(event) {
        initCropper(event.target.result);
    };
    reader.readAsDataURL(file);
});

// 初始化裁剪器
function initCropper(imageSrc) {
    const img = document.getElementById('previewImage');
    const hint = document.querySelector('.upload-hint');

    hint.style.display = 'none';
    img.src = imageSrc;

    if (cropper) {
        cropper.destroy();
    }

    cropper = new Cropper(img, {
        aspectRatio: NaN,
        viewMode: 1,
        autoCropArea: 1,
        responsive: true,
        checkCrossOrigin: false
    });
}

// 处理裁剪
function handleCrop() {
    if (!cropper) {
        alert('请先选择图片');
        return;
    }

    // 获取裁剪数据
    const canvas = cropper.getCroppedCanvas({
        imageSmoothingQuality: 'high'
    });

    // 显示结果预览
    canvas.toBlob(blob => {
        croppedBlob = blob;
        const url = URL.createObjectURL(blob);
        document.getElementById('croppedResult').src = url;
        document.getElementById('resultPreview').style.display = 'block';
        document.getElementById('downloadBtn').disabled = false;
    });
}

// 下载结果
function downloadResult() {
    if (!croppedBlob) return;

    const url = URL.createObjectURL(croppedBlob);
    const a = document.createElement('a');
    a.download = `cropped-${Date.now()}.png`;
    a.href = url;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}
// 修改裁剪处理函数
function handleCrop() {
    if (!cropper) {
        alert('请先选择图片');
        return;
    }

    const canvas = cropper.getCroppedCanvas({
        imageSmoothingQuality: 'high'
    });

    // 添加尺寸限制逻辑
    const maxDisplaySize = {
        width: 800,  // 最大显示宽度
        height: 600  // 最大显示高度
    };

    // 计算缩放比例
    let scale = Math.min(
        maxDisplaySize.width / canvas.width,
        maxDisplaySize.height / canvas.height
    );

    // 创建缩放后的canvas
    const scaledCanvas = document.createElement('canvas');
    scaledCanvas.width = canvas.width * scale;
    scaledCanvas.height = canvas.height * scale;

    const ctx = scaledCanvas.getContext('2d');
    ctx.drawImage(canvas,
        0, 0, canvas.width, canvas.height,
        0, 0, scaledCanvas.width, scaledCanvas.height
    );

    // 显示缩放后的预览
    scaledCanvas.toBlob(blob => {
        croppedBlob = blob;
        const url = URL.createObjectURL(blob);
        const resultImg = document.getElementById('croppedResult');

        resultImg.onload = () => {
            URL.revokeObjectURL(url); // 清除内存
        };

        resultImg.src = url;
        document.getElementById('resultPreview').style.display = 'block';
        document.getElementById('downloadBtn').disabled = false;
    }, 'image/png');
}