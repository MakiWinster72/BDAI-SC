# 备份与恢复

## 通过后台管理界面

详见[后台管理 - 备份与恢复](./06-admin#backup-restore)。

## 通过 MySQL 命令行备份与恢复

### 备份

```bash
# 备份整个数据库
mysqldump -u bdai_sc -p bdai_sc > backup_$(date +%Y%m%d_%H%M%S).sql

# 输入密码后即可完成备份
```

### 恢复

```bash
# 确保数据库已存在
mysql -u bdai_sc -p bdai_sc < backup_file.sql

# 输入密码后即可完成恢复
```

> 注意：手动恢复会直接覆盖目标数据库中的数据，请先确认备份文件正确。
