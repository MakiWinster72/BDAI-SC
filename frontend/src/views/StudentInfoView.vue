<template>
  <div class="dashboard-layout">
    <aside class="dashboard-left">
      <section class="profile-card">
        <div class="profile-row profile-main">
          <div class="profile-avatar">
            <img
              v-if="profile.avatarUrl"
              :src="resolveMediaUrl(profile.avatarUrl)"
              alt="头像"
            />
            <span v-else>{{ avatarText }}</span>
          </div>
          <div class="profile-name-wrap">
            <p class="profile-name">
              {{ profile.displayName || profile.username || "同学" }}
            </p>
            <p class="profile-role">{{ roleLabel }}</p>
          </div>
          <button
            class="profile-settings"
            type="button"
            aria-label="设置"
            @click="goToSettings"
          >
            <img src="/assets/icons/settings.svg" alt="设置" />
          </button>
        </div>
        <div class="profile-row">学号：{{ profile.studentNo || "未填写" }}</div>
        <div class="profile-row">班级：{{ profile.className || "未填写" }}</div>
        <div class="profile-row">学院：{{ profile.college || "未填写" }}</div>
      </section>

      <section class="menu-card">
        <button
          v-for="item in menuItems"
          :key="item.key"
          class="menu-item"
          :class="{
            active: activeMenu === item.key,
            disabled: !isMenuEnabled(item.key),
          }"
          type="button"
          :disabled="!isMenuEnabled(item.key)"
          @click="handleMenuClick(item.key)"
        >
          {{ item.label }}
        </button>
      </section>
    </aside>

    <main class="dashboard-right">
      <header class="feed-header">
        <h1 class="feed-title">学生信息</h1>
      </header>

      <section class="info-shell student-right-stack">
        <section class="info-card student-filter-card">
          <div class="student-filter-header">
            <div class="info-section-title">搜索</div>
            <input
              v-model="filters.keyword"
              class="info-input student-search"
              type="text"
              placeholder="搜索姓名 / 班别 / 学院 / 学号"
            />
          </div>
          <div class="student-filter-grid">
            <div class="student-filter-row">
              <div class="student-filter-field">
                <span class="info-label">年级</span>
                <select v-model="filters.classYear" class="info-input">
                  <option value="">全部</option>
                  <option
                    v-for="year in classYearOptions"
                    :key="year"
                    :value="String(year)"
                  >
                    {{ year }}
                  </option>
                </select>
              </div>
            </div>

            <div class="student-filter-row">
              <div class="student-filter-field">
                <span class="info-label">专业</span>
                <select v-model="filters.major" class="info-input">
                  <option value="">全部</option>
                  <option
                    v-for="major in availableMajors"
                    :key="major"
                    :value="major"
                  >
                    {{ major }}
                  </option>
                </select>
              </div>
            </div>

            <div class="student-filter-row">
              <div class="student-filter-field">
                <span class="info-label">班级</span>
                <div class="student-stepper">
                  <button
                    class="stepper-button"
                    type="button"
                    :disabled="!canDecrementClass"
                    @click="decrementClass"
                  >
                    −
                  </button>
                  <input
                    v-model="filters.classNo"
                    class="info-input stepper-input"
                    type="number"
                    min="1"
                    max="10"
                    step="1"
                    placeholder="全部"
                    @input="normalizeClassNo"
                  />
                  <button
                    class="stepper-button"
                    type="button"
                    :disabled="!canIncrementClass"
                    @click="incrementClass"
                  >
                    +
                  </button>
                </div>
              </div>
            </div>

            <div class="student-filter-row student-filter-inline">
              <label class="info-choice">
                <input v-model="filters.isHkMoTw" type="checkbox" />
                港澳台
              </label>
              <label class="info-choice">
                <input v-model="filters.isSpecial" type="checkbox" />
                特殊学生
              </label>
            </div>
          </div>
        </section>

        <section class="info-card student-results-card">
          <div class="student-results-header">
            <div class="info-section-title">筛选结果</div>
            <div class="student-results-actions">
              <button
                class="ghost-button"
                type="button"
                @click="selectCurrentPage"
              >
                选择本页
              </button>
              <button
                class="ghost-button"
                type="button"
                :disabled="selectAllLoading"
                @click="selectAllFiltered"
              >
                {{ selectAllLoading ? "选择中..." : "选择全部" }}
              </button>
            </div>
          </div>
          <div v-if="pagedStudents.length" class="student-list">
            <div
              v-for="item in pagedStudents"
              :key="item.id"
              class="student-row"
            >
              <input v-model="selectedIds" type="checkbox" :value="item.id" />
              <div class="student-main">
                <div class="student-name">{{ item.name }}</div>
                <div class="student-meta">
                  {{ item.gradeYear }}级 {{ item.major }}{{ item.classNo }}班
                  {{ item.studentNo }}
                </div>
              </div>
              <button
                class="ghost-button"
                type="button"
                @click="openDetail(item)"
              >
                详情
              </button>
            </div>
          </div>
          <div v-else class="empty-tip">没有匹配的学生。</div>

          <div class="student-pagination">
            <div class="student-pages">
              <button
                v-for="page in totalPages"
                :key="page"
                class="page-button"
                :class="{ active: page === currentPage }"
                type="button"
                @click="setPage(page)"
              >
                {{ page }}
              </button>
              <input
                v-model.number="pageInput"
                class="info-input page-input"
                type="number"
                :min="1"
                :max="totalPages"
                placeholder="页码"
                @change="applyPageInput"
              />
            </div>
            <button
              class="action-button"
              type="button"
              :disabled="exportDisabled"
              @click="openExportDialog"
            >
              {{ exportLabel }}
            </button>
          </div>
        </section>
      </section>

      <transition name="publisher-backdrop">
        <div
          v-if="viewOpen"
          class="student-detail-backdrop"
          @click="closeView"
        ></div>
      </transition>
      <section
        class="student-detail-view"
        :class="{
          open: viewOpen,
          closing: viewClosing,
          split: achievementsOpen || achievementsClosing,
        }"
        :aria-hidden="!viewOpen"
      >
        <header class="publisher-header">
          <div class="publisher-title">学生详情</div>
          <button class="publisher-close" type="button" @click="closeView">
            关闭
          </button>
        </header>
        <div v-if="viewLoading" class="empty-tip">加载中...</div>
        <div v-else-if="viewItem" class="info-shell student-detail-body">
          <div class="info-hero">
            <button class="avatar-square" type="button" disabled>
              <img
                v-if="viewItem.avatarUrl"
                :src="resolveMediaUrl(viewItem.avatarUrl)"
                alt="头像"
              />
              <span v-else>{{
                (viewItem.fullName || "同学").slice(0, 1)
              }}</span>
            </button>
            <div class="info-hero-text">
              <div class="info-hero-title">
                {{ viewItem.fullName || "未命名" }}
              </div>
              <div class="info-hero-subtitle">
                学号：{{ viewItem.studentNo || "-" }}
              </div>
            </div>
            <div class="info-actions info-actions-single">
              <button
                class="action-button"
                type="button"
                @click="openAchievements"
              >
                个人成就
              </button>
            </div>
          </div>

          <div class="info-card">
            <div class="info-section-title">学籍信息</div>
            <div class="info-form-grid">
              <div class="field-card">
                <span class="info-label">名字</span>
                <div class="info-input info-static">
                  {{ viewItem.fullName || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">学号</span>
                <div class="info-input info-static">
                  {{ viewItem.studentNo || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">年级</span>
                <div class="info-input info-static">
                  {{ viewItem.classYear ? viewItem.classYear + "级" : "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">学院</span>
                <div class="info-input info-static">
                  {{ viewItem.college || "-" }}
                </div>
              </div>
              <div class="field-card field-full">
                <span class="info-label">班级</span>
                <div class="info-input info-static">
                  {{ buildClassName(viewItem) || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">入学时间</span>
                <div class="info-input info-static">
                  {{ viewItem.enrollmentDate || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">学生类别</span>
                <div class="info-input info-static">
                  {{ viewItem.studentCategory || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">班主任</span>
                <div class="info-input info-static">
                  {{ viewItem.classTeacher || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">辅导员</span>
                <div class="info-input info-static">
                  {{ viewItem.counselor || "-" }}
                </div>
              </div>
            </div>
          </div>

          <div class="info-card">
            <div class="info-section-title">个人证件与联系方式</div>
            <div class="info-form-grid">
              <div class="field-card">
                <span class="info-label">民族</span>
                <div class="info-input info-static">
                  {{ viewItem.ethnicity || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">政治面貌</span>
                <div class="info-input info-static">
                  {{ viewItem.politicalStatus || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">手机号码</span>
                <div class="info-input info-static">
                  {{ viewItem.phone || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">身份证件号</span>
                <div class="info-input info-static">
                  {{ viewItem.idNo || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">籍贯</span>
                <div class="info-input info-static">
                  {{ viewItem.nativePlace || "-" }}
                </div>
              </div>
              <div class="field-card field-full">
                <span class="info-label">住址</span>
                <div class="info-input info-static">
                  {{ viewItem.address || "-" }}
                </div>
              </div>
            </div>
          </div>

          <div class="info-card">
            <div class="info-section-title">住宿信息</div>
            <div class="info-form-grid">
              <div class="field-card field-full">
                <span class="info-label">是否在外居住</span>
                <div class="info-input info-static">
                  {{ viewItem.offCampusLiving ? "是" : "否" }}
                </div>
              </div>
              <div
                class="field-card field-full"
                v-if="viewItem.offCampusLiving"
              >
                <span class="info-label">外居住详细地址</span>
                <div class="info-input info-static">
                  {{ viewItem.offCampusAddress || "-" }}
                </div>
              </div>
              <div class="field-card" v-if="!viewItem.offCampusLiving">
                <span class="info-label">住宿校区</span>
                <div class="info-input info-static">
                  {{ viewItem.dormCampus || "-" }}
                </div>
              </div>
              <div class="field-card" v-if="!viewItem.offCampusLiving">
                <span class="info-label">住宿楼栋</span>
                <div class="info-input info-static">
                  {{ viewItem.dormBuilding || "-" }}
                </div>
              </div>
              <div class="field-card" v-if="!viewItem.offCampusLiving">
                <span class="info-label">住宿房间</span>
                <div class="info-input info-static">
                  {{ viewItem.dormRoom || "-" }}
                </div>
              </div>
            </div>
          </div>

          <div class="info-card">
            <div class="info-section-title">团组织与入党信息</div>
            <div class="info-form-grid three">
              <div class="field-card field-full">
                <span class="info-label">是否入团</span>
                <div class="info-input info-static">
                  {{ viewItem.leagueJoined ? "是" : "否" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">提交入团申请书时间</span>
                <div class="info-input info-static">
                  {{ viewItem.leagueApplicationDate || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">入团时间</span>
                <div class="info-input info-static">
                  {{
                    formatDateOrStatus(
                      viewItem.leagueJoinDate,
                      viewItem.leagueDeveloping,
                      "正在发展",
                    )
                  }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">团号</span>
                <div class="info-input info-static">
                  {{ viewItem.leagueNo || "-" }}
                </div>
              </div>
              <div class="field-card field-full">
                <span class="info-label">是否申请入党</span>
                <div class="info-input info-static">
                  {{ viewItem.partyApplied ? "是" : "否" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">提交入党申请书时间</span>
                <div class="info-input info-static">
                  {{ viewItem.applicationDate || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">确定积极分子时间</span>
                <div class="info-input info-static">
                  {{
                    formatDateOrStatus(
                      viewItem.activistDate,
                      viewItem.activistDeveloping,
                      "正在发展",
                    )
                  }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">上党课时间</span>
                <div class="info-input info-static">
                  {{
                    formatDateOrStatus(
                      viewItem.partyTrainingDate,
                      viewItem.partyTrainingPending,
                      "暂未报名",
                    )
                  }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">确定发展对象时间</span>
                <div class="info-input info-static">
                  {{
                    formatDateOrStatus(
                      viewItem.developmentTargetDate,
                      viewItem.developmentTargetDeveloping,
                      "正在发展",
                    )
                  }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">接收为预备党员时间</span>
                <div class="info-input info-static">
                  {{
                    formatDateOrStatus(
                      viewItem.probationaryMemberDate,
                      viewItem.probationaryDeveloping,
                      "正在发展",
                    )
                  }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">转为正式党员时间</span>
                <div class="info-input info-static">
                  {{
                    formatDateOrStatus(
                      viewItem.fullMemberDate,
                      viewItem.fullMemberDeveloping,
                      "正在发展",
                    )
                  }}
                </div>
              </div>
            </div>
          </div>

          <div class="info-card">
            <div class="info-section-title">教育经历</div>
            <div v-if="educationRows.length" class="education-table-wrap">
              <table class="education-table">
                <thead>
                  <tr>
                    <th>时间段</th>
                    <th>学校名称</th>
                    <th>学历</th>
                    <th>证明人</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="(item, index) in educationRows"
                    :key="`edu-view-${index}`"
                  >
                    <td>
                      <div class="education-period">
                        <div class="info-input info-static">
                          {{ item.startDate || "-" }}
                        </div>
                        <span class="education-sep">至</span>
                        <div class="info-input info-static">
                          {{ item.isCurrent ? "至今" : item.endDate || "-" }}
                        </div>
                      </div>
                    </td>
                    <td>
                      <div class="info-input info-static">
                        {{ item.schoolName || "-" }}
                      </div>
                    </td>
                    <td>
                      <div class="info-input info-static">
                        {{ item.educationLevel || "-" }}
                      </div>
                    </td>
                    <td>
                      <div class="info-input info-static">
                        {{ item.witness || "-" }}
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-else class="empty-tip">暂无教育经历</div>
          </div>

          <div class="info-card">
            <div class="info-section-title">家庭信息</div>
            <div class="info-form-grid family-grid">
              <div class="family-section-title">父亲</div>
              <div class="field-card">
                <span class="info-label">姓名</span>
                <div class="info-input info-static">
                  {{ viewItem.fatherName || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">手机号码</span>
                <div class="info-input info-static">
                  {{ viewItem.fatherPhone || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">工作单位</span>
                <div class="info-input info-static">
                  {{ viewItem.fatherWorkUnit || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">职务</span>
                <div class="info-input info-static">
                  {{ viewItem.fatherTitle || "-" }}
                </div>
              </div>
              <div class="family-section-title">母亲</div>
              <div class="field-card">
                <span class="info-label">姓名</span>
                <div class="info-input info-static">
                  {{ viewItem.motherName || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">手机号码</span>
                <div class="info-input info-static">
                  {{ viewItem.motherPhone || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">工作单位</span>
                <div class="info-input info-static">
                  {{ viewItem.motherWorkUnit || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">职务</span>
                <div class="info-input info-static">
                  {{ viewItem.motherTitle || "-" }}
                </div>
              </div>
            </div>
          </div>

          <div class="info-card">
            <div class="info-section-title">紧急联系人</div>
            <div class="info-form-grid">
              <div class="field-card">
                <span class="info-label">紧急联系人电话</span>
                <div class="info-input info-static">
                  {{ viewItem.emergencyPhone || "-" }}
                </div>
              </div>
              <div class="field-card">
                <span class="info-label">紧急联系人的关系</span>
                <div class="info-input info-static">
                  {{ viewItem.emergencyRelation || "-" }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section
        class="student-achievements-view"
        :class="{ open: achievementsOpen, closing: achievementsClosing }"
        :aria-hidden="!achievementsOpen"
      >
        <header class="publisher-header">
          <div class="publisher-title">个人成就</div>
          <button
            class="publisher-close"
            type="button"
            @click="closeAchievements"
          >
            关闭
          </button>
        </header>
        <div class="student-achievements-body" v-if="viewItem">
          <iframe
            class="student-achievements-frame"
            :key="achievementUrl"
            :src="achievementUrl"
            title="学生个人成就"
          ></iframe>
        </div>
      </section>

      <transition name="publisher-backdrop">
        <div
          v-if="exportDialogOpen"
          class="export-dialog-backdrop"
          @click="closeExportDialog"
        ></div>
      </transition>
      <section class="export-dialog" :class="{ open: exportDialogOpen }">
        <header class="export-dialog-header">
          <div class="export-dialog-title">
            导出信息选择
            <label class="export-all-toggle">
              <input
                type="checkbox"
                :checked="isAllSelected"
                @change="toggleAllSelections($event.target.checked)"
              />
              <span>全选</span>
            </label>
          </div>
          <button class="ghost-button" type="button" @click="closeExportDialog">
            关闭
          </button>
        </header>
        <div class="export-dialog-body">
          <div
            v-for="group in exportGroups"
            :key="group.id"
            class="export-group"
          >
            <label class="export-group-title">
              <span>{{ group.label }}</span>
              <input
                type="checkbox"
                :checked="isGroupSelected(group)"
                @change="toggleGroupSelection(group, $event.target.checked)"
              />
            </label>
            <div class="export-group-options">
              <template v-if="group.id === 'family'">
                <div
                  v-for="(row, index) in familyRows"
                  :key="`family-row-${index}`"
                  class="export-option-row"
                >
                  <label
                    v-for="field in row"
                    :key="field.key"
                    class="export-option"
                  >
                    <input v-model="exportSelections[field.key]" type="checkbox" />
                    <span>{{ field.label }}</span>
                  </label>
                </div>
              </template>
              <template v-else>
                <label
                  v-for="field in group.fields"
                  :key="field.key"
                  class="export-option"
                >
                  <input v-model="exportSelections[field.key]" type="checkbox" />
                  <span>{{ field.label }}</span>
                </label>
              </template>
            </div>
          </div>
        </div>
        <footer class="export-dialog-actions">
          <button class="ghost-button" type="button" @click="closeExportDialog">
            取消
          </button>
          <button
            class="action-button export-confirm"
            type="button"
            :disabled="exporting"
            @click="confirmExport"
          >
            {{ exporting ? "导出中..." : "确认导出" }}
          </button>
        </footer>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import * as XLSX from "xlsx";
import { useRouter } from "vue-router";
import { filterMenuItemsByRole, isMenuEnabled } from "../constants/menu";
import { getStudentProfileById, searchStudentProfiles } from "../api/profile";

const router = useRouter();
const API_BASE = "http://localhost:8080";

const profile = reactive(loadUser());
const activeMenu = ref("student-info");
const menuItems = computed(() => filterMenuItemsByRole(profile.role));
const selectedIds = ref([]);
const exporting = ref(false);
const currentPage = ref(1);
const pageInput = ref(null);
const students = ref([]);
const totalPages = ref(1);
const totalItems = ref(0);
const loading = ref(false);
const pageSize = 5;
const viewOpen = ref(false);
const viewClosing = ref(false);
const viewItem = ref(null);
const viewLoading = ref(false);
const selectAllLoading = ref(false);
const exportDialogOpen = ref(false);
const achievementsOpen = ref(false);
const achievementsClosing = ref(false);

const classYearOptions = Array.from({ length: 11 }, (_, index) => 2020 + index);
const majorOptions = [
  "计算机科学与技术",
  "计算机科学与技术（实验区）",
  "软件工程",
  "电子商务",
  "大数据管理与应用（佛山校区全学段）",
  "大数据管理与应用（数字治理）",
];

const filters = reactive({
  classYear: "",
  major: "",
  classNo: "",
  isHkMoTw: false,
  isSpecial: false,
  keyword: "",
});

const exportGroups = [
  {
    id: "base",
    label: "基础信息",
    fields: [
      { key: "name", label: "姓名", defaultSelected: true },
      { key: "className", label: "班级", defaultSelected: true },
      { key: "studentNo", label: "学号", defaultSelected: true },
    ],
  },
  {
    id: "school",
    label: "学籍信息",
    fields: [
      { key: "enrollmentDate", label: "入学时间" },
      { key: "studentCategory", label: "学生类别" },
      { key: "classTeacher", label: "班主任" },
      { key: "counselor", label: "辅导员" },
    ],
  },
  {
    id: "identity",
    label: "个人证件与联系方式",
    fields: [
      { key: "ethnicity", label: "民族" },
      { key: "politicalStatus", label: "政治面貌" },
      { key: "phone", label: "手机号码" },
      { key: "idNo", label: "身份证号" },
      { key: "nativePlace", label: "籍贯" },
      { key: "address", label: "住址" },
    ],
  },
  {
    id: "dorm",
    label: "住宿信息",
    fields: [
      { key: "offCampusLiving", label: "是否在外居住" },
      { key: "offCampusAddress", label: "外居住地址" },
      { key: "dormCampus", label: "住宿校区" },
      { key: "dormBuilding", label: "住宿楼栋" },
      { key: "dormRoom", label: "住宿房间" },
    ],
  },
  {
    id: "family",
    label: "家庭信息",
    fields: [
      { key: "fatherName", label: "父亲姓名" },
      { key: "fatherPhone", label: "父亲电话" },
      { key: "fatherWorkUnit", label: "父亲工作单位" },
      { key: "fatherTitle", label: "父亲职务" },
      { key: "motherName", label: "母亲姓名" },
      { key: "motherPhone", label: "母亲电话" },
      { key: "motherWorkUnit", label: "母亲工作单位" },
      { key: "motherTitle", label: "母亲职务" },
      { key: "hkMoTw", label: "港澳台" },
      { key: "specialStudent", label: "特殊学生" },
    ],
  },
  {
    id: "emergency",
    label: "紧急联系人",
    fields: [
      { key: "emergencyPhone", label: "紧急联系人电话" },
      { key: "emergencyRelation", label: "紧急联系人关系" },
    ],
  },
  {
    id: "education",
    label: "教育经历",
    fields: [
      { key: "eduPeriod", label: "时间段" },
      { key: "eduSchoolName", label: "学校名称" },
      { key: "eduEducationLevel", label: "学历" },
      { key: "eduWitness", label: "证明人" },
    ],
  },
  {
    id: "party",
    label: "团组织与入党信息",
    fields: [
      { key: "leagueJoined", label: "是否入团" },
      { key: "leagueApplicationDate", label: "提交入团申请书时间" },
      { key: "leagueJoinDate", label: "入团时间" },
      { key: "leagueNo", label: "团号" },
      { key: "partyApplied", label: "是否申请入党" },
      { key: "applicationDate", label: "提交入党申请书时间" },
      { key: "activistDate", label: "确定积极分子时间" },
      { key: "partyTrainingDate", label: "上党课时间" },
      { key: "developmentTargetDate", label: "确定发展对象时间" },
      { key: "probationaryMemberDate", label: "接收为预备党员时间" },
      { key: "fullMemberDate", label: "转为正式党员时间" },
    ],
  },
];

const exportSelections = reactive(initExportSelections());

function initExportSelections() {
  const selections = {};
  exportGroups.forEach((group) => {
    group.fields.forEach((field) => {
      selections[field.key] = Boolean(field.defaultSelected);
    });
  });
  return selections;
}

const familyRows = computed(() => {
  const group = exportGroups.find((item) => item.id === "family");
  if (!group) {
    return [];
  }
  const byKey = Object.fromEntries(group.fields.map((field) => [field.key, field]));
  return [
    ["fatherName", "fatherPhone", "fatherWorkUnit", "fatherTitle"]
      .map((key) => byKey[key])
      .filter(Boolean),
    ["motherName", "motherPhone", "motherWorkUnit", "motherTitle"]
      .map((key) => byKey[key])
      .filter(Boolean),
    ["hkMoTw", "specialStudent"]
      .map((key) => byKey[key])
      .filter(Boolean),
  ];
});

const availableMajors = computed(() => {
  return majorOptions;
});

const hasActiveFilters = computed(() => {
  return Boolean(
    filters.classYear ||
    filters.major ||
    filters.classNo ||
    filters.isHkMoTw ||
    filters.isSpecial ||
    filters.keyword,
  );
});

const pagedStudents = computed(() => students.value);
const selectedStudents = computed(() => {
  const idSet = new Set(selectedIds.value.map((id) => String(id)));
  return students.value.filter((item) => idSet.has(String(item.id)));
});
const exportDisabled = computed(
  () => exporting.value || selectedIds.value.length === 0,
);
const exportLabel = computed(() => {
  const count = selectedIds.value.length;
  return count ? `导出(${count})` : "导出";
});
const educationRows = computed(() => {
  const items = viewItem.value?.educationExperiences;
  if (!Array.isArray(items)) {
    return [];
  }
  return items.filter((item) => {
    if (!item) {
      return false;
    }
    return (
      item.startDate ||
      item.endDate ||
      item.schoolName ||
      item.educationLevel ||
      item.witness ||
      item.isCurrent
    );
  });
});

const achievementUrl = computed(() => {
  if (!viewItem.value) {
    return "/achievements?category=all";
  }
  const params = new URLSearchParams();
  params.set("category", "all");
  if (viewItem.value.studentNo) {
    params.set("studentNo", viewItem.value.studentNo);
  }
  if (viewItem.value.fullName) {
    params.set("studentName", viewItem.value.fullName);
  }
  params.set("embed", "1");
  return `/achievements?${params.toString()}`;
});

watch(
  () => ({
    classYear: filters.classYear,
    major: filters.major,
    classNo: filters.classNo,
    isHkMoTw: filters.isHkMoTw,
    isSpecial: filters.isSpecial,
    keyword: filters.keyword,
  }),
  () => {
    currentPage.value = 1;
    pageInput.value = null;
    selectedIds.value = [];
    fetchStudents();
  },
  { deep: true },
);

watch(currentPage, () => {
  fetchStudents();
});

function resetResults() {
  students.value = [];
  totalPages.value = 1;
  totalItems.value = 0;
}

async function fetchStudents() {
  loading.value = true;
  try {
    const { data } = await searchStudentProfiles(
      buildSearchParams(currentPage.value, pageSize),
    );
    students.value = (data?.items || []).map((item) => ({
      id: item.id,
      name: item.fullName || "未命名",
      gradeYear: item.classYear || "",
      college: item.college || "",
      major: item.classMajor || "",
      classNo: item.classNo || "",
      studentNo: item.studentNo || "",
      hkMoTw: item.hkMoTw || false,
      specialStudent: item.specialStudent || false,
    }));
    totalPages.value = Math.max(1, data?.totalPages || 1);
    totalItems.value = data?.total || 0;
  } catch {
    resetResults();
  } finally {
    loading.value = false;
  }
}

function openDetail(item) {
  viewOpen.value = true;
  viewClosing.value = false;
  viewLoading.value = true;
  viewItem.value = null;
  achievementsOpen.value = false;
  achievementsClosing.value = false;
  getStudentProfileById(item.id)
    .then(({ data }) => {
      viewItem.value = data || null;
    })
    .catch(() => {
      viewItem.value = null;
    })
    .finally(() => {
      viewLoading.value = false;
    });
}

function closeView() {
  if (achievementsOpen.value) {
    closeAchievements();
  }
  viewOpen.value = false;
  viewClosing.value = true;
  setTimeout(() => {
    viewItem.value = null;
    viewLoading.value = false;
    viewClosing.value = false;
  }, 260);
}

function openAchievements() {
  if (!viewItem.value) {
    return;
  }
  achievementsOpen.value = true;
  achievementsClosing.value = false;
}

function closeAchievements() {
  achievementsOpen.value = false;
  achievementsClosing.value = true;
  setTimeout(() => {
    achievementsClosing.value = false;
  }, 260);
}

onMounted(() => {
  fetchStudents();
});

function setPage(page) {
  currentPage.value = page;
  pageInput.value = null;
}

function applyPageInput() {
  if (!pageInput.value) {
    return;
  }
  const safePage = Math.min(Math.max(1, pageInput.value), totalPages.value);
  currentPage.value = safePage;
}

function formatDateOrStatus(dateValue, statusFlag, statusText) {
  if (statusFlag) {
    return statusText;
  }
  return dateValue || "-";
}

function formatDateOrEmpty(dateValue, statusFlag, statusText) {
  if (statusFlag) {
    return statusText;
  }
  return dateValue || "";
}

function buildClassName(item) {
  if (!item) {
    return "";
  }
  if (item.className) {
    return item.className;
  }
  const safeYear = item.classYear ? `${item.classYear}级` : "";
  const safeMajor = item.classMajor || "";
  const safeNo = item.classNo ? `${item.classNo}班` : "";
  return `${safeYear}${safeMajor}${safeNo}`.trim();
}

function normalizeClassNo() {
  if (filters.classNo === "") {
    return;
  }
  const next = Number(filters.classNo);
  if (Number.isNaN(next)) {
    filters.classNo = "";
    return;
  }
  const clamped = Math.min(Math.max(1, next), 10);
  filters.classNo = String(clamped);
}

const canDecrementClass = computed(() => true);
const canIncrementClass = computed(() => true);

function decrementClass() {
  const current = Number(filters.classNo);
  if (!filters.classNo || Number.isNaN(current)) {
    filters.classNo = "10";
    return;
  }
  if (current <= 1) {
    filters.classNo = "";
    return;
  }
  filters.classNo = String(current - 1);
}

function incrementClass() {
  const current = Number(filters.classNo);
  if (!filters.classNo || Number.isNaN(current)) {
    filters.classNo = "1";
    return;
  }
  if (current >= 10) {
    filters.classNo = "";
    return;
  }
  filters.classNo = String(current + 1);
}

function buildSearchParams(page, size) {
  const params = {
    page,
    size,
  };
  if (filters.classYear) {
    params.classYear = filters.classYear;
  }
  if (filters.major) {
    params.major = filters.major;
  }
  if (filters.classNo) {
    params.classNo = Number(filters.classNo);
  }
  if (filters.isHkMoTw) {
    params.hkMoTw = true;
  }
  if (filters.isSpecial) {
    params.specialStudent = true;
  }
  if (filters.keyword && filters.keyword.trim()) {
    params.keyword = filters.keyword.trim();
  }
  return params;
}

function selectCurrentPage() {
  selectedIds.value = pagedStudents.value.map((item) => item.id);
}

async function selectAllFiltered() {
  if (selectAllLoading.value) {
    return;
  }
  selectAllLoading.value = true;
  try {
    const total = totalItems.value || 0;
    if (!total) {
      selectedIds.value = [];
      return;
    }
    const size = Math.min(total, 500);
    const { data } = await searchStudentProfiles(buildSearchParams(1, size));
    const ids = (data?.items || []).map((item) => item.id);
    const pages = data?.totalPages || 1;
    if (pages > 1) {
      const restPages = [];
      for (let page = 2; page <= pages; page += 1) {
        restPages.push(
          searchStudentProfiles(buildSearchParams(page, size)).then(
            ({ data: pageData }) => pageData?.items || [],
          ),
        );
      }
      const moreItems = (await Promise.all(restPages)).flat();
      ids.push(...moreItems.map((item) => item.id));
    }
    selectedIds.value = Array.from(new Set(ids));
  } finally {
    selectAllLoading.value = false;
  }
}

const IDENTITY_FIELDS = [
  { key: "name", label: "姓名", getter: (item) => item.fullName || item.name || "" },
  { key: "className", label: "班级", getter: (item) => buildClassName(item) || "" },
  { key: "studentNo", label: "学号", getter: (item) => item.studentNo || "" },
];

const IDENTITY_KEYS = IDENTITY_FIELDS.map((field) => field.key);

const MAIN_FIELD_ORDER = [
  "name",
  "className",
  "studentNo",
  "enrollmentDate",
  "studentCategory",
  "classTeacher",
  "counselor",
  "ethnicity",
  "politicalStatus",
  "phone",
  "idNo",
  "nativePlace",
  "address",
  "offCampusLiving",
  "offCampusAddress",
  "dormCampus",
  "dormBuilding",
  "dormRoom",
  "hkMoTw",
  "specialStudent",
  "fatherName",
  "fatherPhone",
  "fatherWorkUnit",
  "fatherTitle",
  "motherName",
  "motherPhone",
  "motherWorkUnit",
  "motherTitle",
  "emergencyPhone",
  "emergencyRelation",
];

const MAIN_FIELD_META = {
  name: { label: "姓名", getter: (item) => item.fullName || item.name || "" },
  className: { label: "班级", getter: (item) => buildClassName(item) || "" },
  studentNo: { label: "学号", getter: (item) => item.studentNo || "" },
  enrollmentDate: { label: "入学时间", getter: (item) => item.enrollmentDate || "" },
  studentCategory: { label: "学生类别", getter: (item) => item.studentCategory || "" },
  classTeacher: { label: "班主任", getter: (item) => item.classTeacher || "" },
  counselor: { label: "辅导员", getter: (item) => item.counselor || "" },
  ethnicity: { label: "民族", getter: (item) => item.ethnicity || "" },
  politicalStatus: { label: "政治面貌", getter: (item) => item.politicalStatus || "" },
  phone: { label: "手机号码", getter: (item) => item.phone || "" },
  idNo: { label: "身份证号", getter: (item) => item.idNo || "" },
  nativePlace: { label: "籍贯", getter: (item) => item.nativePlace || "" },
  address: { label: "住址", getter: (item) => item.address || "" },
  offCampusLiving: {
    label: "是否在外居住",
    getter: (item) => formatYesNo(item.offCampusLiving),
  },
  offCampusAddress: { label: "外居住地址", getter: (item) => item.offCampusAddress || "" },
  dormCampus: { label: "住宿校区", getter: (item) => item.dormCampus || "" },
  dormBuilding: { label: "住宿楼栋", getter: (item) => item.dormBuilding || "" },
  dormRoom: { label: "住宿房间", getter: (item) => item.dormRoom || "" },
  hkMoTw: { label: "港澳台", getter: (item) => formatYesNo(item.hkMoTw) },
  specialStudent: {
    label: "特殊学生",
    getter: (item) => formatYesNo(item.specialStudent),
  },
  fatherName: { label: "父亲姓名", getter: (item) => item.fatherName || "" },
  fatherPhone: { label: "父亲电话", getter: (item) => item.fatherPhone || "" },
  fatherWorkUnit: { label: "父亲工作单位", getter: (item) => item.fatherWorkUnit || "" },
  fatherTitle: { label: "父亲职务", getter: (item) => item.fatherTitle || "" },
  motherName: { label: "母亲姓名", getter: (item) => item.motherName || "" },
  motherPhone: { label: "母亲电话", getter: (item) => item.motherPhone || "" },
  motherWorkUnit: { label: "母亲工作单位", getter: (item) => item.motherWorkUnit || "" },
  motherTitle: { label: "母亲职务", getter: (item) => item.motherTitle || "" },
  emergencyPhone: { label: "紧急联系人电话", getter: (item) => item.emergencyPhone || "" },
  emergencyRelation: {
    label: "紧急联系人关系",
    getter: (item) => item.emergencyRelation || "",
  },
};

const EDUCATION_FIELD_ORDER = [
  "eduPeriod",
  "eduSchoolName",
  "eduEducationLevel",
  "eduWitness",
];

const EDUCATION_FIELD_META = {
  eduPeriod: {
    label: "时间段",
    getter: (item, edu) => {
      const start = edu?.startDate || "";
      const end = edu?.isCurrent ? "至今" : edu?.endDate || "";
      return [start, end].filter(Boolean).join("~");
    },
  },
  eduSchoolName: { label: "学校名称", getter: (item, edu) => edu?.schoolName || "" },
  eduEducationLevel: { label: "学历", getter: (item, edu) => edu?.educationLevel || "" },
  eduWitness: { label: "证明人", getter: (item, edu) => edu?.witness || "" },
};

const PARTY_FIELD_ORDER = [
  "leagueJoined",
  "leagueApplicationDate",
  "leagueJoinDate",
  "leagueNo",
  "partyApplied",
  "applicationDate",
  "activistDate",
  "partyTrainingDate",
  "developmentTargetDate",
  "probationaryMemberDate",
  "fullMemberDate",
];

const PARTY_FIELD_META = {
  leagueJoined: { label: "是否入团", getter: (item) => formatYesNo(item.leagueJoined) },
  leagueApplicationDate: {
    label: "提交入团申请书时间",
    getter: (item) => item.leagueApplicationDate || "",
  },
  leagueJoinDate: {
    label: "入团时间",
    getter: (item) => formatDateOrEmpty(item.leagueJoinDate, item.leagueDeveloping, "正在发展"),
  },
  leagueNo: { label: "团号", getter: (item) => item.leagueNo || "" },
  partyApplied: { label: "是否申请入党", getter: (item) => formatYesNo(item.partyApplied) },
  applicationDate: {
    label: "提交入党申请书时间",
    getter: (item) => item.applicationDate || "",
  },
  activistDate: {
    label: "确定积极分子时间",
    getter: (item) => formatDateOrEmpty(item.activistDate, item.activistDeveloping, "正在发展"),
  },
  partyTrainingDate: {
    label: "上党课时间",
    getter: (item) => formatDateOrEmpty(item.partyTrainingDate, item.partyTrainingPending, "暂未报名"),
  },
  developmentTargetDate: {
    label: "确定发展对象时间",
    getter: (item) =>
      formatDateOrEmpty(item.developmentTargetDate, item.developmentTargetDeveloping, "正在发展"),
  },
  probationaryMemberDate: {
    label: "接收为预备党员时间",
    getter: (item) =>
      formatDateOrEmpty(item.probationaryMemberDate, item.probationaryDeveloping, "正在发展"),
  },
  fullMemberDate: {
    label: "转为正式党员时间",
    getter: (item) => formatDateOrEmpty(item.fullMemberDate, item.fullMemberDeveloping, "正在发展"),
  },
};

function getSelectedExportKeys() {
  return new Set(
    Object.entries(exportSelections)
      .filter(([, value]) => Boolean(value))
      .map(([key]) => key),
  );
}

function openExportDialog() {
  exportDialogOpen.value = true;
}

function closeExportDialog() {
  exportDialogOpen.value = false;
}

function isGroupSelected(group) {
  return group.fields.every((field) => exportSelections[field.key]);
}

const isAllSelected = computed(() =>
  exportGroups.every((group) =>
    group.fields.every((field) => exportSelections[field.key]),
  ),
);

function toggleGroupSelection(group, checked) {
  group.fields.forEach((field) => {
    exportSelections[field.key] = checked;
  });
}

function toggleAllSelections(checked) {
  exportGroups.forEach((group) => {
    group.fields.forEach((field) => {
      exportSelections[field.key] = checked;
    });
  });
}

function shouldIncludeMainSheet(selectedKeys) {
  const hasEdu = EDUCATION_FIELD_ORDER.some((key) => selectedKeys.has(key));
  const hasParty = PARTY_FIELD_ORDER.some((key) => selectedKeys.has(key));
  const hasNonBaseMain = MAIN_FIELD_ORDER.some(
    (key) => !IDENTITY_KEYS.includes(key) && selectedKeys.has(key),
  );
  const hasAnyMain = MAIN_FIELD_ORDER.some((key) => selectedKeys.has(key));
  if (hasEdu || hasParty) {
    return hasNonBaseMain;
  }
  return hasAnyMain;
}

async function confirmExport() {
  const success = await handleExport();
  if (success) {
    closeExportDialog();
  }
}

async function handleExport() {
  if (exporting.value) {
    return false;
  }
  if (!selectedIds.value.length) {
    window.alert("请先选择学生再导出。");
    return false;
  }
  exporting.value = true;
  try {
    const ids = [...selectedIds.value];
    const results = await Promise.all(
      ids.map((id) =>
        getStudentProfileById(id)
          .then(({ data }) => data || null)
          .catch(() => null),
      ),
    );
    const rows = results.filter(Boolean);
    if (!rows.length) {
      window.alert("没有获取到学生详情，请稍后再试。");
      return false;
    }
    const workbook = XLSX.utils.book_new();
    const selectedKeys = getSelectedExportKeys();
    if (!selectedKeys.size) {
      window.alert("请选择至少一个导出字段。");
      return false;
    }
    if (shouldIncludeMainSheet(selectedKeys)) {
      const table = buildStudentTable(rows, selectedKeys);
      if (table) {
        const worksheet = XLSX.utils.aoa_to_sheet(table);
        worksheet["!cols"] = computeColumnWidths(table);
        XLSX.utils.book_append_sheet(workbook, worksheet, "学生");
      }
    }
    const educationTable = buildEducationTable(rows, selectedKeys);
    if (educationTable) {
      const educationSheet = XLSX.utils.aoa_to_sheet(educationTable);
      educationSheet["!cols"] = computeColumnWidths(educationTable);
      XLSX.utils.book_append_sheet(workbook, educationSheet, "教育经历");
    }
    const partyTable = buildPartyTable(rows, selectedKeys);
    if (partyTable) {
      const partySheet = XLSX.utils.aoa_to_sheet(partyTable);
      partySheet["!cols"] = computeColumnWidths(partyTable);
      XLSX.utils.book_append_sheet(workbook, partySheet, "团组织与入党信息");
    }
    XLSX.writeFile(workbook, `students_export_${formatTimestamp()}.xlsx`, {
      compression: true,
    });
  } catch (error) {
    const fallbackRows = selectedStudents.value;
    const csvContent = buildStudentCsv(fallbackRows);
    const blob = new Blob([csvContent], {
      type: "text/csv;charset=utf-8;",
    });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `students_export_${formatTimestamp()}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  } finally {
    exporting.value = false;
  }
  return true;
}

function buildStudentTable(rows, selectedKeys) {
  const activeFields = MAIN_FIELD_ORDER.filter((key) => selectedKeys.has(key));
  if (!activeFields.length) {
    return null;
  }
  const header = activeFields.map((key) => MAIN_FIELD_META[key].label);
  const body = rows.map((item) =>
    activeFields.map((key) => MAIN_FIELD_META[key].getter(item)),
  );
  return [header, ...body];
}

function computeColumnWidths(table) {
  const widths = [];
  table.forEach((row) => {
    row.forEach((cell, index) => {
      const text = cell == null ? "" : String(cell);
      const length = text.length;
      const minWidth = 8;
      const maxWidth = 40;
      const width = Math.min(Math.max(length + 2, minWidth), maxWidth);
      if (!widths[index] || width > widths[index].wch) {
        widths[index] = { wch: width };
      }
    });
  });
  return widths;
}

function buildEducationTable(rows, selectedKeys) {
  const activeIdentity = IDENTITY_FIELDS.filter((field) =>
    selectedKeys.has(field.key),
  );
  const activeFields = EDUCATION_FIELD_ORDER.filter((key) =>
    selectedKeys.has(key),
  );
  if (!activeFields.length) {
    return null;
  }
  const header = [
    ...activeIdentity.map((field) => field.label),
    ...activeFields.map((key) => EDUCATION_FIELD_META[key].label),
  ];
  const lines = [header];
  rows.forEach((item) => {
    const identityValues = activeIdentity.map((field) => field.getter(item));
    const experiences = Array.isArray(item.educationExperiences)
      ? item.educationExperiences.filter(Boolean)
      : [];
    if (!experiences.length) {
      lines.push([...identityValues, ...activeFields.map(() => "")]);
      return;
    }
    experiences.forEach((edu) => {
      lines.push([
        ...identityValues,
        ...activeFields.map((key) =>
          EDUCATION_FIELD_META[key].getter(item, edu),
        ),
      ]);
    });
  });
  return lines;
}

function buildPartyTable(rows, selectedKeys) {
  const activeIdentity = IDENTITY_FIELDS.filter((field) =>
    selectedKeys.has(field.key),
  );
  const activeFields = PARTY_FIELD_ORDER.filter((key) =>
    selectedKeys.has(key),
  );
  if (!activeFields.length) {
    return null;
  }
  const header = [
    ...activeIdentity.map((field) => field.label),
    ...activeFields.map((key) => PARTY_FIELD_META[key].label),
  ];
  const lines = [header];
  rows.forEach((item) => {
    lines.push([
      ...activeIdentity.map((field) => field.getter(item)),
      ...activeFields.map((key) => PARTY_FIELD_META[key].getter(item)),
    ]);
  });
  return lines;
}

function buildStudentCsv(rows) {
  const header = ["姓名", "年级", "学院", "专业", "班级", "学号"];
  const lines = [header.map(escapeCsvCell).join(",")];
  rows.forEach((item) => {
    const values = [
      item.name || "",
      item.gradeYear || "",
      item.college || "",
      item.major || "",
      item.classNo || "",
      item.studentNo || "",
    ];
    lines.push(values.map(escapeCsvCell).join(","));
  });
  return `\ufeff${lines.join("\n")}`;
}

function escapeCsvCell(value) {
  const text = value == null ? "" : String(value);
  if (/[",\n]/.test(text)) {
    return `"${text.replace(/"/g, '""')}"`;
  }
  return text;
}

function formatYesNo(value) {
  if (value === true) {
    return "是";
  }
  if (value === false) {
    return "否";
  }
  return "";
}

function formatEducationText(items) {
  if (!Array.isArray(items) || items.length === 0) {
    return "";
  }
  return items
    .filter(Boolean)
    .map((item) => {
      const start = item.startDate || "";
      const end = item.isCurrent ? "至今" : item.endDate || "";
      const period = [start, end].filter(Boolean).join("~");
      const parts = [
        period,
        item.schoolName || "",
        item.educationLevel || "",
        item.witness || "",
      ].filter(Boolean);
      return parts.join(" / ");
    })
    .filter(Boolean)
    .join(" | ");
}

function formatTimestamp() {
  const now = new Date();
  const pad = (value) => String(value).padStart(2, "0");
  const yyyy = now.getFullYear();
  const mm = pad(now.getMonth() + 1);
  const dd = pad(now.getDate());
  const hh = pad(now.getHours());
  const min = pad(now.getMinutes());
  const ss = pad(now.getSeconds());
  return `${yyyy}${mm}${dd}_${hh}${min}${ss}`;
}

const roleLabelMap = {
  STUDENT: "学生",
  TEACHER: "教师",
  ADMIN: "管理员",
};

const roleLabel = computed(() => roleLabelMap[profile.role] || "学生");
const avatarText = computed(() => {
  const name = profile.displayName || profile.username || "同学";
  return name.slice(0, 1).toUpperCase();
});

function handleMenuClick(key) {
  if (!isMenuEnabled(key)) {
    return;
  }
  if (key === "achievements") {
    router.push({ path: "/achievements", query: { category: "all" } });
    return;
  }
  if (key === "my-info") {
    router.push("/myinfos");
    return;
  }
  if (key === "student-info") {
    router.push("/student-info");
    return;
  }
  router.push("/myinfos");
}

function resolveMediaUrl(url) {
  if (!url) {
    return "";
  }
  if (url.startsWith("http://") || url.startsWith("https://")) {
    return url;
  }
  return `${API_BASE}${url}`;
}

function goToSettings() {
  router.push("/settings");
}

function loadUser() {
  try {
    const raw = JSON.parse(localStorage.getItem("gcsc_user") || "{}");
    return {
      username: raw.username || "",
      displayName: raw.displayName || "",
      avatarUrl: raw.avatarUrl || "",
      role: raw.role || "STUDENT",
      studentNo: raw.studentNo || "",
      className: raw.className || "",
      college: raw.college || "",
    };
  } catch {
    return {
      username: "",
      displayName: "",
      avatarUrl: "",
      role: "STUDENT",
      studentNo: "",
      className: "",
      college: "",
    };
  }
}
</script>

<style scoped>
.student-filter-card {
  gap: 18px;
  position: relative;
  z-index: 40;
}

.student-right-stack {
  display: grid;
  gap: 14px;
}

.student-filter-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.student-search {
  min-width: 220px;
}

.student-filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
}

.student-filter-row {
  display: grid;
  gap: 10px;
}

.student-filter-two {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.student-filter-field {
  display: grid;
  gap: 8px;
}

.student-stepper {
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr) 36px;
  gap: 10px;
  align-items: center;
}

.stepper-button {
  height: 38px;
  border-radius: 12px;
  border: 1px solid rgba(3, 107, 114, 0.25);
  background: rgba(255, 255, 255, 0.6);
  color: #0f4d55;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.stepper-button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.stepper-input {
  text-align: center;
}

.student-filter-inline {
  display: flex;
  gap: 18px;
  align-items: center;
  grid-column: 1 / -1;
}

.student-results-card {
  gap: 16px;
  position: relative;
  z-index: 1;
}

.student-results-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.student-results-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.student-list {
  display: grid;
  gap: 10px;
}

.student-row {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  align-items: center;
  padding: 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(3, 107, 114, 0.1);
}

.student-main {
  display: grid;
  gap: 4px;
}

.student-name {
  font-weight: 700;
  color: #0f4d55;
}

.student-meta {
  font-size: 12px;
  color: #5c7178;
}

.student-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.student-pages {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.page-button {
  border-radius: 999px;
  border: 1px solid rgba(3, 107, 114, 0.25);
  background: rgba(255, 255, 255, 0.7);
  color: #0f4d55;
  height: 32px;
  padding: 0 12px;
  cursor: pointer;
  font-weight: 600;
}

.page-button.active {
  background: rgba(205, 255, 249, 0.9);
  border-color: rgba(3, 107, 114, 0.45);
}

.page-input {
  width: 90px;
  height: 32px;
}

.student-detail-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(3, 18, 20, 0.35);
  backdrop-filter: blur(8px);
  z-index: 60;
}

.student-detail-view {
  position: fixed;
  left: 50%;
  bottom: 16px;
  width: min(980px, calc(100vw - 32px));
  transform: translate(-50%, 120%) scale(0.98);
  opacity: 0;
  filter: blur(6px);
  pointer-events: none;
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: linear-gradient(
    140deg,
    rgba(205, 255, 249, 0.92),
    rgba(197, 217, 226, 0.78)
  );
  box-shadow: 0 28px 70px rgba(3, 107, 114, 0.22);
  backdrop-filter: blur(12px);
  z-index: 70;
  transition:
    transform 0.9s cubic-bezier(0.22, 1, 0.36, 1),
    opacity 0.75s ease,
    filter 0.75s ease,
    left 0.75s cubic-bezier(0.22, 1, 0.36, 1),
    width 0.75s cubic-bezier(0.22, 1, 0.36, 1);
  max-height: 84vh;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: auto;
  scrollbar-width: none;
  padding: 12px 14px 16px;
}

.student-detail-view.split {
  left: 16px;
  width: min(720px, calc(50vw - 24px));
  transform: translate(0, 120%) scale(0.98);
}

.student-detail-view.open {
  transform: translate(-50%, 0) scale(1);
  opacity: 1;
  filter: blur(0px);
  pointer-events: auto;
}

.student-detail-view.split.open {
  transform: translate(0, 0) scale(1);
}

.student-detail-view.closing {
  transform: translate(-50%, 120%) scale(0.98);
  opacity: 0;
  filter: blur(6px);
}

.student-detail-view.split.closing {
  transform: translate(0, 120%) scale(0.98);
}

.student-detail-view::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.export-dialog-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(3, 18, 20, 0.35);
  backdrop-filter: blur(8px);
  z-index: 80;
}

.export-dialog {
  position: fixed;
  left: 50%;
  bottom: 16px;
  width: min(860px, calc(100vw - 32px));
  transform: translate(-50%, 120%) scale(0.98);
  opacity: 0;
  pointer-events: none;
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  background: linear-gradient(
    140deg,
    rgba(205, 255, 249, 0.96),
    rgba(197, 217, 226, 0.85)
  );
  box-shadow: 0 26px 60px rgba(3, 107, 114, 0.22);
  backdrop-filter: blur(12px);
  z-index: 90;
  transition:
    transform 0.8s cubic-bezier(0.22, 1, 0.36, 1),
    opacity 0.6s ease;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.export-dialog.open {
  transform: translate(-50%, 0) scale(1);
  opacity: 1;
  pointer-events: auto;
}

.export-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px 10px;
  border-bottom: 1px solid rgba(3, 107, 114, 0.12);
}

.export-dialog-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f4d55;
  display: inline-flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.export-all-toggle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #2d5d64;
}

.export-dialog-body {
  padding: 14px 18px 8px;
  overflow: auto;
  display: grid;
  gap: 14px;
}

.export-group {
  border-radius: 14px;
  border: 1px solid rgba(3, 107, 114, 0.12);
  background: rgba(255, 255, 255, 0.6);
  padding: 12px;
  display: grid;
  gap: 10px;
}

.export-group-title {
  font-size: 14px;
  font-weight: 700;
  color: #0f4d55;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.export-group-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
}

.export-option-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  width: 100%;
}

.export-option {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #2d5d64;
}

.export-dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 18px 16px;
  border-top: 1px solid rgba(3, 107, 114, 0.12);
  align-items: center;
}

.export-dialog-actions .ghost-button,
.export-dialog-actions .export-confirm {
  width: auto;
  min-width: 110px;
  height: 40px;
}

.export-confirm {
  padding: 0 22px;
}

.student-detail-body {
  display: grid;
  gap: 14px;
}

.info-static {
  display: flex;
  align-items: center;
}

.info-actions-single {
  grid-template-columns: minmax(0, 1fr);
}

.student-achievements-view {
  position: fixed;
  right: 16px;
  bottom: 16px;
  width: min(720px, calc(50vw - 24px));
  height: 84vh;
  transform: translate(0, 120%) scale(0.98);
  opacity: 0;
  filter: blur(6px);
  pointer-events: none;
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: linear-gradient(
    140deg,
    rgba(205, 255, 249, 0.92),
    rgba(197, 217, 226, 0.78)
  );
  box-shadow: 0 28px 70px rgba(3, 107, 114, 0.22);
  backdrop-filter: blur(12px);
  z-index: 72;
  transition:
    transform 0.9s cubic-bezier(0.22, 1, 0.36, 1),
    opacity 0.75s ease,
    filter 0.75s ease;
  max-height: 84vh;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
  padding: 12px 14px 16px;
}

.student-achievements-view.open {
  transform: translate(0, 0) scale(1);
  opacity: 1;
  filter: blur(0px);
  pointer-events: auto;
}

.student-achievements-view.closing {
  transform: translate(0, 120%) scale(0.98);
  opacity: 0;
  filter: blur(6px);
}

.student-achievements-body {
  flex: 1;
  min-height: 0;
  display: flex;
}

.student-achievements-frame {
  border: none;
  width: 100%;
  height: 100%;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.92);
}

@media (max-width: 1100px) {
  .student-detail-view.split {
    left: 50%;
    width: min(980px, calc(100vw - 32px));
    transform: translate(-50%, 120%) scale(0.98);
  }

  .student-detail-view.split.open {
    transform: translate(-50%, 0) scale(1);
  }

  .student-detail-view.split.closing {
    transform: translate(-50%, 120%) scale(0.98);
  }

  .student-achievements-view {
    left: 50%;
    right: auto;
    width: min(980px, calc(100vw - 32px));
    height: 84vh;
    transform: translate(-50%, 120%) scale(0.98);
  }

  .student-achievements-view.open {
    transform: translate(-50%, 0) scale(1);
  }

  .student-achievements-view.closing {
    transform: translate(-50%, 120%) scale(0.98);
  }
}
</style>
