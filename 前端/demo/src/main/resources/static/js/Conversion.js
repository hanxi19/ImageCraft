// 获取DOM元素
        const dropArea = document.getElementById('dropArea');
        const fileInput = document.getElementById('fileInput');
        const selectBtn = document.getElementById('selectBtn');
        const convertBtn = document.getElementById('convertBtn');
        const downloadBtn = document.getElementById('downloadBtn');
        const formatSelect = document.getElementById('formatSelect');
        const jpegOptions = document.getElementById('jpegOptions');
        const jpegQuality = document.getElementById('jpegQuality');
        const qualityValue = document.getElementById('qualityValue');
        const preview = document.getElementById('preview');
        const statusEl = document.getElementById('status');
        
        let originalFile = null;
        let convertedBlob = null;
        
        // 显示JPEG质量选项
        function updateFormatOptions() {
            if (formatSelect.value === 'jpeg') {
                jpegOptions.style.display = 'block';
            } else {
                jpegOptions.style.display = 'none';
            }
        }
        
        // 初始化
        updateFormatOptions();
        
        // 事件监听器
        formatSelect.addEventListener('change', updateFormatOptions);
        
        jpegQuality.addEventListener('input', function() {
            qualityValue.textContent = this.value;
        });
        
        selectBtn.addEventListener('click', function() {
            fileInput.click();
        });
        
        fileInput.addEventListener('change', function(e) {
            if (e.target.files.length) {
                handleFile(e.target.files[0]);
            }
        });
        
        // 拖放区域事件
        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
            dropArea.addEventListener(eventName, preventDefaults, false);
        });
        
        function preventDefaults(e) {
            e.preventDefault();
            e.stopPropagation();
        }
        
        ['dragenter', 'dragover'].forEach(eventName => {
            dropArea.addEventListener(eventName, highlight, false);
        });
        
        ['dragleave', 'drop'].forEach(eventName => {
            dropArea.addEventListener(eventName, unhighlight, false);
        });
        
        function highlight() {
            dropArea.classList.add('highlight');
        }
        
        function unhighlight() {
            dropArea.classList.remove('highlight');
        }
        
        dropArea.addEventListener('drop', function(e) {
            const dt = e.dataTransfer;
            const file = dt.files[0];
            if (file && file.type.startsWith('image/')) {
                handleFile(file);
            } else {
                statusEl.textContent = '请选择有效的图片文件';
            }
        }, false);
        
        // 处理选择的文件
        function handleFile(file) {
            originalFile = file;
            convertBtn.disabled = false;
            statusEl.textContent = `已选择文件: ${file.name} (${(file.size / 1024).toFixed(2)} KB)`;
            
            // 显示预览
            const reader = new FileReader();
            reader.onload = function(e) {
                preview.src = e.target.result;
                preview.style.display = 'block';
            };
            reader.readAsDataURL(file);
        }
        
        // 转换图片
        convertBtn.addEventListener('click', function() {
            if (!originalFile) return;
            
            statusEl.textContent = '正在转换...';
            
            const reader = new FileReader();
            reader.onload = function(e) {
                const img = new Image();
                img.onload = function() {
                    // 创建canvas
                    const canvas = document.createElement('canvas');
                    canvas.width = img.width;
                    canvas.height = img.height;
                    const ctx = canvas.getContext('2d');
                    ctx.drawImage(img, 0, 0);
                    
                    // 转换格式
                    let mimeType;
                    switch (formatSelect.value) {
                        case 'jpeg':
                            mimeType = 'image/jpeg';
                            break;
                        case 'png':
                            mimeType = 'image/png';
                            break;
                        case 'webp':
                            mimeType = 'image/webp';
                            break;
                        case 'bmp':
                            mimeType = 'image/bmp';
                            break;
                        case 'gif':
                            mimeType = 'image/gif';
                            break;
                        default:
                            mimeType = 'image/png';
                    }
                    
                    // 转换质量
                    let quality = 1.0;
                    if (formatSelect.value === 'jpeg') {
                        quality = parseInt(jpegQuality.value) / 100;
                    }
                    
                    // 转换为Blob
                    canvas.toBlob(function(blob) {
                       if (!blob) {
        					console.error("转换失败：未生成 Blob 数据");
        					return;
    					}
    				   convertedBlob = blob;
    				   console.log("转换成功，Blob:", blob); // 检查是否生成
    					// 启用下载按钮并显示
    					downloadBtn.disabled = false;
    					downloadBtn.style.display = "inline-block"; // 强制显示                       
                        // 显示转换后的预览
                        const url = URL.createObjectURL(blob);
                        preview.src = url;
                        
                        statusEl.textContent = `转换完成! 新格式: ${formatSelect.value.toUpperCase()}, 大小: ${(blob.size / 1024).toFixed(2)} KB`;
                    }, mimeType, quality);
                };
                img.src = e.target.result;
            };
            reader.readAsDataURL(originalFile);
        });
        
        // 下载转换后的图片
        downloadBtn.addEventListener('click', function() {
            if (!convertedBlob) return;
            
            const url = URL.createObjectURL(convertedBlob);
            const a = document.createElement('a');
            a.href = url;
            
            // 获取原始文件名（不含扩展名）
            const originalName = originalFile.name.split('.').slice(0, -1).join('.');
            a.download = `${originalName}.${formatSelect.value}`;
            
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            
            // 释放URL对象
            setTimeout(() => {
                URL.revokeObjectURL(url);
            }, 100);
        });