#!/usr/bin/env python3
"""
BDAI_SC 测试数据生成器 - 纯 INSERT 追加模式（不删除现有数据）
用法: python generate_test_data.py --users 20 --achievements 50 -o test_data.sql
"""

import argparse, random, sys
from datetime import datetime, timedelta
from pathlib import Path

try:
    from faker import Faker
except ImportError:
    print("错误: pip install faker")
    sys.exit(1)

random.seed(42)
faker = Faker(['zh_CN'])

# -------- 词汇表 --------
FIXED_COLLEGE = "大数据与人工智能学院"

GRAD_MAJORS = [
    "管理科学与工程", "技术经济及管理", "智能科学与技术",
    "计算机技术", "图书情报",
]

MAJORS = [
    "计算机科学与技术", "计算机科学与技术（实验区）",
    "计算机科学与技术(中外联合培养项目班)",
    "2025计算机科学与技术（中外联合培养项目班未赴国外学习）",
    "软件工程", "人工智能", "电子商务", "电子商务（大数据决策分析）",
    "大数据管理与应用", "大数据管理与应用（佛山校区全学段）",
    "大数据管理与应用（数字治理）",
]

FOSHAN_BUILDINGS = [f"{n}号楼" for n in range(1, 22)] + ["有为9栋", "有为21栋"]
GUANGZHOU_BUILDINGS = [f"{n}号楼" for n in range(17, 33)] + ["凌云楼", "揽月楼", "丽枫酒店"]
DORM_CAMPUSES = ["佛山校区", "广州校区"]

ETHNICITIES = ["汉族", "壮族", "回族", "满族", "维吾尔族", "苗族", "土家族"]
POLITICAL_STATUSES = ["群众", "共青团员", "中共预备党员", "中共党员"]
RELATIONS = ["父亲", "母亲", "哥哥", "姐姐", "叔叔", "阿姨"]
PARENT_TITLES = ["工程师", "教师", "医生", "商人"]
DEPARTMENTS = ["学生会", "团委", "学生会组织部", "学生会宣传部", "班级委员会", "团支部"]
POSITIONS = ["班长", "团支书", "学习委员", "学生会主席", "部门部长", "干事"]
PAPER_INDEXED = ["SCI", "EI", "中文核心", "核心期刊", "普通期刊"]
PATENT_TYPES = ["发明专利", "实用新型专利", "外观设计专利"]
CERTIFICATE_TYPES = ["专业技能证书", "语言能力证书", "计算机等级证书"]
WORK_TYPES = ["文学作品", "艺术作品", "设计作品", "软件作品"]
WORK_CATEGORIES = ["文学创作", "艺术创作", "设计创作", "软件开发"]
IMPACT_SCOPES = ["国际级", "国家级", "省级", "市级", "校级"]
DOUBLE_CATEGORIES = ["志愿服务", "社会实践", "创新创业"]
TRAINING_TYPES = ["专业技能", "创新能力", "职业素养"]
FINAL_STATUSES = ["合格", "优秀", "良好"]
CONTEST_CATEGORIES = ["学科竞赛", "创新创业", "综合竞赛", "技能竞赛"]
AWARD_CATEGORIES = ["个人赛", "团体赛"]
AWARD_LEVELS = ["特等奖", "一等奖", "二等奖", "三等奖", "优秀奖"]
CONTEST_TYPES = ["国家级", "省部级", "市级", "校级"]
AUTHOR_ORDERS = ["第一作者", "第二作者", "第三作者", "通讯作者"]


# -------- 辅助函数 --------
def rand_date(y1=2000, y2=2006):
    d = datetime(random.randint(y1, y2), random.randint(1, 12), random.randint(1, 28))
    return d.strftime("%Y-%m-%d")

def rand_datetime6(y1=2024, y2=2026):
    start = datetime(y1, 1, 1)
    end = datetime(y2, 12, 31, 23, 59, 59)
    delta = int((end - start).total_seconds())
    d = start + timedelta(seconds=random.randint(0, delta))
    micros = f"{random.randint(0, 999999):06d}"
    return d.strftime("%Y-%m-%d %H:%M:%S") + "." + micros

def q(s):
    if s is None:
        return "NULL"
    escaped = str(s).replace("\\", "\\\\").replace("'", "''")
    return "'" + escaped + "'"

def ins(table, cols, vals):
    vals_str = ','.join(str(v) if v is not None else 'NULL' for v in vals)
    return f"INSERT INTO {table} ({','.join(cols)}) VALUES ({vals_str});"

def rand_phone():
    return faker.phone_number()

def rand_id_card():
    area = random.choice(["110101", "310101", "440101", "440301", "510101", "320101", "330101", "420101", "610101"])
    b = rand_date(1998, 2005).replace("-", "")
    seq = f"{random.randint(0, 999):03d}"
    return area + b + seq + random.choice(list("0123456789X"))

def rand_native_place():
    provinces = ["广东", "浙江", "江苏", "安徽", "福建", "江西", "山东", "河南", "湖北", "湖南",
                  "四川", "重庆", "云南", "贵州", "陕西", "山西", "河北", "辽宁", "吉林", "黑龙江",
                  "内蒙古", "新疆", "西藏", "甘肃", "青海", "宁夏", "广西", "海南", "北京", "上海", "天津"]
    cities = ["广州", "深圳", "杭州", "宁波", "温州", "南京", "苏州", "无锡", "常州", "合肥",
              "福州", "厦门", "泉州", "南昌", "赣州", "济南", "青岛", "郑州", "洛阳", "武汉",
              "长沙", "成都", "绵阳", "昆明", "贵阳", "西安", "咸阳", "太原", "石家庄", "保定",
              "沈阳", "大连", "长春", "哈尔滨", "呼和浩特", "乌鲁木齐", "拉萨", "兰州", "西宁",
              "银川", "南宁", "桂林", "海口", "北京", "上海", "天津"]
    return random.choice(provinces) + random.choice(cities)

def rand_full_address():
    prov = random.choice(["广东省", "浙江省", "江苏省", "安徽省", "福建省", "江西省", "山东省",
                          "河南省", "湖北省", "湖南省", "四川省", "重庆", "云南省", "贵州省", "陕西省",
                          "山西省", "河北省", "辽宁省", "吉林省", "黑龙江省", "内蒙古自治区", "新疆维吾尔自治区",
                          "西藏自治区", "甘肃省", "青海省", "宁夏回族自治区", "广西壮族自治区", "海南省"])
    city = random.choice(["广州", "深圳", "杭州", "宁波", "南京", "苏州", "合肥", "福州", "厦门", "泉州",
                          "南昌", "济南", "青岛", "郑州", "武汉", "长沙", "成都", "昆明", "贵阳", "西安",
                          "太原", "石家庄", "沈阳", "大连", "长春", "哈尔滨", "呼和浩特", "乌鲁木齐",
                          "拉萨", "兰州", "西宁", "银川", "南宁", "桂林", "海口", "北京", "上海", "天津"])
    county = faker.city()[:-1] if random.random() > 0.3 else ""
    road = random.choice(["人民路", "建设路", "中山路", "解放路", "文化路", "新华路", "和平路",
                          "胜利路", "民主路", "光明路", "友谊路", "幸福路"])
    num = random.randint(1, 999)
    detail = random.choice(["栋", "号楼", "座"]) if random.random() > 0.5 else ""
    return f"{prov}{city}{county}{road}{num}{detail}"


# -------- users --------
USERS_COLS = ["display_name", "username", "password_hash", "role", "student_no",
              "class_name", "college", "avatar_url", "assigned_classes", "remark", "created_at"]

PROFILE_COLS = [
    "user_id", "full_name", "student_no", "class_year", "class_major", "class_no",
    "class_name", "college", "enrollment_date", "student_category", "ethnicity",
    "political_status", "dorm_campus", "dorm_building", "dorm_room", "off_campus_living",
    "off_campus_address", "class_teacher", "counselor", "phone", "backup_contact",
    "address", "id_type", "id_no", "birth_date", "native_place", "league_no",
    "league_application_date", "league_join_date", "league_joined", "league_developing",
    "party_applied", "not_developed", "application_date", "activist_date",
    "activist_developing", "party_training_date", "party_training_pending",
    "development_target_date", "development_target_developing",
    "probationary_member_date", "probationary_developing",
    "full_member_date", "full_member_developing", "emergency_phone",
    "emergency_relation", "is_hk", "is_mo", "is_tw", "is_special",
    "special_student_type", "special_student_remark",
    "father_name", "father_phone", "father_work_unit", "father_title",
    "mother_name", "mother_phone", "mother_work_unit", "mother_title",
]

EDU_COLS = ["profile_id", "start_date", "end_date", "school_name", "education_level", "witness", "is_current"]

CADRE_COLS = ["profile_id", "start_date", "end_date", "department", "position", "description", "is_current"]

LOGIN_COLS = ["user_id", "ip_address", "user_agent", "device_name", "login_time"]


def gen_users(n):
    """
    生成用户及关联数据，每个用户一个 block：
    INSERT user → SET @u_N → INSERT profile → SET @p_N → education → cadre
    返回 (sql_lines, u_vars, p_vars, u_reviewer_vars)
    u_vars:      所有用户的 @u_N 列表
    p_vars:      学生用户的 @p_N 列表 (用于 education/cadre)
    u_reviewer_vars: 管理员/教师的 @u_N 列表 (用于审批)
    """
    sql = []
    u_vars = []           # [{"var": "@u1", "name": ..., "student_no": ...}, ...]
    p_vars = []           # ["@p1", "@p2", ...]
    u_reviewer_vars = []  # [{"var": "@u1", "name": ...}, ...]  admin + teacher

    idx = 0
    for i in range(1, n + 1):
        idx += 1
        u_var = f"@u{idx}"

        if i <= 2:
            role = "ADMIN"
        elif i == 3:
            role = "TEACHER"
        elif i == 4:
            role = "CADRE"
        else:
            role = "STUDENT"

        uname = f"test_{role.lower()}_{i:03d}_{random.randint(1000, 9999)}"
        dname = faker.name()
        pwd = "$2a$10$dummy_hash_not_real_bcrypt_for_testing"

        is_admin = (role == "ADMIN")
        is_teacher = (role == "TEACHER")
        is_student_like = role in ("STUDENT", "CADRE")

        avatar_url = None if is_admin else f"https://i.pravatar.cc/150?img={random.randint(1, 99)}"

        assigned_classes = None
        if is_teacher:
            assigned_classes = ",".join(random.sample(MAJORS, random.randint(1, 3)))

        if is_student_like:
            major = random.choice(MAJORS)
            class_no = f"{random.randint(1, 10):02d}"
            short = major[:4]
            cname = f"{short}{class_no}班"
            sno = f"2021{random.randint(1000, 9999)}"
        else:
            cname = None
            sno = None

        # INSERT user
        sql.append("")
        sql.append(f"-- {'='*20} 用户 {idx}: {role} {'='*20}")
        sql.append(ins("users", USERS_COLS,
            [q(dname), q(uname), q(pwd), q(role),
             q(sno), q(cname), q(FIXED_COLLEGE), q(avatar_url),
             q(assigned_classes), None, q(rand_datetime6())]))
        sql.append(f"SET {u_var} = LAST_INSERT_ID();")

        # 记录用户变量
        info = {"var": u_var, "name": dname, "student_no": sno, "role": role}
        u_vars.append(info)
        if is_admin or is_teacher:
            u_reviewer_vars.append(info)

        # 只为 STUDENT/CADRE 生成 profile
        if not is_student_like:
            continue

        eth = random.choice(ETHNICITIES)
        pol = random.choice(POLITICAL_STATUSES)
        ph = rand_phone()
        er = random.choice(RELATIONS)
        fn = faker.name_male()
        mn = faker.name_female()
        bd = rand_date(1998, 2005)
        native = rand_native_place()
        addr = rand_full_address()

        student_category = random.choice(["本科生", "研究生"])
        if student_category == "研究生":
            major = random.choice(GRAD_MAJORS)
        else:
            major = random.choice(MAJORS)
        class_no = f"{random.randint(1, 10):02d}"
        short = major[:4]
        cname = f"{short}{class_no}班"

        ey = random.randint(2020, 2026)
        class_year = ey
        enrollment_date = f"{ey}-09-01"

        campus = random.choice(DORM_CAMPUSES)
        building = random.choice(FOSHAN_BUILDINGS if campus == "佛山校区" else GUANGZHOU_BUILDINGS)
        room = f"{random.randint(1, 6)}{random.randint(1, 20):02d}"

        off_campus = 1 if random.random() < 0.05 else 0
        off_campus_addr = q(rand_full_address()) if off_campus else "NULL"

        league_joined_val = 1 if random.random() < 0.8 else 0
        league_app_date = q(rand_date(2016, 2020)) if league_joined_val else "NULL"
        league_join_date = q(rand_date(2017, 2021)) if league_joined_val else "NULL"
        league_developing = 1 if not league_joined_val and random.random() < 0.3 else 0

        party_val = 1 if random.random() < 0.7 else 0
        not_dev_val = 1 if pol == "群众" and random.random() < 0.3 else 0
        app_date = q(rand_date(2021, 2024)) if party_val else "NULL"
        ad_date = q(rand_date(2022, 2024)) if party_val else "NULL"
        activist_dev = 1 if party_val and random.random() < 0.3 else 0
        ppt_date = q(rand_date(2022, 2024)) if party_val else "NULL"
        ppt_pending = 1 if party_val and random.random() < 0.2 else 0
        dd_date = q(rand_date(2023, 2025)) if party_val and random.random() > 0.4 else "NULL"
        dd_developing = 1 if party_val and dd_date == "NULL" and random.random() < 0.3 else 0
        pd_date = q(rand_date(2023, 2025)) if party_val and pol in ("中共预备党员", "中共党员") else "NULL"
        pd_developing = 1 if party_val and pd_date == "NULL" and random.random() < 0.3 else 0
        fd_date = q(rand_date(2023, 2025)) if pol == "中共党员" else "NULL"
        fd_developing = 1 if pol == "中共预备党员" and random.random() < 0.3 else 0

        is_hk = 1 if random.random() < 0.02 else 0
        is_mo = 1 if random.random() < 0.02 else 0
        is_tw = 1 if random.random() < 0.02 else 0
        is_special = 1 if random.random() < 0.05 else 0
        special_type = q(random.choice(["体育特长生", "艺术特长生", "退役大学生士兵", "少数民族骨干"])) if is_special else "NULL"
        special_remark = q(faker.sentence(8)) if is_special else "NULL"

        p_var = f"@p{len(p_vars) + 1}"
        p_vars.append({
            "var": p_var,
            "u_var": u_var,
            "name": dname,
            "student_no": sno,
        })

        sql.append(ins("student_profiles", PROFILE_COLS, [
            u_var,  # user_id 使用 MySQL 变量
            q(dname), q(sno), class_year, q(major), q(class_no),
            q(cname), q(FIXED_COLLEGE), q(enrollment_date), q(student_category), q(eth),
            q(pol), q(campus), q(building), q(room), off_campus,
            off_campus_addr, q(faker.name()), q(faker.name()), q(ph), q(rand_phone()),
            q(addr), q("居民身份证"), q(rand_id_card()), q(bd), q(native),
            q(f"LM{random.randint(100000, 999999)}"),
            league_app_date, league_join_date, league_joined_val, league_developing,
            party_val, not_dev_val, app_date, ad_date,
            activist_dev, ppt_date, ppt_pending,
            dd_date, dd_developing,
            pd_date, pd_developing,
            fd_date, fd_developing, q(ph),
            q(er), is_hk, is_mo, is_tw, is_special,
            special_type, special_remark,
            q(fn), q(rand_phone()), q(faker.company()), q(random.choice(PARENT_TITLES)),
            q(mn), q(rand_phone()), q(faker.company()), q(random.choice(PARENT_TITLES)),
        ]))
        sql.append(f"SET {p_var} = LAST_INSERT_ID();")

        # 教育经历
        for _ in range(random.randint(1, 2)):
            s = rand_date(2015, 2021)
            e = rand_date(2016, 2022)
            edu_level = random.choice(["小学", "初中", "高中", "中专"])
            school = random.choice([
                "北京市第一小学", "上海师范大学附属中学", "广东省实验中学", "江苏省天一中学",
                "浙江省宁波中学", "湖北省武汉中学", "四川省成都七中", "重庆市南开中学",
                "陕西省西安中学", "山东省实验中学", "河南省郑州中学", "湖南省长郡中学",
            ])
            sql.append(ins("education_experiences", EDU_COLS,
                [p_var, q(s), q(e), q(school), q(edu_level), q(faker.name()), 0]))

        # 学生干部经历
        for _ in range(random.randint(1, 3)):
            s = rand_date(2021, 2024)
            cur = random.random() > 0.3
            sql.append(ins("cadre_experiences", CADRE_COLS,
                [p_var, q(s),
                 q(rand_date(2022, 2025)) if not cur else "NULL",
                 q(random.choice(DEPARTMENTS)), q(random.choice(POSITIONS)),
                 q(faker.sentence(10)), 1 if cur else 0]))

    return sql, u_vars, p_vars, u_reviewer_vars


def gen_login(u_vars, n_per_user=8):
    sql = []
    for info in u_vars:
        for _ in range(random.randint(n_per_user // 2, n_per_user)):
            sql.append(ins("login_histories", LOGIN_COLS,
                [info["var"],
                 q(faker.ipv4()),
                 q(random.choice(["Mozilla/5.0 (Windows NT 10) AppleWebKit/537.36",
                                 "Mozilla/5.0 (Macintosh) AppleWebKit/537.36",
                                 "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36"])),
                 q(random.choice(["Chrome", "Safari", "Firefox", "Edge"])),
                 q(rand_datetime6())]))
    return sql


def gen_settings():
    now = q(rand_datetime6())
    settings = [
        ("profile_review_enabled", "true"),
        ("achievement_review_enabled", "true"),
        ("profile_review_auto_approve", "false"),
        ("achievement_review_auto_approve", "false"),
        ("max_image_upload_count", "9"),
        ("max_attachment_upload_count", "9"),
        ("max_image_size_mb", "10"),
        ("max_attachment_size_mb", "20"),
        ("allowed_image_extensions", '["jpg","jpeg","png","gif","webp"]'),
        ("allowed_attachment_extensions", '["pdf","doc","docx","xls","xlsx","ppt","pptx"]'),
    ]
    # INSERT IGNORE: 如果 setting_key 已存在则跳过
    return [f"INSERT IGNORE INTO system_settings (setting_key,setting_value,updated_at) VALUES ({q(k)},{q(v)},{now});" for k, v in settings]


# -------- achievements --------

def _pick_student(p_vars):
    """随机选取一个学生"""
    return random.choice(p_vars)

ACH_CERT_COLS = ["author_id", "student_no", "student_name",
                 "certificate_type", "certificate_name", "obtain_date",
                 "remark", "image_url", "_image_urls", "_attachments", "created_at"]

ACH_CONTEST_COLS = ["author_id", "student_no", "student_name",
                    "contest_name", "organizer", "contest_category", "award_category",
                    "award_level", "contest_type", "award_date", "award_count",
                    "team_members", "instructors", "remark",
                    "image_url", "_image_urls", "_attachments", "created_at"]

ACH_PAPER_COLS = ["author_id", "student_no", "student_name",
                  "paper_title", "journal_name", "publish_date", "author_order",
                  "indexed", "remark",
                  "image_url", "_image_urls", "_attachments", "created_at"]

ACH_PATENT_COLS = ["author_id", "student_no", "student_name",
                   "patent_name", "patent_type", "grant_no", "grant_date",
                   "first_inventor", "remark",
                   "image_url", "_image_urls", "_attachments", "created_at"]

ACH_RESEARCH_COLS = ["author_id", "student_no", "student_name",
                     "project_name", "teacher_no", "project_leader", "remark",
                     "image_url", "_image_urls", "_attachments", "created_at"]

ACH_WORKS_COLS = ["author_id", "student_no", "student_name",
                  "work_name", "work_category", "work_type", "publish_date",
                  "publish_occasion", "organizer", "impact_scope", "note",
                  "image_url", "_image_urls", "_attachments", "created_at"]

ACH_DOUBLE_COLS = ["author_id", "student_no", "student_name",
                   "project_category", "project_domain", "project_name",
                   "project_leader", "leader_student_no", "education_level",
                   "team_members", "instructors", "team_size",
                   "planned_level", "college", "final_level", "remark",
                   "image_url", "_image_urls", "_attachments", "created_at"]

ACH_IEER_COLS = ["author_id", "student_no", "student_name",
                 "college_name", "project_name", "project_type",
                 "project_leader", "instructor_name", "recommended_level",
                 "is_key_area", "final_status", "remark",
                 "image_url", "_image_urls", "_attachments", "created_at"]

ACH_JOURNAL_COLS = ["author_id", "student_no", "student_name",
                    "work_title", "publication_name", "publish_date", "remark",
                    "image_url", "_image_urls", "_attachments", "created_at"]


def gen_certificates(p_vars, count):
    sql = []
    for _ in range(count):
        u = _pick_student(p_vars)
        sql.append(ins("achievement_certificates", ACH_CERT_COLS, [
            u["u_var"], q(u["student_no"]), q(u["name"]),
            q(random.choice(CERTIFICATE_TYPES)),
            q(faker.sentence(4)[:50]),
            q(rand_date(2021, 2025)),
            q(faker.sentence(10)), "NULL", "NULL", "NULL", q(rand_datetime6()),
        ]))
    return sql

def gen_contests(p_vars, count):
    sql = []
    for _ in range(count):
        u = _pick_student(p_vars)
        sql.append(ins("achievement_contests", ACH_CONTEST_COLS, [
            u["u_var"], q(u["student_no"]), q(u["name"]),
            q(f"第{random.randint(1, 20)}届{faker.sentence(3)[:30]}大赛"),
            q(random.choice(["教育部", "省教育厅", "中国计算机学会", "校团委"])),
            q(random.choice(CONTEST_CATEGORIES)),
            q(random.choice(AWARD_CATEGORIES)),
            q(random.choice(AWARD_LEVELS)),
            q(random.choice(CONTEST_TYPES)),
            q(rand_date(2021, 2025)),
            q(str(random.randint(1, 100))),
            q(faker.name() + "," + faker.name()) if random.random() > 0.5 else "NULL",
            q(faker.name()) if random.random() > 0.5 else "NULL",
            q(faker.sentence(8)),
            "NULL", "NULL", "NULL", q(rand_datetime6()),
        ]))
    return sql

def gen_papers(p_vars, count):
    sql = []
    for _ in range(count):
        u = _pick_student(p_vars)
        sql.append(ins("achievement_papers", ACH_PAPER_COLS, [
            u["u_var"], q(u["student_no"]), q(u["name"]),
            q(faker.sentence(6)[:50]),
            q(random.choice(["计算机学报", "软件学报", "自动化学报", "电子学报", "中国科学", "Nature", "Science"])),
            q(rand_date(2021, 2025)),
            q(random.choice(AUTHOR_ORDERS)),
            q(random.choice(PAPER_INDEXED)),
            q(faker.sentence(10)),
            "NULL", "NULL", "NULL", q(rand_datetime6()),
        ]))
    return sql

def gen_patents(p_vars, count):
    sql = []
    for _ in range(count):
        u = _pick_student(p_vars)
        sql.append(ins("achievement_patents", ACH_PATENT_COLS, [
            u["u_var"], q(u["student_no"]), q(u["name"]),
            q(faker.sentence(5)[:50]),
            q(random.choice(PATENT_TYPES)),
            q(f"CN{random.randint(1000000000, 9999999999)}"),
            q(rand_date(2021, 2025)),
            q(u["name"]),
            q(faker.sentence(10)),
            "NULL", "NULL", "NULL", q(rand_datetime6()),
        ]))
    return sql

def gen_researches(p_vars, count):
    sql = []
    for _ in range(count):
        u = _pick_student(p_vars)
        sql.append(ins("achievement_researches", ACH_RESEARCH_COLS, [
            u["u_var"], q(u["student_no"]), q(u["name"]),
            q(faker.sentence(5)[:50]),
            q(f"T{random.randint(1000, 9999)}"),
            q(faker.name()),
            q(faker.sentence(10)),
            "NULL", "NULL", "NULL", q(rand_datetime6()),
        ]))
    return sql

def gen_works(p_vars, count):
    sql = []
    for _ in range(count):
        u = _pick_student(p_vars)
        sql.append(ins("achievement_works", ACH_WORKS_COLS, [
            u["u_var"], q(u["student_no"]), q(u["name"]),
            q(faker.sentence(4)[:50]),
            q(random.choice(WORK_CATEGORIES)),
            q(random.choice(WORK_TYPES)),
            q(rand_date(2021, 2025)),
            q(random.choice(["校园文化艺术节", "省级文艺汇演", "全国大学生艺术展演", "网络平台发布"])),
            q(random.choice(["校团委", "省教育厅", "文化部", "中国文联"])),
            q(random.choice(IMPACT_SCOPES)),
            q(faker.sentence(10)),
            "NULL", "NULL", "NULL", q(rand_datetime6()),
        ]))
    return sql

def gen_double_hundreds(p_vars, count):
    sql = []
    for _ in range(count):
        u = _pick_student(p_vars)
        sql.append(ins("achievement_double_hundreds", ACH_DOUBLE_COLS, [
            u["u_var"], q(u["student_no"]), q(u["name"]),
            q(random.choice(DOUBLE_CATEGORIES)),
            q(random.choice(["人工智能", "大数据", "乡村振兴", "社区服务", "环保"])),
            q(faker.sentence(5)[:50]),
            q(u["name"]),
            q(u["student_no"]),
            q(random.choice(["本科生", "研究生"])),
            q(faker.name() + "," + faker.name()),
            q(faker.name()),
            q(str(random.randint(2, 10))),
            q(random.choice(["国家级", "省级", "校级"])),
            q(FIXED_COLLEGE),
            q(random.choice(FINAL_STATUSES)),
            q(faker.sentence(10)),
            "NULL", "NULL", "NULL", q(rand_datetime6()),
        ]))
    return sql

def gen_ieer_trainings(p_vars, count):
    sql = []
    for _ in range(count):
        u = _pick_student(p_vars)
        sql.append(ins("achievement_ieer_trainings", ACH_IEER_COLS, [
            u["u_var"], q(u["student_no"]), q(u["name"]),
            q(FIXED_COLLEGE),
            q(faker.sentence(5)[:50]),
            q(random.choice(TRAINING_TYPES)),
            q(u["name"]),
            q(faker.name()),
            q(random.choice(["优秀", "良好", "合格"])),
            q(random.choice(["是", "否"])),
            q(random.choice(FINAL_STATUSES)),
            q(faker.sentence(10)),
            "NULL", "NULL", "NULL", q(rand_datetime6()),
        ]))
    return sql

def gen_journals(p_vars, count):
    sql = []
    for _ in range(count):
        u = _pick_student(p_vars)
        sql.append(ins("achievement_journals", ACH_JOURNAL_COLS, [
            u["u_var"], q(u["student_no"]), q(u["name"]),
            q(faker.sentence(5)[:50]),
            q(random.choice(["人民日报", "光明日报", "中国青年报", "南方日报", "广州日报"])),
            q(rand_date(2021, 2025)),
            q(faker.sentence(10)),
            "NULL", "NULL", "NULL", q(rand_datetime6()),
        ]))
    return sql


# -------- review requests --------
ACH_REVIEW_COLS = ["requester_id", "reviewer_id", "category", "action", "status",
                   "record_id", "title", "summary", "payload_json", "payload_snapshot_json",
                   "changes_json", "rejection_reason", "supporting_documents_json",
                   "created_at", "updated_at"]

PROF_REVIEW_COLS = ["requester_id", "reviewer_id", "status", "title", "summary",
                    "payload_snapshot_json", "changes_json", "rejection_reason",
                    "supporting_documents_json", "created_at", "updated_at"]


def gen_achievement_review_requests(p_vars, u_reviewer_vars):
    """为部分学生生成成就审批记录"""
    sql = []
    for cat in ["certificate", "contest", "paper", "patent"]:
        for u in random.sample(p_vars, min(3, len(p_vars))):
            reviewer = random.choice(u_reviewer_vars)
            status = random.choice(["pending", "approved", "approved", "rejected"])
            now = q(rand_datetime6())
            sql.append(ins("achievement_review_requests", ACH_REVIEW_COLS, [
                u["u_var"],
                reviewer["var"] if status != "pending" else "NULL",
                q(cat), q("create"), q(status),
                "NULL",
                q(f"{u['name']}的{cat}申报"),
                q("测试审批摘要"),
                q("{}"), "NULL", "NULL",
                q("不符合要求") if status == "rejected" else "NULL",
                "NULL",
                now, now,
            ]))
    return sql


def gen_profile_review_requests(p_vars, u_reviewer_vars):
    """为部分学生生成档案审批记录"""
    sql = []
    for u in random.sample(p_vars, min(len(p_vars) // 3, len(p_vars))):
        reviewer = random.choice(u_reviewer_vars)
        status = random.choice(["pending", "approved", "approved", "rejected"])
        now = q(rand_datetime6())
        sql.append(ins("profile_review_requests", PROF_REVIEW_COLS, [
            u["u_var"],
            reviewer["var"] if status != "pending" else "NULL",
            q(status),
            q(f"{u['name']}的档案修改申请"),
            q("更新个人信息"),
            q("{}"), "NULL",
            q("信息不完整") if status == "rejected" else "NULL",
            "NULL",
            now, now,
        ]))
    return sql


# -------- main --------
def main():
    ap = argparse.ArgumentParser(description="BDAI_SC 测试数据生成器 (纯 INSERT, 不删除现有数据)")
    ap.add_argument("--users", type=int, default=20, help="用户数量 (默认20)")
    ap.add_argument("--achievements", type=int, default=50, help="每种成就数量 (默认50)")
    ap.add_argument("-o", "--outfile", type=str, help="输出文件路径")
    args = ap.parse_args()
    nu = args.users
    na = args.achievements

    sql = [f"-- BDAI_SC 测试数据 (追加模式)", f"-- 生成时间: {datetime.now()}", ""]

    # --- 生成用户 + 档案 + 教育经历 + 干部经历 (每个用户一个 block) ---
    print(f"[+] 生成 {nu} 用户 (ADMIN×2 + TEACHER×1 + CADRE×1 + STUDENT×{nu-4})")
    user_blocks, u_vars, p_vars, u_reviewer_vars = gen_users(nu)
    sql += user_blocks

    # --- 登录记录 ---
    print(f"[+] 生成登录记录")
    sql += ["", "-- ====== 登录记录 ======"]
    sql += gen_login(u_vars, n_per_user=8)

    # --- 系统设置 (INSERT IGNORE) ---
    print(f"[+] 生成系统设置")
    sql += ["", "-- ====== 系统设置 ======"]
    sql += gen_settings()

    # --- 成就 ---
    print(f"[+] 生成成就 (每种 {na} 条)")
    sql += ["", "-- ====== 成就-证书 ======"]
    sql += gen_certificates(p_vars, na)
    sql += ["", "-- ====== 成就-竞赛 ======"]
    sql += gen_contests(p_vars, na)
    sql += ["", "-- ====== 成就-论文 ======"]
    sql += gen_papers(p_vars, na)
    sql += ["", "-- ====== 成就-专利 ======"]
    sql += gen_patents(p_vars, na)
    sql += ["", "-- ====== 成就-科研 ======"]
    sql += gen_researches(p_vars, na)
    sql += ["", "-- ====== 成就-作品 ======"]
    sql += gen_works(p_vars, na)
    sql += ["", "-- ====== 成就-双百 ======"]
    sql += gen_double_hundreds(p_vars, na)
    sql += ["", "-- ====== 成就-培训 ======"]
    sql += gen_ieer_trainings(p_vars, na)
    sql += ["", "-- ====== 成就-期刊 ======"]
    sql += gen_journals(p_vars, na)

    # --- 审批 ---
    print(f"[+] 生成审批记录")
    sql += ["", "-- ====== 成就审批 ======"]
    sql += gen_achievement_review_requests(p_vars, u_reviewer_vars)
    sql += ["", "-- ====== 档案审批 ======"]
    sql += gen_profile_review_requests(p_vars, u_reviewer_vars)

    sql += ["", "-- 完成!"]

    tot = sum(1 for line in sql if line.lstrip().upper().startswith("INSERT"))
    print(f"[+] 总计 {tot} 条 INSERT")

    out = "\n".join(sql)
    if args.outfile:
        Path(args.outfile).write_text(out, encoding='utf-8')
        print(f"[+] 写入: {args.outfile}")
    else:
        print(out)


if __name__ == "__main__":
    main()
