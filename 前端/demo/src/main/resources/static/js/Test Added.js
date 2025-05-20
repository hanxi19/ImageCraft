document.addEventListener('DOMContentLoaded', function() {
            const canvas = document.getElementById('imageCanvas');
            const ctx = canvas.getContext('2d');
            const imageUpload = document.getElementById('imageUpload');
            const textInput = document.getElementById('textInput');
            const fontSize = document.getElementById('fontSize');
            const fontColor = document.getElementById('fontColor');
            const fontFamily = document.getElementById('fontFamily');
            const textX = document.getElementById('textX');
            const textY = document.getElementById('textY');
            const addTextBtn = document.getElementById('addTextBtn');
            const clearTextBtn = document.getElementById('clearTextBtn');
            const downloadBtn = document.getElementById('downloadBtn');
            
            let currentImage = null;
            
             //上传图片
            imageUpload.addEventListener('change', function(e) {
                const file = e.target.files[0];
                if (file) {
                    const reader = new FileReader();
                    reader.onload = function(event) {
                        const img = new Image();
                        img.onload = function() {
                            canvas.width = img.naturalWidth;
                canvas.height = img.naturalHeight;
                canvas.style.width = '100%';
                canvas.style.height = 'auto';
                ctx.drawImage(img, 0, 0);
                currentImage = img;
                        };
                        img.src = event.target.result;
                    };
                    reader.readAsDataURL(file);
                }
            });
            
            // 添加文字
            addTextBtn.addEventListener('click', function() {
                if (!currentImage) {
                    alert('请先上传图片');
                    return;
                }
                
                const text = textInput.value;
                if (!text) {
                    alert('请输入要添加的文字');
                    return;
                }
                
                // 重绘原始图片
                ctx.drawImage(currentImage, 0, 0);
                
                // 设置文字样式
                ctx.font = `${fontSize.value}px ${fontFamily.value}`;
                ctx.fillStyle = fontColor.value;
                ctx.textBaseline = 'top';
                
                // 添加文字
                ctx.fillText(text, parseInt(textX.value), parseInt(textY.value));
            });
            
            // 清除文字
            clearTextBtn.addEventListener('click', function() {
                if (currentImage) {
                    ctx.drawImage(currentImage, 0, 0);
                }
            });
            
            // 下载图片
            downloadBtn.addEventListener('click', function() {
                if (!currentImage) {
                    alert('请先上传图片');
                    return;
                }
                
                const link = document.createElement('a');
                link.download = 'image-with-text.png';
                link.href = canvas.toDataURL('image/png');
                link.click();
            });
            
            // 点击图片设置文字位置
            canvas.addEventListener('click', function(e) {
                if (!currentImage) return;
               
                const rect = canvas.getBoundingClientRect();
                 const scaleX = canvas.width / rect.width;
    			 const scaleY = canvas.height / rect.height;
    // 应用缩放比例计算实际画布坐标
                 const x = (e.clientX - rect.left) * scaleX;
    			 const y = (e.clientY - rect.top) * scaleY;
    
    			 textX.value = Math.round(x);
    			 textY.value = Math.round(y);
			});
        });