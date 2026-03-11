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

| Field | Type | Null | Key | Default | Extra |
| --- | --- | --- | --- | --- | --- |
| id | bigint(20) | NO | PRI | NULL | auto_increment |
| award_date | date | YES |  | NULL |  |
| created_at | datetime(6) | NO |  | NULL |  |
| description | text | YES |  | NULL |  |
| end_date | date | YES |  | NULL |  |
| image_url | varchar(255) | YES |  | NULL |  |
| name | varchar(120) | NO |  | NULL |  |
| start_date | date | YES |  | NULL |  |
| thoughts | text | YES |  | NULL |  |
| author_id | bigint(20) | NO | MUL | NULL |  |

### post_media

| Field | Type | Null | Key | Default | Extra |
| --- | --- | --- | --- | --- | --- |
| id | bigint(20) | NO | PRI | NULL | auto_increment |
| media_type | varchar(16) | NO |  | NULL |  |
| original_name | varchar(255) | YES |  | NULL |  |
| url | varchar(255) | NO |  | NULL |  |
| post_id | bigint(20) | NO | MUL | NULL |  |

### posts

| Field | Type | Null | Key | Default | Extra |
| --- | --- | --- | --- | --- | --- |
| id | bigint(20) | NO | PRI | NULL | auto_increment |
| is_achievement | bit(1) | NO |  | NULL |  |
| content | text | NO |  | NULL |  |
| created_at | datetime(6) | NO |  | NULL |  |
| is_good_news | bit(1) | NO |  | NULL |  |
| is_private | bit(1) | NO |  | NULL |  |
| author_id | bigint(20) | NO | MUL | NULL |  |

### student_profiles

| Field | Type | Null | Key | Default | Extra |
| --- | --- | --- | --- | --- | --- |
| id | bigint(20) | NO | PRI | NULL | auto_increment |
| activist_date | date | YES |  | NULL |  |
| address | varchar(255) | YES |  | NULL |  |
| application_date | date | YES |  | NULL |  |
| class_major | varchar(64) | YES |  | NULL |  |
| class_name | varchar(64) | YES |  | NULL |  |
| class_no | varchar(16) | YES |  | NULL |  |
| class_year | int(11) | YES |  | NULL |  |
| college | varchar(64) | YES |  | NULL |  |
| emergency_phone | varchar(32) | YES |  | NULL |  |
| emergency_relation | varchar(32) | YES |  | NULL |  |
| full_name | varchar(64) | YES |  | NULL |  |
| id_no | varchar(32) | YES |  | NULL |  |
| league_no | varchar(32) | YES |  | NULL |  |
| native_place | varchar(64) | YES |  | NULL |  |
| not_developed | bit(1) | YES |  | NULL |  |
| party_applied | bit(1) | YES |  | NULL |  |
| phone | varchar(32) | YES |  | NULL |  |
| student_no | varchar(32) | YES |  | NULL |  |
| user_id | bigint(20) | NO | UNI | NULL |  |

### users

| Field | Type | Null | Key | Default | Extra |
| --- | --- | --- | --- | --- | --- |
| id | bigint(20) | NO | PRI | NULL | auto_increment |
| created_at | datetime(6) | NO |  | NULL |  |
| display_name | varchar(64) | NO |  | NULL |  |
| password_hash | varchar(100) | NO |  | NULL |  |
| username | varchar(32) | NO | UNI | NULL |  |
| class_name | varchar(64) | YES |  | NULL |  |
| college | varchar(64) | YES |  | NULL |  |
| role | enum('ADMIN','STUDENT','TEACHER') | NO |  | NULL |  |
| student_no | varchar(32) | YES |  | NULL |  |

