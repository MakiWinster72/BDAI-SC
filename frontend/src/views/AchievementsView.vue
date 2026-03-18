<template>
  <div class="dashboard-layout">
    <transition name="publisher-backdrop">
      <div
        v-if="sidebarOpen"
        class="mobile-sidebar-backdrop"
        @click="closeSidebar"
      ></div>
    </transition>
    <aside class="dashboard-left" :class="{ open: sidebarOpen }">
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
        <template v-for="item in menuItems" :key="item.key">
          <div
            v-if="item.key === 'achievements'"
            class="menu-drawer"
            :class="{ open: achievementsOpen }"
          >
            <button
              class="menu-item menu-drawer-trigger"
              :class="{
                active: activeMenu === item.key,
                disabled: !isMenuEnabled(item.key),
              }"
              type="button"
              :disabled="!isMenuEnabled(item.key)"
              @click="toggleAchievements"
            >
              <span>{{ item.label }}</span>
              <span class="menu-drawer-caret" aria-hidden="true"></span>
            </button>
            <transition name="menu-drawer-panel">
              <div v-show="achievementsOpen" class="menu-drawer-panel">
                <div
                  class="menu-drawer-indicator"
                  :style="drawerIndicatorStyle"
                  aria-hidden="true"
                ></div>
                <button
                  v-for="entry in achievementEntries"
                  :key="entry.key"
                  class="menu-drawer-item"
                  :class="{ active: activeCategory === entry.key }"
                  type="button"
                  @click="handleAchievementEntry(entry.key)"
                >
                  {{ entry.label }}
                </button>
              </div>
            </transition>
          </div>
          <button
            v-else
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
        </template>
      </section>
    </aside>

    <main class="dashboard-right">
      <header class="feed-header">
        <h1 class="feed-title">个人成就</h1>
      </header>

      <section
        v-if="activeCategory === 'all'"
        class="info-card achievement-intro-card"
      >
        <div class="info-section-title">全部</div>
        <p class="achievement-intro-text">这里存放了我所有的成就！</p>
      </section>

      <div v-if="!filteredAchievements.length" class="empty-tip">
        {{ emptyMessage }}
      </div>
      <div v-if="errorMessage" class="form-tip">{{ errorMessage }}</div>

      <section class="achievement-list">
        <article
          v-for="item in filteredAchievements"
          :key="item.id"
          class="achievement-card"
          :class="{
            'achievement-card-paper': item.category === 'paper',
            'achievement-card-journal': item.category === 'journal',
            'achievement-card-patent': item.category === 'patent',
            'achievement-card-certificate': item.category === 'certificate',
            'achievement-card-research': item.category === 'research',
            'achievement-card-works': item.category === 'works',
          }"
          @click="openDetail(item)"
        >
          <div class="achievement-card-image">
            <img v-if="item.image" :src="item.image" alt="成就图片" />
            <div v-else class="achievement-card-placeholder">未上传图片</div>
          </div>
          <div v-if="item.category === 'contest'" class="achievement-card-body">
            <div class="achievement-card-title-row">
              <div class="achievement-card-title">{{ item.title || "-" }}</div>
              <span
                v-if="formatContestAwardPill(item.fields)"
                class="achievement-award-pill"
              >
                {{ formatContestAwardPill(item.fields) }}
              </span>
            </div>
            <div class="achievement-card-date-italic">
              {{ formatContestDate(item.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatContestCategoryLine(item.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatContestMembers(item.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatContestAwardLine(item.fields) }}
            </div>
            <div class="achievement-card-organizer">
              {{ formatContestOrganizer(item.fields) }}
            </div>
          </div>
          <div v-else-if="item.category === 'paper'" class="achievement-card-body">
            <div class="achievement-card-title achievement-paper-title">
              {{ item.title || "-" }}
            </div>
            <div class="achievement-card-text">
              {{ formatPaperJournal(item.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatPaperAuthors(item.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatPaperDate(item.fields) }}
            </div>
            <div class="achievement-paper-tag" aria-hidden="true">学术论文</div>
          </div>
          <div v-else-if="item.category === 'journal'" class="achievement-card-body">
            <div class="achievement-card-title achievement-paper-title">
              {{ item.title || "-" }}
            </div>
            <div class="achievement-card-text">
              {{ formatJournalPublication(item.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatJournalAuthor(item.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatJournalDate(item.fields) }}
            </div>
            <div class="achievement-journal-tag" aria-hidden="true">期刊作品</div>
          </div>
          <div v-else-if="item.category === 'patent'" class="achievement-card-body">
            <div class="achievement-card-title achievement-paper-title">
              {{ item.title || "-" }}
            </div>
            <div class="achievement-card-text">
              {{ formatPatentGrantNo(item.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatPatentInventor(item.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatPatentDate(item.fields) }}
            </div>
            <div class="achievement-patent-tag" aria-hidden="true">
              {{ formatPatentTag(item.fields) }}
            </div>
          </div>
          <div
            v-else-if="item.category === 'certificate'"
            class="achievement-card-body"
          >
            <div class="achievement-card-title achievement-paper-title">
              {{ item.title || "-" }}
            </div>
            <div class="achievement-card-text">
              {{ formatCertificateType(item.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatCertificateOwner(item.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatCertificateDate(item.fields) }}
            </div>
            <div class="achievement-certificate-tag" aria-hidden="true">
              职业资格证书
            </div>
          </div>
          <div v-else-if="item.category === 'research'" class="achievement-card-body">
            <div class="achievement-card-title achievement-paper-title">
              {{ item.title || "-" }}
            </div>
            <div class="achievement-card-text">
              {{ formatResearchLeader(item.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatResearchTeacher(item.fields) }}
            </div>
            <div class="achievement-research-tag" aria-hidden="true">科研项目</div>
          </div>
          <div v-else-if="item.category === 'works'" class="achievement-card-body">
            <div class="achievement-card-title achievement-paper-title">
              {{ item.title || "-" }}
            </div>
            <div class="achievement-card-text">
              {{ formatWorksCategory(item.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatWorksOccasion(item.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatWorksDate(item.fields) }}
            </div>
            <div class="achievement-works-tag" aria-hidden="true">
              {{ formatWorksTag(item.fields) }}
            </div>
            <div class="achievement-card-organizer">
              {{ formatWorksOrganizer(item.fields) }}
            </div>
          </div>
          <div v-else class="achievement-card-body">
            <div class="achievement-card-title">{{ item.title || "-" }}</div>
            <div v-if="item.dateLabel" class="achievement-card-dates">
              <span>{{ item.dateLabel }}：{{ item.dateValue || "-" }}</span>
            </div>
            <div
              v-for="line in item.previewFields"
              :key="line"
              class="achievement-card-text"
            >
              {{ line }}
            </div>
          </div>
        </article>
      </section>

      <button
        class="achievement-add"
        type="button"
        :aria-label="addButtonLabel"
        @click="openEditorForCategory"
      >
        <span aria-hidden="true">+</span>
      </button>
      <div class="mobile-capsule">
        <div class="capsule-left">
          <div
            class="capsule-action"
            role="button"
            tabindex="0"
            @click="openSidebar"
          >
            <span class="capsule-icon" aria-hidden="true">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </span>
          </div>
        </div>
        <div class="capsule-right">
          <div
            class="capsule-action capsule-primary"
            role="button"
            tabindex="0"
            :aria-label="addButtonLabel"
            @click="openEditorForCategory"
          >
            +
          </div>
        </div>
      </div>

      <transition name="publisher-backdrop">
        <div
          v-if="viewOpen"
          class="achievement-backdrop"
          @click="closeView"
        ></div>
      </transition>
      <section
        class="achievement-view"
        :class="{ open: viewOpen, closing: viewClosing, 'exit-up': viewExitUp }"
        :aria-hidden="!viewOpen"
      >
        <header class="publisher-header">
          <div class="publisher-title">成就查看</div>
          <button class="publisher-close" type="button" @click="closeView">
            关闭
          </button>
        </header>
        <div v-if="viewLoading" class="empty-tip">加载中...</div>
        <div v-else-if="viewItem" class="achievement-view-body">
          <div class="achievement-detail-image">
            <img v-if="viewItem.image" :src="viewItem.image" alt="成就图片" />
            <div v-else class="achievement-card-placeholder">未上传图片</div>
          </div>
          <div class="achievement-detail-body">
            <div v-if="viewItem.category === 'contest'">
              <div class="achievement-card-title-row">
                <div class="achievement-card-title">{{ viewItem.title || "-" }}</div>
                <span
                  v-if="formatContestAwardPill(viewItem.fields)"
                  class="achievement-award-pill"
                >
                  {{ formatContestAwardPill(viewItem.fields) }}
                </span>
              </div>
              <div class="achievement-card-date-italic">
                {{ formatContestDate(viewItem.fields) }}
              </div>
              <div class="achievement-card-text">
                {{ formatContestStudentNo(viewItem.fields) }}
              </div>
              <div class="achievement-card-text">
                {{ formatContestCategoryLine(viewItem.fields) }}
              </div>
              <div class="achievement-card-text">
                {{ formatContestMembers(viewItem.fields) }}
              </div>
              <div class="achievement-card-text">
                {{ formatContestAwardLine(viewItem.fields) }}
              </div>
              <div class="achievement-card-organizer">
                {{ formatContestOrganizer(viewItem.fields) }}
              </div>
            </div>
            <div v-else-if="viewItem.category === 'paper'">
              <div class="achievement-card-title achievement-paper-title">
                {{ viewItem.title || "-" }}
              </div>
              <div class="achievement-card-text">
                {{ formatPaperJournal(viewItem.fields) }}
              </div>
              <div class="achievement-card-text">
                {{ formatPaperAuthors(viewItem.fields) }}
              </div>
              <div class="achievement-card-text">
                {{ formatPaperDate(viewItem.fields) }}
              </div>
          </div>
          <div v-else-if="viewItem.category === 'journal'">
            <div class="achievement-card-title achievement-paper-title">
              {{ viewItem.title || "-" }}
            </div>
            <div class="achievement-card-text">
              {{ formatJournalPublication(viewItem.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatJournalAuthor(viewItem.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatJournalDate(viewItem.fields) }}
            </div>
          </div>
          <div v-else-if="viewItem.category === 'patent'">
            <div class="achievement-card-title achievement-paper-title">
              {{ viewItem.title || "-" }}
            </div>
            <div class="achievement-card-text">
              {{ formatPatentGrantNo(viewItem.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatPatentInventor(viewItem.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatPatentDate(viewItem.fields) }}
            </div>
          </div>
          <div v-else-if="viewItem.category === 'certificate'">
            <div class="achievement-card-title achievement-paper-title">
              {{ viewItem.title || "-" }}
            </div>
            <div class="achievement-card-text">
              {{ formatCertificateType(viewItem.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatCertificateOwner(viewItem.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatCertificateDate(viewItem.fields) }}
            </div>
          </div>
          <div v-else-if="viewItem.category === 'research'">
            <div class="achievement-card-title achievement-paper-title">
              {{ viewItem.title || "-" }}
            </div>
            <div class="achievement-card-text">
              {{ formatResearchLeader(viewItem.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatResearchTeacher(viewItem.fields) }}
            </div>
          </div>
          <div v-else-if="viewItem.category === 'works'">
            <div class="achievement-card-title achievement-paper-title">
              {{ viewItem.title || "-" }}
            </div>
            <div class="achievement-card-text">
              {{ formatWorksCategory(viewItem.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatWorksOccasion(viewItem.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatWorksDate(viewItem.fields) }}
            </div>
            <div class="achievement-card-organizer">
              {{ formatWorksOrganizer(viewItem.fields) }}
            </div>
          </div>
            <div v-else>
              <div class="achievement-card-title">{{ viewItem.title || "-" }}</div>
              <div v-if="viewItem.dateLabel" class="achievement-card-dates">
                <span>{{ viewItem.dateLabel }}：{{ viewItem.dateValue || "-" }}</span>
              </div>
              <div
                v-for="line in viewItem.fieldLines"
                :key="line"
                class="achievement-card-text"
              >
                {{ line }}
              </div>
            </div>
            <div class="achievement-card-actions">
              <button class="post-action" type="button" @click="editFromView">
                编辑
              </button>
              <button
                class="post-action danger"
                type="button"
                @click="openDelete"
              >
                删除
              </button>
            </div>
          </div>
        </div>
      </section>

      <transition name="publisher-backdrop">
        <div
          v-if="editorOpen"
          class="achievement-backdrop"
          @click="closeEditor"
        ></div>
      </transition>
      <section
        class="achievement-sheet"
        :class="{ open: editorOpen }"
        :aria-hidden="!editorOpen"
      >
        <header class="publisher-header">
          <div class="publisher-title">
            {{ editId ? "编辑成就" : "新增成就" }}
          </div>
          <button class="publisher-close" type="button" @click="closeEditor">
            关闭
          </button>
        </header>
        <div class="achievement-grid">
          <button class="achievement-image" type="button" @click="triggerImage">
            <img v-if="imagePreview" :src="imagePreview" alt="成就图片" />
            <span v-else>选择图片</span>
          </button>

          <div class="achievement-fields">
            <div class="field-row">
              <label class="field-label">成就分类</label>
              <select v-model="form.category" class="form-input">
                <option disabled value="">请选择分类</option>
                <option
                  v-for="entry in categoryOptions"
                  :key="entry.key"
                  :value="entry.key"
                >
                  {{ entry.label }}
                </option>
              </select>
            </div>
            <div v-if="activeCategoryHint" class="achievement-hint">
              <ol class="achievement-hint-list">
                <li v-for="item in activeCategoryHint.notes" :key="item">
                  {{ item }}
                </li>
              </ol>
            </div>
            <div v-if="!activeFormConfig" class="empty-tip">
              请选择成就分类后填写对应信息。
            </div>
            <div v-else class="achievement-dynamic-fields">
              <div
                v-for="field in activeFormConfig.fields"
                :key="field.key"
                class="field-row"
              >
                <label class="field-label">{{ field.label }}</label>
                <input
                  v-if="field.kind === 'input'"
                  v-model="form.fields[field.key]"
                  class="form-input"
                  :type="field.type || 'text'"
                  :placeholder="field.placeholder || ''"
                />
                <textarea
                  v-else
                  v-model="form.fields[field.key]"
                  class="publisher-input"
                  :rows="field.rows || 2"
                  :placeholder="field.placeholder || ''"
                ></textarea>
              </div>
            </div>

            <div class="achievement-actions">
              <button class="ghost-button" type="button" @click="closeEditor">
                取消
              </button>
              <button
                class="action-button"
                type="button"
                @click="saveAchievement"
              >
                {{ editId ? "保存修改" : "保存成就" }}
              </button>
            </div>
          </div>
        </div>
      </section>

      <transition name="dialog-fade">
        <div
          v-if="deleteDialogOpen"
          class="dialog-backdrop"
          @click="closeDelete"
        ></div>
      </transition>
      <transition name="dialog-pop">
        <section v-if="deleteDialogOpen" class="dialog-card" @click.stop>
          <header class="dialog-header">确认删除</header>
          <div class="dialog-body">删除后无法恢复，确定要删除这条动态吗？</div>
          <div class="dialog-actions">
            <button class="ghost-button" type="button" @click="closeDelete">
              取消
            </button>
            <button
              class="action-button"
              type="button"
              :disabled="deleteBusy"
              @click="confirmDelete"
            >
              确定删除
            </button>
          </div>
        </section>
      </transition>

      <input
        ref="imageInput"
        type="file"
        accept="image/*"
        hidden
        @change="onImageChange"
      />
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { filterMenuItemsByRole, isMenuEnabled } from "../constants/menu";
import {
  createAchievement,
  deleteAchievement,
  getAchievements,
  updateAchievement,
} from "../api/achievements";
import { uploadMedia } from "../api/upload";
import { API_BASE } from "../api/request";

const router = useRouter();
const route = useRoute();
const profile = reactive(loadUser());
const activeMenu = ref("achievements");
const editorOpen = ref(false);
const imageInput = ref(null);
const imagePreview = ref("");
const errorMessage = ref("");
const viewOpen = ref(false);
const viewLoading = ref(false);
const viewItem = ref(null);
const viewClosing = ref(false);
const editId = ref(null);
const deleteDialogOpen = ref(false);
const deleteBusy = ref(false);
const sidebarOpen = ref(false);
const achievementsOpen = ref(true);
const activeCategory = ref("all");

const achievements = ref([]);

const achievementEntries = [
  { key: "all", label: "全部" },
  { key: "contest", label: "学科竞赛、文体艺术" },
  { key: "paper", label: "发表学术论文" },
  { key: "journal", label: "发表期刊作品" },
  { key: "patent", label: "专利(著作权)授权数(项)" },
  { key: "certificate", label: "职业资格证书" },
  { key: "research", label: "学生参与教师科研项目情况" },
  { key: "works", label: "创作、表演的代表性作品" },
];

const activeCategoryIndex = computed(() => {
  const index = achievementEntries.findIndex(
    (entry) => entry.key === activeCategory.value,
  );
  return index === -1 ? 0 : index;
});

const drawerIndicatorStyle = computed(() => ({
  transform: `translateY(calc(${activeCategoryIndex.value} * (var(--drawer-item-height) + var(--drawer-item-gap))))`,
}));

const form = reactive({
  imageUrl: "",
  category: "",
  fields: {},
});

const menuItems = computed(() => filterMenuItemsByRole(profile.role));
const categoryOptions = computed(() =>
  achievementEntries.filter((entry) => entry.key !== "all"),
);
const categoryFieldMap = {
  contest: {
    titleKey: "contestName",
    dateKey: "awardDate",
    noteKey: "remark",
    fields: [
      {
        key: "studentNo",
        label: "学号",
        kind: "input",
        placeholder: "请输入学号",
      },
      {
        key: "studentName",
        label: "学生姓名",
        kind: "input",
        placeholder: "请输入学生姓名",
      },
      {
        key: "contestName",
        label: "竞赛名称",
        kind: "input",
        placeholder: "请输入竞赛名称",
      },
      {
        key: "organizer",
        label: "主办单位",
        kind: "input",
        placeholder: "请输入主办单位",
      },
      {
        key: "contestCategory",
        label: "竞赛类别",
        kind: "input",
        placeholder: "国家级/省部级/校级",
      },
      {
        key: "awardCategory",
        label: "获奖类别",
        kind: "input",
        placeholder: "国家级/省部级/校级",
      },
      {
        key: "awardLevel",
        label: "获奖等级",
        kind: "input",
        placeholder: "特等奖/一等奖/二等奖/三等奖",
      },
      {
        key: "contestType",
        label: "竞赛类型",
        kind: "input",
        placeholder: "互联网+/挑战杯/创青春/其他",
      },
      { key: "awardDate", label: "获奖时间", kind: "input", type: "date" },
      {
        key: "awardCount",
        label: "获奖人数",
        kind: "input",
        placeholder: "请输入获奖人数",
      },
      {
        key: "teamMembers",
        label: "团队其他成员",
        kind: "input",
        placeholder: "按证书顺序填写",
      },
      {
        key: "instructors",
        label: "指导老师",
        kind: "input",
        placeholder: "可填写多名老师",
      },
      { key: "remark", label: "备注", kind: "input", placeholder: "团体/个人" },
    ],
  },
  paper: {
    titleKey: "paperTitle",
    dateKey: "publishDate",
    fields: [
      {
        key: "studentNo",
        label: "学号",
        kind: "input",
        placeholder: "请输入学号",
      },
      {
        key: "studentName",
        label: "学生姓名",
        kind: "input",
        placeholder: "请输入学生姓名",
      },
      {
        key: "paperTitle",
        label: "论文名称",
        kind: "input",
        placeholder: "请输入论文名称",
      },
      {
        key: "journalName",
        label: "发表期刊",
        kind: "input",
        placeholder: "请输入期刊名称",
      },
      { key: "publishDate", label: "发表时间", kind: "input", type: "date" },
      {
        key: "authorOrder",
        label: "作者排序",
        kind: "input",
        placeholder: "如：第一作者",
      },
      {
        key: "indexed",
        label: "收录情况",
        kind: "input",
        placeholder: "如：EI/SCI/北大核心",
      },
    ],
  },
  journal: {
    titleKey: "workTitle",
    dateKey: "publishDate",
    fields: [
      {
        key: "studentNo",
        label: "学号",
        kind: "input",
        placeholder: "请输入学号",
      },
      {
        key: "studentName",
        label: "学生姓名",
        kind: "input",
        placeholder: "请输入学生姓名",
      },
      {
        key: "workTitle",
        label: "作品名称",
        kind: "input",
        placeholder: "请输入作品名称",
      },
      {
        key: "publicationName",
        label: "发表刊物名称",
        kind: "input",
        placeholder: "请输入刊物名称",
      },
      { key: "publishDate", label: "发表时间", kind: "input", type: "date" },
    ],
  },
  patent: {
    titleKey: "patentName",
    dateKey: "grantDate",
    fields: [
      {
        key: "studentNo",
        label: "学号",
        kind: "input",
        placeholder: "请输入学号",
      },
      {
        key: "studentName",
        label: "学生姓名",
        kind: "input",
        placeholder: "请输入学生姓名",
      },
      {
        key: "patentName",
        label: "名称",
        kind: "input",
        placeholder: "请输入专利/著作权名称",
      },
      {
        key: "patentType",
        label: "类别",
        kind: "input",
        placeholder: "发明/实用新型/外观设计/软件著作权",
      },
      {
        key: "grantNo",
        label: "授权号",
        kind: "input",
        placeholder: "请输入授权号",
      },
      { key: "grantDate", label: "获批时间", kind: "input", type: "date" },
      {
        key: "firstInventor",
        label: "是否第一发明人",
        kind: "input",
        placeholder: "是/否",
      },
    ],
  },
  certificate: {
    titleKey: "certificateName",
    dateKey: "obtainDate",
    fields: [
      {
        key: "studentNo",
        label: "学号",
        kind: "input",
        placeholder: "请输入学号",
      },
      {
        key: "studentName",
        label: "学生姓名",
        kind: "input",
        placeholder: "请输入学生姓名",
      },
      {
        key: "certificateType",
        label: "证书类别",
        kind: "input",
        placeholder: "专业技术人员/技能人员",
      },
      {
        key: "certificateName",
        label: "证书名称",
        kind: "input",
        placeholder: "请输入证书名称",
      },
      { key: "obtainDate", label: "获得时间", kind: "input", type: "date" },
    ],
  },
  research: {
    titleKey: "projectName",
    fields: [
      {
        key: "studentNo",
        label: "学号",
        kind: "input",
        placeholder: "请输入学号",
      },
      {
        key: "studentName",
        label: "学生姓名",
        kind: "input",
        placeholder: "请输入学生姓名",
      },
      {
        key: "projectName",
        label: "参与科研项目名称",
        kind: "input",
        placeholder: "请输入项目名称",
      },
      {
        key: "teacherNo",
        label: "教师工号",
        kind: "input",
        placeholder: "请输入教师工号",
      },
      {
        key: "projectLeader",
        label: "项目负责人",
        kind: "input",
        placeholder: "请输入负责人姓名",
      },
    ],
  },
  works: {
    titleKey: "workName",
    dateKey: "publishDate",
    noteKey: "note",
    fields: [
      {
        key: "studentNo",
        label: "学号",
        kind: "input",
        placeholder: "请输入学号",
      },
      {
        key: "studentName",
        label: "学生姓名",
        kind: "input",
        placeholder: "请输入学生姓名",
      },
      {
        key: "workName",
        label: "作品名称",
        kind: "input",
        placeholder: "请输入作品名称",
      },
      {
        key: "workCategory",
        label: "类别",
        kind: "input",
        placeholder: "理论类/创作类/表演类",
      },
      {
        key: "workType",
        label: "类型",
        kind: "input",
        placeholder: "大型/中型/小型作品",
      },
      { key: "publishDate", label: "发布时间", kind: "input", type: "date" },
      {
        key: "publishOccasion",
        label: "发布场合",
        kind: "input",
        placeholder: "请输入发布场合",
      },
      {
        key: "organizer",
        label: "主办单位",
        kind: "input",
        placeholder: "请输入主办单位",
      },
      {
        key: "impactScope",
        label: "影响范围",
        kind: "input",
        placeholder: "全国/区域/省内",
      },
      {
        key: "note",
        label: "说明",
        kind: "textarea",
        rows: 2,
        placeholder: "补充说明",
      },
    ],
  },
};
const categoryHints = {
  contest: {
    notes: [
      "学科竞赛获奖指本科生校级及以上竞赛类活动获奖。其中省级以上统计范围为：全国大学生电子设计竞赛、全国大学生电子设计竞赛嵌入式专题竞赛、全国大学生数学建模竞赛、全国大学生广告艺术设计大赛、全国大学生英语竞赛、全国大学生英语演讲竞赛、全国大学生化学实验竞赛、全国大学生电子商务竞赛、全国大学生机械创新设计大赛、全国周培源大学生力学竞赛、全国大学生结构设计竞赛、“挑战杯”全国大学生科技作品竞赛、“挑战杯”全国大学生创业计划大赛、美国数学模型竞赛（MCM）、美国大学生程序设计竞赛（ACM）、国际大学生机械设计竞赛、全国临床技能大赛及其他具有全球影响和全国影响的比赛等。",
      "文体艺术获奖指本科生在国内外及省、部级等文艺、体育竞赛中获得的奖项数。切勿与前项学科竞赛获奖重复。",
      "“竞赛名称”填写须完整、规范，如：第十三届“挑战杯”中国大学生创业计划竞赛，第十五届大学生科技学术季活动之广东大学生社会治理调研大赛。",
      "主办单位：以活动通知和荣誉证书上的记载为准。填写单位规范全称，多个单位联合主办的，填写“第一主办单位名称+等”，如“共青团广东省委员会等”。",
      "获奖时间具体到月，如“2024年10月”。",
      "获奖类别：限选国家级、省部级、校级。国际级竞赛等同于国家级，全国性行业协会主办赛事等同省部级。",
      "获奖等级：指特等奖、一等奖、二等奖、三等奖；冠军、亚军、季军；金奖、银奖、铜奖。",
      "竞赛类型：“互联网+”创新创业大赛、挑战杯、创青春中国青年创新创业大赛、其他。",
      "指导老师：以正式获奖通知文件或荣誉证书记载为准，指导老师多人的，全部填写。比赛未设指导老师或无指导老师的，可不填。",
      "备注：团体、个人。",
    ],
    fields:
      "学号 学生姓名 竞赛名称 主办单位 竞赛类别 获奖类别 获奖等级 竞赛类型 获奖时间 获奖人数 团队其他成员（请按证书顺序撰写） 指导老师 备注",
  },
  paper: {
    notes: [
      "发表时间具体到月，如“2024年10月”。",
      "发表期刊如是论文集，需在刊物名称中括号备注。",
      "佐证材料需提供：(1)期刊封面、目录、论文全文或见刊证明、论文全文；(2)已被权威数据库收录的需提供检索证明。",
    ],
    fields: "学号 学生姓名 论文名称 发表期刊 发表时间 作者排序 收录情况",
  },
  journal: {
    notes: [
      "发表时间具体到月，如“2024年10月”。",
      "作品指在校本科生在国内外正式出版刊物或重大活动上以第一作者发表作品的数量（例如：诗歌、散文、小说等）。",
    ],
    fields: "学号 学生姓名 作品名称 发表刊物名称 发表时间",
  },
  patent: {
    notes: [
      "发表时间具体到月，如“2024年10月”。",
      "专利类别指发明专利、实用新型专利、外观设计专利、软件著作权。",
    ],
    fields: "学号 学生姓名 名称 类别 授权号 获批时间 是否第一发明人",
  },
  certificate: {
    notes: [
      "发表时间具体到月，如“2024年10月”。",
      "证书类别指包括专业技术人员职业资格、技能人员职业资格。",
      "证书指在人力资源社会保障部公布的《国家职业资格目录（2021年版）》内的职业资格证。",
    ],
    fields: "学号 学生姓名 证书类别 证书名称 获得时间",
  },
  research: {
    notes: [
      "发表时间具体到月，如“2024年10月”。",
      "参与科研项目指本科生参加的各类教师主持的国家、省部纵向项目，以及学校科技管理部门科研考核统计的横向项目（自然年内在研项目）。",
    ],
    fields: "学号 学生姓名 参与科研项目名称 教师工号 项目负责人",
  },
  works: {
    notes: [
      "发表时间具体到月，如“2024年10月”。",
      "类别：理论类、创作类、表演类。",
      "类型:指大型作品、中型作品、小型作品。其中大型作品、中型作品、小型作品的划分，依据音乐、戏剧、影视类作品的规模（包括作品时长、技术含量、参与程度等）。",
      "影响范围：指全国（含国际）、区域、省内。",
    ],
    fields:
      "学号 学生姓名 作品名称 类别 类型 发布时间 发布场合 主办单位 影响范围 说明",
  },
};

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
const activeCategoryLabel = computed(() => {
  const match = achievementEntries.find(
    (entry) => entry.key === activeCategory.value,
  );
  return match ? match.label : "全部";
});
const editorCategory = computed(() => {
  if (form.category) {
    return form.category;
  }
  return activeCategory.value === "all" ? "" : activeCategory.value;
});
const activeCategoryHint = computed(() => {
  if (!editorCategory.value) {
    return null;
  }
  return categoryHints[editorCategory.value] || null;
});
const activeFormConfig = computed(() => {
  if (!editorCategory.value) {
    return null;
  }
  return categoryFieldMap[editorCategory.value] || null;
});
const filteredAchievements = computed(() => {
  if (activeCategory.value === "all") {
    return achievements.value;
  }
  return achievements.value.filter(
    (item) => item.category === activeCategory.value,
  );
});
const emptyMessage = computed(() => {
  if (activeCategory.value === "all") {
    return "还没有成就，点击右下角添加。";
  }
  return "该分类暂无成就，点击右下角添加。";
});
const addButtonLabel = computed(() => {
  if (activeCategory.value === "all") {
    return "添加成就";
  }
  return `添加${activeCategoryLabel.value}`;
});

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

function getCurrentStudentNo() {
  try {
    const raw = JSON.parse(localStorage.getItem("gcsc_user") || "{}");
    return raw.studentNo || profile.studentNo || "";
  } catch {
    return profile.studentNo || "";
  }
}

function handleMenuClick(key) {
  if (!isMenuEnabled(key)) {
    return;
  }
  sidebarOpen.value = false;
  if (key === "achievements") {
    handleAchievementEntry("all");
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

function toggleAchievements() {
  if (!isMenuEnabled("achievements")) {
    return;
  }
  achievementsOpen.value = !achievementsOpen.value;
  activeMenu.value = "achievements";
  if (achievementsOpen.value) {
    handleAchievementEntry("all");
  }
}

function handleAchievementEntry(key) {
  if (!isMenuEnabled("achievements")) {
    return;
  }
  const safeKey = achievementEntries.some((entry) => entry.key === key)
    ? key
    : "all";
  activeCategory.value = safeKey;
  achievementsOpen.value = true;
  activeMenu.value = "achievements";
  sidebarOpen.value = false;
  router.push({ path: "/achievements", query: { category: safeKey } });
}

function openSidebar() {
  sidebarOpen.value = true;
}

function closeSidebar() {
  sidebarOpen.value = false;
}

function openEditorForCategory() {
  editId.value = null;
  resetForm();
  form.category = activeCategory.value === "all" ? "" : activeCategory.value;
  applyFieldDefaults();
  editorOpen.value = true;
}

function closeEditor() {
  editorOpen.value = false;
  editId.value = null;
}

function goToSettings() {
  router.push("/settings");
}

function resetForm() {
  form.imageUrl = "";
  form.category = "";
  form.fields = {};
  imagePreview.value = "";
}

async function saveAchievement() {
  const config = activeFormConfig.value;
  if (!config) {
    errorMessage.value = "请先选择成就分类";
    return;
  }
  const category =
    form.category || (activeCategory.value === "all" ? "" : activeCategory.value);
  if (!category) {
    errorMessage.value = "请先选择成就分类";
    return;
  }
  const titleKey = config.titleKey;
  const titleValue = (form.fields[titleKey] || "").trim();
  if (!titleValue) {
    errorMessage.value = "请填写必填项";
    return;
  }
  const payload = {
    imageUrl: form.imageUrl || null,
    fields: { ...form.fields },
  };
  try {
    if (editId.value) {
      const { data } = await updateAchievement(category, editId.value, payload);
      achievements.value = dedupeAchievements(
        achievements.value.map((item) =>
          item.id === data.id ? normalizeAchievement(data) : item,
        ),
      );
      if (viewItem.value && viewItem.value.id === data.id) {
        viewItem.value = normalizeAchievement(data);
      }
    } else {
      const { data } = await createAchievement(category, payload);
      achievements.value = dedupeAchievements([
        normalizeAchievement(data),
        ...achievements.value,
      ]);
    }
    resetForm();
    closeEditor();
    errorMessage.value = "";
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || "保存失败，请重新登录";
  }
}

function triggerImage() {
  imageInput.value && imageInput.value.click();
}

async function onImageChange(event) {
  const [file] = Array.from(event.target.files || []);
  event.target.value = "";
  if (!file) {
    return;
  }
  const { data } = await uploadMedia(file);
  form.imageUrl = data.url;
  imagePreview.value = resolveMediaUrl(data.url);
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

function formatContestDate(fields = {}) {
  return fields.awardDate || "-";
}

function formatContestAwardPill(fields = {}) {
  const parts = [fields.awardCategory, fields.awardLevel].filter(Boolean);
  return parts.join(" ");
}

function formatContestStudentNo(fields = {}) {
  return `学号：${fields.studentNo || "-"}`;
}

function formatContestCategoryLine(fields = {}) {
  const parts = [fields.contestCategory, fields.contestType].filter(Boolean);
  const text = parts.length ? parts.join(" · ") : "-";
  return `类别：${text}`;
}

function formatContestMembers(fields = {}) {
  const peopleParts = [fields.studentName, fields.teamMembers].filter(Boolean);
  const people = peopleParts.length ? peopleParts.join("、") : "-";
  const teachers = fields.instructors ? ` | 指导老师：${fields.instructors}` : "";
  return `成员：${people}${teachers}`;
}

function formatContestAwardLine(fields = {}) {
  const count = fields.awardCount || "-";
  const remark = fields.remark ? ` · ${fields.remark}` : "";
  return `获奖人数：${count}${remark}`;
}

function formatContestOrganizer(fields = {}) {
  return `主办单位：${fields.organizer || "-"}`;
}

function formatPaperJournal(fields = {}) {
  return fields.journalName || "-";
}

function formatPaperAuthors(fields = {}) {
  const name = fields.studentName || "-";
  const order = fields.authorOrder ? `（${fields.authorOrder}）` : "";
  return `${name}${order}`;
}

function formatPaperDate(fields = {}) {
  return fields.publishDate || "-";
}

function formatJournalPublication(fields = {}) {
  return fields.publicationName || "-";
}

function formatJournalAuthor(fields = {}) {
  return fields.studentName || "-";
}

function formatJournalDate(fields = {}) {
  return fields.publishDate || "-";
}

function formatPatentGrantNo(fields = {}) {
  return fields.grantNo || "-";
}

function formatPatentInventor(fields = {}) {
  const name = fields.studentName || "-";
  const first = fields.firstInventor ? `（${fields.firstInventor}）` : "";
  return `${name}${first}`;
}

function formatPatentDate(fields = {}) {
  return fields.grantDate || "-";
}

function formatPatentTag(fields = {}) {
  return fields.patentType || "专利";
}

function formatCertificateType(fields = {}) {
  return fields.certificateType || "-";
}

function formatCertificateOwner(fields = {}) {
  return fields.studentName || "-";
}

function formatCertificateDate(fields = {}) {
  return fields.obtainDate || "-";
}

function formatResearchLeader(fields = {}) {
  return `项目负责人：${fields.projectLeader || "-"}`;
}

function formatResearchTeacher(fields = {}) {
  return `教师工号：${fields.teacherNo || "-"}`;
}

function formatWorksCategory(fields = {}) {
  return fields.workCategory || "-";
}

function formatWorksOccasion(fields = {}) {
  return fields.publishOccasion || "-";
}

function formatWorksDate(fields = {}) {
  return fields.publishDate || "-";
}

function formatWorksTag(fields = {}) {
  return fields.workCategory || "作品";
}

function formatWorksOrganizer(fields = {}) {
  return `主办单位：${fields.organizer || "-"}`;
}

function dedupeAchievements(list) {
  const seen = new Set();
  return list.filter((item) => {
    if (!item || item.id == null) {
      return true;
    }
    const key = `${item.category || ""}:${item.id}`;
    if (seen.has(key)) {
      return false;
    }
    seen.add(key);
    return true;
  });
}

function normalizeAchievement(item) {
  const config = categoryFieldMap[item.category] || null;
  const fields = item.fields || {};
  const titleKey = config?.titleKey;
  const dateKey = config?.dateKey;
  const dateLabel =
    config?.fields.find((field) => field.key === dateKey)?.label || "";
  const fieldLines = config
    ? config.fields.map(
        (field) => `${field.label}：${fields[field.key] || "-"}`,
      )
    : [];
  return {
    id: item.id,
    title: titleKey ? fields[titleKey] : "",
    dateLabel,
    dateValue: dateKey ? fields[dateKey] : "",
    fields,
    fieldLines,
    previewFields: fieldLines.slice(0, 2),
    image: resolveMediaUrl(item.imageUrl),
    category: item.category || "",
  };
}

function openDetail(item) {
  viewItem.value = item;
  viewOpen.value = true;
  viewClosing.value = false;
}

function closeView() {
  viewOpen.value = false;
  viewClosing.value = true;
  setTimeout(() => {
    viewItem.value = null;
    viewClosing.value = false;
  }, 260);
}

function editFromView() {
  if (!viewItem.value) {
    return;
  }
  const item = viewItem.value;
  editId.value = item.id;
  form.category = item.category || "";
  form.fields = { ...(item.fields || {}) };
  form.imageUrl = item.image ? item.image.replace(API_BASE, "") : "";
  imagePreview.value = item.image || "";
  applyFieldDefaults();
  viewOpen.value = false;
  viewClosing.value = true;
  editorOpen.value = true;
  setTimeout(() => {
    viewItem.value = null;
    viewClosing.value = false;
  }, 260);
}

function openDelete() {
  deleteDialogOpen.value = true;
}

function closeDelete() {
  if (deleteBusy.value) {
    return;
  }
  deleteDialogOpen.value = false;
}

async function confirmDelete() {
  if (!viewItem.value) {
    closeDelete();
    return;
  }
  deleteBusy.value = true;
  try {
    await deleteAchievement(viewItem.value.category, viewItem.value.id);
    achievements.value = achievements.value.filter(
      (item) => item.id !== viewItem.value.id,
    );
    closeDelete();
    closeView();
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || "删除失败";
  } finally {
    deleteBusy.value = false;
  }
}

async function fetchAchievements() {
  try {
    const { data } = await getAchievements();
    achievements.value = Array.isArray(data)
      ? dedupeAchievements(data.map(normalizeAchievement))
      : [];
    errorMessage.value = "";
  } catch (err) {
    achievements.value = [];
    errorMessage.value = err?.response?.data?.message || "无法获取成就列表";
  }
}

function syncCategoryFromRoute() {
  const rawCategory = route.query.category;
  const safeCategory =
    typeof rawCategory === "string" &&
    achievementEntries.some((entry) => entry.key === rawCategory)
      ? rawCategory
      : "all";
  activeCategory.value = safeCategory;
  achievementsOpen.value = true;
  if (rawCategory !== safeCategory) {
    router.replace({
      path: "/achievements",
      query: { category: safeCategory },
    });
  }
}

function applyFieldDefaults() {
  const config = activeFormConfig.value;
  if (!config) {
    return;
  }
  const hasStudentNo = config.fields.some((field) => field.key === "studentNo");
  const hasStudentName = config.fields.some(
    (field) => field.key === "studentName",
  );
  if (hasStudentNo && !form.fields.studentNo) {
    form.fields.studentNo = getCurrentStudentNo();
  }
  if (hasStudentName && !form.fields.studentName) {
    form.fields.studentName = profile.displayName || profile.username || "";
  }
}

onMounted(() => {
  fetchAchievements();
  syncCategoryFromRoute();
});

watch(
  () => route.query.category,
  () => {
    syncCategoryFromRoute();
  },
);

watch(
  () => form.category,
  () => {
    applyFieldDefaults();
  },
);
</script>
