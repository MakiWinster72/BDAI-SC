---
name: bdai-sc
description: 'BDAI_SC Student Center project guide. Use when working on this Java Spring Boot + Vue 3 student achievement management system. Covers backend architecture (controllers, services, entities, JPA repositories, JWT security, achievement entity pattern), frontend structure (Vue 3 Composition API, router, API modules, composables, CSS organization), naming conventions (snake_case DB, camelCase JS, kebab-case CSS, PascalCase components), key features (StudentProfile, 9 achievement types, file uploads, review/approval workflow, embedded mode), and important file locations.'
---

# BDAI_SC Student Center

Full-stack student achievement management system for 大数据与人工智能学院.

## Tech Stack

- **Backend**: Java 21 + Spring Boot 3.3.5 + MySQL + JPA + Spring Security + JWT
- **Frontend**: Vue 3 (Composition API `<script setup>`) + Vite + Vue Router + Axios + AG Grid
- **Ports**: Backend 8080, Frontend 5173

## Reference Index

| Topic | Reference File |
|-------|----------------|
| Backend package structure, entities, security, JWT flow | [references/backend-architecture.md](references/backend-architecture.md) |
| Frontend directory, naming conventions, router, API modules, composables, CSS | [references/frontend-architecture.md](references/frontend-architecture.md) |
| StudentProfile sections, review/approval workflow, file upload | [references/features.md](references/features.md) |
| Backend & frontend key file locations | [references/file-locations.md](references/file-locations.md) |
| Build commands, database setup, role-based access | [references/build-run.md](references/build-run.md) |

## Common Patterns

### Adding a New Achievement Type
1. **Backend**: Create new entity class (extend base or copy pattern), add repository, add to `AchievementService` polymorphic map, add controller endpoints
2. **Frontend**: Add entry to `achievementConstants.js` category list, add detail schema to `achievementDetailSchemas.js`, ensure field map in category entries

### Adding a New API Endpoint
1. Add DTO in `dto/` (Request for input, Response for output)
2. Add service method in `service/`
3. Add controller endpoint in `controller/`
4. Add frontend API function in `api/` module
5. Update `SecurityConfig.java` if route needs auth

### Component Style
Vue SFCs use `<script setup>` with Composition API:
- Imports at top (Vue, components, composables, utils, constants, API)
- Reactive state via `ref()`, `reactive()`, `computed()`
- `defineProps` / `defineEmits`
- Lifecycle: `onMounted`, `onBeforeUnmount`, `watch`
- Styles via `@import` at bottom, scoped