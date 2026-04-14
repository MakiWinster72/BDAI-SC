# 成就系统文件存储说明

## 存储位置

文件存储在 `backend/uploads/` 目录下（可通过 `application.yml` 中的 `app.upload-dir` 配置）。

```
backend/uploads/
├── 2026/
│   ├── 03/
│   │   ├── xxx.jpg
│   │   ├── yyy.pdf
│   │   └── zzz.mp4
│   └── 04/
│       └── ...
```

## 访问方式

通过后端服务访问：
```
http://localhost:8080/uploads/2026/03/xxx.jpg
```

## 数据库存储

### 成就图片

| 字段 | 类型 | 说明 |
|------|------|------|
| `image_url` | varchar(255) | 第一张图片路径 |
| `_image_urls` | text | 多张图片路径（JSON 数组） |

### 附件

| 字段 | 类型 | 说明 |
|------|------|------|
| `_attachments` | text | 附件列表（JSON 数组） |

### 附件 JSON 格式

```json
[
  {
    "url": "/uploads/2026/03/xxx.pdf",
    "name": "获奖证书.pdf",
    "mediaType": "application/pdf"
  },
  {
    "url": "/uploads/2026/03/yyy.jpg",
    "name": "现场照片.jpg",
    "mediaType": "image/jpeg"
  }
]
```

## 相关配置

`backend/src/main/resources/application.yml`：
```yaml
app:
  upload-dir: ./uploads

spring:
  servlet:
    multipart:
      max-file-size: 200MB      # 单文件最大 200MB
      max-request-size: 200MB   # 请求最大 200MB
```

## 文件类型支持

### 成就图片
- jpeg / jpg
- png
- heif

### 附件
- 文档：docx / doc / pdf / xls / xlsx / pptx / ppt
- 图片：jpeg / jpg / png / heif
- 视频：mp4 / mov
- 压缩包：zip / rar / 7z

## 注意事项

1. **文件不上传到 Git 仓库** - `uploads/` 目录通常在 `.gitignore` 中
2. **生产环境需配置独立存储** - 如 S3、七牛云等对象存储
3. **定期清理** - 建议添加清理机制删除未引用的文件
