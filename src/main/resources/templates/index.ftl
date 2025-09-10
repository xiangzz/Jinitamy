<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${title}</title>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            margin: 0;
            padding: 0;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
        }
        
        .container {
            background: rgba(255, 255, 255, 0.95);
            border-radius: 15px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
            padding: 2rem;
            margin: 2rem;
            max-width: 800px;
            width: 80%;
            text-align: center;
        }
        
        h1 {
            color: #333;
            margin-bottom: 2rem;
            font-size: 2.5rem;
            background: linear-gradient(90deg, #667eea, #764ba2);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }
        
        .content {
            font-size: 1.2rem;
            line-height: 1.6;
            color: #444;
            min-height: 150px;
            padding: 1.5rem;
            border-radius: 8px;
            background: #f8f9fa;
            overflow: hidden;
        }
        
        /* 光标样式 */
        .typewriter-cursor {
            display: inline-block;
            background-color: #764ba2;
            width: 3px;
            height: 1.2em;
            margin-left: 4px;
            animation: blink 1s infinite;
            vertical-align: middle;
        }
        
        @keyframes blink {
            0%, 50% { opacity: 1; }
            51%, 100% { opacity: 0; }
        }
        
        /* 控制按钮 */
        .controls {
            margin-top: 1.5rem;
            display: flex;
            gap: 1rem;
            justify-content: center;
        }
        
        .controls button {
            background: linear-gradient(90deg, #667eea, #764ba2);
            color: white;
            border: none;
            padding: 0.5rem 1rem;
            border-radius: 4px;
            cursor: pointer;
            transition: transform 0.2s, opacity 0.2s;
        }
        
        .controls button:hover {
            transform: translateY(-2px);
            opacity: 0.9;
        }
        
        @media (max-width: 768px) {
            .container {
                width: 95%;
                padding: 1rem;
            }
            
            h1 {
                font-size: 2rem;
            }
            
            .content {
                min-height: 120px;
                padding: 1rem;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>${title}</h1>
        <div class="content" id="typewriter-content">${content}</div>
        <div class="controls">
            <button id="pause-btn">暂停</button>
            <button id="restart-btn">重新开始</button>
        </div>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            
            const contentElement = document.getElementById('typewriter-content');
            const pauseBtn = document.getElementById('pause-btn');
            const restartBtn = document.getElementById('restart-btn');
            
            // 示例文本 - 可以替换为您自己的内容
            const text = contentElement.textContent || contentElement.innerText;
            contentElement.innerHTML = '';
            const texts = [text.trim()];
            
            let currentTextIndex = 0;
            let currentCharIndex = 0;
            let isDeleting = false;
            let isPaused = false;
            let typingSpeed = 60;
            let deletingSpeed = 30;
            let pauseTime = 2000;
            let timeoutId = null;
            
            // 启动打字机效果
            function typeWriter() {
                if (isPaused) return;
                
                const currentText = texts[currentTextIndex];
                
                if (!isDeleting && currentCharIndex <= currentText.length) {
                    // 打字阶段
                    contentElement.innerHTML = currentText.substring(0, currentCharIndex) + '<span class="typewriter-cursor"></span>';
                    currentCharIndex++;
                    timeoutId = setTimeout(typeWriter, typingSpeed);
                } else if (isDeleting && currentCharIndex >= 0) {
                    // 删除阶段
                    contentElement.innerHTML = currentText.substring(0, currentCharIndex) + '<span class="typewriter-cursor"></span>';
                    currentCharIndex--;
                    timeoutId = setTimeout(typeWriter, deletingSpeed);
                } else {
                    // 切换模式或文本
                    isDeleting = !isDeleting;
                    
                    if (!isDeleting) {
                        // 移动到下一段文本
                        currentTextIndex = (currentTextIndex + 1) % texts.length;
                    }
                    
                    timeoutId = setTimeout(typeWriter, pauseTime);
                }
            }
            
            // 初始化
            typeWriter();
            
            // 控制按钮事件
            pauseBtn.addEventListener('click', function() {
                isPaused = !isPaused;
                this.textContent = isPaused ? '继续' : '暂停';
                
                if (!isPaused) {
                    typeWriter();
                } else {
                    clearTimeout(timeoutId);
                }
            });
            
            restartBtn.addEventListener('click', function() {
                clearTimeout(timeoutId);
                currentTextIndex = 0;
                currentCharIndex = 0;
                isDeleting = false;
                isPaused = false;
                pauseBtn.textContent = '暂停';
                contentElement.innerHTML = '';
                typeWriter();
            });
        });
    </script>
</body>
</html>