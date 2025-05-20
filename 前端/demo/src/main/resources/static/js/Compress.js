document.getElementById('compressBtn').addEventListener('click', function() {
            const input = document.getElementById('inputImage');
            const quality = parseInt(document.getElementById('quality').value);
            const optimize = document.getElementById('optimize').checked;
            const maxWidth = document.getElementById('maxWidth').value;
            const maxHeight = document.getElementById('maxHeight').value;
            
            const resultDiv = document.getElementById('result');
            const errorDiv = document.getElementById('error');
            
            // 重置显示
            resultDiv.style.display = 'none';
            errorDiv.textContent = '';
            
            if (!input.files || input.files.length === 0) {
                errorDiv.textContent = '请选择一张图片';
                return;
            }
            
            if (isNaN(quality) || quality < 1 || quality > 100) {
                errorDiv.textContent = '请输入1-100之间的质量值';
                return;
            }
            
            const file = input.files[0];
            const reader = new FileReader();
            
            reader.onload = function(e) {
                const img = new Image();
                img.onload = function() {
                    // 创建canvas进行压缩
                    const canvas = document.createElement('canvas');
                    const ctx = canvas.getContext('2d');
                    
                    let width = img.width;
                    let height = img.height;
                    
                    // 如果有最大尺寸限制，计算新的尺寸
                    if (maxWidth || maxHeight) {
                        const maxW = maxWidth ? parseInt(maxWidth) : Infinity;
                        const maxH = maxHeight ? parseInt(maxHeight) : Infinity;
                        
                        const ratio = Math.min(maxW / width, maxH / height, 1);
                        width = Math.floor(width * ratio);
                        height = Math.floor(height * ratio);
                    }
                    
                    canvas.width = width;
                    canvas.height = height;
                    ctx.drawImage(img, 0, 0, width, height);
                    
                    try {
                        // 转换为Blob对象
                        canvas.toBlob(function(blob) {
                            // 创建下载链接
                            const url = URL.createObjectURL(blob);
                            const a = document.createElement('a');
                            a.href = url;
                            a.download = 'compressed_' + (file.name || 'image.jpg');;
                            document.body.appendChild(a);
                            a.click();
                            document.body.removeChild(a);
                            URL.revokeObjectURL(url);
                            
                            // 显示结果
                            const originalSize = file.size;
                            const compressedSize = blob.size;
                            
                            resultDiv.innerHTML = `
                                <p>压缩完成: ${file.name}</p>
                                <p>原始大小: ${(originalSize/1024).toFixed(2)} KB</p>
                                <p>压缩后大小: ${(compressedSize/1024).toFixed(2)} KB</p>
                                <p>压缩率: ${((1 - compressedSize/originalSize)*100).toFixed(2)}%</p>
                            `;
                            resultDiv.style.display = 'block';
                        }, file.type, quality/100);
                    } catch (e) {
                        errorDiv.textContent = '压缩失败: ' + e.message;
                    }
                };
                
                img.onerror = function() {
                    errorDiv.textContent = '图片加载失败';
                };
                
                img.src = e.target.result;
            };
            
            reader.onerror = function() {
                errorDiv.textContent = '文件读取失败';
            };
            
            reader.readAsDataURL(file);
        });