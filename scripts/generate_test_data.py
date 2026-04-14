#!/usr/bin/env python3
"""
GCSC 测试数据生成器 - 基于 DB schema 精确列顺序
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

# -------- 词汇表 (与前端一致) --------
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

# 佛山校区: 1-21号楼 + 有为9栋 + 有为21栋
FOSHAN_BUILDINGS = [f"{n}号楼" for n in range(1, 22)] + ["有为9栋", "有为21栋"]
# 广州校区: 17-32号楼 + 凌云楼/揽月楼/丽枫酒店
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
DOUBLE_CATEGORIES = ["志愿服务", "社会实践", "创新创业"]
TRAINING_TYPES = ["专业技能", "创新能力", "职业素养"]
FINAL_STATUSES = ["合格", "优秀", "良好"]

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
    return f"INSERT INTO {table} ({','.join(cols)}) VALUES ({','.join(str(v) if v is not None else 'NULL' for v in vals)});"

def rand_phone():
    return faker.phone_number()

def rand_id_card():
    area = random.choice(["110101", "310101", "440101", "440301", "510101", "320101", "330101", "420101", "610101"])
    b = rand_date(1998, 2005).replace("-", "")
    seq = f"{random.randint(0, 999):03d}"
    return area + b + seq + random.choice(list("0123456789X"))

def rand_native_place():
    """生成省+市格式的籍贯，如 广东广州"""
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
    """生成详细住址: 省+市+县+路+号"""
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
    detail = faker.random_element(["栋", "号楼", "座"]) if random.random() > 0.5 else ""
    addr = f"{prov}{city}{county}{road}{num}{detail}"
    return addr

# -------- users --------
# DB cols: id(AUTO),created_at,display_name,password_hash,username,class_name,college,role,student_no,avatar_url
USERS_COLS = ["created_at", "display_name", "password_hash", "username", "class_name", "college", "role", "student_no", "avatar_url"]

def gen_users(n=20):
    users_sql, profiles_sql = [], []
    for i in range(1, n + 1):
        is_admin = (i <= 2)
        role = "ADMIN" if is_admin else "STUDENT"
        uname = f"user{i:04d}"
        dname = faker.name()
        pwd = "$2a$10$dummy_hash_not_real_bcrypt_for_testing"
        # 随机头像 URL (ADMIN 不设头像)
        avatar_url = None if is_admin else f"https://i.pravatar.cc/150?img={random.randint(1, 99)}"
        # ADMIN: 不填 class 信息; STUDENT: 填
        if is_admin:
            cname = None
            sno = None
        else:
            major = random.choice(MAJORS)
            class_no = f"{random.randint(1, 10):02d}"
            short = major[:4]
            cname = f"{short}{class_no}班"
            sno = f"2021{random.randint(1000, 9999)}"

        users_sql.append(ins("users", USERS_COLS,
            [q(rand_datetime6()), q(dname), q(pwd), q(uname), q(cname), q(FIXED_COLLEGE),
             q(role), q(sno), q(avatar_url)]))

        # -------- student_profiles --------
        eth = random.choice(ETHNICITIES)
        pol = random.choice(POLITICAL_STATUSES)
        ph = rand_phone()
        er = random.choice(RELATIONS)
        fn = faker.name_male()
        mn = faker.name_female()
        bd = rand_date(1998, 2005)
        native = rand_native_place()
        addr = rand_full_address()

        # ADMIN 用户: 不填入学/班级/宿舍信息
        if is_admin:
            major = None; cname = None; class_no = None; sno = None
            class_year = None; enrollment_date = None
            building = None; campus = None; room = None
            student_category = None
            league_joined_val = 0; league_app_date = "NULL"; league_join_date = "NULL"
            party_val = 0; not_dev_val = 0
            ad_date = "NULL"; dd_date = "NULL"; ppt_date = "NULL"; pd_date = "NULL"
        else:
            # 随机本科生或研究生
            student_category = random.choice(["本科生", "研究生"])
            if student_category == "研究生":
                major = random.choice(GRAD_MAJORS)
            else:
                major = random.choice(MAJORS)
            class_no = f"{random.randint(1, 10):02d}"
            short = major[:4]
            cname = f"{short}{class_no}班"
            # class_year 存入学年份(2020-2026)
            ey = random.randint(2020, 2026)
            class_year = ey
            enrollment_date = f"{ey}-09-01"

            # 住宿校区和楼栋
            campus = random.choice(DORM_CAMPUSES)
            if campus == "佛山校区":
                building = random.choice(FOSHAN_BUILDINGS)
            else:
                building = random.choice(GUANGZHOU_BUILDINGS)
            room = f"{random.randint(1, 6)}{random.randint(1, 20):02d}"

            # -------- 团组织信息 --------
            league_joined_val = 1 if random.random() < 0.8 else 0
            league_app_date = q(rand_date(2016, 2020)) if league_joined_val else "NULL"
            league_join_date = q(rand_date(2017, 2021)) if league_joined_val else "NULL"

            # -------- 入党信息 --------
            party_val = 1 if random.random() < 0.7 else 0
            not_dev_val = 1 if pol == "群众" and random.random() < 0.3 else 0

            ad_date = q(rand_date(2022, 2024)) if party_val else "NULL"
            dd_date = q(rand_date(2023, 2025)) if party_val and random.random() > 0.4 else "NULL"
            ppt_date = q(rand_date(2022, 2024)) if party_val else "NULL"
            pd_date = q(rand_date(2023, 2025)) if party_val and pol in ("中共预备党员", "中共党员") else "NULL"

        profiles_sql.append(ins("student_profiles",
            ["activist_date", "address", "application_date", "class_major", "class_name", "class_no",
             "class_year", "college", "emergency_phone", "emergency_relation", "full_name", "id_no",
             "league_no", "native_place", "not_developed", "party_applied", "phone", "student_no",
             "user_id", "is_hk_mo_tw", "is_special", "activist_developing", "class_teacher",
             "counselor", "development_target_date", "development_target_developing",
             "dorm_building", "dorm_campus", "dorm_room", "enrollment_date", "ethnicity",
             "father_name", "father_phone", "father_title", "father_work_unit",
             "full_member_date", "full_member_developing", "league_application_date",
             "league_developing", "league_join_date", "league_joined", "mother_name",
             "mother_phone", "mother_title", "mother_work_unit", "off_campus_address",
             "off_campus_living", "party_training_date", "party_training_pending",
             "political_status", "probationary_developing", "probationary_member_date",
             "student_category", "birth_date", "backup_contact", "id_type"],
            [ad_date, q(addr), "NULL", q(major), q(cname), q(class_no), class_year, q(FIXED_COLLEGE),
             q(ph), q(er), q(dname), q(rand_id_card()),
             q(f"LM{random.randint(100000, 999999)}") if not is_admin else "NULL", q(native),
             not_dev_val, party_val, q(ph), q(sno), i,
             0, 0, 0, q(faker.name()), q(faker.name()),
             dd_date, 0, q(building) if building else "NULL",
             q(campus) if campus else "NULL",
             q(room) if room else "NULL",
             q(enrollment_date), q(eth),
             q(fn), q(rand_phone()), q(random.choice(PARENT_TITLES)), q(faker.company()),
             "NULL", 0, league_app_date, 0, league_join_date, league_joined_val,
             q(mn), q(rand_phone()), q(random.choice(PARENT_TITLES)), q(faker.company()),
             "NULL", 0, ppt_date, "NULL", q(pol), 0, pd_date,
             q(student_category), q(bd), q(rand_phone()), q("居民身份证")]
        ))
    return users_sql, profiles_sql

# -------- education_experiences --------
# DB cols: id,education_level,end_date,is_current,school_name,start_date,witness,profile_id
EDU_COLS = ["education_level", "end_date", "is_current", "school_name", "start_date", "witness", "profile_id"]

def gen_edu(n=20):
    sql = []
    for i in range(1, n + 1):
        if i <= 2:
            continue  # skip ADMIN
        for _ in range(random.randint(1, 2)):
            s = rand_date(2015, 2021)
            e = rand_date(2016, 2022)
            edu_level = random.choice(["小学", "初中", "高中", "中专"])
            school = faker.random_element([
                "北京市第一小学", "上海师范大学附属中学", "广东省实验中学", "江苏省天一中学",
                "浙江省宁波中学", "湖北省武汉中学", "四川省成都七中", "重庆市南开中学",
                "陕西省西安中学", "山东省实验中学", "河南省郑州中学", "湖南省长郡中学",
            ])
            sql.append(ins("education_experiences", EDU_COLS,
                [q(edu_level), q(e), 0, q(school), q(s), q(faker.name()), i]))
    return sql

# -------- cadre_experiences --------
# DB cols: id,department,description,end_date,is_current,position,start_date,profile_id
CADRE_COLS = ["department", "description", "end_date", "is_current", "position", "start_date", "profile_id"]

def gen_cadre(n=20):
    sql = []
    for i in range(1, n + 1):
        if i <= 2:
            continue  # skip ADMIN
        for _ in range(random.randint(1, 3)):
            s = rand_date(2021, 2024)
            cur = random.random() > 0.3
            sql.append(ins("cadre_experiences", CADRE_COLS,
                [q(random.choice(DEPARTMENTS)), q(faker.sentence(10)),
                 q(rand_date(2022, 2025)) if not cur else "NULL",
                 1 if cur else 0, q(random.choice(POSITIONS)), q(s), i]))
    return sql

# -------- login_histories --------
# DB cols: id,device_name,ip_address,login_time,user_agent,user_id
LOGIN_COLS = ["device_name", "ip_address", "login_time", "user_agent", "user_id"]

def gen_login(n=20):
    sql = []
    for _ in range(n * 10):
        uid = random.randint(1, 20)
        sql.append(ins("login_histories", LOGIN_COLS,
            [q(random.choice(["Chrome", "Safari", "Firefox", "Edge"])),
             q(faker.ipv4()), q(rand_datetime6()),
             q(random.choice(["Mozilla/5.0 (Windows NT 10) AppleWebKit/537.36",
                             "Mozilla/5.0 (Macintosh) AppleWebKit/537.36"])), uid]))
    return sql

# -------- main --------
def main():
    ap = argparse.ArgumentParser(description="GCSC 测试数据生成器")
    ap.add_argument("--users", type=int, default=20)
    ap.add_argument("-o", "--outfile", type=str)
    args = ap.parse_args()
    nu = args.users

    sql = ["-- GCSC 测试数据", f"-- {datetime.now()}", ""]
    sql.append("SET FOREIGN_KEY_CHECKS=0;")
    for t in ["login_histories", "cadre_experiences", "education_experiences",
              "student_profiles", "post_media", "posts", "contacts", "users"]:
        sql.append(f"DELETE FROM {t};")
    for t in ["users", "student_profiles", "posts", "contacts", "post_media"]:
        sql.append(f"ALTER TABLE {t} AUTO_INCREMENT=1;")
    sql.append("")

    print(f"[+] 生成 {nu} 用户")
    u, p = gen_users(nu)
    e = gen_edu(nu)
    c = gen_cadre(nu)
    l = gen_login(nu)

    sql += ["-- users"] + u + ["", "-- student_profiles"] + p + [
        "", "-- education_experiences"] + e + ["", "-- cadre_experiences"] + c + [
        "", "-- login_histories"] + l + ["", "SET FOREIGN_KEY_CHECKS=1;", "-- 完成!"]

    tot = len(u) + len(p) + len(e) + len(c) + len(l)
    print(f"[+] 总计 {tot} 条 SQL")
    out = "\n".join(sql)
    if args.outfile:
        Path(args.outfile).write_text(out)
        print(f"[+] 写入: {args.outfile}")
    else:
        print(out)

if __name__ == "__main__":
    main()
