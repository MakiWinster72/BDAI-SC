<template>
  <footer class="app-footer">
    <div class="footer-inner">
      <div class="footer-links-row">
        <div class="footer-links">
          <span class="footer-links-title">友情链接</span>
          <div class="footer-links-items">
            <a
              v-for="link in friendLinks"
              :key="link.label"
              :href="link.href"
              target="_blank"
              rel="noopener noreferrer"
              class="footer-link"
              >{{ link.label }}</a
            >
          </div>
        </div>
        <div class="footer-contact">
          <div class="footer-contact-item">
            <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
              <path
                d="M2 5a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5z"
                stroke="currentColor"
                stroke-width="1.4"
              />
              <path
                d="M2 7l8 5 8-5"
                stroke="currentColor"
                stroke-width="1.4"
                stroke-linecap="round"
              />
            </svg>
            <span>广东省广州市海珠区仑头路21号</span>
          </div>
          <div class="footer-contact-item">
            <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
              <path
                d="M3 5a2 2 0 0 1 2-2h1.5A1.5 1.5 0 0 1 8 4.5v0A1.5 1.5 0 0 1 6.5 6v0A2.5 2.5 0 0 0 9 8.5h1A2.5 2.5 0 0 0 12.5 6v0A1.5 1.5 0 0 1 14 4.5v0A1.5 1.5 0 0 1 12.5 3H11A2 2 0 0 1 9 1a7 7 0 0 0-6 7v1a2 2 0 0 0 2 2z"
                stroke="currentColor"
                stroke-width="1.4"
              />
            </svg>
            <span>020-84096458</span>
          </div>
        </div>
      </div>

      <div class="footer-bottom">
        <div class="footer-logo-row">
          <img
            src="/assets/icons/xylogo.png"
            alt="学院"
            class="footer-school-logo"
          />
          <div class="footer-brand-text">
            <span class="footer-brand-name">大数据与人工智能学院</span>
            <span class="footer-brand-sub">学生信息管理中心</span>
          </div>
        </div>
        <div class="footer-copy">
          <p class="footer-copyright">
            版权所有&copy;广东财经大学-大数据与人工智能学院　备案号：粤ICP备05000001号
          </p>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
const friendLinks = [
  { label: "学校官网", href: "https://www.gdufe.edu.cn/main.htm" },
  { label: "教务处", href: "https://jwc.gdufe.edu.cn/" },
  { label: "科研处", href: "https://kyc.gdufe.edu.cn/" },
  { label: "研究生处", href: "https://yjsc.gdufe.edu.cn/" },
  { label: "图书馆", href: "https://lib.gdufe.edu.cn/" },
  {
    label: "信息门户",
    href: "https://authserver.gdufe.edu.cn/authserver/login?service=https://imy.gdufe.edu.cn/shiro-cas",
  },
];
</script>

<style scoped>
.app-footer {
  margin-top: 40px;
  border-radius: 20px;
  border: 1px solid rgba(100, 12, 114, 0.12);
  background: var(--card);
  backdrop-filter: blur(12px);
  overflow: hidden;
}

.footer-inner {
  padding: 24px 28px 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ── Links Row ─────────────────────────────── */
.footer-links-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
}

.footer-links {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.footer-links-title {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-sub);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  white-space: nowrap;
  flex-shrink: 0;
}

.footer-links-items {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.footer-link {
  font-size: 13px;
  color: var(--primary);
  text-decoration: none;
  padding: 3px 10px;
  border-radius: 999px;
  background: var(--primary-surface);
  transition:
    background 180ms ease,
    color 180ms ease,
    transform 120ms ease;
  white-space: nowrap;
}

.footer-link:hover {
  background: rgba(100, 12, 114, 0.14);
  color: var(--primary-dark);
  transform: translateY(-1px);
}

.footer-link:active {
  transform: translateY(0);
}

/* ── Contact ─────────────────────────────── */
.footer-contact {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex-shrink: 0;
}

.footer-contact-item {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 13px;
  color: var(--text-sub);
}

.footer-contact-item svg {
  width: 15px;
  height: 15px;
  flex-shrink: 0;
  color: var(--text-sub);
  opacity: 0.7;
}

/* ── Divider ─────────────────────────────── */
.footer-divider {
  height: 1px;
  background: var(--line);
  margin: 0;
}

/* ── Bottom Row ─────────────────────────── */
.footer-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--line);
  flex-wrap: wrap;
}

.footer-logo-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.footer-school-logo {
  height: 38px;
  width: auto;
  object-fit: contain;
  opacity: 0.7;
}

.footer-brand-text {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.footer-brand-name {
  font-size: 13px;
  font-weight: 700;
  color: var(--primary-dark);
  white-space: nowrap;
}

.footer-brand-sub {
  font-size: 11px;
  color: var(--text-sub);
  white-space: nowrap;
}

.footer-copy {
  flex: 1;
  min-width: 0;
}

.footer-copyright {
  margin: 0;
  font-size: 12px;
  color: var(--text-sub);
  text-align: right;
  line-height: 1.5;
}

/* ── Responsive ─────────────────────────── */
@media (max-width: 640px) {
  .footer-inner {
    padding: 16px;
  }

  .footer-links-row {
    flex-direction: column;
    gap: 16px;
  }

  .footer-bottom {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .footer-copyright {
    text-align: left;
  }
}
</style>
