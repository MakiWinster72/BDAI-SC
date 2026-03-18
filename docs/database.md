# Database

数据库：`gcsc`

## Tables

```text
achievement_certificates
achievement_contests
achievement_journals
achievement_papers
achievement_patents
achievement_researches
achievement_works
contacts
education_experiences
post_media
posts
student_profiles
users
```

## Table Schemas

### achievement_certificates

| Field            | Type         | Null | Key | Default | Extra          | 备注       |
| ---------------- | ------------ | ---- | --- | ------- | -------------- | ---------- |
| id               | bigint(20)   | NO   | PRI | NULL    | auto_increment | 主键       |
| certificate_name | varchar(255) | NO   |     | NULL    |                | 证书名称   |
| certificate_type | varchar(64)  | YES  |     | NULL    |                | 证书类型   |
| created_at       | datetime(6)  | NO   |     | NULL    |                | 创建时间   |
| image_url        | varchar(255) | YES  |     | NULL    |                | 图片地址   |
| obtain_date      | date         | YES  |     | NULL    |                | 获得日期   |
| student_name     | varchar(64)  | YES  |     | NULL    |                | 学生姓名   |
| student_no       | varchar(32)  | YES  |     | NULL    |                | 学号       |
| author_id        | bigint(20)   | NO   | MUL | NULL    |                | 作者用户ID |

### achievement_contests

| Field            | Type         | Null | Key | Default | Extra          | 备注         |
| ---------------- | ------------ | ---- | --- | ------- | -------------- | ------------ |
| id               | bigint(20)   | NO   | PRI | NULL    | auto_increment | 主键         |
| award_category   | varchar(64)  | YES  |     | NULL    |                | 奖项类别     |
| award_count      | varchar(32)  | YES  |     | NULL    |                | 获奖人数/数量 |
| award_date       | date         | YES  |     | NULL    |                | 获奖日期     |
| award_level      | varchar(64)  | YES  |     | NULL    |                | 奖项等级     |
| contest_category | varchar(64)  | YES  |     | NULL    |                | 竞赛类别     |
| contest_name     | varchar(255) | NO   |     | NULL    |                | 竞赛名称     |
| contest_type     | varchar(64)  | YES  |     | NULL    |                | 竞赛类型     |
| created_at       | datetime(6)  | NO   |     | NULL    |                | 创建时间     |
| image_url        | varchar(255) | YES  |     | NULL    |                | 图片地址     |
| instructors      | text         | YES  |     | NULL    |                | 指导教师     |
| organizer        | varchar(255) | YES  |     | NULL    |                | 主办方       |
| remark           | varchar(64)  | YES  |     | NULL    |                | 备注         |
| student_name     | varchar(64)  | YES  |     | NULL    |                | 学生姓名     |
| student_no       | varchar(32)  | YES  |     | NULL    |                | 学号         |
| team_members     | text         | YES  |     | NULL    |                | 团队成员     |
| author_id        | bigint(20)   | NO   | MUL | NULL    |                | 作者用户ID   |

### achievement_journals

| Field            | Type         | Null | Key | Default | Extra          | 备注       |
| ---------------- | ------------ | ---- | --- | ------- | -------------- | ---------- |
| id               | bigint(20)   | NO   | PRI | NULL    | auto_increment | 主键       |
| created_at       | datetime(6)  | NO   |     | NULL    |                | 创建时间   |
| image_url        | varchar(255) | YES  |     | NULL    |                | 图片地址   |
| publication_name | varchar(255) | YES  |     | NULL    |                | 刊物名称   |
| publish_date     | date         | YES  |     | NULL    |                | 发表日期   |
| student_name     | varchar(64)  | YES  |     | NULL    |                | 学生姓名   |
| student_no       | varchar(32)  | YES  |     | NULL    |                | 学号       |
| work_title       | varchar(255) | NO   |     | NULL    |                | 作品题目   |
| author_id        | bigint(20)   | NO   | MUL | NULL    |                | 作者用户ID |

### achievement_papers

| Field        | Type         | Null | Key | Default | Extra          | 备注         |
| ------------ | ------------ | ---- | --- | ------- | -------------- | ------------ |
| id           | bigint(20)   | NO   | PRI | NULL    | auto_increment | 主键         |
| author_order | varchar(64)  | YES  |     | NULL    |                | 作者排序     |
| created_at   | datetime(6)  | NO   |     | NULL    |                | 创建时间     |
| image_url    | varchar(255) | YES  |     | NULL    |                | 图片地址     |
| indexed      | varchar(128) | YES  |     | NULL    |                | 收录情况     |
| journal_name | varchar(255) | YES  |     | NULL    |                | 期刊名称     |
| paper_title  | varchar(255) | NO   |     | NULL    |                | 论文题目     |
| publish_date | date         | YES  |     | NULL    |                | 发表日期     |
| student_name | varchar(64)  | YES  |     | NULL    |                | 学生姓名     |
| student_no   | varchar(32)  | YES  |     | NULL    |                | 学号         |
| author_id    | bigint(20)   | NO   | MUL | NULL    |                | 作者用户ID   |

### achievement_patents

| Field          | Type         | Null | Key | Default | Extra          | 备注       |
| -------------- | ------------ | ---- | --- | ------- | -------------- | ---------- |
| id             | bigint(20)   | NO   | PRI | NULL    | auto_increment | 主键       |
| created_at     | datetime(6)  | NO   |     | NULL    |                | 创建时间   |
| first_inventor | varchar(16)  | YES  |     | NULL    |                | 第一发明人 |
| grant_date     | date         | YES  |     | NULL    |                | 授权日期   |
| grant_no       | varchar(64)  | YES  |     | NULL    |                | 授权号     |
| image_url      | varchar(255) | YES  |     | NULL    |                | 图片地址   |
| patent_name    | varchar(255) | NO   |     | NULL    |                | 专利名称   |
| patent_type    | varchar(64)  | YES  |     | NULL    |                | 专利类型   |
| student_name   | varchar(64)  | YES  |     | NULL    |                | 学生姓名   |
| student_no     | varchar(32)  | YES  |     | NULL    |                | 学号       |
| author_id      | bigint(20)   | NO   | MUL | NULL    |                | 作者用户ID |

### achievement_researches

| Field          | Type         | Null | Key | Default | Extra          | 备注         |
| -------------- | ------------ | ---- | --- | ------- | -------------- | ------------ |
| id             | bigint(20)   | NO   | PRI | NULL    | auto_increment | 主键         |
| created_at     | datetime(6)  | NO   |     | NULL    |                | 创建时间     |
| image_url      | varchar(255) | YES  |     | NULL    |                | 图片地址     |
| project_leader | varchar(64)  | YES  |     | NULL    |                | 项目负责人   |
| project_name   | varchar(255) | NO   |     | NULL    |                | 项目名称     |
| student_name   | varchar(64)  | YES  |     | NULL    |                | 学生姓名     |
| student_no     | varchar(32)  | YES  |     | NULL    |                | 学号         |
| teacher_no     | varchar(64)  | YES  |     | NULL    |                | 教师工号     |
| author_id      | bigint(20)   | NO   | MUL | NULL    |                | 作者用户ID   |

### achievement_works

| Field            | Type         | Null | Key | Default | Extra          | 备注         |
| ---------------- | ------------ | ---- | --- | ------- | -------------- | ------------ |
| id               | bigint(20)   | NO   | PRI | NULL    | auto_increment | 主键         |
| created_at       | datetime(6)  | NO   |     | NULL    |                | 创建时间     |
| image_url        | varchar(255) | YES  |     | NULL    |                | 图片地址     |
| impact_scope     | varchar(64)  | YES  |     | NULL    |                | 影响范围     |
| note             | text         | YES  |     | NULL    |                | 备注说明     |
| organizer        | varchar(255) | YES  |     | NULL    |                | 主办方       |
| publish_date     | date         | YES  |     | NULL    |                | 发布日期     |
| publish_occasion | varchar(255) | YES  |     | NULL    |                | 发布场合     |
| student_name     | varchar(64)  | YES  |     | NULL    |                | 学生姓名     |
| student_no       | varchar(32)  | YES  |     | NULL    |                | 学号         |
| work_category    | varchar(64)  | YES  |     | NULL    |                | 作品类别     |
| work_name        | varchar(255) | NO   |     | NULL    |                | 作品名称     |
| work_type        | varchar(64)  | YES  |     | NULL    |                | 作品类型     |
| author_id        | bigint(20)   | NO   | MUL | NULL    |                | 作者用户ID   |

### contacts

| Field      | Type                         | Null | Key | Default | Extra          | 备注         |
| ---------- | ---------------------------- | ---- | --- | ------- | -------------- | ------------ |
| id         | bigint(20)                   | NO   | PRI | NULL    | auto_increment | 主键         |
| is_active  | bit(1)                       | NO   |     | NULL    |                | 是否启用     |
| created_at | datetime(6)                  | NO   |     | NULL    |                | 创建时间     |
| duty       | varchar(64)                  | YES  |     | NULL    |                | 职务         |
| entry_type | enum('DEPARTMENT','TEACHER') | NO   |     | NULL    |                | 条目类型     |
| name       | varchar(64)                  | NO   |     | NULL    |                | 名称         |
| office     | varchar(64)                  | YES  |     | NULL    |                | 办公室       |
| phone      | varchar(32)                  | YES  |     | NULL    |                | 电话         |
| photo_url  | varchar(255)                 | YES  |     | NULL    |                | 照片地址     |
| sort_order | int(11)                      | NO   |     | NULL    |                | 排序         |
| updated_at | datetime(6)                  | NO   |     | NULL    |                | 更新时间     |

### education_experiences

| Field           | Type         | Null | Key | Default | Extra          | 备注         |
| --------------- | ------------ | ---- | --- | ------- | -------------- | ------------ |
| id              | bigint(20)   | NO   | PRI | NULL    | auto_increment | 主键         |
| education_level | varchar(64)  | YES  |     | NULL    |                | 教育层次     |
| end_date        | date         | YES  |     | NULL    |                | 结束日期     |
| is_current      | bit(1)       | YES  |     | NULL    |                | 是否当前经历 |
| school_name     | varchar(128) | YES  |     | NULL    |                | 学校名称     |
| start_date      | date         | YES  |     | NULL    |                | 开始日期     |
| witness         | varchar(64)  | YES  |     | NULL    |                | 证明人       |
| profile_id      | bigint(20)   | NO   | MUL | NULL    |                | 关联档案ID   |

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
| role          | enum('ADMIN','STUDENT','TEACHER') | NO   |     | NULL    |                | 角色     |
| student_no    | varchar(32)                       | YES  |     | NULL    |                | 学号     |
| avatar_url    | varchar(255)                      | YES  |     | NULL    |                | 头像地址 |
