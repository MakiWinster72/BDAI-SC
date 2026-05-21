package com.gcsc.studentcenter.audit;

/**
 * 操作日志 action 常量（与前端 LogsView ACTION_LABELS 保持一致）。
 */
public final class AuditActions {

  private AuditActions() {
  }

  public static final String BACKUP_DB = "BACKUP_DB";
  public static final String RESTORE_DB = "RESTORE_DB";
  public static final String BACKUP_ATTACHMENTS = "BACKUP_ATTACHMENTS";
  public static final String RESTORE_ATTACHMENTS = "RESTORE_ATTACHMENTS";
  public static final String DELETE_STORAGE = "DELETE_STORAGE";

  public static final String EXPORT_STUDENTS = "EXPORT_STUDENTS";
  public static final String EXPORT_STUDENT_PDF = "EXPORT_STUDENT_PDF";

  public static final String CREATE_USER = "CREATE_USER";
  public static final String UPDATE_USER = "UPDATE_USER";
  public static final String DELETE_USER = "DELETE_USER";
  public static final String UPDATE_TEACHER_CLASSES = "UPDATE_TEACHER_CLASSES";

  public static final String UPDATE_REVIEW_SETTINGS = "UPDATE_REVIEW_SETTINGS";
  public static final String UPDATE_SYSTEM_SETTINGS = "UPDATE_SYSTEM_SETTINGS";
  public static final String UPDATE_UPLOAD_SETTINGS = "UPDATE_UPLOAD_SETTINGS";

  public static final String REGISTER = "REGISTER";
  public static final String LOGIN_FAILED = "LOGIN_FAILED";
  public static final String CHANGE_PASSWORD = "CHANGE_PASSWORD";

  public static final String SUBMIT_PROFILE_REVIEW = "SUBMIT_PROFILE_REVIEW";
  public static final String CANCEL_PROFILE_REVIEW = "CANCEL_PROFILE_REVIEW";
  public static final String AUTO_APPROVE_PROFILE = "AUTO_APPROVE_PROFILE";
  public static final String APPROVE_PROFILE = "APPROVE_PROFILE";
  public static final String REJECT_PROFILE = "REJECT_PROFILE";

  public static final String SUBMIT_ACHIEVEMENT_REVIEW = "SUBMIT_ACHIEVEMENT_REVIEW";
  public static final String CANCEL_ACHIEVEMENT_REVIEW = "CANCEL_ACHIEVEMENT_REVIEW";
  public static final String AUTO_APPROVE_ACHIEVEMENT = "AUTO_APPROVE_ACHIEVEMENT";
  public static final String APPROVE_ACHIEVEMENT = "APPROVE_ACHIEVEMENT";
  public static final String REJECT_ACHIEVEMENT = "REJECT_ACHIEVEMENT";

  public static final String DIRECT_UPDATE_PROFILE = "DIRECT_UPDATE_PROFILE";
  public static final String ADMIN_UPDATE_PROFILE = "ADMIN_UPDATE_PROFILE";

  public static final String DIRECT_CREATE_ACHIEVEMENT = "DIRECT_CREATE_ACHIEVEMENT";
  public static final String DIRECT_UPDATE_ACHIEVEMENT = "DIRECT_UPDATE_ACHIEVEMENT";
  public static final String DIRECT_DELETE_ACHIEVEMENT = "DIRECT_DELETE_ACHIEVEMENT";
}
