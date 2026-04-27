# Important File Locations

## Backend
```
backend/src/main/resources/application.yml              # DB, JWT secret, CORS, upload dir
backend/src/main/java/com/gcsc/studentcenter/config/SecurityConfig.java
backend/src/main/java/com/gcsc/studentcenter/config/JwtAuthenticationFilter.java
backend/src/main/java/com/gcsc/studentcenter/config/GlobalCorsFilter.java
backend/src/main/java/com/gcsc/studentcenter/config/WebConfig.java
backend/src/main/java/com/gcsc/studentcenter/service/JwtService.java
backend/src/main/java/com/gcsc/studentcenter/service/AchievementService.java
backend/src/main/java/com/gcsc/studentcenter/service/StudentProfileService.java
backend/src/main/java/com/gcsc/studentcenter/service/AuthService.java
backend/src/main/java/com/gcsc/studentcenter/entity/AppUser.java
backend/src/main/java/com/gcsc/studentcenter/entity/StudentProfile.java
backend/src/main/java/com/gcsc/studentcenter/entity/AchievementContest.java  # Pattern for all 9 types
```

## Frontend
```
frontend/src/main.js                                    # Vue bootstrap, toast exposure
frontend/src/App.vue                                    # Root (RouterView + ToastContainer)
frontend/src/router/index.js                            # Routes with auth/meta guards
frontend/src/api/request.js                             # Axios instance, 401 interceptor
frontend/src/layouts/DashboardLayout.vue                # Shell with embedded mode
frontend/src/views/LoginView.vue                         # Login with decorative viz
frontend/src/views/AchievementsView.vue                  # Achievement feed/editor/preview
frontend/src/views/MyInfosView.vue                       # Student profile editor
frontend/src/views/NotificationsView.vue                 # Review request inbox
frontend/src/views/AdminView.vue                         # User/backup management
frontend/src/composables/useToast.js
frontend/src/composables/useAchievementEditor.js
frontend/src/composables/useAchievementUpload.js
frontend/src/composables/useNotifications.js
frontend/src/constants/achievementConstants.js           # Field configs for all 9 types
frontend/src/utils/media.js                              # URL resolution, media type detection
frontend/src/utils/achievement.js                       # Achievement normalization
```