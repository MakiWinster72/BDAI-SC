# Database

数据库：`gcsc`

## Tables

```
achievements
post_media
posts
student_profiles
users
```

## Table Schemas

### achievements

| Field       | Type         | Null | Key | Default | Extra          | 备注       |
| ----------- | ------------ | ---- | --- | ------- | -------------- | ---------- |
| id          | bigint(20)   | NO   | PRI | NULL    | auto_increment | 主键       |
| award_date  | date         | YES  |     | NULL    |                | 获奖日期   |
| created_at  | datetime(6)  | NO   |     | NULL    |                | 创建时间   |
| description | text         | YES  |     | NULL    |                | 描述       |
| end_date    | date         | YES  |     | NULL    |                | 结束日期   |
| image_url   | varchar(255) | YES  |     | NULL    |                | 图片地址   |
| name        | varchar(120) | NO   |     | NULL    |                | 成就名称   |
| start_date  | date         | YES  |     | NULL    |                | 开始日期   |
| thoughts    | text         | YES  |     | NULL    |                | 心得感想   |
| author_id   | bigint(20)   | NO   | MUL | NULL    |                | 作者用户ID |

### post_media

| Field         | Type         | Null | Key | Default | Extra          | 备注       |
| ------------- | ------------ | ---- | --- | ------- | -------------- | ---------- |
| id            | bigint(20)   | NO   | PRI | NULL    | auto_increment | 主键       |
| media_type    | varchar(16)  | NO   |     | NULL    |                | 媒体类型   |
| original_name | varchar(255) | YES  |     | NULL    |                | 原始文件名 |
| url           | varchar(255) | NO   |     | NULL    |                | 媒体URL    |
| post_id       | bigint(20)   | NO   | MUL | NULL    |                | 关联帖子ID |

### posts

| Field          | Type        | Null | Key | Default | Extra          | 备注       |
| -------------- | ----------- | ---- | --- | ------- | -------------- | ---------- |
| id             | bigint(20)  | NO   | PRI | NULL    | auto_increment | 主键       |
| is_achievement | bit(1)      | NO   |     | NULL    |                | 是否成就   |
| content        | text        | NO   |     | NULL    |                | 内容       |
| created_at     | datetime(6) | NO   |     | NULL    |                | 创建时间   |
| is_good_news   | bit(1)      | NO   |     | NULL    |                | 是否喜报   |
| is_private     | bit(1)      | NO   |     | NULL    |                | 是否私密   |
| author_id      | bigint(20)  | NO   | MUL | NULL    |                | 作者用户ID |

### student_profiles

| Field                         | Type         | Null | Key | Default | Extra          | 备注                     |
| ----------------------------- | ------------ | ---- | --- | ------- | -------------- | ------------------------ |
| id                            | bigint(20)   | NO   | PRI | NULL    | auto_increment | 主键                     |
| activist_date                 | date         | YES  |     | NULL    |                | 积极分子日期             |
| address                       | varchar(255) | YES  |     | NULL    |                | 现住址                   |
| application_date              | date         | YES  |     | NULL    |                | 入党申请日期             |
| class_major                   | varchar(64)  | YES  |     | NULL    |                | 专业                     |
| class_name                    | varchar(64)  | YES  |     | NULL    |                | 班级名称                 |
| class_no                      | varchar(16)  | YES  |     | NULL    |                | 班级编号                 |
| class_year                    | int(11)      | YES  |     | NULL    |                | 年级                     |
| college                       | varchar(64)  | YES  |     | NULL    |                | 学院                     |
| emergency_phone               | varchar(32)  | YES  |     | NULL    |                | 紧急联系电话             |
| emergency_relation            | varchar(32)  | YES  |     | NULL    |                | 紧急联系人关系           |
| full_name                     | varchar(64)  | YES  |     | NULL    |                | 姓名                     |
| id_no                         | varchar(32)  | YES  |     | NULL    |                | 身份证号                 |
| league_no                     | varchar(32)  | YES  |     | NULL    |                | 团员编号                 |
| native_place                  | varchar(64)  | YES  |     | NULL    |                | 籍贯                     |
| not_developed                 | bit(1)       | YES  |     | NULL    |                | 是否未发展党员           |
| party_applied                 | bit(1)       | YES  |     | NULL    |                | 是否已申请入党           |
| phone                         | varchar(32)  | YES  |     | NULL    |                | 联系电话                 |
| student_no                    | varchar(32)  | YES  |     | NULL    |                | 学号                     |
| user_id                       | bigint(20)   | NO   | UNI | NULL    |                | 关联用户ID               |
| is_hk_mo_tw                   | bit(1)       | YES  |     | NULL    |                | 港澳台                   |
| is_special                    | bit(1)       | YES  |     | NULL    |                | 特殊学生                 |
| activist_developing           | bit(1)       | YES  |     | NULL    |                | 积极分子培养中           |
| class_teacher                 | varchar(64)  | YES  |     | NULL    |                | 班主任                   |
| counselor                     | varchar(64)  | YES  |     | NULL    |                | 辅导员                   |
| development_target_date       | date         | YES  |     | NULL    |                | 发展对象日期             |
| development_target_developing | bit(1)       | YES  |     | NULL    |                | 发展对象培养中           |
| dorm_building                 | varchar(64)  | YES  |     | NULL    |                | 宿舍楼                   |
| dorm_campus                   | varchar(64)  | YES  |     | NULL    |                | 宿舍校区                 |
| dorm_room                     | varchar(64)  | YES  |     | NULL    |                | 宿舍房间                 |
| enrollment_date               | date         | YES  |     | NULL    |                | 入学日期                 |
| ethnicity                     | varchar(32)  | YES  |     | NULL    |                | 民族                     |
| father_name                   | varchar(64)  | YES  |     | NULL    |                | 父亲姓名                 |
| father_phone                  | varchar(32)  | YES  |     | NULL    |                | 父亲联系电话             |
| father_title                  | varchar(64)  | YES  |     | NULL    |                | 父亲职务                 |
| father_work_unit              | varchar(128) | YES  |     | NULL    |                | 父亲工作单位             |
| full_member_date              | date         | YES  |     | NULL    |                | 转正日期                 |
| full_member_developing        | bit(1)       | YES  |     | NULL    |                | 预备党员转正培养中       |
| league_application_date       | date         | YES  |     | NULL    |                | 入团申请日期             |
| league_developing             | bit(1)       | YES  |     | NULL    |                | 团员培养中               |
| league_join_date              | date         | YES  |     | NULL    |                | 入团日期                 |
| league_joined                 | bit(1)       | YES  |     | NULL    |                | 是否已入团               |
| mother_name                   | varchar(64)  | YES  |     | NULL    |                | 母亲姓名                 |
| mother_phone                  | varchar(32)  | YES  |     | NULL    |                | 母亲联系电话             |
| mother_title                  | varchar(64)  | YES  |     | NULL    |                | 母亲职务                 |
| mother_work_unit              | varchar(128) | YES  |     | NULL    |                | 母亲工作单位             |
| off_campus_address            | varchar(255) | YES  |     | NULL    |                | 校外居住地址             |
| off_campus_living             | bit(1)       | YES  |     | NULL    |                | 是否校外居住             |
| party_training_date           | date         | YES  |     | NULL    |                | 党校培训日期             |
| party_training_pending        | bit(1)       | YES  |     | NULL    |                | 党校培训待定             |
| political_status              | varchar(32)  | YES  |     | NULL    |                | 政治面貌                 |
| probationary_developing       | bit(1)       | YES  |     | NULL    |                | 预备党员培养中           |
| probationary_member_date      | date         | YES  |     | NULL    |                | 预备党员日期             |
| student_category              | varchar(32)  | YES  |     | NULL    |                | 学生类别                 |

### users

| Field         | Type                              | Null | Key | Default | Extra          | 备注     |
| ------------- | --------------------------------- | ---- | --- | ------- | -------------- | -------- |
| id            | bigint(20)                        | NO   | PRI | NULL    | auto_increment | 主键     |
| created_at    | datetime(6)                       | NO   |     | NULL    |                | 创建时间 |
| display_name  | varchar(64)                       | NO   |     | NULL    |                | 显示名   |
| password_hash | varchar(100)                      | NO   |     | NULL    |                | 密码哈希 |
| username      | varchar(32)                       | NO   | UNI | NULL    |                | 登录名   |
| class_name    | varchar(64)                       | YES  |     | NULL    |                | 班级名称 |
| college       | varchar(64)                       | YES  |     | NULL    |                | 学院     |
| avatar_url    | varchar(255)                      | YES  |     | NULL    |                | 头像地址 |
| role          | enum('ADMIN','STUDENT','TEACHER') | NO   |     | NULL    |                | 角色     |
| student_no    | varchar(32)                       | YES  |     | NULL    |                | 学号     |
