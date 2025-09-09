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
            min-height: 100px;
            padding: 1rem;
            border-radius: 8px;
            background: #f8f9fa;
        }
        
        /* 打字机效果样式 */
        .typewriter {
            overflow: hidden;
            border-right: .15em solid orange;
            white-space: nowrap;
            margin: 0 auto;
            letter-spacing: .15em;
            animation: 
                typing 3.5s steps(40, end),
                blink-caret .75s step-end infinite;
        }
        
        /* 打字机效果动画 */
        @keyframes typing {
            from { width: 0 }
            to { width: 100% }
        }
        
        /* 光标闪烁动画 */
        @keyframes blink-caret {
            from, to { border-color: transparent }
            50% { border-color: orange; }
        }
        
        @media (max-width: 768px) {
            .container {
                width: 95%;
                padding: 1rem;
            }
            
            h1 {
                font-size: 2rem;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>${title}</h1>
        <div class="content" id="typewriter-content">
            ${content}
        </div>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const contentElement = document.getElementById('typewriter-content');
            const originalText = contentElement.textContent;
            
            // 清空内容并添加打字机类
            contentElement.textContent = '';
            contentElement.classList.add('typewriter');
            
            // 重新添加文本以触发动画
            setTimeout(() => {
                contentElement.textContent = originalText;
            }, 100);
        });
    </script>
</body>
</html>