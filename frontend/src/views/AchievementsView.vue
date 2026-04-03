<template>
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
          <div
            v-else-if="item.category === 'paper'"
            class="achievement-card-body"
          >
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
          <div
            v-else-if="item.category === 'journal'"
            class="achievement-card-body"
          >
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
            <div class="achievement-journal-tag" aria-hidden="true">
              期刊作品
            </div>
          </div>
          <div
            v-else-if="item.category === 'patent'"
            class="achievement-card-body"
          >
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
          <div
            v-else-if="item.category === 'research'"
            class="achievement-card-body"
          >
            <div class="achievement-card-title achievement-paper-title">
              {{ item.title || "-" }}
            </div>
            <div class="achievement-card-text">
              {{ formatResearchLeader(item.fields) }}
            </div>
            <div class="achievement-card-text">
              {{ formatResearchTeacher(item.fields) }}
            </div>
            <div class="achievement-research-tag" aria-hidden="true">
              科研项目
            </div>
          </div>
          <div
            v-else-if="item.category === 'works'"
            class="achievement-card-body"
          >
            <div class="achievement-card-title achievement-paper-title">
              {{ item.title || "-" }}
            </div>
            <div class="achievement-card-meta">
              <span class="achievement-card-meta-item">{{ formatWorksCategory(item.fields) }}</span>
              <span class="achievement-card-meta-dot">·</span>
              <span class="achievement-card-meta-item">{{ formatWorksDate(item.fields) }}</span>
            </div>
            <div class="achievement-card-text" v-if="item.fields.publishOccasion">
              {{ formatWorksOccasion(item.fields) }}
            </div>
            <div class="achievement-card-text" v-if="item.fields.studentName">
              {{ item.fields.studentName }}
            </div>
            <div class="achievement-works-tag" aria-hidden="true">
              {{ formatWorksTag(item.fields) }}
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
      <MobileCapsule :hidden="editorOpen" @open-sidebar="openDashboardSidebar">
        <template #right>
          <div
            class="capsule-action capsule-primary"
            role="button"
            tabindex="0"
            :aria-label="addButtonLabel"
            @click="openEditorForCategory"
          >
            +
          </div>
        </template>
      </MobileCapsule>

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
        <div
          v-else-if="viewItem"
          class="achievement-view-body"
          :class="{ 'no-media': !viewItem.imageUrls?.length }"
          >
          <div
            v-if="!viewItem.imageUrls?.length"
            class="achievement-view-media"
          ></div>

          <div
            v-else-if="viewItem.imageUrls.length === 1"
            class="achievement-view-media"
          >
            <img
              class="media-single-img"
              :src="viewItem.imageUrls[0]"
              alt="成就图片"
              @click="showPreview(viewItem.imageUrls, 0)"
            />
          </div>

          <div
            v-else-if="viewItem.imageUrls.length === 2"
            class="achievement-view-media achievement-view-media-2"
          >
            <div class="media-2-cell" @click="showPreview(viewItem.imageUrls, 0)">
              <img :src="viewItem.imageUrls[0]" alt="成就图片" />
            </div>
            <div class="media-2-cell" @click="showPreview(viewItem.imageUrls, 1)">
              <img :src="viewItem.imageUrls[1]" alt="成就图片" />
            </div>
          </div>

          <div
            v-else
            class="achievement-view-media achievement-view-media-3"
          >
            <div class="media-3-grid">
              <div class="media-3-cell media-3-top-left" @click="showPreview(viewItem.imageUrls, 0)">
                <img :src="viewItem.imageUrls[0]" alt="成就图片" />
              </div>
              <div class="media-3-cell media-3-top-right" @click="showPreview(viewItem.imageUrls, 1)">
                <img :src="viewItem.imageUrls[1]" alt="成就图片" />
              </div>
              <div class="media-3-cell media-3-bottom" @click="showPreview(viewItem.imageUrls, 2)">
                <img :src="viewItem.imageUrls[2]" alt="成就图片" />
              </div>
            </div>
          </div>

          <div class="achievement-detail-body">
            <!-- contest -->
            <template v-if="viewItem.category === 'contest'">
              <div class="detail-header">
                <div class="detail-title">{{ viewItem.title || "-" }}</div>
                <span v-if="formatContestAwardPill(viewItem.fields)" class="detail-award-pill">
                  {{ formatContestAwardPill(viewItem.fields) }}
                </span>
              </div>
              <div class="detail-date">{{ formatContestDate(viewItem.fields) }}</div>
              <div class="detail-group">
                <div class="detail-group-label">基本信息</div>
                <div class="detail-row">
                  <span class="detail-label">学号</span>
                  <span class="detail-value">{{ viewItem.fields.studentNo || "-" }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">姓名</span>
                  <span class="detail-value">{{ viewItem.fields.studentName || "-" }}</span>
                </div>
              </div>
              <div class="detail-group">
                <div class="detail-group-label">参赛信息</div>
                <div class="detail-row">
                  <span class="detail-label">竞赛名称</span>
                  <span class="detail-value">{{ viewItem.fields.contestName || "-" }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">竞赛类别</span>
                  <span class="detail-value">{{ [viewItem.fields.contestCategory, viewItem.fields.contestType].filter(Boolean).join(' · ') || "-" }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">获奖类别</span>
                  <span class="detail-value">{{ [viewItem.fields.awardCategory, viewItem.fields.awardLevel].filter(Boolean).join(' · ') || "-" }}</span>
                </div>
              </div>
              <div class="detail-group">
                <div class="detail-group-label">团队信息</div>
                <div class="detail-row">
                  <span class="detail-label">团队成员</span>
                  <span class="detail-value">{{ [viewItem.fields.studentName, viewItem.fields.teamMembers].filter(Boolean).join('、') || "-" }}</span>
                </div>
                <div class="detail-row" v-if="viewItem.fields.instructors">
                  <span class="detail-label">指导老师</span>
                  <span class="detail-value">{{ viewItem.fields.instructors }}</span>
                </div>
              </div>
              <div class="detail-group">
                <div class="detail-group-label">获奖信息</div>
                <div class="detail-row">
                  <span class="detail-label">获奖人数</span>
                  <span class="detail-value">{{ viewItem.fields.awardCount || "-" }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">备注</span>
                  <span class="detail-value">{{ viewItem.fields.remark || "-" }}</span>
                </div>
              </div>
              <div class="detail-group">
                <div class="detail-row">
                  <span class="detail-label">主办单位</span>
                  <span class="detail-value">{{ viewItem.fields.organizer || "-" }}</span>
                </div>
              </div>
            </template>

            <!-- paper -->
            <template v-else-if="viewItem.category === 'paper'">
              <div class="detail-header">
                <div class="detail-title">{{ viewItem.title || "-" }}</div>
                <span class="detail-category-tag">学术论文</span>
              </div>
              <div class="detail-group">
                <div class="detail-group-label">期刊信息</div>
                <div class="detail-row">
                  <span class="detail-label">发表期刊</span>
                  <span class="detail-value">{{ viewItem.fields.journalName || "-" }}</span>
                </div>
                <div class="detail-row" v-if="viewItem.fields.indexed">
                  <span class="detail-label">收录情况</span>
                  <span class="detail-value">{{ viewItem.fields.indexed }}</span>
                </div>
              </div>
              <div class="detail-group">
                <div class="detail-group-label">作者信息</div>
                <div class="detail-row">
                  <span class="detail-label">作者</span>
                  <span class="detail-value">{{ viewItem.fields.studentName || "-" }}<template v-if="viewItem.fields.authorOrder">（{{ viewItem.fields.authorOrder }}）</template></span>
                </div>
              </div>
              <div class="detail-group">
                <div class="detail-row">
                  <span class="detail-label">发表时间</span>
                  <span class="detail-value">{{ viewItem.fields.publishDate || "-" }}</span>
                </div>
              </div>
            </template>

            <!-- journal -->
            <template v-else-if="viewItem.category === 'journal'">
              <div class="detail-header">
                <div class="detail-title">{{ viewItem.title || "-" }}</div>
                <span class="detail-category-tag">期刊作品</span>
              </div>
              <div class="detail-group">
                <div class="detail-group-label">刊物信息</div>
                <div class="detail-row">
                  <span class="detail-label">刊物名称</span>
                  <span class="detail-value">{{ viewItem.fields.publicationName || "-" }}</span>
                </div>
              </div>
              <div class="detail-group">
                <div class="detail-group-label">作者信息</div>
                <div class="detail-row">
                  <span class="detail-label">作者</span>
                  <span class="detail-value">{{ viewItem.fields.studentName || "-" }}</span>
                </div>
              </div>
              <div class="detail-group">
                <div class="detail-row">
                  <span class="detail-label">发表时间</span>
                  <span class="detail-value">{{ viewItem.fields.publishDate || "-" }}</span>
                </div>
              </div>
            </template>

            <!-- patent -->
            <template v-else-if="viewItem.category === 'patent'">
              <div class="detail-header">
                <div class="detail-title">{{ viewItem.title || "-" }}</div>
                <span class="detail-category-tag">{{ viewItem.fields.patentType || "专利" }}</span>
              </div>
              <div class="detail-group">
                <div class="detail-group-label">专利信息</div>
                <div class="detail-row">
                  <span class="detail-label">授权号</span>
                  <span class="detail-value">{{ viewItem.fields.grantNo || "-" }}</span>
                </div>
              </div>
              <div class="detail-group">
                <div class="detail-group-label">发明人信息</div>
                <div class="detail-row">
                  <span class="detail-label">发明人</span>
                  <span class="detail-value">{{ viewItem.fields.studentName || "-" }}<template v-if="viewItem.fields.firstInventor">（{{ viewItem.fields.firstInventor }}）</template></span>
                </div>
              </div>
              <div class="detail-group">
                <div class="detail-row">
                  <span class="detail-label">获批时间</span>
                  <span class="detail-value">{{ viewItem.fields.grantDate || "-" }}</span>
                </div>
              </div>
            </template>

            <!-- certificate -->
            <template v-else-if="viewItem.category === 'certificate'">
              <div class="detail-header">
                <div class="detail-title">{{ viewItem.title || "-" }}</div>
                <span class="detail-category-tag">职业资格证书</span>
              </div>
              <div class="detail-group">
                <div class="detail-group-label">证书信息</div>
                <div class="detail-row">
                  <span class="detail-label">证书类别</span>
                  <span class="detail-value">{{ viewItem.fields.certificateType || "-" }}</span>
                </div>
              </div>
              <div class="detail-group">
                <div class="detail-group-label">持证人</div>
                <div class="detail-row">
                  <span class="detail-label">姓名</span>
                  <span class="detail-value">{{ viewItem.fields.studentName || "-" }}</span>
                </div>
              </div>
              <div class="detail-group">
                <div class="detail-row">
                  <span class="detail-label">获得时间</span>
                  <span class="detail-value">{{ viewItem.fields.obtainDate || "-" }}</span>
                </div>
              </div>
            </template>

            <!-- research -->
            <template v-else-if="viewItem.category === 'research'">
              <div class="detail-header">
                <div class="detail-title">{{ viewItem.title || "-" }}</div>
                <span class="detail-category-tag">科研项目</span>
              </div>
              <div class="detail-group">
                <div class="detail-group-label">项目信息</div>
                <div class="detail-row">
                  <span class="detail-label">项目负责人</span>
                  <span class="detail-value">{{ viewItem.fields.projectLeader || "-" }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">教师工号</span>
                  <span class="detail-value">{{ viewItem.fields.teacherNo || "-" }}</span>
                </div>
              </div>
            </template>

            <!-- works -->
            <template v-else-if="viewItem.category === 'works'">
              <div class="detail-header">
                <div class="detail-title">{{ viewItem.title || "-" }}</div>
                <div class="detail-badges">
                  <span class="detail-category-tag">{{ viewItem.fields.workCategory || "创作作品" }}</span>
                  <span v-if="viewItem.fields.workType" class="detail-type-badge">{{ viewItem.fields.workType }}</span>
                </div>
              </div>
              <div class="detail-date">{{ viewItem.fields.publishDate || "-" }}</div>

              <div class="detail-info-strip">
                <div class="detail-strip-item" v-if="viewItem.fields.impactScope">
                  <span class="strip-icon">◎</span>
                  <span>{{ viewItem.fields.impactScope }}</span>
                </div>
                <div class="detail-strip-item" v-if="viewItem.fields.publishOccasion">
                  <span class="strip-icon">◈</span>
                  <span>{{ viewItem.fields.publishOccasion }}</span>
                </div>
              </div>

              <div class="detail-group" v-if="viewItem.fields.studentName || viewItem.fields.studentNo">
                <div class="detail-group-label">创作者</div>
                <div class="detail-row" v-if="viewItem.fields.studentName">
                  <span class="detail-label">姓名</span>
                  <span class="detail-value">{{ viewItem.fields.studentName }}</span>
                </div>
                <div class="detail-row" v-if="viewItem.fields.studentNo">
                  <span class="detail-label">学号</span>
                  <span class="detail-value">{{ viewItem.fields.studentNo }}</span>
                </div>
              </div>

              <div class="detail-group" v-if="viewItem.fields.organizer">
                <div class="detail-group-label">主办单位</div>
                <div class="detail-row">
                  <span class="detail-value">{{ viewItem.fields.organizer }}</span>
                </div>
              </div>

              <div class="detail-note" v-if="viewItem.fields.note">
                <div class="detail-note-label">说明</div>
                <div class="detail-note-text">{{ viewItem.fields.note }}</div>
              </div>
            </template>

            <!-- fallback -->
            <template v-else>
              <div class="detail-header">
                <div class="detail-title">{{ viewItem.title || "-" }}</div>
              </div>
              <div v-if="viewItem.dateLabel" class="detail-date">
                {{ viewItem.dateLabel }}：{{ viewItem.dateValue || "-" }}
              </div>
              <div
                v-for="line in viewItem.fieldLines"
                :key="line"
                class="detail-row"
              >
                <span class="detail-value">{{ line }}</span>
              </div>
            </template>

            <div
              v-if="viewItem.attachments && viewItem.attachments.length"
              class="detail-attachments"
            >
              <div class="detail-attachments-title">附件</div>
              <div class="attachment-list">
                <div
                  v-for="(file, index) in viewItem.attachments"
                  :key="`${file.url}-${index}`"
                  class="attachment-item"
                >
                  <img :src="attachmentIcon(file)" alt="" />
                  <div class="attachment-name">{{ file.name }}</div>
                  <button
                    v-if="isVideoFile(file) || isDocumentFile(file) || isSheetFile(file) || isPdfFile(file)"
                    class="attachment-link"
                    @click="showPreview([file.url], 0)"
                  >
                    预览
                  </button>
                  <a
                    v-else-if="!isPptxFile(file)"
                    class="attachment-link"
                    :href="file.url"
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    查看
                  </a>
                  <a
                    class="attachment-download"
                    :href="file.url"
                    download
                    rel="noopener noreferrer"
                    title="下载"
                  >
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                      <polyline points="7 10 12 15 17 10"></polyline>
                      <line x1="12" y1="15" x2="12" y2="3"></line>
                    </svg>
                  </a>
                </div>
              </div>
            </div>

            <div class="detail-actions">
              <button class="post-action" type="button" @click="editFromView">
                编辑
              </button>
              <button
                class="post-action danger"
                type="button"
                @click.stop="openDelete"
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
        <div class="achievement-grid" :class="{ 'has-media': form.category }">
          <transition name="panel-fade">
            <div v-show="form.category" class="achievement-media-panel">
            <div class="media-header">
              <div>
                <div class="media-title">图片(可选)</div>
                <div class="media-subtitle">
                  最多 {{ imageMaxCount }} 张 · 单张不超过 {{ mediaLimitLabel }}
                </div>
              </div>
            </div>

            <div
              v-if="imagePreviews.length === 0"
              class="media-empty-state"
              @click="triggerImage"
            >
              <div class="media-empty-icon">+</div>
              <div class="media-empty-text">点击添加图片</div>
            </div>

            <div v-else-if="imagePreviews.length === 1" class="media-single">
              <button
                class="media-single-item"
                type="button"
                @click="selectEditorImage(0)"
              >
                <img :src="imagePreviews[0]" alt="成就图片" />
                <span class="media-remove" @click.stop="removeImage(0)"
                  >移除</span
                >
              </button>
              <button class="media-add-btn" type="button" @click="triggerImage">
                <span>+</span>
              </button>
            </div>

            <div v-else-if="imagePreviews.length === 2" class="media-two">
              <button
                v-for="(image, index) in imagePreviews"
                :key="`${image}-${index}`"
                class="media-thumb"
                type="button"
                @click="selectEditorImage(index)"
              >
                <img :src="image" alt="成就图片" />
                <span class="media-remove" @click.stop="removeImage(index)"
                  >移除</span
                >
              </button>
              <button
                class="media-thumb media-add"
                type="button"
                @click="triggerImage"
              >
                <span>+</span>
              </button>
            </div>

            <div v-else class="media-grid">
              <button
                v-for="(image, index) in imagePreviews"
                :key="`${image}-${index}`"
                class="media-thumb"
                type="button"
                @click="selectEditorImage(index)"
              >
                <img :src="image" alt="成就图片" />
                <span class="media-remove" @click.stop="removeImage(index)">
                  移除
                </span>
              </button>
              <button
                v-if="imagePreviews.length < imageMaxCount"
                class="media-thumb media-add"
                type="button"
                @click="triggerImage"
              >
                <span>+</span>
              </button>
            </div>
          </div>
          </transition>

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
            <transition name="panel-fade">
            <div v-show="form.category" class="achievement-attachments-panel">
              <div class="media-header">
                <div>
                  <div class="media-title">附件(可选)</div>
                  <div class="media-subtitle">支持多文件</div>
                </div>
              </div>
              <div v-if="!attachmentPreviews.length" class="media-empty">
                暂无附件
              </div>
              <div v-else class="attachment-list">
                <div
                  v-for="(file, index) in attachmentPreviews"
                  :key="`${file.url}-${index}`"
                  class="attachment-item"
                >
                  <img :src="attachmentIcon(file)" alt="" />
                  <div class="attachment-name">{{ file.name }}</div>
                  <button
                    class="attachment-remove"
                    type="button"
                    @click="removeAttachment(index)"
                  >
                    移除
                  </button>
                </div>
              </div>
              <div class="attachment-formats" @click="triggerAttachment">
                <div class="format-row">
                  <div class="format-item">
                    <img class="format-icon" src="/assets/icons/doc.svg" alt="" />
                    <span class="format-label">文档</span>
                    <span class="format-exts">docx/doc/pdf/xls/xlsx/pptx/ppt</span>
                  </div>
                  <div class="format-item">
                    <img class="format-icon" src="/assets/icons/image.svg" alt="" />
                    <span class="format-label">图片</span>
                    <span class="format-exts">jpeg/jpg/png/heif</span>
                  </div>
                </div>
                <div class="format-row">
                  <div class="format-item">
                    <img class="format-icon" src="/assets/icons/video.svg" alt="" />
                    <span class="format-label">视频</span>
                    <span class="format-exts">mp4/mov</span>
                  </div>
                  <div class="format-item">
                    <img class="format-icon" src="/assets/icons/zip.svg" alt="" />
                    <span class="format-label">压缩包</span>
                    <span class="format-exts">zip/rar/7z</span>
                  </div>
                </div>
              </div>
              <div class="media-tip">单个不超过 {{ attachmentLimitLabel }}</div>
            </div>
            </transition>

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

      <!-- Media Preview -->
      <Teleport to="body">
        <Transition name="viewer" appear>
          <div v-if="previewVisible" class="viewer-backdrop" @click="hidePreview">
            <div class="viewer-header" @click.stop>
              <div class="viewer-counter">
                <span class="viewer-current">{{ previewIndex + 1 }}</span>
                <span class="viewer-sep">/</span>
                <span class="viewer-total">{{ previewImages.length }}</span>
              </div>
              <button class="viewer-close" @click="hidePreview">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </button>
            </div>

            <div class="viewer-stage" @click.stop>
              <button
                v-if="previewImages.length > 1 && previewIndex > 0"
                class="viewer-arrow viewer-prev"
                @click="previewPrev"
              >
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="15 18 9 12 15 6"></polyline>
                </svg>
              </button>

              <Transition :name="'slide-' + slideDirection" mode="out-in">
                <div class="viewer-media" :key="previewIndex">
                  <video
                    v-if="previewType === 'video'"
                    :src="previewImages[previewIndex]"
                    class="viewer-video"
                    controls
                    autoplay
                  ></video>
                  <div
                    v-else-if="previewType === 'document' || previewType === 'sheet' || previewType === 'pdf'"
                    class="viewer-document"
                    :class="{ 'viewer-doc-full': previewType === 'pdf' }"
                  >
                    <div v-if="previewLoading" class="viewer-loading">
                      <div class="viewer-spinner"></div>
                      <span>加载中...</span>
                    </div>
                    <div v-else class="viewer-content-wrapper">
                      <div v-if="previewType === 'document'" class="viewer-tip">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <circle cx="12" cy="12" r="10"></circle>
                          <line x1="12" y1="16" x2="12" y2="12"></line>
                          <line x1="12" y1="8" x2="12.01" y2="8"></line>
                        </svg>
                        因渲染限制，预览与实际文件可能存在样式差异
                      </div>
                      <div v-if="previewType === 'sheet'" class="viewer-tip">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <circle cx="12" cy="12" r="10"></circle>
                          <line x1="12" y1="16" x2="12" y2="12"></line>
                          <line x1="12" y1="8" x2="12.01" y2="8"></line>
                        </svg>
                        点击工作表标签可切换Sheet | 因渲染限制，预览与实际可能存在差异
                      </div>
                      <div v-html="previewContent" class="viewer-content" :class="{ 'viewer-content-full': previewType === 'pdf' || previewType === 'document' }"></div>
                    </div>
                  </div>
                  <img
                    v-else
                    :src="previewImages[previewIndex]"
                    class="viewer-image"
                    alt=""
                  />
                </div>
              </Transition>

              <button
                v-if="previewImages.length > 1 && previewIndex < previewImages.length - 1"
                class="viewer-arrow viewer-next"
                @click="previewNext"
              >
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
              </button>
            </div>

            <div class="viewer-dots" v-if="previewImages.length > 1" @click.stop>
              <button
                v-for="(_, i) in previewImages"
                :key="i"
                class="viewer-dot"
                :class="{ active: i === previewIndex }"
                @click="previewIndex = i; slideDirection = i > previewIndex ? 'right' : 'left'"
              ></button>
            </div>
          </div>
        </Transition>
      </Teleport>

      <div
        v-if="deleteDialogOpen"
        class="dialog-backdrop"
        @click="closeDelete"
      ></div>
      <section
        v-if="deleteDialogOpen"
        class="dialog-card"
        @click.stop
      >
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

      <input
        ref="imageInput"
        type="file"
        accept=".jpeg,.jpg,.png,.heif,image/jpeg,image/png,image/heif"
        multiple
        hidden
        @change="onImageChange"
      />
      <input
        ref="attachmentInput"
        type="file"
        accept=".docx,.doc,.pdf,.xls,.xlsx,.zip,.rar,.7z,.pptx,.ppt,.mp4,.mov,.jpeg,.jpg,.png,.heif,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/pdf,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation,video/mp4,video/quicktime,image/jpeg,image/png,image/heif"
        multiple
        hidden
        @change="onAttachmentChange"
      />
  </main>
</template>

<script setup>
import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
  watch,
} from "vue";
import { useRouter, useRoute } from "vue-router";
import {
  getMenuLocation,
  isMenuEnabled,
} from "../constants/menu";
import {
  createAchievement,
  deleteAchievement,
  getAchievements,
  updateAchievement,
} from "../api/achievements";
import { useAchievementUploadSettings } from "../composables/useAchievementUploadSettings";
import { uploadMedia } from "../api/upload";
import { renderDocx } from "../utils/docxRenderer";
import { renderSheet } from "../utils/sheetRenderer";
import { renderPdf } from "../utils/pdfRenderer";
import { API_BASE } from "../api/request";
import MobileCapsule from "../components/MobileCapsule.vue";
import { navigateWithViewTransition } from "../utils/viewTransition";
import { useDashboardShell } from "../composables/useDashboardShell";

const router = useRouter();
const route = useRoute();
const { openSidebar: openDashboardSidebar } = useDashboardShell();
const profile = reactive(loadUser());
const activeMenu = ref("achievements");
const editorOpen = ref(false);
const imageInput = ref(null);
const attachmentInput = ref(null);
const errorMessage = ref("");
const viewOpen = ref(false);
const viewLoading = ref(false);
const viewItem = ref(null);
const viewClosing = ref(false);
const viewExitUp = ref(false);
const editId = ref(null);
const deleteDialogOpen = ref(false);
const deleteBusy = ref(false);
const sidebarOpen = ref(false);
const achievementsOpen = ref(true);
const activeCategory = ref("all");
const previewImages = ref([]);
const previewIndex = ref(0);
const previewVisible = ref(false);
const previewType = ref("image");
const previewContent = ref("");
const previewLoading = ref(false);
const previewWorkbook = ref(null);
const slideDirection = ref("right");
const {
  settings: achievementUploadSettings,
  fetchSettings: fetchAchievementUploadSettings,
} = useAchievementUploadSettings();

function isMediaVideo(url) {
  if (!url) return false;
  const ext = resolveMediaTypeByExtension(url);
  return ["mp4", "mov", "webm"].includes(ext);
}

function isMediaDocument(url) {
  if (!url) return false;
  const ext = resolveMediaTypeByExtension(url);
  return ["doc", "docx"].includes(ext);
}

function isMediaSheet(url) {
  if (!url) return false;
  const ext = resolveMediaTypeByExtension(url);
  return ["xls", "xlsx"].includes(ext);
}

function isMediaPdf(url) {
  if (!url) return false;
  const ext = resolveMediaTypeByExtension(url);
  return ["pdf"].includes(ext);
}

const achievements = ref([]);

const activeStudentQuery = computed(() => {
  const rawName = route.query.studentName;
  const rawNo = route.query.studentNo;
  const rawEmbed = route.query.embed;
  const studentName = typeof rawName === "string" ? rawName.trim() : "";
  const studentNo = typeof rawNo === "string" ? rawNo.trim() : "";
  const embedValue = typeof rawEmbed === "string" ? rawEmbed.trim() : "";
  return {
    studentName,
    studentNo,
    embedValue,
  };
});

const isEmbedded = computed(() => {
  const value = activeStudentQuery.value.embedValue;
  return value === "1" || value.toLowerCase() === "true";
});

const achievementEntries = [
  { key: "all", label: "全部" },
  { key: "contest", label: "学科竞赛、文体艺术" },
  { key: "paper", label: "发表学术论文" },
  { key: "journal", label: "发表期刊作品" },
  { key: "patent", label: "专利(著作权)授权数(项)" },
  { key: "certificate", label: "职业资格证书" },
  { key: "research", label: "学生参与教师科研项目情况" },
  { key: "works", label: "创作、表演的代表性作品" },
  { key: "doubleHundred", label: "双百工程" },
  { key: "ieerTraining", label: "大学生创新创业训练计划项目" },
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
  imageUrls: [],
  attachments: [],
  category: "",
  fields: {},
});

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
  doubleHundred: {
    titleKey: "projectName",
    dateKey: "projectDate",
    fields: [
      {
        key: "projectCategory",
        label: "项目类别",
        kind: "input",
        placeholder: "请输入项目类别",
      },
      {
        key: "projectDomain",
        label: "项目所属领域",
        kind: "input",
        placeholder: "请输入项目所属领域",
      },
      {
        key: "projectName",
        label: "申报作品名",
        kind: "input",
        placeholder: "请输入申报作品名",
      },
      {
        key: "projectLeader",
        label: "项目负责人",
        kind: "input",
        placeholder: "请输入项目负责人",
      },
      {
        key: "leaderStudentNo",
        label: "负责人学号",
        kind: "input",
        placeholder: "请输入负责人学号",
      },
      {
        key: "educationLevel",
        label: "本科/研究生",
        kind: "input",
        placeholder: "请输入本科或研究生",
      },
      {
        key: "teamMembers",
        label: "项目其他成员",
        kind: "input",
        placeholder: "请输入项目其他成员",
      },
      {
        key: "instructors",
        label: "指导老师（全体）",
        kind: "input",
        placeholder: "请输入指导老师",
      },
      {
        key: "teamSize",
        label: "项目人数",
        kind: "input",
        placeholder: "请输入项目人数",
      },
      {
        key: "plannedLevel",
        label: "拟立项等级",
        kind: "input",
        placeholder: "请输入拟立项等级",
      },
      {
        key: "college",
        label: "学院",
        kind: "input",
        placeholder: "请输入学院",
      },
      {
        key: "finalLevel",
        label: "结项等级",
        kind: "input",
        placeholder: "优秀/良好/合格/不合格",
      },
    ],
  },
  ieerTraining: {
    titleKey: "projectName",
    dateKey: "projectDate",
    fields: [
      {
        key: "collegeName",
        label: "学院名称",
        kind: "input",
        placeholder: "请输入学院名称",
      },
      {
        key: "projectName",
        label: "项目名称",
        kind: "input",
        placeholder: "请输入项目名称",
      },
      {
        key: "projectType",
        label: "项目类型",
        kind: "input",
        placeholder: "请输入项目类型",
      },
      {
        key: "projectLeader",
        label: "项目负责人姓名",
        kind: "input",
        placeholder: "请输入项目负责人姓名",
      },
      {
        key: "instructorName",
        label: "指导教师姓名",
        kind: "input",
        placeholder: "请输入指导教师姓名",
      },
      {
        key: "recommendedLevel",
        label: "推荐项目级别",
        kind: "input",
        placeholder: "请输入推荐项目级别",
      },
      {
        key: "isKeyArea",
        label: "是否推荐为重点领域支持项目",
        kind: "input",
        placeholder: "是/否",
      },
      {
        key: "finalStatus",
        label: "结项情况",
        kind: "input",
        placeholder: "优秀/通过/延期结项/终止项目",
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
  const baseList =
    activeCategory.value === "all"
      ? achievements.value
      : achievements.value.filter(
          (item) => item.category === activeCategory.value,
        );
  const { studentName, studentNo } = activeStudentQuery.value;
  if (!studentName && !studentNo) {
    return baseList;
  }
  const normalizeText = (value) =>
    String(value || "")
      .trim()
      .toLowerCase();
  const targetName = normalizeText(studentName);
  const targetNo = normalizeText(studentNo);
  return baseList.filter((item) => {
    const fields = item.fields || {};
    const itemNo = normalizeText(fields.studentNo);
    const itemName = normalizeText(fields.studentName);
    if (targetNo) {
      return itemNo ? itemNo === targetNo : true;
    }
    return itemName ? itemName === targetName : true;
  });
});
const emptyMessage = computed(() => {
  const { studentName, studentNo } = activeStudentQuery.value;
  if (studentName || studentNo) {
    return "该学生暂无成就。";
  }
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
  const { studentNo } = activeStudentQuery.value;
  if (studentNo) {
    return studentNo;
  }
  try {
    const raw = JSON.parse(localStorage.getItem("gcsc_user") || "{}");
    return raw.studentNo || profile.studentNo || "";
  } catch {
    return profile.studentNo || "";
  }
}

function getCurrentStudentName() {
  const { studentName } = activeStudentQuery.value;
  if (studentName) {
    return studentName;
  }
  return profile.displayName || profile.username || "";
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
  navigateWithViewTransition(router, getMenuLocation(key));
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
  const { studentName, studentNo, embedValue } = activeStudentQuery.value;
  const query = { category: safeKey };
  if (studentName) {
    query.studentName = studentName;
  }
  if (studentNo) {
    query.studentNo = studentNo;
  }
  if (embedValue) {
    query.embed = embedValue;
  }
  navigateWithViewTransition(router, { path: "/achievements", query });
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
  navigateWithViewTransition(router, "/settings");
}

function resetForm() {
  form.imageUrl = "";
  form.imageUrls = [];
  form.attachments = [];
  form.category = "";
  form.fields = {};
}

async function saveAchievement() {
  const config = activeFormConfig.value;
  if (!config) {
    errorMessage.value = "请先选择成就分类";
    return;
  }
  const category =
    form.category ||
    (activeCategory.value === "all" ? "" : activeCategory.value);
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
    imageUrl: form.imageUrls[0] || form.imageUrl || null,
    fields: {
      ...form.fields,
      [IMAGE_URLS_FIELD]: JSON.stringify(form.imageUrls || []),
      [ATTACHMENTS_FIELD]: JSON.stringify(form.attachments || []),
    },
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

function triggerAttachment() {
  attachmentInput.value && attachmentInput.value.click();
}

async function onImageChange(event) {
  const files = Array.from(event.target.files || []);
  event.target.value = "";
  if (!files.length) {
    return;
  }
  const remaining = imageMaxCount.value - form.imageUrls.length;
  if (remaining <= 0) {
    errorMessage.value = `最多上传${imageMaxCount.value}张图片`;
    return;
  }
  const uploadList = files.slice(0, remaining);
  for (const file of uploadList) {
    if (!isAllowedImage(file)) {
      errorMessage.value = "仅支持 jpeg/jpg/png/heif 图片格式";
      continue;
    }
    if (!isFileSizeAllowed(file, uploadLimitConfig.mediaMaxMB)) {
      errorMessage.value = `图片大小不可超过 ${mediaLimitLabel.value}`;
      continue;
    }
    try {
      const { data } = await uploadMedia(file, {
        context: "achievement-image",
      });
      if (data?.url) {
        form.imageUrls.push(data.url);
      }
    } catch (err) {
      errorMessage.value = err?.response?.data?.message || "图片上传失败";
    }
  }
}

async function onAttachmentChange(event) {
  const files = Array.from(event.target.files || []);
  event.target.value = "";
  if (!files.length) {
    return;
  }
  for (const file of files) {
    if (!isAllowedAttachment(file)) {
      errorMessage.value = "附件格式不支持";
      continue;
    }
    if (!isFileSizeAllowed(file, uploadLimitConfig.attachmentMaxMB)) {
      errorMessage.value = `附件大小不可超过 ${attachmentLimitLabel.value}`;
      continue;
    }
    try {
      const { data } = await uploadMedia(file, {
        context: "achievement-attachment",
      });
      if (data?.url) {
        form.attachments.push({
          url: data.url,
          name: data.originalName || file.name,
          mediaType: data.mediaType || resolveMediaTypeByExtension(file.name),
        });
      }
    } catch (err) {
      errorMessage.value = err?.response?.data?.message || "附件上传失败";
    }
  }
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
  const teachers = fields.instructors
    ? ` | 指导老师：${fields.instructors}`
    : "";
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
  const imageUrls = resolveImageUrls(item);
  const attachments = resolveAttachments(fields);
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
    image: imageUrls[0] || "",
    imageUrls,
    attachments,
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
  form.imageUrls = (item.imageUrls || [])
    .map((url) => stripMediaUrl(url))
    .filter(Boolean);
  form.imageUrl = form.imageUrls[0] || "";
  form.attachments = (item.attachments || []).map((entry) => ({
    ...entry,
    url: stripMediaUrl(entry.url),
  }));
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
  if (deleteDialogOpen.value) {
    return;
  }
  deleteDialogOpen.value = true;
}

function isVideoUrl(url) {
  if (!url) return false;
  const ext = resolveMediaTypeByExtension(url);
  return ["mp4", "mov"].includes(ext);
}

function isVideoFile(file) {
  if (!file || !file.name) return false;
  const ext = resolveMediaTypeByExtension(file.name);
  return ["mp4", "mov"].includes(ext);
}

function isDocumentFile(file) {
  if (!file || !file.name) return false;
  const ext = resolveMediaTypeByExtension(file.name);
  return ["doc", "docx"].includes(ext);
}

function isSheetFile(file) {
  if (!file || !file.name) return false;
  const ext = resolveMediaTypeByExtension(file.name);
  return ["xls", "xlsx"].includes(ext);
}

function isPdfFile(file) {
  if (!file || !file.name) return false;
  const ext = resolveMediaTypeByExtension(file.name);
  return ["pdf"].includes(ext);
}

function isPptxFile(file) {
  if (!file || !file.name) return false;
  const ext = resolveMediaTypeByExtension(file.name);
  return ["ppt", "pptx"].includes(ext);
}

function showPreview(urls, index = 0) {
  previewImages.value = urls;
  previewIndex.value = index;
  previewVisible.value = true;
  document.body.style.overflow = 'hidden';
  previewContent.value = "";
  previewLoading.value = false;
  // Detect media type
  if (urls.length > 0) {
    const url = urls[index];
    if (isMediaVideo(url)) {
      previewType.value = "video";
    } else if (isMediaDocument(url)) {
      previewType.value = "document";
      loadDocumentPreview(url);
    } else if (isMediaSheet(url)) {
      previewType.value = "sheet";
      loadSheetPreview(url);
    } else if (isMediaPdf(url)) {
      previewType.value = "pdf";
      loadPdfPreview(url);
    } else {
      previewType.value = "image";
    }
  }
}

async function loadDocumentPreview(url) {
  previewLoading.value = true;
  try {
    const html = await renderDocx(url);
    previewContent.value = html;
  } catch (e) {
    previewContent.value = `<div class="docx-error"><div>加载失败</div></div>`;
  } finally {
    previewLoading.value = false;
  }
}

async function loadSheetPreview(url) {
  previewLoading.value = true;
  try {
    const result = await renderSheet(url);
    previewContent.value = result.html;
    // Store workbook for sheet switching
    if (result.workbook) {
      previewWorkbook.value = result.workbook;
    }
  } catch (e) {
    previewContent.value = `<div class="sheet-error"><div>加载失败</div></div>`;
  } finally {
    previewLoading.value = false;
  }
}

async function loadPdfPreview(url) {
  previewLoading.value = true;
  try {
    const html = await renderPdf(url);
    previewContent.value = html;
  } catch (e) {
    previewContent.value = `<div class="pdf-error"><div>加载失败</div></div>`;
  } finally {
    previewLoading.value = false;
  }
}

async function switchSheet(sheetIndex) {
  if (!previewWorkbook.value) return;
  previewLoading.value = true;
  try {
    const XLSX = await import("xlsx");
    const sheetNames = previewWorkbook.value.SheetNames;
    const sheet = previewWorkbook.value.Sheets[sheetNames[sheetIndex]];
    const html = XLSX.utils.sheet_to_html(sheet, {
      header: true,
      footer: false,
      editable: false,
    });
    // Build new tabs
    const tabs = sheetNames.map((name, i) => `
      <div class="sheet-tab ${i === sheetIndex ? 'active' : ''}" onclick="window.__switchSheet(${i})">
        ${name}
      </div>
    `).join('');
    previewContent.value = `
      <div class="sheet-container">
        <div class="sheet-tabs">${tabs}</div>
        <div class="sheet-content">
          <div class="sheet-table-wrapper">${html}</div>
        </div>
        <div class="sheet-footer">
          <span>${sheetNames.length} 个工作表</span>
        </div>
      </div>
    `;
  } catch (e) {
    previewContent.value = `<div class="sheet-error"><div>切换失败</div></div>`;
  } finally {
    previewLoading.value = false;
  }
}

function hidePreview() {
  previewVisible.value = false;
  document.body.style.overflow = '';
}

function previewPrev() {
  if (previewIndex.value > 0) {
    slideDirection.value = "left";
    previewIndex.value--;
  }
}

function previewNext() {
  if (previewIndex.value < previewImages.value.length - 1) {
    slideDirection.value = "right";
    previewIndex.value++;
  }
}

function closeDelete() {
  if (deleteBusy.value || !deleteDialogOpen.value) {
    return;
  }
  deleteDialogOpen.value = false;
}


function selectEditorImage(index) {
  if (index < 0 || index >= form.imageUrls.length) {
    return;
  }
  const [selected] = form.imageUrls.splice(index, 1);
  form.imageUrls.unshift(selected);
}

function removeImage(index) {
  form.imageUrls.splice(index, 1);
}

function removeAttachment(index) {
  form.attachments.splice(index, 1);
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
    closeView();
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || "删除失败";
  } finally {
    deleteBusy.value = false;
    closeDelete();
  }
}

async function fetchAchievements() {
  try {
    const params = {};
    if (activeCategory.value && activeCategory.value !== "all") {
      params.category = activeCategory.value;
    }
    const { studentName, studentNo } = activeStudentQuery.value;
    if (studentName) {
      params.studentName = studentName;
    }
    if (studentNo) {
      params.studentNo = studentNo;
    }
    const { data } = await getAchievements(params);
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
    const { studentName, studentNo, embedValue } = activeStudentQuery.value;
    const query = { category: safeCategory };
    if (studentName) {
      query.studentName = studentName;
    }
    if (studentNo) {
      query.studentNo = studentNo;
    }
    if (embedValue) {
      query.embed = embedValue;
    }
    router.replace({
      path: "/achievements",
      query,
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
    form.fields.studentName = getCurrentStudentName();
  }
}

const IMAGE_URLS_FIELD = "_imageUrls";
const ATTACHMENTS_FIELD = "_attachments";
const uploadLimitConfig = reactive({
  imageMaxCount: achievementUploadSettings.imageMaxCount,
  mediaMaxMB: achievementUploadSettings.imageMaxSizeMb,
  attachmentMaxMB: achievementUploadSettings.attachmentMaxSizeMb,
});

const imageMaxCount = computed(() => uploadLimitConfig.imageMaxCount);

const mediaLimitLabel = computed(() =>
  formatFileSize(uploadLimitConfig.mediaMaxMB),
);
const attachmentLimitLabel = computed(() =>
  formatFileSize(uploadLimitConfig.attachmentMaxMB),
);

const imagePreviews = computed(() =>
  (form.imageUrls || []).map((url) => resolveMediaUrl(url)),
);
const attachmentPreviews = computed(() =>
  (form.attachments || []).map((file) => ({
    ...file,
    url: resolveMediaUrl(file.url),
  })),
);

function setUploadLimits({ imageMaxCount, mediaMaxMB, attachmentMaxMB }) {
  if (Number.isFinite(imageMaxCount) && imageMaxCount > 0) {
    uploadLimitConfig.imageMaxCount = imageMaxCount;
  }
  if (Number.isFinite(mediaMaxMB) && mediaMaxMB > 0) {
    uploadLimitConfig.mediaMaxMB = mediaMaxMB;
  }
  if (Number.isFinite(attachmentMaxMB) && attachmentMaxMB > 0) {
    uploadLimitConfig.attachmentMaxMB = attachmentMaxMB;
  }
}

const attachmentIconMap = {
  doc: "/assets/icons/doc.svg",
  docx: "/assets/icons/doc.svg",
  pdf: "/assets/icons/pdf.svg",
  xls: "/assets/icons/excel.svg",
  xlsx: "/assets/icons/excel.svg",
  ppt: "/assets/icons/ppt.svg",
  pptx: "/assets/icons/ppt.svg",
  zip: "/assets/icons/zip.svg",
  rar: "/assets/icons/zip.svg",
  "7z": "/assets/icons/zip.svg",
  mp4: "/assets/icons/video.svg",
  mov: "/assets/icons/video.svg",
  jpeg: "/assets/icons/image.svg",
  jpg: "/assets/icons/image.svg",
  png: "/assets/icons/image.svg",
  heif: "/assets/icons/image.svg",
};

function attachmentIcon(file) {
  const ext = resolveMediaTypeByExtension(file.name || file.url || "");
  return attachmentIconMap[ext] || "/assets/icons/doc.svg";
}

function isAllowedImage(file) {
  const ext = resolveMediaTypeByExtension(file.name || "");
  return ["jpeg", "jpg", "png", "heif"].includes(ext);
}

function isAllowedAttachment(file) {
  const ext = resolveMediaTypeByExtension(file.name || "");
  return [
    "docx",
    "doc",
    "pdf",
    "xls",
    "xlsx",
    "zip",
    "rar",
    "7z",
    "pptx",
    "ppt",
    "mp4",
    "mov",
    "jpeg",
    "jpg",
    "png",
    "heif",
  ].includes(ext);
}

function isFileSizeAllowed(file, limitMb) {
  if (!Number.isFinite(limitMb)) {
    return true;
  }
  return file.size / (1024 * 1024) <= limitMb;
}

function formatFileSize(value) {
  return `${value}MB`;
}

function resolveMediaTypeByExtension(name = "") {
  const cleanName = name.toLowerCase();
  const parts = cleanName.split(".");
  if (parts.length < 2) {
    return "";
  }
  return parts.pop() || "";
}

function stripMediaUrl(url) {
  if (!url) {
    return "";
  }
  if (url.startsWith(API_BASE)) {
    return url.replace(API_BASE, "");
  }
  return url;
}

function resolveImageUrls(item) {
  const urls = [];
  if (item?.imageUrl) {
    urls.push(resolveMediaUrl(item.imageUrl));
  }
  const rawField = item?.fields?.[IMAGE_URLS_FIELD];
  const parsed = parseJsonArray(rawField);
  parsed.forEach((url) => {
    const resolved = resolveMediaUrl(url);
    if (resolved && !urls.includes(resolved)) {
      urls.push(resolved);
    }
  });
  return urls.slice(0, uploadLimitConfig.imageMaxCount);
}

function resolveAttachments(fields = {}) {
  const raw = fields[ATTACHMENTS_FIELD];
  const parsed = parseJsonArray(raw);
  return parsed
    .map((item) => ({
      url: resolveMediaUrl(item.url),
      name: item.name || item.originalName || "附件",
      mediaType: item.mediaType || "",
    }))
    .filter((item) => item.url);
}

function parseJsonArray(value) {
  if (!value) {
    return [];
  }
  try {
    const parsed = JSON.parse(value);
    return Array.isArray(parsed) ? parsed : [];
  } catch (err) {
    return [];
  }
}

function handleUploadSettingsUpdated(event) {
  const nextSettings = event.detail || {};
  setUploadLimits({
    imageMaxCount: nextSettings.imageMaxCount,
    mediaMaxMB: nextSettings.imageMaxSizeMb,
    attachmentMaxMB: nextSettings.attachmentMaxSizeMb,
  });
}

onMounted(() => {
  syncCategoryFromRoute();
  fetchAchievementUploadSettings().then(() => {
    setUploadLimits({
      imageMaxCount: achievementUploadSettings.imageMaxCount,
      mediaMaxMB: achievementUploadSettings.imageMaxSizeMb,
      attachmentMaxMB: achievementUploadSettings.attachmentMaxSizeMb,
    });
  });
  fetchAchievements();
  window.addEventListener(
    "achievement-upload-settings-updated",
    handleUploadSettingsUpdated,
  );
  // Expose sheet switcher globally for v-html content
  window.__switchSheet = (index) => {
    switchSheet(index);
  };
});

onBeforeUnmount(() => {
  window.removeEventListener(
    "achievement-upload-settings-updated",
    handleUploadSettingsUpdated,
  );
  if (window.__switchSheet) {
    delete window.__switchSheet;
  }
});

watch(
  () => [route.query.category, route.query.studentNo, route.query.studentName],
  () => {
    syncCategoryFromRoute();
    fetchAchievements();
  },
);

watch(
  () => form.category,
  () => {
    applyFieldDefaults();
  },
);

// Delegate sheet tab clicks after content renders
watch(previewContent, () => {
  if (previewType.value === 'sheet') {
    nextTick(() => {
      const container = document.querySelector(".viewer-document");
      if (container) {
        container.onclick = (e) => {
          const tab = e.target.closest(".sheet-tab");
          if (tab) {
            const idx = parseInt(tab.dataset.sheet, 10);
            if (!isNaN(idx)) {
              switchSheet(idx);
            }
          }
        };
      }
    });
  }
});
</script>
