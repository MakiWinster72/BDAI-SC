# Features

## StudentProfile Sections
- Basic (name, studentNo, class*, college, enrollmentDate)
- Personal (ethnicity, politicalStatus, birthDate, nativePlace, idType/idNo)
- Contact (phone, backupContact, address)
- Dorm (dormCampus, dormBuilding, dormRoom)
- Academic admin (classTeacher, counselor)
- Party membership progression (league → party application → activist → training → development → probationary → full member)
- Education history (5 slots, current row clears subsequent)
- Cadre experience (5 slots, same current behavior)
- Family info (parents' names, work, phone)
- Emergency contact

## Review/Approval Workflow
1. Student submits review request with `payloadSnapshot` and `changes`
2. TEACHER/ADMIN sees pending entry in NotificationsView
3. Approve: sets status to `approved`; Reject: requires reason
4. Cancel: requester can cancel own pending request
5. Auto-approve: if `reviewSettings.achievementReviewAutoApprove` or `profileReviewAutoApprove`

## File Upload
- `POST /api/upload` with `multipart/form-data`
- Stored in `backend/uploads/` directory, served at `/uploads/**`
- Per-category limits: `imageMaxCount`, `imageMaxSizeMb`, `attachmentMaxCount`, `attachmentMaxSizeMb`
- Configurable allowed extensions per type
- Max 9 images + 9 attachments per achievement