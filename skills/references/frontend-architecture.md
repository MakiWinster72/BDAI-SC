# Frontend Architecture

## Directory Structure
```
frontend/src/
├── api/           # Axios modules (auth, achievements, profile, upload, admin...)
├── assets/styles/ # CSS files (dialogs.css, layout.css, achievements.css...)
├── components/    # Shared Vue components
├── composables/   # Vue Composition API hooks
├── constants/     # Menu config, achievement field definitions
├── layouts/       # DashboardLayout.vue
├── router/        # Vue Router index.js
├── utils/         # Media resolution, achievement normalization
└── views/         # Page components (LoginView, AchievementsView...)
```

## Naming Conventions

| Layer | Convention | Example |
|-------|-----------|---------|
| Database | snake_case | `student_no`, `class_name` |
| Java | camelCase (fields), PascalCase (classes) | `studentNo`, `AppUser` |
| JavaScript/Vue | camelCase (vars/fns), PascalCase (components) | `studentNo`, `AchievementsView` |
| CSS classes | kebab-case | `dashboard-layout`, `achievement-card` |
| API endpoints | camelCase | `/api/achievements`, `/api/student-profiles` |

## Router & Auth

| Route | View | Access |
|-------|------|--------|
| `/login` | LoginView | guestOnly |
| `/register` | RegisterView | guestOnly (checks registration flag) |
| `/myinfos` | MyInfosView | requiresAuth |
| `/achievements` | AchievementsView | requiresAuth (supports `?embed=1`) |
| `/notifications` | NotificationsView | requiresAuth |
| `/student-info` | StudentInfoView | requiresAuth + TEACHER/ADMIN |
| `/admin` | AdminView | requiresAuth + ADMIN only |
| `/settings` | SettingsView | requiresAuth |

**Auth flow**: Token stored as `bdai_sc_token`, user as `bdai_sc_user` in localStorage. 401 response interceptor clears storage and redirects to `/login`.

## API Modules (src/api/)

| Module | Key Exports |
|--------|-------------|
| `request.js` | Axios instance; auto-attaches Bearer token; 401 handler |
| `auth.js` | register, login, getMe, changePassword, getLoginHistory |
| `achievements.js` | getAchievements, getAchievement, createAchievement, updateAchievement, deleteAchievement |
| `profile.js` | getStudentProfile, saveStudentProfile, searchStudentProfiles |
| `upload.js` | uploadMedia(file, options) - multipart form |

## Key Composables (src/composables/)

| Composable | Purpose |
|-----------|---------|
| `useToast` | Global toast notifications (`success/error/info/warn`, 4500ms) |
| `useAchievementEditor` | Achievement form state, image/attachment upload, save with review flow |
| `useAchievementUpload` | Upload limits, file type/size validation |
| `useAchievementList` | Achievement list fetching and filtering |
| `useAchievementPreview` | Media preview (image/video/document/sheet/pdf) |
| `useNotifications` | Review request inbox, submit/approve/reject/cancel |
| `useReviewSettings` | Review feature toggles (auto-approve) |
| `useDashboardShell` | Sidebar state, navigation |

## Key Constants (src/constants/)

| File | Purpose |
|------|---------|
| `achievementConstants.js` | Field configs, hints, title keys, date keys for all 9 categories |
| `achievementDetailSchemas.js` | Detail view schemas for rendering achievement cards |
| `menu.js` | Sidebar menu items |

## CSS Organization (src/assets/styles/)

| File | Purpose |
|------|---------|
| `dialogs.css` | Sheet overlays, toasts, hints - z-index 1000+ |
| `layout.css` | Dashboard shell, grid, mobile capsule, breakpoints |
| `achievements.css` | Achievement card layouts, form styles, media grid |
| `auth.css` | Auth page layout, ghost/action button base styles |
| `my-infos-view.css` | Student profile editor styles |
| `login-view.css` | Login page decorative visualizations |

**Animation timing**: `0.42s cubic-bezier(0.22, 1, 0.36, 1)` for transform, `0.38s ease` for opacity.

## Embedded Mode

Activated via `?embed=1` query param. Hides `BrandHeader` and `DashboardSidebar` in `DashboardLayout`. Used for iframe integration.