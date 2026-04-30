<template>
  <main class="dashboard-right">
    <header class="feed-header">
      <h1 class="feed-title">我的信息</h1>
    </header>

    <section class="info-shell" :class="{ 'info-shell-editing': isEditing }">
      <div class="info-hero">
        <button
          class="avatar-square"
          type="button"
          :disabled="!isEditing"
          @click="triggerAvatarUpload"
        >
          <img
            v-if="info.avatarUrl"
            :src="resolveMediaUrl(info.avatarUrl)"
            alt="头像"
          />
          <span v-else>点击设置头像</span>
          <input
            ref="avatarInput"
            type="file"
            accept="image/*"
            hidden
            @change="onAvatarChange"
          />
        </button>
        <div class="info-hero-text">
          <div class="info-hero-title">基础信息</div>
          <div class="info-hero-subtitle">请使用真实照片，确保五官清晰。</div>
        </div>
        <div class="info-actions">
          <ExportPdfButton
            :disabled="isEditing"
            :get-student="buildPdfStudentSnapshot"
            :resolve-media-url="resolveMediaUrl"
            button-class="ghost-button"
            @export-complete="toastSuccess('PDF 导出成功！')"
            @export-error="toastError('PDF 导出失败')"
          />
          <button
            class="action-button"
            type="button"
            :disabled="isEditing"
            @click="enterEdit"
          >
            编辑
          </button>
        </div>
      </div>

      <div class="card info-card">
        <div class="info-section-title">学籍信息</div>
        <div class="info-form-grid">
          <label class="field-card">
            <span class="info-label">名字</span>
            <input
              v-model="info.name"
              class="info-input"
              type="text"
              placeholder="请输入名字"
              :disabled="!isEditing"
            />
          </label>
          <label class="field-card">
            <span class="info-label">学号</span>
            <input
              v-model="info.studentNo"
              class="info-input"
              type="text"
              placeholder="请输入学号"
              disabled
            />
          </label>
          <label class="field-card">
            <span class="info-label">年级</span>
            <YearPicker
              v-model="info.classYear"
              placeholder="今年"
              :disabled="!isEditing"
            />
          </label>
          <label class="field-card">
            <span class="info-label">学生类别</span>
            <select
              v-model="info.studentCategory"
              class="info-input"
              :disabled="!isEditing"
            >
              <option disabled value="">选择学生类别</option>
              <option
                v-for="item in studentCategoryOptions"
                :key="item"
                :value="item"
              >
                {{ item }}
              </option>
            </select>
          </label>
          <label class="field-card field-full">
            <span class="info-label">班级</span>
            <div class="class-inline">
              <select
                v-model="info.classMajor"
                class="info-input"
                :disabled="
                  !isEditing ||
                  !info.college ||
                  !info.studentCategory ||
                  !classMajorOptions.length
                "
              >
                <option disabled value="">选择专业</option>
                <option
                  v-for="major in classMajorOptions"
                  :key="major"
                  :value="major"
                >
                  {{ major }}
                </option>
              </select>
              <select
                v-model.number="info.classNo"
                class="info-input class-num"
                :disabled="!isEditing || info.studentCategory === '研究生'"
              >
                <option disabled value="">班</option>
                <option v-for="n in 10" :key="n" :value="n">
                  {{ n }}
                </option>
              </select>
              <span class="class-text">班</span>
            </div>
          </label>
          <label class="field-card">
            <span class="info-label">班主任</span>
            <input
              v-model="info.classTeacher"
              class="info-input"
              type="text"
              placeholder="请输入班主任"
              :disabled="!isEditing"
            />
          </label>
          <label class="field-card">
            <span class="info-label">辅导员</span>
            <input
              v-model="info.counselor"
              class="info-input"
              type="text"
              placeholder="请输入辅导员"
              :disabled="!isEditing"
            />
          </label>
          <label class="field-card field-full">
            <span class="info-label">入学时间</span>
            <input
              v-model="info.enrollmentDate"
              class="info-input"
              type="date"
              lang="zh-CN"
              :max="today"
              :disabled="!isEditing"
            />
          </label>
        </div>
      </div>

      <div class="card info-card">
        <div class="info-section-title">个人证件与联系方式</div>
        <div class="info-form-grid">
          <label class="field-card">
            <span class="info-label">民族</span>
            <div class="class-inline">
              <input
                v-model="info.ethnicity"
                class="info-input"
                type="text"
                placeholder="请输入民族"
                :disabled="!isEditing"
              />
              <span class="class-text">族</span>
            </div>
          </label>
          <label class="field-card">
            <span class="info-label">政治面貌</span>
            <select
              v-model="info.politicalStatus"
              class="info-input"
              :disabled="!isEditing"
            >
              <option disabled value="">选择政治面貌</option>
              <option
                v-for="item in politicalStatusOptions"
                :key="item"
                :value="item"
              >
                {{ item }}
              </option>
            </select>
          </label>
          <label class="field-card field-full">
            <span class="info-label">手机号码 / 备用联系方式</span>
            <div class="class-inline">
              <input
                v-model="info.phone"
                class="info-input"
                type="tel"
                placeholder="手机号"
                maxlength="11"
                inputmode="numeric"
                @input="handleDigitsInput('phone', 11, $event)"
                :disabled="!isEditing"
              />
              <span class="class-text">/</span>
              <input
                v-model="info.backupContact"
                class="info-input"
                type="text"
                placeholder="微信/QQ/邮箱"
                :disabled="!isEditing"
              />
            </div>
          </label>
          <label class="field-card field-full">
            <span class="info-label">证件类型 / 证件号码</span>
            <div class="class-inline id-type-inline">
              <select
                v-model="info.idType"
                class="info-input"
                :disabled="!isEditing"
              >
                <option v-for="item in idTypeOptions" :key="item" :value="item">
                  {{ item }}
                </option>
              </select>
              <input
                v-model="info.idNo"
                class="info-input"
                type="text"
                placeholder="证件号码"
                :maxlength="idNoMaxLength"
                inputmode="text"
                @input="handleIdNoInput"
                :disabled="!isEditing"
              />
            </div>
            <div v-if="idNoHint" class="info-hint">
              {{ idNoHint }}，如与真实证件不符请联系管理员
            </div>
          </label>
          <label class="field-card">
            <span class="info-label">出生年月</span>
            <input
              v-model="info.birthDate"
              class="info-input"
              type="date"
              lang="zh-CN"
              :max="today"
              :disabled="!isEditing"
            />
          </label>
          <label class="field-card">
            <!-- TODO: 做地址选择器 -->
            <span class="info-label">籍贯</span>
            <input
              v-model="info.nativePlace"
              class="info-input"
              type="text"
              placeholder="例：广东广州"
              :disabled="!isEditing"
            />
          </label>
          <label class="field-card field-full">
            <!-- TODO: 做地址选择器 -->
            <span class="info-label">住址</span>
            <div class="info-inline address-inline">
              <select
                v-model="info.addressProvince"
                class="info-input"
                :disabled="!isEditing"
              >
                <option disabled value="">选择省份</option>
                <option
                  v-for="item in addressProvinceOptions"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </option>
              </select>
              <select
                v-model="info.addressCity"
                class="info-input"
                :disabled="!isEditing || !addressCityOptions.length"
              >
                <option disabled value="">选择城市</option>
                <option
                  v-for="item in addressCityOptions"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </option>
              </select>
              <select
                v-model="info.addressCounty"
                class="info-input"
                :disabled="!isEditing || !addressCountyOptions.length"
              >
                <option disabled value="">选择区县</option>
                <option
                  v-for="item in addressCountyOptions"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </option>
              </select>
            </div>
            <input
              v-model="info.addressDetail"
              class="info-input address-detail"
              type="text"
              placeholder="请输入详细地址，精确到门牌号"
              :disabled="!isEditing"
            />
          </label>
        </div>
      </div>

      <div class="card info-card">
        <div class="info-section-title">住宿信息</div>
        <div class="info-form-grid">
          <div class="field-card field-full">
            <span class="info-label">是否在外居住</span>
            <div class="info-inline">
              <label class="info-choice">
                <input
                  v-model="info.offCampusLiving"
                  type="radio"
                  :value="true"
                  :disabled="!isEditing"
                />
                是
              </label>
              <label class="info-choice">
                <input
                  v-model="info.offCampusLiving"
                  type="radio"
                  :value="false"
                  :disabled="!isEditing"
                />
                否
              </label>
            </div>
          </div>
          <label class="field-card field-full" v-if="info.offCampusLiving">
            <span class="info-label">外居住详细地址</span>
            <div class="info-inline address-inline">
              <select
                v-model="info.offCampusProvince"
                class="info-input"
                :disabled="!isEditing"
              >
                <option disabled value="">选择省份</option>
                <option
                  v-for="item in addressProvinceOptions"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </option>
              </select>
              <select
                v-model="info.offCampusCity"
                class="info-input"
                :disabled="!isEditing || !offCampusCityOptions.length"
              >
                <option disabled value="">选择城市</option>
                <option
                  v-for="item in offCampusCityOptions"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </option>
              </select>
              <select
                v-model="info.offCampusCounty"
                class="info-input"
                :disabled="!isEditing || !offCampusCountyOptions.length"
              >
                <option disabled value="">选择区县</option>
                <option
                  v-for="item in offCampusCountyOptions"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </option>
              </select>
            </div>
            <input
              v-model="info.offCampusDetail"
              class="info-input address-detail"
              type="text"
              placeholder="请输入详细地址，精确到门牌号"
              :disabled="!isEditing"
            />
          </label>
          <label class="field-card" v-if="!info.offCampusLiving">
            <span class="info-label">住宿校区</span>
            <select
              v-model="info.dormCampus"
              class="info-input"
              :disabled="!isEditing || info.offCampusLiving"
            >
              <option disabled value="">选择住宿校区</option>
              <option
                v-for="item in dormCampusOptions"
                :key="item"
                :value="item"
              >
                {{ item }}
              </option>
            </select>
          </label>
          <label class="field-card" v-if="!info.offCampusLiving">
            <!-- TODO: 等待佩佩姐发文件 -->
            <span class="info-label">住宿楼栋</span>
            <select
              v-model="info.dormBuilding"
              class="info-input"
              :disabled="dormBuildingDisabled"
            >
              <option disabled value="">选择住宿楼栋</option>
              <option
                v-for="item in dormBuildingOptions"
                :key="item.value"
                :value="item.value"
                :disabled="item.disabled"
              >
                {{ item.label }}
              </option>
            </select>
          </label>
          <label class="field-card field-full" v-if="!info.offCampusLiving">
            <span class="info-label">住宿房间</span>
            <div class="class-inline">
              <input
                v-model="info.dormFloor"
                class="info-input class-num"
                type="number"
                min="1"
                step="1"
                placeholder="楼层"
                :disabled="dormRoomDisabled"
              />
              <span class="class-text">层</span>
              <input
                v-model="info.dormRoomNo"
                class="info-input"
                type="text"
                placeholder="房间号"
                :disabled="dormRoomDisabled"
              />
              <span class="class-text">号</span>
            </div>
            <div class="info-hint">如：223 -> 2 层 23 号</div>
          </label>
        </div>
      </div>

      <div class="card info-card">
        <div class="info-section-title">团组织与入党信息</div>
        <div class="info-form-grid three">
          <div class="field-card field-full">
            <span class="info-label">是否入团</span>
            <div class="info-inline">
              <label class="info-choice">
                <input
                  v-model="info.leagueJoined"
                  type="radio"
                  :value="true"
                  :disabled="!isEditing"
                />
                是
              </label>
              <label class="info-choice">
                <input
                  v-model="info.leagueJoined"
                  type="radio"
                  :value="false"
                  :disabled="!isEditing"
                />
                否
              </label>
            </div>
          </div>
          <template v-if="info.leagueJoined">
            <label class="field-card field-full">
              <span class="info-label">提交入团申请书时间</span>
              <input
                v-model="info.leagueApplicationDate"
                class="info-input"
                type="date"
                lang="zh-CN"
                :max="today"
                :disabled="leagueApplicationDisabled"
              />
            </label>
            <label class="field-card field-full">
              <span class="info-label">入团时间</span>
              <div class="info-inline">
                <input
                  v-model="info.leagueJoinDate"
                  class="info-input"
                  type="date"
                  lang="zh-CN"
                  :max="today"
                  :disabled="leagueJoinDisabled"
                />
                <label class="info-choice info-choice-muted">
                  <input
                    v-model="info.leagueDeveloping"
                    type="checkbox"
                    :disabled="leagueApplicationDisabled"
                  />
                  正在发展
                </label>
              </div>
            </label>
            <label class="field-card field-full">
              <span class="info-label">团号</span>
              <input
                v-model="info.leagueNo"
                class="info-input"
                type="text"
                placeholder="请输入团号"
                :disabled="leagueNoDisabled"
              />
            </label>
            <div class="field-card field-full">
              <span class="info-label">是否申请入党</span>
              <div class="info-inline">
                <label class="info-choice">
                  <input
                    v-model="info.partyApplied"
                    type="radio"
                    :value="true"
                    :disabled="partyAppliedDisabled"
                  />
                  是
                </label>
                <label class="info-choice">
                  <input
                    v-model="info.partyApplied"
                    type="radio"
                    :value="false"
                    :disabled="partyAppliedDisabled"
                  />
                  否
                </label>
              </div>
            </div>
            <template v-if="info.partyApplied">
              <label class="field-card field-full">
                <span class="info-label">提交入党申请书时间</span>
                <input
                  v-model="info.applicationDate"
                  class="info-input"
                  type="date"
                  lang="zh-CN"
                  :max="today"
                  :disabled="applicationDateDisabled"
                />
              </label>
              <label class="field-card field-full">
                <span class="info-label">确定积极分子时间</span>
                <div class="info-inline">
                  <input
                    v-model="info.activistDate"
                    class="info-input"
                    type="date"
                    lang="zh-CN"
                    :max="today"
                    :disabled="activistDateDisabled"
                  />
                  <label class="info-choice info-choice-muted">
                    <input
                      v-model="info.activistDeveloping"
                      type="checkbox"
                      :disabled="applicationDateDisabled"
                    />
                    正在发展
                  </label>
                </div>
              </label>
              <label class="field-card field-full">
                <span class="info-label">上党课时间</span>
                <div class="info-inline">
                  <input
                    v-model="info.partyTrainingDate"
                    class="info-input"
                    type="date"
                    lang="zh-CN"
                    :max="today"
                    :disabled="partyTrainingDisabled"
                  />
                  <label class="info-choice info-choice-muted">
                    <input
                      v-model="info.partyTrainingPending"
                      type="checkbox"
                      :disabled="activistDateDisabled"
                    />
                    暂未报名
                  </label>
                </div>
              </label>
              <label class="field-card field-full">
                <span class="info-label">确定发展对象时间</span>
                <div class="info-inline">
                  <input
                    v-model="info.developmentTargetDate"
                    class="info-input"
                    type="date"
                    lang="zh-CN"
                    :max="today"
                    :disabled="developmentTargetDisabled"
                  />
                  <label class="info-choice info-choice-muted">
                    <input
                      v-model="info.developmentTargetDeveloping"
                      type="checkbox"
                      :disabled="partyTrainingDisabled"
                    />
                    正在发展
                  </label>
                </div>
              </label>
              <label class="field-card field-full">
                <span class="info-label">接收为预备党员时间</span>
                <div class="info-inline">
                  <input
                    v-model="info.probationaryMemberDate"
                    class="info-input"
                    type="date"
                    lang="zh-CN"
                    :max="today"
                    :disabled="probationaryDisabled"
                  />
                  <label class="info-choice info-choice-muted">
                    <input
                      v-model="info.probationaryDeveloping"
                      type="checkbox"
                      :disabled="developmentTargetDisabled"
                    />
                    正在发展
                  </label>
                </div>
              </label>
              <label class="field-card field-full">
                <span class="info-label">转为正式党员时间</span>
                <div class="info-inline">
                  <input
                    v-model="info.fullMemberDate"
                    class="info-input"
                    type="date"
                    lang="zh-CN"
                    :max="today"
                    :disabled="fullMemberDisabled"
                  />
                  <label class="info-choice info-choice-muted">
                    <input
                      v-model="info.fullMemberDeveloping"
                      type="checkbox"
                      :disabled="probationaryDisabled"
                    />
                    正在发展
                  </label>
                </div>
              </label>
            </template>
          </template>
        </div>
      </div>

      <div class="card info-card">
        <div class="info-section-title">教育经历</div>
        <div class="info-hint">从小学开始填</div>
        <div ref="educationTableWrap" class="record-list-wrap">
          <transition-group name="education-row" tag="div" class="record-list">
            <RecordRow
              v-for="(item, index) in educationItems"
              :key="`edu-${index}`"
              type="education"
              :item="item"
              :index="index"
              :disabled="isEducationRowDisabled(index)"
              :today="today"
              @update:item="educationItems[index] = $event"
              @current-change="handleEducationCurrentChange"
            />
          </transition-group>
          <div class="record-controls">
            <button
              class="record-ctl"
              type="button"
              :disabled="!isEditing"
              aria-label="增加一行"
              @click="addEducationRow"
            >
              +
            </button>
            <button
              class="record-ctl"
              type="button"
              :disabled="!isEditing || educationItems.length <= 1"
              aria-label="减少一行"
              @click="removeEducationRow"
            >
              −
            </button>
            <button
              class="record-ctl record-ctl-clear"
              type="button"
              :disabled="!isEditing"
              aria-label="清空教育经历"
              @click="handleClearEducation"
            >
              清空
            </button>
          </div>
        </div>
      </div>

      <div class="card info-card">
        <div class="info-section-title">学生干部经历</div>
        <div ref="cadreTableWrap" class="record-list-wrap">
          <transition-group name="education-row" tag="div" class="record-list">
            <RecordRow
              v-for="(item, index) in cadreItems"
              :key="`cadre-${index}`"
              type="cadre"
              :item="item"
              :index="index"
              :disabled="isCadreRowDisabled(index)"
              :today="today"
              @update:item="cadreItems[index] = $event"
              @current-change="handleCadreCurrentChange"
            />
          </transition-group>
          <div class="record-controls">
            <button
              class="record-ctl"
              type="button"
              :disabled="!isEditing"
              aria-label="增加一行"
              @click="addCadreRow"
            >
              +
            </button>
            <button
              class="record-ctl"
              type="button"
              :disabled="!isEditing || cadreItems.length <= 1"
              aria-label="减少一行"
              @click="removeCadreRow"
            >
              −
            </button>
            <button
              class="record-ctl record-ctl-clear"
              type="button"
              :disabled="!isEditing"
              aria-label="清空干部经历"
              @click="handleClearCadre"
            >
              清空
            </button>
          </div>
        </div>
      </div>

      <div class="card info-card">
        <!-- TODO: 单亲/离异等待现场演示求助 -->
        <div class="info-section-title">
          家庭信息
          <button
            class="hint-button"
            type="button"
            aria-label="填写说明"
            @click="workUnitHintOpen = true"
          >
            ?
          </button>
        </div>
        <transition name="dialog-fade">
          <div
            v-if="workUnitHintOpen"
            class="dialog-backdrop"
            @click="workUnitHintOpen = false"
          ></div>
        </transition>
        <transition name="dialog-pop">
          <section v-if="workUnitHintOpen" class="dialog-card" @click.stop>
            <header class="dialog-header">填写说明</header>
            <div class="dialog-body">
              <div class="hint-item">
                <span class="hint-label">工作单位：</span>无
                <span class="hint-sep">|</span>
                <span class="hint-label">职务：</span>待业/务农
              </div>
              <div class="hint-item">
                <span class="hint-label">工作单位：</span>无固定单位
                <span class="hint-sep">|</span>
                <span class="hint-label">职务：</span>散工
              </div>
              <div class="hint-item">
                <span class="hint-label">工作单位：</span>个体户
                <span class="hint-sep">|</span>
                <span class="hint-label">职务：</span>店主
              </div>
            </div>
            <div class="dialog-actions">
              <button
                class="ghost-button"
                type="button"
                @click="workUnitHintOpen = false"
              >
                知道了
              </button>
            </div>
          </section>
        </transition>
        <div class="info-form-grid family-grid">
          <div class="family-section-title">父亲（监护人）</div>
          <label class="field-card">
            <span class="info-label">姓名</span>
            <input
              v-model="info.fatherName"
              class="info-input"
              type="text"
              placeholder="请输入父亲姓名"
              :disabled="!isEditing"
            />
          </label>
          <label class="field-card">
            <span class="info-label">手机号码</span>
            <input
              v-model="info.fatherPhone"
              class="info-input"
              type="tel"
              placeholder="请输入父亲手机号码"
              maxlength="11"
              inputmode="numeric"
              @input="handleDigitsInput('fatherPhone', 11, $event)"
              :disabled="!isEditing"
            />
          </label>
          <label class="field-card">
            <span class="info-label">工作单位</span>
            <input
              v-model="info.fatherWorkUnit"
              class="info-input"
              type="text"
              placeholder="请输入父亲工作单位"
              :disabled="!isEditing"
            />
          </label>
          <label class="field-card">
            <span class="info-label">职务</span>
            <input
              v-model="info.fatherTitle"
              class="info-input"
              type="text"
              placeholder="请输入父亲职务"
              :disabled="!isEditing"
            />
          </label>
          <div class="family-section-title">母亲（监护人2）</div>
          <label class="field-card">
            <span class="info-label">姓名</span>
            <input
              v-model="info.motherName"
              class="info-input"
              type="text"
              placeholder="请输入母亲姓名"
              :disabled="!isEditing"
            />
          </label>
          <label class="field-card">
            <span class="info-label">手机号码</span>
            <input
              v-model="info.motherPhone"
              class="info-input"
              type="tel"
              placeholder="请输入母亲手机号码"
              maxlength="11"
              inputmode="numeric"
              @input="handleDigitsInput('motherPhone', 11, $event)"
              :disabled="!isEditing"
            />
          </label>
          <label class="field-card">
            <span class="info-label">工作单位</span>
            <input
              v-model="info.motherWorkUnit"
              class="info-input"
              type="text"
              placeholder="请输入母亲工作单位"
              :disabled="!isEditing"
            />
          </label>
          <label class="field-card">
            <span class="info-label">职务</span>
            <input
              v-model="info.motherTitle"
              class="info-input"
              type="text"
              placeholder="请输入母亲职务"
              :disabled="!isEditing"
            />
          </label>
        </div>
      </div>

      <div class="card info-card">
        <div class="info-section-title">紧急联系人（除亲戚外）</div>
        <div class="info-form-grid">
          <label class="field-card">
            <span class="info-label">紧急联系人电话</span>
            <input
              v-model="info.emergencyPhone"
              class="info-input"
              type="tel"
              placeholder="请输入紧急联系人电话"
              maxlength="11"
              inputmode="numeric"
              @input="handleDigitsInput('emergencyPhone', 11, $event)"
              :disabled="!isEditing"
            />
          </label>
          <label class="field-card">
            <span class="info-label">紧急联系人的关系</span>
            <input
              v-model="info.emergencyRelation"
              class="info-input"
              type="text"
              placeholder="如父母、亲属"
              :disabled="!isEditing"
            />
          </label>
        </div>
      </div>
    </section>
    <transition name="edit-dock">
      <div v-if="isEditing" class="edit-dock">
        <div class="edit-dock-inner">
          <button class="ghost-button" type="button" @click="cancelEdit">
            取消
          </button>
          <button class="action-button" type="button" @click="confirmEdit">
            {{ saveActionLabel }}
          </button>
        </div>
      </div>
    </transition>
    <MobileCapsule @open-sidebar="openDashboardSidebar">
      <template v-if="isEditing" #right>
        <div class="capsule-action" @click="cancelEdit">取消</div>
        <div class="capsule-primary" @click="confirmEdit">
          {{ saveActionLabel }}
        </div>
      </template>
    </MobileCapsule>
  </main>
</template>

<script setup>
import { reactive, computed, ref, onMounted, watch, nextTick } from "vue";
import { useRouter } from "vue-router";
import ExportPdfButton from "../components/ExportPdfButton.vue";
import MobileCapsule from "../components/MobileCapsule.vue";
import YearPicker from "../components/YearPicker.vue";
import RecordRow from "../components/RecordRow.vue";
import { getMenuLocation, isMenuEnabled } from "../constants/menu";
import { regionData, codeToText } from "element-china-area-data";
import { getStudentProfile, saveStudentProfile } from "../api/profile";
import { uploadMedia } from "../api/upload";
import { useUploadProgress } from "../composables/useUploadProgress";
import { API_BASE } from "../api/request";
import { navigateWithViewTransition } from "../utils/viewTransition";
import { resolveMediaUrl } from "../utils/media";
import {
  buildClassName,
  buildDormRoom,
  buildAddress,
  parseAddressToRegion,
  parseDormRoom,
} from "../utils/profile";
import { useDashboardShell } from "../composables/useDashboardShell";
import { useNotifications } from "../composables/useNotifications";
import { useReviewSettings } from "../composables/useReviewSettings";
import { useToast } from "../composables/useToast";

const router = useRouter();
const { openSidebar: openDashboardSidebar } = useDashboardShell();
const FIXED_COLLEGE = "大数据与人工智能学院";

const profile = reactive(loadUser());
const activeMenu = ref("my-info");
const activeAchievement = ref("all");
const isEditing = ref(false);
const avatarInput = ref(null);
const sidebarOpen = ref(false);
const achievementsOpen = ref(false);
const educationTableWrap = ref(null);
const cadreTableWrap = ref(null);
const workUnitHintOpen = ref(false);
const today = getTodayString();
const originalProfileData = ref(null);
const savedProfileData = ref(null);
const {
  submitProfileReviewRequest,
  updateReviewRequestStatus,
  fetchProfileReviewRequests,
  hasPendingProfileReviewRequest,
} = useNotifications(profile);
const { settings: reviewSettings, fetchSettings: fetchReviewSettings } =
  useReviewSettings();
const { success: toastSuccess, error: toastError } = useToast();
const { uploadWithProgress } = useUploadProgress();

const info = reactive({
  name: profile.displayName || profile.username || "",
  avatarUrl: profile.avatarUrl || "",
  studentNo: profile.studentNo || "",
  classYear: new Date().getFullYear(),
  classMajor: "",
  classNo: 1,
  className: profile.className || "",
  college: FIXED_COLLEGE,
  enrollmentDate: "",
  studentCategory: "",
  ethnicity: "",
  politicalStatus: "",
  dormCampus: "",
  dormBuilding: "",
  dormRoom: "",
  dormFloor: "",
  dormRoomNo: "",
  offCampusLiving: false,
  offCampusAddress: "",
  classTeacher: "",
  counselor: "",
  phone: "",
  backupContact: "",
  address: "",
  addressProvince: "",
  addressCity: "",
  addressCounty: "",
  addressDetail: "",
  offCampusProvince: "",
  offCampusCity: "",
  offCampusCounty: "",
  offCampusDetail: "",
  idType: "居民身份证",
  idNo: "",
  birthDate: "",
  nativePlace: "",
  leagueNo: "",
  leagueApplicationDate: "",
  leagueJoinDate: "",
  leagueJoined: false,
  leagueDeveloping: false,
  partyApplied: false,
  notDeveloped: false,
  applicationDate: "",
  activistDate: "",
  activistDeveloping: false,
  partyTrainingDate: "",
  partyTrainingPending: false,
  developmentTargetDate: "",
  developmentTargetDeveloping: false,
  probationaryMemberDate: "",
  probationaryDeveloping: false,
  fullMemberDate: "",
  fullMemberDeveloping: false,
  emergencyPhone: "",
  emergencyRelation: "",
  isHk: false,
  isMo: false,
  isTw: false,
  specialStudent: false,
  specialStudentType: "",
  specialStudentRemark: "",
  fatherName: "",
  fatherPhone: "",
  fatherWorkUnit: "",
  fatherTitle: "",
  motherName: "",
  motherPhone: "",
  motherWorkUnit: "",
  motherTitle: "",
});

const majorOptionsByCategory = {
  本科生: [
    "计算机科学与技术",
    "计算机科学与技术（实验区）",
    "计算机科学与技术(中外联合培养项目班)",
    "2025计算机科学与技术（中外联合培养项目班未赴国外学习）",
    "软件工程",
    "人工智能",
    "电子商务",
    "电子商务（大数据决策分析）",
    "大数据管理与应用",
    "大数据管理与应用（佛山校区全学段）",
    "大数据管理与应用（数字治理）",
  ],
  研究生: [
    "管理科学与工程",
    "技术经济及管理",
    "智能科学与技术",
    "计算机技术",
    "图书情报",
  ],
};
const studentCategoryOptions = ["本科生", "研究生"];
const politicalStatusOptions = ["群众", "共青团员", "中共预备党员", "中共党员"];
const idTypeOptions = [
  "居民身份证",
  "台湾居民来往大陆通行证",
  "港澳居民来往内地通行证",
  "普通护照",
  "台湾居民居住证",
  "港澳居民居住证",
  "外国人永久居留身份证",
  "外国护照",
];

function detectIdType(raw) {
  if (!raw) return null;
  const cleaned = raw.toUpperCase().replace(/[^0-9A-Z]/g, "");
  if (!cleaned) return null;
  // 港澳居民来往内地通行证: H/M + 8 digits
  if (/^[HM]\d{8}$/.test(cleaned)) return "港澳居民来往内地通行证";
  // 普通护照: E + 8 digits
  if (/^E\d{8}$/.test(cleaned)) return "普通护照";
  // 18-digit: 居民身份证, 台湾居民居住证(83), 港澳居民居住证(81/82), 外国人永久居留身份证
  if (/^\d{17}[\dX]$/.test(cleaned)) {
    if (cleaned.startsWith("83")) return "台湾居民居住证";
    if (cleaned.startsWith("81") || cleaned.startsWith("82"))
      return "港澳居民居住证";
    return "居民身份证";
  }
  // 8-digit: 台湾居民来往大陆通行证(台胞证)
  // Must NOT start with 81/82/83 — those are partial 18-digit residence permits
  if (/^\d{8}$/.test(cleaned) && !/^8[123]/.test(cleaned))
    return "台湾居民来往大陆通行证";
  return null;
}
const dormCampusOptions = ["佛山校区", "广州校区"];
const PROFILE_CHANGE_FIELDS = [
  { key: "fullName", label: "姓名", section: "学籍信息" },
  { key: "avatarUrl", label: "头像", section: "基础信息" },
  { key: "studentNo", label: "学号", section: "学籍信息" },
  { key: "classYear", label: "年级", section: "学籍信息" },
  { key: "classMajor", label: "专业", section: "学籍信息" },
  { key: "classNo", label: "班级", section: "学籍信息" },
  { key: "className", label: "班级名称", section: "学籍信息" },
  { key: "enrollmentDate", label: "入学时间", section: "学籍信息" },
  { key: "studentCategory", label: "学生类别", section: "学籍信息" },
  { key: "ethnicity", label: "民族", section: "联系方式" },
  { key: "politicalStatus", label: "政治面貌", section: "联系方式" },
  { key: "phone", label: "手机号码", section: "联系方式" },
  { key: "backupContact", label: "备用联系方式", section: "联系方式" },
  { key: "address", label: "家庭住址", section: "联系方式" },
  { key: "idType", label: "证件类型", section: "联系方式" },
  { key: "idNo", label: "证件号码", section: "联系方式" },
  { key: "birthDate", label: "出生年月", section: "联系方式" },
  { key: "nativePlace", label: "籍贯", section: "联系方式" },
  { key: "dormCampus", label: "宿舍校区", section: "住宿信息" },
  { key: "dormBuilding", label: "宿舍楼栋", section: "住宿信息" },
  { key: "dormRoom", label: "宿舍房间", section: "住宿信息" },
  { key: "offCampusLiving", label: "是否校外居住", section: "住宿信息" },
  { key: "offCampusAddress", label: "校外住址", section: "住宿信息" },
  { key: "classTeacher", label: "班主任", section: "学籍信息" },
  { key: "counselor", label: "辅导员", section: "学籍信息" },
  { key: "leagueJoined", label: "是否入团", section: "党团信息" },
  { key: "leagueNo", label: "团员编号", section: "党团信息" },
  { key: "leagueApplicationDate", label: "入团申请时间", section: "党团信息" },
  { key: "leagueJoinDate", label: "入团时间", section: "党团信息" },
  { key: "partyApplied", label: "是否申请入党", section: "党团信息" },
  { key: "applicationDate", label: "入党申请时间", section: "党团信息" },
  { key: "activistDate", label: "积极分子时间", section: "党团信息" },
  { key: "partyTrainingDate", label: "党校培训时间", section: "党团信息" },
  { key: "developmentTargetDate", label: "发展对象时间", section: "党团信息" },
  { key: "probationaryMemberDate", label: "预备党员时间", section: "党团信息" },
  { key: "fullMemberDate", label: "转正时间", section: "党团信息" },
  { key: "emergencyPhone", label: "紧急联系人电话", section: "家庭信息" },
  { key: "emergencyRelation", label: "与紧急联系人关系", section: "家庭信息" },
  { key: "isHk", label: "香港身份", section: "身份信息" },
  { key: "isMo", label: "澳门身份", section: "身份信息" },
  { key: "isTw", label: "台湾身份", section: "身份信息" },
  { key: "specialStudent", label: "特殊学生", section: "身份信息" },
  { key: "specialStudentType", label: "特殊学生类型", section: "身份信息" },
  { key: "specialStudentRemark", label: "特殊学生备注", section: "身份信息" },
  { key: "fatherName", label: "父亲姓名", section: "家庭信息" },
  { key: "fatherPhone", label: "父亲电话", section: "家庭信息" },
  { key: "fatherWorkUnit", label: "父亲工作单位", section: "家庭信息" },
  { key: "fatherTitle", label: "父亲职务", section: "家庭信息" },
  { key: "motherName", label: "母亲姓名", section: "家庭信息" },
  { key: "motherPhone", label: "母亲电话", section: "家庭信息" },
  { key: "motherWorkUnit", label: "母亲工作单位", section: "家庭信息" },
  { key: "motherTitle", label: "母亲职务", section: "家庭信息" },
];
const idNoMaxLength = computed(() => {
  switch (info.idType) {
    case "居民身份证":
      return 18;
    case "台湾居民来往大陆通行证":
      return 8;
    case "港澳居民来往内地通行证":
      return 9;
    case "普通护照":
      return 9;
    case "台湾居民居住证":
      return 18;
    case "港澳居民居住证":
      return 18;
    case "外国人永久居留身份证":
      return 18;
    case "外国护照":
      return 20;
    default:
      return 32;
  }
});

const idNoHint = computed(() => {
  switch (info.idType) {
    case "居民身份证":
      return "18位（17位数字 + 1位数字/X）";
    case "台湾居民来往大陆通行证":
      return "8位纯数字（台胞证）";
    case "港澳居民来往内地通行证":
      return "9位（H/M + 8位数字，回乡证）";
    case "普通护照":
      return "9位（E + 8位数字）";
    case "台湾居民居住证":
      return "18位纯数字（83开头）";
    case "港澳居民居住证":
      return "18位纯数字（81/82开头）";
    case "外国人永久居留身份证":
      return "18位纯数字（新版五星卡）";
    case "外国护照":
      return "6-9位字母数字组合";
    default:
      return "";
  }
});
const hasSavedProfileBefore = computed(() =>
  Boolean(savedProfileData.value?.id),
);
const isReviewer = computed(
  () => profile.role === "ADMIN" || profile.role === "TEACHER",
);
const saveActionLabel = computed(() => {
  if (isReviewer.value) return "保存";
  return hasSavedProfileBefore.value && reviewSettings.profileReviewEnabled
    ? "请求审核"
    : "保存";
});

const dormBuildingOptions = computed(() => {
  if (info.dormCampus === "佛山校区") {
    return [
      ...Array.from({ length: 21 }, (_, index) => {
        const label = `${index + 1}号楼`;
        return { label, value: label };
      }),
      { label: "有为9栋", value: "有为9栋" },
      { label: "有为21栋", value: "有为21栋" },
      {
        label: "教师公寓（请选择校外居住）",
        value: "教师公寓",
        disabled: true,
      },
    ];
  }
  if (info.dormCampus === "广州校区") {
    return [
      ...Array.from({ length: 16 }, (_, index) => {
        const label = `${index + 17}号楼`;
        return { label, value: label };
      }),
      { label: "凌云楼", value: "凌云楼" },
      { label: "揽月楼", value: "揽月楼" },
      { label: "丽枫酒店", value: "丽枫酒店" },
    ];
  }
  return [];
});
const educationItems = reactive(
  Array.from({ length: 5 }, () => createEducationItem()),
);
const cadreItems = reactive(Array.from({ length: 5 }, () => createCadreItem()));

function getTodayString() {
  const now = new Date();
  const offsetMs = now.getTimezoneOffset() * 60 * 1000;
  return new Date(now.getTime() - offsetMs).toISOString().slice(0, 10);
}

function createEducationItem() {
  return {
    startDate: "",
    endDate: "",
    schoolName: "",
    educationLevel: "",
    witness: "",
    isCurrent: false,
  };
}

function createCadreItem() {
  return {
    startDate: "",
    endDate: "",
    department: "",
    position: "",
    description: "",
    isCurrent: false,
  };
}

async function addEducationRow() {
  await animateEducationHeightWithUpdate(() => {
    educationItems.push(createEducationItem());
  });
}

async function removeEducationRow() {
  if (educationItems.length <= 1) {
    return;
  }
  await animateEducationHeightWithUpdate(() => {
    educationItems.pop();
  });
}

async function addCadreRow() {
  await animateCadreHeightWithUpdate(() => {
    cadreItems.push(createCadreItem());
  });
}

async function removeCadreRow() {
  if (cadreItems.length <= 1) {
    return;
  }
  await animateCadreHeightWithUpdate(() => {
    cadreItems.pop();
  });
}

async function handleClearEducation() {
  await animateEducationHeightWithUpdate(() => {
    educationItems.splice(0, educationItems.length, createEducationItem());
  });
}

async function handleClearCadre() {
  await animateCadreHeightWithUpdate(() => {
    cadreItems.splice(0, cadreItems.length, createCadreItem());
  });
}

async function animateEducationHeightWithUpdate(updateFn) {
  const el = educationTableWrap.value;
  if (!el) {
    updateFn();
    return;
  }
  const startHeight = el.getBoundingClientRect().height;
  updateFn();
  await nextTick();
  const targetHeight = el.getBoundingClientRect().height;
  el.style.height = `${startHeight}px`;
  el.style.overflow = "hidden";
  requestAnimationFrame(() => {
    el.style.transition = "height 260ms ease";
    el.style.height = `${targetHeight}px`;
  });
  const cleanup = () => {
    el.style.height = "";
    el.style.transition = "";
    el.style.overflow = "";
    el.removeEventListener("transitionend", cleanup);
  };
  el.addEventListener("transitionend", cleanup);
}

async function animateCadreHeightWithUpdate(updateFn) {
  const el = cadreTableWrap.value;
  if (!el) {
    updateFn();
    return;
  }
  const startHeight = el.getBoundingClientRect().height;
  updateFn();
  await nextTick();
  const targetHeight = el.getBoundingClientRect().height;
  el.style.height = `${startHeight}px`;
  el.style.overflow = "hidden";
  requestAnimationFrame(() => {
    el.style.transition = "height 260ms ease";
    el.style.height = `${targetHeight}px`;
  });
  const cleanup = () => {
    el.style.height = "";
    el.style.transition = "";
    el.style.overflow = "";
    el.removeEventListener("transitionend", cleanup);
  };
  el.addEventListener("transitionend", cleanup);
}

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

const activeAchievementIndex = computed(() => {
  const index = achievementEntries.findIndex(
    (entry) => entry.key === activeAchievement.value,
  );
  return index === -1 ? 0 : index;
});

const drawerIndicatorStyle = computed(() => ({
  transform: `translateY(calc(${activeAchievementIndex.value} * (var(--drawer-item-height) + var(--drawer-item-gap))))`,
}));

const avatarText = computed(() => {
  const name = profile.displayName || profile.username || "同学";
  return name.slice(0, 1).toUpperCase();
});

const roleLabel = computed(() => {
  if (profile.role === "ADMIN") {
    return "管理员";
  }
  if (profile.role === "TEACHER") {
    return "老师";
  }
  return "学生";
});

const classMajorOptions = computed(() => {
  return majorOptionsByCategory[info.studentCategory] || [];
});
const addressProvinceOptions = computed(() =>
  regionData.map((item) => ({ value: item.value, label: item.label })),
);
const addressCityOptions = computed(() => {
  const province = regionData.find(
    (item) => item.value === info.addressProvince,
  );
  return province?.children || [];
});
const addressCountyOptions = computed(() => {
  const province = regionData.find(
    (item) => item.value === info.addressProvince,
  );
  const city = province?.children?.find(
    (entry) => entry.value === info.addressCity,
  );
  return city?.children || [];
});
const offCampusCityOptions = computed(() => {
  const province = regionData.find(
    (item) => item.value === info.offCampusProvince,
  );
  return province?.children || [];
});
const offCampusCountyOptions = computed(() => {
  const province = regionData.find(
    (item) => item.value === info.offCampusProvince,
  );
  const city = province?.children?.find(
    (entry) => entry.value === info.offCampusCity,
  );
  return city?.children || [];
});

const hasEducationCurrent = computed(() =>
  educationItems.some((entry) => entry.isCurrent),
);
const currentEducationIndex = computed(() =>
  educationItems.findIndex((entry) => entry.isCurrent),
);
const hasCadreCurrent = computed(() =>
  cadreItems.some((entry) => entry.isCurrent),
);
const currentCadreIndex = computed(() =>
  cadreItems.findIndex((entry) => entry.isCurrent),
);

async function handleEducationCurrentChange({ item, checked, index }) {
  if (checked) {
    item.endDate = "";
    await animateEducationHeightWithUpdate(() => {
      clearEducationRowsAfter(index);
      pruneEducationRowsAfter(index);
    });
  }
}

async function handleCadreCurrentChange({ item, checked, index }) {
  if (checked) {
    item.endDate = "";
    await animateCadreHeightWithUpdate(() => {
      clearCadreRowsAfter(index);
      pruneCadreRowsAfter(index);
    });
  }
}

function isEducationCurrentDisabled(item) {
  if (item.isCurrent) {
    return false;
  }
  return hasEducationCurrent.value;
}

function isCadreCurrentDisabled(item) {
  if (item.isCurrent) {
    return false;
  }
  return hasCadreCurrent.value;
}

function isEducationRowDisabled(index) {
  if (!isEditing.value) {
    return true;
  }
  const currentIndex = currentEducationIndex.value;
  return currentIndex !== -1 && index > currentIndex;
}

function isCadreRowDisabled(index) {
  if (!isEditing.value) {
    return true;
  }
  const currentIndex = currentCadreIndex.value;
  return currentIndex !== -1 && index > currentIndex;
}

function clearEducationRowsAfter(index) {
  educationItems.slice(index + 1).forEach((entry) => {
    entry.startDate = "";
    entry.endDate = "";
    entry.schoolName = "";
    entry.educationLevel = "";
    entry.witness = "";
    entry.isCurrent = false;
  });
}

function clearCadreRowsAfter(index) {
  cadreItems.slice(index + 1).forEach((entry) => {
    entry.startDate = "";
    entry.endDate = "";
    entry.department = "";
    entry.position = "";
    entry.description = "";
    entry.isCurrent = false;
  });
}

function pruneEducationRowsAfter(index) {
  if (educationItems.length <= index + 1) {
    return;
  }
  const kept = educationItems.slice(0, index + 1);
  educationItems.slice(index + 1).forEach((entry) => {
    if (!isEducationRowEmpty(entry)) {
      kept.push(entry);
    }
  });
  if (kept.length !== educationItems.length) {
    educationItems.splice(0, educationItems.length, ...kept);
  }
}

function pruneCadreRowsAfter(index) {
  if (cadreItems.length <= index + 1) {
    return;
  }
  const kept = cadreItems.slice(0, index + 1);
  cadreItems.slice(index + 1).forEach((entry) => {
    if (!isCadreRowEmpty(entry)) {
      kept.push(entry);
    }
  });
  if (kept.length !== cadreItems.length) {
    cadreItems.splice(0, cadreItems.length, ...kept);
  }
}

function isEducationRowEmpty(entry) {
  return (
    !entry.startDate &&
    !entry.endDate &&
    !entry.schoolName &&
    !entry.educationLevel &&
    !entry.witness &&
    !entry.isCurrent
  );
}

function isCadreRowEmpty(entry) {
  return (
    !entry.startDate &&
    !entry.endDate &&
    !entry.department &&
    !entry.position &&
    !entry.description &&
    !entry.isCurrent
  );
}

const dormBuildingDisabled = computed(
  () => !isEditing.value || info.offCampusLiving || !info.dormCampus,
);
const dormRoomDisabled = computed(
  () => dormBuildingDisabled.value || !info.dormBuilding,
);

const leagueApplicationDisabled = computed(
  () => !isEditing.value || !info.leagueJoined,
);
const leagueJoinDisabled = computed(
  () =>
    leagueApplicationDisabled.value ||
    !info.leagueApplicationDate ||
    info.leagueDeveloping,
);
const leagueNoDisabled = computed(
  () =>
    !isEditing.value ||
    !info.leagueJoinDate ||
    info.leagueDeveloping ||
    !info.leagueJoined,
);
const partyAppliedDisabled = computed(
  () => !isEditing.value || !info.leagueJoinDate || info.leagueDeveloping,
);
const applicationDateDisabled = computed(
  () => !isEditing.value || partyAppliedDisabled.value || !info.partyApplied,
);
const activistDateDisabled = computed(
  () =>
    !isEditing.value ||
    !info.applicationDate ||
    info.activistDeveloping ||
    partyAppliedDisabled.value ||
    !info.partyApplied,
);
const partyTrainingDisabled = computed(
  () =>
    !isEditing.value ||
    !info.activistDate ||
    info.activistDeveloping ||
    info.partyTrainingPending ||
    partyAppliedDisabled.value ||
    !info.partyApplied,
);
const developmentTargetDisabled = computed(
  () =>
    !isEditing.value ||
    !info.partyTrainingDate ||
    info.partyTrainingPending ||
    info.developmentTargetDeveloping ||
    info.activistDeveloping ||
    partyAppliedDisabled.value ||
    !info.partyApplied,
);
const probationaryDisabled = computed(
  () =>
    !isEditing.value ||
    !info.developmentTargetDate ||
    info.developmentTargetDeveloping ||
    info.probationaryDeveloping ||
    info.partyTrainingPending ||
    info.activistDeveloping ||
    partyAppliedDisabled.value ||
    !info.partyApplied,
);
const fullMemberDisabled = computed(
  () =>
    !isEditing.value ||
    !info.probationaryMemberDate ||
    info.probationaryDeveloping ||
    info.fullMemberDeveloping ||
    info.developmentTargetDeveloping ||
    info.partyTrainingPending ||
    info.activistDeveloping ||
    partyAppliedDisabled.value ||
    !info.partyApplied,
);

function handleMenuClick(key) {
  if (!isMenuEnabled(key)) {
    return;
  }
  sidebarOpen.value = false;
  if (key === "my-info") {
    return;
  }
  if (key === "achievements") {
    toggleAchievements();
    return;
  }
  navigateWithViewTransition(router, getMenuLocation(key));
}

function toggleAchievements() {
  if (!isMenuEnabled("achievements")) {
    return;
  }
  achievementsOpen.value = !achievementsOpen.value;
  if (achievementsOpen.value) {
    activeMenu.value = "achievements";
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
  activeAchievement.value = safeKey;
  activeMenu.value = "achievements";
  sidebarOpen.value = false;
  navigateWithViewTransition(router, {
    path: "/achievements",
    query: { category: safeKey },
  });
}

function openSidebar() {
  sidebarOpen.value = true;
}

function closeSidebar() {
  sidebarOpen.value = false;
}

function goToSettings() {
  navigateWithViewTransition(router, "/settings");
}

function handleDigitsInput(field, maxLength, event) {
  const raw = event.target.value || "";
  const next = raw.replace(/\D/g, "").slice(0, maxLength);
  info[field] = next;
}

function handleIdNoInput(event) {
  const raw = (event.target.value || "").toUpperCase();
  const maxLen = idNoMaxLength.value;
  // 居民身份证: 17位数字 + 1位数字/X
  if (info.idType === "居民身份证") {
    const cleaned = raw.replace(/[^0-9X]/g, "");
    const digits = cleaned.replace(/X/g, "").slice(0, 17);
    if (cleaned.endsWith("X")) {
      info.idNo = `${digits}X`.slice(0, maxLen);
      return;
    }
    info.idNo = digits;
    return;
  }
  // 台湾居民来往大陆通行证（台胞证）: 8位纯数字
  if (info.idType === "台湾居民来往大陆通行证") {
    info.idNo = raw.replace(/\D/g, "").slice(0, 8);
    return;
  }
  // 港澳居民来往内地通行证（回乡证）: H/M + 8位数字
  if (info.idType === "港澳居民来往内地通行证") {
    const cleaned = raw.replace(/[^0-9A-Z]/g, "").toUpperCase();
    const letter = cleaned.slice(0, 1).replace(/[^HM]/g, "");
    const digits = cleaned.replace(/[A-Z]/g, "").slice(0, 8);
    info.idNo = `${letter}${digits}`.slice(0, maxLen);
    return;
  }
  // 普通护照: E + 8位数字
  if (info.idType === "普通护照") {
    const cleaned = raw.replace(/[^0-9A-Z]/g, "").toUpperCase();
    const letter = cleaned.slice(0, 1).replace(/[^E]/g, "");
    const digits = cleaned.replace(/[A-Z]/g, "").slice(0, 8);
    info.idNo = `${letter}${digits}`.slice(0, maxLen);
    return;
  }
  // 台湾居民居住证: 18位纯数字（83开头）
  if (info.idType === "台湾居民居住证") {
    info.idNo = raw.replace(/\D/g, "").slice(0, 18);
    return;
  }
  // 港澳居民居住证: 18位纯数字（81/82开头）
  if (info.idType === "港澳居民居住证") {
    info.idNo = raw.replace(/\D/g, "").slice(0, 18);
    return;
  }
  // 外国人永久居留身份证（五星卡）: 18位纯数字
  if (info.idType === "外国人永久居留身份证") {
    info.idNo = raw.replace(/\D/g, "").slice(0, 18);
    return;
  }
  // 外国护照: 6-9位字母数字组合
  if (info.idType === "外国护照") {
    info.idNo = raw
      .replace(/[^0-9A-Z]/g, "")
      .toUpperCase()
      .slice(0, maxLen);
    return;
  }
  // 默认: alphanumeric
  info.idNo = raw
    .replace(/[^0-9A-Z]/g, "")
    .toUpperCase()
    .slice(0, maxLen);
}

function triggerAvatarUpload() {
  avatarInput.value && avatarInput.value.click();
}

async function onAvatarChange(event) {
  const [file] = Array.from(event.target.files || []);
  event.target.value = "";
  if (!file) {
    return;
  }
  try {
    const { data } = await uploadWithProgress(file, uploadMedia);
    if (data?.mediaType !== "IMAGE") {
      return;
    }
    info.avatarUrl = data.url || "";
  } catch (err) {
    console.error(err);
  }
}

function enterEdit() {
  if (hasPendingProfileReviewRequest.value) {
    toastError("个人信息正在审核，请等待审核结果或前往通知页取消申请");
    return;
  }
  originalProfileData.value = buildCurrentProfileState();
  isEditing.value = true;
}

function cancelEdit() {
  if (originalProfileData.value) {
    applyProfileResponse(originalProfileData.value, {
      syncSavedProfile: false,
    });
  }
  isEditing.value = false;
}

async function confirmEdit() {
  if (info.classYear && !info.classMajor) {
    toastError("请先选择专业");
    return;
  }
  if (info.classYear && !info.classNo) {
    toastError("请先选择班级");
    return;
  }
  const requiresReview =
    hasSavedProfileBefore.value &&
    reviewSettings.profileReviewEnabled &&
    !isReviewer.value;
  const className = buildClassName(
    info.classYear,
    info.classMajor,
    info.classNo,
    info.className,
  );
  const address = buildAddress(
    info.addressProvince,
    info.addressCity,
    info.addressCounty,
    info.addressDetail,
    info.address,
  );
  const offCampusAddress = buildAddress(
    info.offCampusProvince,
    info.offCampusCity,
    info.offCampusCounty,
    info.offCampusDetail,
    info.offCampusAddress,
  );
  const dormRoom = buildDormRoom(
    info.dormFloor,
    info.dormRoomNo,
    info.dormRoom,
  );
  const educationExperiences = educationItems
    .filter((item) => !isEducationRowEmpty(item))
    .map((item) => ({
      startDate: item.startDate,
      endDate: item.endDate,
      schoolName: item.schoolName,
      educationLevel: item.educationLevel,
      witness: item.witness,
      isCurrent: item.isCurrent,
    }));
  const cadreExperiences = cadreItems
    .filter((item) => !isCadreRowEmpty(item))
    .map((item) => ({
      startDate: item.startDate,
      endDate: item.endDate,
      department: item.department,
      position: item.position,
      description: item.description,
      isCurrent: item.isCurrent,
    }));
  const payload = {
    fullName: info.name,
    avatarUrl: info.avatarUrl,
    studentNo: info.studentNo,
    classYear: info.classYear || null,
    classMajor: info.classMajor,
    classNo: info.classNo,
    className,
    college: FIXED_COLLEGE,
    enrollmentDate: info.enrollmentDate || null,
    studentCategory: info.studentCategory,
    ethnicity: info.ethnicity,
    politicalStatus: info.politicalStatus,
    dormCampus: info.dormCampus,
    dormBuilding: info.dormBuilding,
    dormRoom,
    offCampusLiving: info.offCampusLiving,
    offCampusAddress,
    classTeacher: info.classTeacher,
    counselor: info.counselor,
    phone: info.phone,
    backupContact: info.backupContact,
    address,
    idType: info.idType,
    idNo: info.idNo,
    birthDate: info.birthDate || null,
    nativePlace: info.nativePlace,
    leagueNo: info.leagueNo,
    leagueApplicationDate: info.leagueApplicationDate || null,
    leagueJoinDate: info.leagueJoinDate || null,
    leagueJoined: info.leagueJoined,
    leagueDeveloping: info.leagueDeveloping,
    partyApplied: info.partyApplied,
    notDeveloped: info.notDeveloped,
    applicationDate: info.applicationDate || null,
    activistDate: info.activistDate || null,
    activistDeveloping: info.activistDeveloping,
    partyTrainingDate: info.partyTrainingDate || null,
    partyTrainingPending: info.partyTrainingPending,
    developmentTargetDate: info.developmentTargetDate || null,
    developmentTargetDeveloping: info.developmentTargetDeveloping,
    probationaryMemberDate: info.probationaryMemberDate || null,
    probationaryDeveloping: info.probationaryDeveloping,
    fullMemberDate: info.fullMemberDate || null,
    fullMemberDeveloping: info.fullMemberDeveloping,
    emergencyPhone: info.emergencyPhone,
    emergencyRelation: info.emergencyRelation,
    fatherName: info.fatherName,
    fatherPhone: info.fatherPhone,
    fatherWorkUnit: info.fatherWorkUnit,
    fatherTitle: info.fatherTitle,
    motherName: info.motherName,
    motherPhone: info.motherPhone,
    motherWorkUnit: info.motherWorkUnit,
    motherTitle: info.motherTitle,
    isHk: info.isHk,
    isMo: info.isMo,
    isTw: info.isTw,
    specialStudent: info.specialStudent,
    specialStudentType: info.specialStudentType || "",
    specialStudentRemark: info.specialStudentRemark || "",
    educationExperiences,
    cadreExperiences,
  };
  if (info.offCampusLiving) {
    payload.dormCampus = null;
    payload.dormBuilding = null;
    payload.dormRoom = null;
  } else {
    payload.offCampusAddress = null;
  }
  if (leagueApplicationDisabled.value) {
    payload.leagueApplicationDate = null;
  }
  if (leagueJoinDisabled.value) {
    payload.leagueJoinDate = null;
  }
  if (leagueNoDisabled.value) {
    payload.leagueNo = null;
  }
  if (partyAppliedDisabled.value) {
    payload.partyApplied = false;
  }
  if (applicationDateDisabled.value) {
    payload.applicationDate = null;
  }
  if (activistDateDisabled.value) {
    payload.activistDate = null;
  }
  if (partyTrainingDisabled.value) {
    payload.partyTrainingDate = null;
  }
  if (developmentTargetDisabled.value) {
    payload.developmentTargetDate = null;
  }
  if (probationaryDisabled.value) {
    payload.probationaryMemberDate = null;
  }
  if (fullMemberDisabled.value) {
    payload.fullMemberDate = null;
  }
  if (!info.leagueJoined) {
    payload.leagueJoined = false;
    payload.leagueApplicationDate = null;
    payload.leagueJoinDate = null;
    payload.leagueDeveloping = false;
    payload.leagueNo = null;
    payload.partyApplied = false;
    payload.applicationDate = null;
    payload.activistDate = null;
    payload.activistDeveloping = false;
    payload.partyTrainingDate = null;
    payload.partyTrainingPending = false;
    payload.developmentTargetDate = null;
    payload.developmentTargetDeveloping = false;
    payload.probationaryMemberDate = null;
    payload.probationaryDeveloping = false;
    payload.fullMemberDate = null;
    payload.fullMemberDeveloping = false;
  } else if (!info.partyApplied) {
    payload.partyApplied = false;
    payload.applicationDate = null;
    payload.activistDate = null;
    payload.activistDeveloping = false;
    payload.partyTrainingDate = null;
    payload.partyTrainingPending = false;
    payload.developmentTargetDate = null;
    payload.developmentTargetDeveloping = false;
    payload.probationaryMemberDate = null;
    payload.probationaryDeveloping = false;
    payload.fullMemberDate = null;
    payload.fullMemberDeveloping = false;
  }
  const changes = buildProfileChanges(originalProfileData.value, payload);
  try {
    if (requiresReview) {
      const { data: requestData } = await submitProfileReviewRequest({
        actor: profile.value?.username,
        payloadSnapshot: payload,
        changes,
      });
      if (
        reviewSettings.profileReviewAutoApprove &&
        requestData?.status === "approved"
      ) {
        await fetchProfileReviewRequests(true);
        const updatedProfile = await getStudentProfile();
        applyProfileResponse(updatedProfile.data);
      }
      isEditing.value = false;
      return;
    }
    const { data } = await saveStudentProfile(payload);
    applyProfileResponse(data);
    isEditing.value = false;
  } catch (err) {
    console.error(err);
  }
}

function buildPdfStudentSnapshot() {
  const studentName =
    info.name || profile.displayName || profile.username || "";
  const studentNo = info.studentNo || profile.studentNo || "";
  const className = buildClassName(
    info.classYear,
    info.classMajor,
    info.classNo,
    info.className,
  );
  const addressText = buildAddress(
    info.addressProvince,
    info.addressCity,
    info.addressCounty,
    info.addressDetail,
    info.address,
  );
  const offCampusAddress = buildAddress(
    info.offCampusProvince,
    info.offCampusCity,
    info.offCampusCounty,
    info.offCampusDetail,
    info.offCampusAddress,
  );
  const educationExperiences = educationItems
    .filter((item) => !isEducationRowEmpty(item))
    .map((item) => ({
      startDate: item.startDate,
      endDate: item.endDate,
      schoolName: item.schoolName,
      educationLevel: item.educationLevel,
      witness: item.witness,
      isCurrent: item.isCurrent,
    }));
  const cadreExperiences = cadreItems
    .filter((item) => !isCadreRowEmpty(item))
    .map((item) => ({
      startDate: item.startDate,
      endDate: item.endDate,
      department: item.department,
      position: item.position,
      description: item.description,
      isCurrent: item.isCurrent,
    }));
  return {
    fullName: studentName,
    studentNo,
    classYear: info.classYear,
    classMajor: info.classMajor,
    classNo: info.classNo,
    className,
    college: info.college,
    enrollmentDate: info.enrollmentDate,
    studentCategory: info.studentCategory,
    classTeacher: info.classTeacher,
    counselor: info.counselor,
    ethnicity: info.ethnicity,
    politicalStatus: info.politicalStatus,
    phone: info.phone,
    backupContact: info.backupContact,
    idType: info.idType,
    idNo: info.idNo,
    birthDate: info.birthDate,
    nativePlace: info.nativePlace,
    address: addressText,
    dormCampus: info.dormCampus,
    dormBuilding: info.dormBuilding,
    dormRoom: info.dormRoom,
    offCampusLiving: info.offCampusLiving,
    offCampusAddress,
    emergencyPhone: info.emergencyPhone,
    emergencyRelation: info.emergencyRelation,
    fatherName: info.fatherName,
    fatherPhone: info.fatherPhone,
    fatherWorkUnit: info.fatherWorkUnit,
    fatherTitle: info.fatherTitle,
    motherName: info.motherName,
    motherPhone: info.motherPhone,
    motherWorkUnit: info.motherWorkUnit,
    motherTitle: info.motherTitle,
    leagueNo: info.leagueNo,
    leagueApplicationDate: info.leagueApplicationDate,
    leagueJoinDate: info.leagueJoinDate,
    leagueJoined: info.leagueJoined,
    leagueDeveloping: info.leagueDeveloping,
    partyApplied: info.partyApplied,
    notDeveloped: info.notDeveloped,
    applicationDate: info.applicationDate,
    activistDate: info.activistDate,
    activistDeveloping: info.activistDeveloping,
    partyTrainingDate: info.partyTrainingDate,
    partyTrainingPending: info.partyTrainingPending,
    developmentTargetDate: info.developmentTargetDate,
    developmentTargetDeveloping: info.developmentTargetDeveloping,
    probationaryMemberDate: info.probationaryMemberDate,
    probationaryDeveloping: info.probationaryDeveloping,
    fullMemberDate: info.fullMemberDate,
    fullMemberDeveloping: info.fullMemberDeveloping,
    isHk: info.isHk,
    isMo: info.isMo,
    isTw: info.isTw,
    specialStudent: info.specialStudent,
    specialStudentType: info.specialStudentType,
    specialStudentRemark: info.specialStudentRemark,
    educationExperiences,
    cadreExperiences,
    avatarUrl: info.avatarUrl,
  };
}

function buildCurrentProfileState() {
  return {
    fullName: info.name,
    avatarUrl: info.avatarUrl,
    studentNo: info.studentNo,
    classYear: info.classYear || null,
    classMajor: info.classMajor,
    classNo: info.classNo,
    className: buildClassName(
      info.classYear,
      info.classMajor,
      info.classNo,
      info.className,
    ),
    college: info.college,
    enrollmentDate: info.enrollmentDate || null,
    studentCategory: info.studentCategory,
    ethnicity: info.ethnicity,
    politicalStatus: info.politicalStatus,
    dormCampus: info.dormCampus,
    dormBuilding: info.dormBuilding,
    dormRoom: buildDormRoom(info.dormFloor, info.dormRoomNo, info.dormRoom),
    offCampusLiving: info.offCampusLiving,
    offCampusAddress: buildAddress(
      info.offCampusProvince,
      info.offCampusCity,
      info.offCampusCounty,
      info.offCampusDetail,
      info.offCampusAddress,
    ),
    classTeacher: info.classTeacher,
    counselor: info.counselor,
    phone: info.phone,
    backupContact: info.backupContact,
    address: buildAddress(
      info.addressProvince,
      info.addressCity,
      info.addressCounty,
      info.addressDetail,
      info.address,
    ),
    idType: info.idType,
    idNo: info.idNo,
    birthDate: info.birthDate || null,
    nativePlace: info.nativePlace,
    leagueNo: info.leagueNo,
    leagueApplicationDate: info.leagueApplicationDate || null,
    leagueJoinDate: info.leagueJoinDate || null,
    leagueJoined: info.leagueJoined,
    leagueDeveloping: info.leagueDeveloping,
    partyApplied: info.partyApplied,
    notDeveloped: info.notDeveloped,
    applicationDate: info.applicationDate || null,
    activistDate: info.activistDate || null,
    activistDeveloping: info.activistDeveloping,
    partyTrainingDate: info.partyTrainingDate || null,
    partyTrainingPending: info.partyTrainingPending,
    developmentTargetDate: info.developmentTargetDate || null,
    developmentTargetDeveloping: info.developmentTargetDeveloping,
    probationaryMemberDate: info.probationaryMemberDate || null,
    probationaryDeveloping: info.probationaryDeveloping,
    fullMemberDate: info.fullMemberDate || null,
    fullMemberDeveloping: info.fullMemberDeveloping,
    emergencyPhone: info.emergencyPhone,
    emergencyRelation: info.emergencyRelation,
    fatherName: info.fatherName,
    fatherPhone: info.fatherPhone,
    fatherWorkUnit: info.fatherWorkUnit,
    fatherTitle: info.fatherTitle,
    motherName: info.motherName,
    motherPhone: info.motherPhone,
    motherWorkUnit: info.motherWorkUnit,
    motherTitle: info.motherTitle,
    isHk: info.isHk,
    isMo: info.isMo,
    isTw: info.isTw,
    specialStudent: info.specialStudent,
    specialStudentType: info.specialStudentType,
    specialStudentRemark: info.specialStudentRemark,
    educationExperiences: educationItems
      .filter((item) => !isEducationRowEmpty(item))
      .map((item) => ({ ...item })),
    cadreExperiences: cadreItems
      .filter((item) => !isCadreRowEmpty(item))
      .map((item) => ({ ...item })),
  };
}

function buildProfileChanges(previousState, nextState) {
  const changes = PROFILE_CHANGE_FIELDS.reduce((list, field) => {
    const before = stringifyProfileChangeValue(previousState?.[field.key]);
    const after = stringifyProfileChangeValue(nextState?.[field.key]);
    if (before === after) {
      return list;
    }
    list.push({
      section: field.section,
      label: field.label,
      before,
      after,
    });
    return list;
  }, []);

  const previousEducation = stringifyProfileCollection(
    previousState?.educationExperiences,
  );
  const nextEducation = stringifyProfileCollection(
    nextState?.educationExperiences,
  );
  if (previousEducation !== nextEducation) {
    changes.push({
      section: "教育经历",
      label: "教育经历",
      before: previousEducation,
      after: nextEducation,
    });
  }

  const previousCadre = stringifyProfileCollection(
    previousState?.cadreExperiences,
  );
  const nextCadre = stringifyProfileCollection(nextState?.cadreExperiences);
  if (previousCadre !== nextCadre) {
    changes.push({
      section: "学生干部经历",
      label: "学生干部经历",
      before: previousCadre,
      after: nextCadre,
    });
  }

  return changes;
}

function stringifyProfileCollection(items) {
  const list = Array.isArray(items) ? items.filter(Boolean) : [];
  if (!list.length) {
    return "-";
  }
  const firstItem = list[0] || {};
  if (
    "schoolName" in firstItem ||
    "educationLevel" in firstItem ||
    "witness" in firstItem
  ) {
    return list.map(formatEducationExperienceItem).filter(Boolean).join("\n");
  }
  if (
    "department" in firstItem ||
    "position" in firstItem ||
    "description" in firstItem
  ) {
    return list.map(formatCadreExperienceItem).filter(Boolean).join("\n");
  }
  return list
    .map((item) =>
      Object.entries(item)
        .map(([, value]) => stringifyProfileChangeValue(value))
        .filter((value) => value !== "-")
        .join(" / "),
    )
    .filter(Boolean)
    .join("\n");
}

function formatEducationExperienceItem(item, index) {
  const period = formatPeriodText(
    item?.startDate,
    item?.endDate,
    item?.isCurrent,
  );
  const schoolName = stringifyProfileChangeValue(item?.schoolName);
  const educationLevel = stringifyProfileChangeValue(item?.educationLevel);
  const witness = stringifyProfileChangeValue(item?.witness);
  return [
    `第${index + 1}条`,
    `时间：${period}`,
    `学校名称：${schoolName}`,
    `学历：${educationLevel}`,
    `证明人：${witness}`,
  ].join("\n");
}

function formatCadreExperienceItem(item, index) {
  const period = formatPeriodText(
    item?.startDate,
    item?.endDate,
    item?.isCurrent,
  );
  const department = stringifyProfileChangeValue(item?.department);
  const position = stringifyProfileChangeValue(item?.position);
  const description = stringifyProfileChangeValue(item?.description);
  return [
    `第${index + 1}条`,
    `起止时间：${period}`,
    `班级/社团部门：${department}`,
    `职位：${position}`,
    `职责说明：${description}`,
  ].join("\n");
}

function formatPeriodText(startDate, endDate, isCurrent) {
  const start = stringifyProfileChangeValue(startDate);
  if (isCurrent) {
    return `${start} 至今`;
  }
  const end = stringifyProfileChangeValue(endDate);
  return `${start} - ${end}`;
}

function stringifyProfileChangeValue(value) {
  if (typeof value === "boolean") {
    return value ? "是" : "否";
  }
  if (Array.isArray(value)) {
    return stringifyProfileCollection(value);
  }
  const text = String(value ?? "").trim();
  return text || "-";
}

function applyProfileResponse(data, options = {}) {
  if (!data) {
    return;
  }
  const { syncSavedProfile = true } = options;
  info.name = data.fullName || data.displayName || "";
  info.avatarUrl = data.avatarUrl || profile.avatarUrl || "";
  info.studentNo = data.studentNo || profile.studentNo || "";
  info.classYear = data.classYear || new Date().getFullYear();
  info.classMajor = data.classMajor || "";
  info.classNo = data.classNo ?? 1;
  info.className = data.className || "";
  info.college = FIXED_COLLEGE;
  info.enrollmentDate = data.enrollmentDate || "";
  info.studentCategory = data.studentCategory || "";
  info.ethnicity = data.ethnicity || "";
  info.politicalStatus = data.politicalStatus || "";
  info.dormCampus = data.dormCampus || "";
  info.dormBuilding = data.dormBuilding || "";
  info.dormRoom = data.dormRoom || "";
  const parsedDormRoom = parseDormRoom(info.dormRoom);
  info.dormFloor = parsedDormRoom.floor;
  info.dormRoomNo = parsedDormRoom.roomNo;
  info.offCampusLiving = Boolean(data.offCampusLiving);
  info.offCampusAddress = data.offCampusAddress || "";
  const parsedOffCampusAddress = parseAddressToRegion(info.offCampusAddress);
  info.offCampusProvince = parsedOffCampusAddress.province;
  info.offCampusCity = parsedOffCampusAddress.city;
  info.offCampusCounty = parsedOffCampusAddress.county;
  info.offCampusDetail = parsedOffCampusAddress.detail;
  info.classTeacher = data.classTeacher || "";
  info.counselor = data.counselor || "";
  info.phone = data.phone || "";
  info.backupContact = data.backupContact || "";
  info.address = data.address || "";
  const parsedAddress = parseAddressToRegion(info.address);
  info.addressProvince = parsedAddress.province;
  info.addressCity = parsedAddress.city;
  info.addressCounty = parsedAddress.county;
  info.addressDetail = parsedAddress.detail;
  info.idType = data.idType || "居民身份证";
  info.idNo = data.idNo || "";
  info.birthDate = data.birthDate || "";
  info.nativePlace = data.nativePlace || "";
  info.leagueNo = data.leagueNo || "";
  info.leagueApplicationDate = data.leagueApplicationDate || "";
  info.leagueJoinDate = data.leagueJoinDate || "";
  info.leagueJoined = Boolean(data.leagueJoined);
  info.leagueDeveloping = Boolean(data.leagueDeveloping);
  info.partyApplied = Boolean(data.partyApplied);
  info.notDeveloped = Boolean(data.notDeveloped);
  info.applicationDate = data.applicationDate || "";
  info.activistDate = data.activistDate || "";
  info.activistDeveloping = Boolean(data.activistDeveloping);
  info.partyTrainingDate = data.partyTrainingDate || "";
  info.partyTrainingPending = Boolean(data.partyTrainingPending);
  info.developmentTargetDate = data.developmentTargetDate || "";
  info.developmentTargetDeveloping = Boolean(data.developmentTargetDeveloping);
  info.probationaryMemberDate = data.probationaryMemberDate || "";
  info.probationaryDeveloping = Boolean(data.probationaryDeveloping);
  info.fullMemberDate = data.fullMemberDate || "";
  info.fullMemberDeveloping = Boolean(data.fullMemberDeveloping);
  info.emergencyPhone = data.emergencyPhone || "";
  info.emergencyRelation = data.emergencyRelation || "";
  info.fatherName = data.fatherName || "";
  info.fatherPhone = data.fatherPhone || "";
  info.fatherWorkUnit = data.fatherWorkUnit || "";
  info.fatherTitle = data.fatherTitle || "";
  info.motherName = data.motherName || "";
  info.motherPhone = data.motherPhone || "";
  info.motherWorkUnit = data.motherWorkUnit || "";
  info.motherTitle = data.motherTitle || "";
  info.isHk = Boolean(data.isHk);
  info.isMo = Boolean(data.isMo);
  info.isTw = Boolean(data.isTw);
  info.specialStudent = Boolean(data.specialStudent);
  info.specialStudentType = data.specialStudentType || "";
  info.specialStudentRemark = data.specialStudentRemark || "";
  applyEducationExperiences(data.educationExperiences);
  applyCadreExperiences(data.cadreExperiences);

  profile.displayName = data.displayName || profile.displayName;
  profile.username = data.username || profile.username;
  profile.avatarUrl = data.avatarUrl || profile.avatarUrl;
  profile.role = data.role || profile.role;
  profile.studentNo = data.studentNo || profile.studentNo;
  profile.className = data.className || profile.className;
  profile.college = FIXED_COLLEGE;
  if (syncSavedProfile) {
    savedProfileData.value = data;
    originalProfileData.value = data;
  }

  saveUser(profile);
}

function applyEducationExperiences(rawItems) {
  const nextItems = Array.isArray(rawItems) ? rawItems : [];
  const normalized = nextItems.map((item) => ({
    startDate: item?.startDate || "",
    endDate: item?.isCurrent ? "" : item?.endDate || "",
    schoolName: item?.schoolName || "",
    educationLevel: item?.educationLevel || "",
    witness: item?.witness || "",
    isCurrent: Boolean(item?.isCurrent),
  }));
  const filtered = normalized.filter((item) => !isEducationRowEmpty(item));
  if (!filtered.length) {
    filtered.push(createEducationItem());
  }
  educationItems.splice(0, educationItems.length, ...filtered);
}

function applyCadreExperiences(rawItems) {
  const nextItems = Array.isArray(rawItems) ? rawItems : [];
  const normalized = nextItems.map((item) => ({
    startDate: item?.startDate || "",
    endDate: item?.isCurrent ? "" : item?.endDate || "",
    department: item?.department || "",
    position: item?.position || "",
    description: item?.description || "",
    isCurrent: Boolean(item?.isCurrent),
  }));
  const filtered = normalized.filter((item) => !isCadreRowEmpty(item));
  if (!filtered.length) {
    filtered.push(createCadreItem());
  }
  cadreItems.splice(0, cadreItems.length, ...filtered);
}

function saveUser(data) {
  const user = {
    username: data.username,
    displayName: data.displayName,
    avatarUrl: data.avatarUrl || "",
    role: data.role || profile.role || "STUDENT",
    studentNo: data.studentNo || "",
    className: data.className || "",
    college: FIXED_COLLEGE,
  };
  localStorage.setItem("bdai_sc_user", JSON.stringify(user));
}

onMounted(async () => {
  fetchReviewSettings().catch(() => {});
  try {
    const { data } = await getStudentProfile();
    applyProfileResponse(data);
  } catch (err) {
    console.error(err);
  }
});

watch(
  () => info.offCampusLiving,
  (next) => {
    if (next) {
      info.dormCampus = "";
      info.dormBuilding = "";
      info.dormRoom = "";
      info.dormFloor = "";
      info.dormRoomNo = "";
    } else {
      info.offCampusAddress = "";
      info.offCampusProvince = "";
      info.offCampusCity = "";
      info.offCampusCounty = "";
      info.offCampusDetail = "";
    }
  },
);

watch(
  () => info.studentCategory,
  (category) => {
    if (!majorOptionsByCategory[category]) {
      info.classMajor = "";
      return;
    }
    if (!majorOptionsByCategory[category].includes(info.classMajor)) {
      info.classMajor = "";
    }
    if (category === "研究生") {
      info.classNo = 1;
    }
  },
);

watch(
  () => info.dormCampus,
  () => {
    if (!info.dormCampus) {
      info.dormBuilding = "";
      return;
    }
    const exists = dormBuildingOptions.value.some(
      (item) => item.value === info.dormBuilding && !item.disabled,
    );
    if (!exists) {
      info.dormBuilding = "";
    }
  },
);

watch(
  () => info.addressProvince,
  () => {
    if (!info.addressProvince) {
      info.addressCity = "";
      info.addressCounty = "";
      return;
    }
    if (
      !addressCityOptions.value.some((item) => item.value === info.addressCity)
    ) {
      info.addressCity = "";
    }
    if (
      !addressCountyOptions.value.some(
        (item) => item.value === info.addressCounty,
      )
    ) {
      info.addressCounty = "";
    }
  },
);

watch(
  () => info.addressCity,
  () => {
    if (!info.addressCity) {
      info.addressCounty = "";
      return;
    }
    if (
      !addressCountyOptions.value.some(
        (item) => item.value === info.addressCounty,
      )
    ) {
      info.addressCounty = "";
    }
  },
);

watch(
  () => info.offCampusProvince,
  () => {
    if (!info.offCampusProvince) {
      info.offCampusCity = "";
      info.offCampusCounty = "";
      return;
    }
    if (
      !offCampusCityOptions.value.some(
        (item) => item.value === info.offCampusCity,
      )
    ) {
      info.offCampusCity = "";
    }
    if (
      !offCampusCountyOptions.value.some(
        (item) => item.value === info.offCampusCounty,
      )
    ) {
      info.offCampusCounty = "";
    }
  },
);

watch(
  () => info.offCampusCity,
  () => {
    if (!info.offCampusCity) {
      info.offCampusCounty = "";
      return;
    }
    if (
      !offCampusCountyOptions.value.some(
        (item) => item.value === info.offCampusCounty,
      )
    ) {
      info.offCampusCounty = "";
    }
  },
);

watch(
  () => info.leagueDeveloping,
  (next) => {
    if (!next) {
      return;
    }
    info.leagueJoinDate = "";
    info.leagueNo = "";
    info.partyApplied = false;
    info.applicationDate = "";
    info.activistDate = "";
    info.partyTrainingDate = "";
    info.developmentTargetDate = "";
    info.probationaryMemberDate = "";
    info.fullMemberDate = "";
    info.activistDeveloping = false;
    info.partyTrainingPending = false;
    info.developmentTargetDeveloping = false;
    info.probationaryDeveloping = false;
    info.fullMemberDeveloping = false;
  },
);

watch(
  () => info.activistDeveloping,
  (next) => {
    if (!next) {
      return;
    }
    info.activistDate = "";
    info.partyTrainingDate = "";
    info.developmentTargetDate = "";
    info.probationaryMemberDate = "";
    info.fullMemberDate = "";
    info.partyTrainingPending = false;
    info.developmentTargetDeveloping = false;
    info.probationaryDeveloping = false;
    info.fullMemberDeveloping = false;
  },
);

watch(
  () => info.partyTrainingPending,
  (next) => {
    if (!next) {
      return;
    }
    info.partyTrainingDate = "";
    info.developmentTargetDate = "";
    info.probationaryMemberDate = "";
    info.fullMemberDate = "";
    info.developmentTargetDeveloping = false;
    info.probationaryDeveloping = false;
    info.fullMemberDeveloping = false;
  },
);

watch(
  () => info.developmentTargetDeveloping,
  (next) => {
    if (!next) {
      return;
    }
    info.developmentTargetDate = "";
    info.probationaryMemberDate = "";
    info.fullMemberDate = "";
    info.probationaryDeveloping = false;
    info.fullMemberDeveloping = false;
  },
);

watch(
  () => info.probationaryDeveloping,
  (next) => {
    if (!next) {
      return;
    }
    info.probationaryMemberDate = "";
    info.fullMemberDate = "";
    info.fullMemberDeveloping = false;
  },
);

watch(
  () => info.fullMemberDeveloping,
  (next) => {
    if (!next) {
      return;
    }
    info.fullMemberDate = "";
  },
);

const TW_ID_TYPES = ["台湾居民来往大陆通行证", "台湾居民居住证"];
const HKMO_ID_TYPES = ["港澳居民来往内地通行证", "港澳居民居住证"];

function detectHkMoFromIdNo(idNo) {
  if (!idNo) return { isHk: false, isMo: false };
  const cleaned = idNo.toUpperCase().replace(/[^0-9A-Z]/g, "");
  // 港澳居民来往内地通行证: H=香港, M=澳门
  if (/^H\d{8}$/.test(cleaned)) return { isHk: true, isMo: false };
  if (/^M\d{8}$/.test(cleaned)) return { isHk: false, isMo: true };
  // 港澳居民居住证: 81=香港, 82=澳门
  if (/^81\d{16}$/.test(cleaned)) return { isHk: true, isMo: false };
  if (/^82\d{16}$/.test(cleaned)) return { isHk: false, isMo: true };
  return { isHk: false, isMo: false };
}

watch(
  () => info.idType,
  (nextType) => {
    if (isEditing.value) {
      info.idNo = "";
    }
    if (TW_ID_TYPES.includes(nextType)) {
      info.isHk = false;
      info.isMo = false;
      info.isTw = true;
    } else if (HKMO_ID_TYPES.includes(nextType)) {
      info.isTw = false;
      const detected = detectHkMoFromIdNo(info.idNo);
      info.isHk = detected.isHk;
      info.isMo = detected.isMo;
    } else {
      info.isHk = false;
      info.isMo = false;
      info.isTw = false;
    }
  },
);

watch(
  () => info.idNo,
  (nextIdNo) => {
    if (HKMO_ID_TYPES.includes(info.idType)) {
      const detected = detectHkMoFromIdNo(nextIdNo);
      info.isHk = detected.isHk;
      info.isMo = detected.isMo;
    }
  },
);

function loadUser() {
  try {
    const raw = JSON.parse(localStorage.getItem("bdai_sc_user") || "{}");
    return {
      username: raw.username || "",
      displayName: raw.displayName || "",
      avatarUrl: raw.avatarUrl || "",
      role: raw.role || "STUDENT",
      studentNo: raw.studentNo || "",
      className: raw.className || "",
      college: FIXED_COLLEGE,
    };
  } catch {
    return {
      username: "",
      displayName: "",
      avatarUrl: "",
      role: "STUDENT",
      studentNo: "",
      className: "",
      college: FIXED_COLLEGE,
    };
  }
}
</script>

<style scoped>
@import "../assets/styles/my-infos-view.css";

.class-num {
  width: 76px !important;
  flex-shrink: 0;
}
</style>
