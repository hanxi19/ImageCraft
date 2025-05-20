// script.js
let selectedFiles = [];

// 处理文件选择
document.getElementById('gifFileInput').addEventListener('change', function(e) {
    selectedFiles = Array.from(e.target.files);
    updateFileList();
});

// 更新文件列表显示
function updateFileList() {
    const list = document.getElementById('fileList');
    list.innerHTML = '';
    
    selectedFiles.forEach((file, index) => {
        const li = document.createElement('li');
        li.className = 'file-item';
        li.draggable = true;
        li.innerHTML = `
            <span>${file.name}</span>
            <button onclick="removeFile(${index})">×</button>
        `;

        // 拖拽排序处理
        li.addEventListener('dragstart', e => {
            e.dataTransfer.setData('text/plain', index);
        });

        li.addEventListener('dragover', e => {
            e.preventDefault();
            li.style.backgroundColor = '#e0e0e0';
        });

        li.addEventListener('dragleave', e => {
            li.style.backgroundColor = '';
        });

        li.addEventListener('drop', e => {
            e.preventDefault();
            li.style.backgroundColor = '';
            const fromIndex = parseInt(e.dataTransfer.getData('text/plain'));
            const toIndex = index;
            swapFiles(fromIndex, toIndex);
        });

        list.appendChild(li);
    });
}

// 移除文件
function removeFile(index) {
    selectedFiles.splice(index, 1);
    updateFileList();
}

// 交换文件位置
function swapFiles(fromIndex, toIndex) {
    const temp = selectedFiles[fromIndex];
    selectedFiles[fromIndex] = selectedFiles[toIndex];
    selectedFiles[toIndex] = temp;
    updateFileList();
}

// 上传文件
async function uploadFiles() {
    if (selectedFiles.length < 2) {
        alert('请至少选择2张图片');
        return;
    }

    const formData = new FormData();
    selectedFiles.forEach(file => formData.append('files', file));

    try {
        const response = await fetch('/api/convert', {
            method: 'POST',
            body: formData
        });

        const resultDiv = document.getElementById('result');
        if (response.ok) {
            const blob = await response.blob();
            const url = URL.createObjectURL(blob);
            resultDiv.innerHTML = `
                <h2>转换成功！<br>
                <a href="${url}" download="animation.gif">下载GIF</a></h2>
                <br>
                <img src="${url}" style="max-width: 500px;">
            `;
        } else {
            const error = await response.text();
            resultDiv.innerHTML = `<div class="error">错误：${error}</div>`;
        }
    } catch (error) {
        alert('请求失败: ' + error.message);
    }
}