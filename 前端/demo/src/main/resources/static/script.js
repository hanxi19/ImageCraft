        const body = document.body;

        // 监听鼠标移动事件
        document.addEventListener('mousemove', function(event) {
            // 获取鼠标在窗口中的位置
            const x = event.clientX;
            const y = event.clientY;
            // 根据鼠标位置计算背景颜色（这里简单使用x和y的某种组合）
            const red = Math.min(255, Math.round((x / window.innerWidth) * 255 * 0.8));
            const green = Math.min(255, Math.round((y / window.innerHeight) * 255 * 0.8));
            const blue = 128; // 固定蓝色值，也可以根据x和y计算
            // 设置背景颜色
            body.style.backgroundColor = `rgb(${red}, ${green}, ${blue})`;
        });
		
		//-----------------------------------------------------------------------生成泡泡
		function createBubble() {
  		const bubble = document.createElement('div');
  		bubble.classList.add('bubble');
  
  		// 随机决定泡泡是从左边还是右边出现
  		const isLeft = Math.random() < 0.5;
  		bubble.style.width = `${Math.random() * 30 + 20}px`;
  		bubble.style.height = bubble.style.width;
  		bubble.style.bottom = `${Math.random() * 100}%`;
  		bubble.style.animationDuration = `${Math.random() * 3 + 2}s`;
  		bubble.style.animationDelay = `${Math.random() * 5}s`;

  		// 设置泡泡的位置
  		if (isLeft) {
  			bubble.style.left = `${Math.random() * 10}%`; // 左侧范围0%-10%
    		bubble.style.right = 'auto'; // 确保right不被设置
  		} else {
    		bubble.style.right = `${Math.random() * 10}%`; // 右侧范围0%-10%
    		bubble.style.left = 'auto'; // 确保left不被设置
  		}

  		document.querySelector('body').appendChild(bubble);
		}

		// 生成一定数量的气泡
		document.addEventListener('DOMContentLoaded', function() {
  		for (let i = 0; i < 50; i++) {
    		createBubble();
  			}
		});

		function adjustBubblePositionAndSize() {
  		const screenWidth = window.innerWidth;
  		const bubbles = document.querySelectorAll('.bubble');
  		bubbles.forEach(bubble => {
    		// 重新随机决定泡泡是从左边还是右边出现
    		const isLeft = Math.random() < 0.5;
    		bubble.style.width = `${Math.random() * screenWidth / 10 + 20}px`;
    		bubble.style.height = bubble.style.width;
    		bubble.style.bottom = `${Math.random() * 100}%`; 

    		// 更新位置
    		if (isLeft) {
      		bubble.style.left = `${Math.random() * 10}%`;
      		bubble.style.right = 'auto';
    		} else {
      		bubble.style.right = `${Math.random() * 10}%`;
      		bubble.style.left = 'auto';
    		}

    		bubble.style.animation = `rise ${bubble.style.animationDuration}s ease-in-out ${bubble.style.animationDelay}s`;
  		});
		}

		window.addEventListener('resize', adjustBubblePositionAndSize);
		//----------------------------------------------------------------------------------点击特效
        (function () {
            var a_idx = 0;
			var left = 500; // 区域的左边界
    		var right = 1100; // 区域的右边界
			
            window.onclick = function (event) {
				if (event.clientX >= right || event.clientX <= left) {
                	//var a = new Array("压力差ΔP","Pa","mbar","流量Q","m^3/s^-1","μl/min","流阻R","Pa*s/m^-3","mbar*min/μl","电压差ΔV","V","电流I","A","电阻R","Ω");
 					var a = new Array("❤富强❤","❤民主❤", "❤文明❤", "❤和谐❤","❤自由❤", "❤平等❤", "❤公正❤","❤法治❤", "❤爱国❤", "❤敬业❤","❤诚信❤", "❤友善❤");
                	var heart = document.createElement("b"); //创建b元素
                	heart.onselectstart = new Function('event.returnValue=false'); //防止拖动
 
                	document.body.appendChild(heart).innerHTML = a[a_idx]; //将b元素添加到页面上
                	a_idx = (a_idx + 1) % a.length;
                	heart.style.cssText = "position: fixed;left:-100%;"; //给p元素设置样式
 
                	var f = 16, // 字体大小
                	    x = event.clientX - f / 2, // 横坐标
                	    y = event.clientY - f, // 纵坐标
                	    c = randomColor(), // 随机颜色
                 	    a = 1, // 透明度
                	    s = 1.2; // 放大缩小
 
                	var timer = setInterval(function () { //添加定时器
                	    if (a <= 0) {
                	        document.body.removeChild(heart);
                 	       clearInterval(timer);
                	    } else {
                	        heart.style.cssText = "font-size:16px;cursor: default;position: fixed;color:" +
                	           c + ";left:" + x + "px;top:" + y + "px;opacity:" + a + ";transform:scale(" +
                 	           s + ");";
                        	y--;
                        	a -= 0.016;
                        	s += 0.002;
                    	}
                	}, 15)
            	}
			};
            // 随机颜色
            function randomColor() {
    			var r = ~~(Math.random() * 128) + 128; // 红色分量在128到255之间
    			var g = ~~(Math.random() * 128) + 128; // 绿色分量在128到255之间
    			var b = ~~(Math.random() * 128) + 128; // 蓝色分量在128到255之间
    			return "rgb(" + r + "," + g + "," + b + ")";
			}
        }());
		//------------------------------------------------------------------------------------------

        function registerUser() {
            var username = document.getElementById('username').value;
            var password = document.getElementById('password').value;

            fetch('/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
            })
            .then(response => response.json())
            .then(data => {
                if (data.error) {
                    alert(data.error);
                } else {
                    alert(data.message);
                    window.location.href = '/login';
                }
            })
            .catch(error => {
                console.error('Error:', error);
            });
        }
	

document.addEventListener('DOMContentLoaded', function () {
    const inputForm = document.getElementById('inputForm');
    const resultArea = document.getElementById('resultArea');
    const dataTypeSelect = document.getElementById('dataType');
    const type1Inputs = document.getElementById('type1Inputs');
    const type2Inputs = document.getElementById('type2Inputs');
    const type3Inputs = document.getElementById('type3Inputs');

    if (dataTypeSelect) {
		type1Inputs.style.display = 'block';
		
        dataTypeSelect.onchange = function () {
            type1Inputs.style.display = 'none';
            type2Inputs.style.display = 'none';
            type3Inputs.style.display = 'none';
            if (dataTypeSelect.value === 'type1') {
                type1Inputs.style.display = 'block';
            } else if (dataTypeSelect.value === 'type2') {
                type2Inputs.style.display = 'block';
            } else if (dataTypeSelect.value === 'type3') {
                type3Inputs.style.display = 'block';
            }
        };
    }

    if (inputForm) {
        inputForm.addEventListener('submit', function (event) {
            event.preventDefault();
            const dataType = dataTypeSelect.value;
            let inputData = [];

            if (dataType === 'type3') {
                for (let i = 1; i <= 9; i++) {
                    const input = document.querySelector(`input[name="${dataType}_data${i}"]`);
                    if (!input) {
                        console.error(`未找到 name 为 ${dataType}_data${i} 的输入框`);
                        return;
                    }
                    const value = ensureNumber(input.value);
                    console.log(`${dataType}_data${i} 的输入值:`, input.value, '解析后的值:', value);
                    inputData.push(value);
                }
                if (inputData.length!== 9) {
                    alert('输入数据长度必须为 9');
                    console.error('输入数据长度不符合要求:', inputData);
                    return;
                }
            } else {
                for (let i = 1; i <= 8; i++) {
                    const input = document.querySelector(`input[name="${dataType}_data${i}"]`);
                    if (!input) {
                        console.error(`未找到 name 为 ${dataType}_data${i} 的输入框`);
                        return;
                    }
                    const value = ensureNumber(input.value);
                    console.log(`${dataType}_data${i} 的输入值:`, input.value, '解析后的值:', value);
                    inputData.push(value);
                }
                if (inputData.length!== 8) {
                    alert('输入数据长度必须为 8');
                    console.error('输入数据长度不符合要求:', inputData);
                    return;
                }
            }

            if (inputData.some(value => isNaN(value))) {
                alert('输入数据必须是有效的数字');
                console.error('无效输入数据:', inputData);
                return;
            }

            console.log('即将发送的数据:', { dataType: dataType, inputData: inputData });

            fetch('/predict', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ dataType: dataType, inputData: inputData })
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`HTTP 错误！状态码: ${response.status}`);
                    }
                    return response.json();
                })
                .then(data => {
                    if (data && data.prediction!== undefined) {
                        resultArea.textContent = data.prediction;
                    } else {
                        throw new Error('无效的响应格式。缺少预测字段。');
                    }
                })
                .catch(error => {
                    console.error('错误:', error);
                    resultArea.textContent = '请登录后使用，可查询历史记录。';
                });
        });
    }
});

function ensureNumber(value) {
    value = value.trim();
    const num = Number(value);
    if (isNaN(num)) {
        return NaN;
    }
    return num;
}