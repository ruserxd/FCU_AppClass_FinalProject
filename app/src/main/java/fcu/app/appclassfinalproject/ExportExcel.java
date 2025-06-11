package fcu.app.appclassfinalproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.widget.Toast;
import fcu.app.appclassfinalproject.helper.ProjectHelper;
import fcu.app.appclassfinalproject.helper.SqlDataBaseHelper;
import fcu.app.appclassfinalproject.model.Issue;
import fcu.app.appclassfinalproject.model.Project;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExportExcel {

  private Context context;
  private SQLiteDatabase db;
  private SqlDataBaseHelper dbHelper;
  private int currentUserId;

  public ExportExcel(Context context, SQLiteDatabase database) {
    this.context = context;
    this.db = database;
    dbHelper = new SqlDataBaseHelper(context);
    db = dbHelper.getReadableDatabase();

    SharedPreferences prefs = context.getSharedPreferences("FCUPrefs", Context.MODE_PRIVATE);
    currentUserId = prefs.getInt("user_id", -1);
  }

  /**
   * 獲取當前用戶參與的所有專案
   */
  private List<Project> getAllUserProjects() {
    List<Project> projects = new ArrayList<>();

    if (currentUserId == -1) {
      return projects;
    }

    // 使用 ProjectHelper 獲取用戶參與的所有專案
    projects = ProjectHelper.getProjectsByUser(db, currentUserId);

    return projects;
  }

  /**
   * 根據專案ID獲取議題
   */
  private List<Issue> getIssuesByProjectId(int projectId) {
    List<Issue> issues = new ArrayList<>();
    String query = "SELECT * FROM Issues WHERE project_id = ? ORDER BY id";

    Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(projectId)});
    if (cursor.moveToFirst()) {
      do {
        Issue issue = new Issue(
            cursor.getString(1),        // name
            cursor.getString(2),        // summary
            cursor.getString(3),        // start time
            cursor.getString(4),        // end time
            cursor.getString(5),        // status
            cursor.getString(6)         // designee
        );
        issues.add(issue);
      } while (cursor.moveToNext());
    }
    cursor.close();
    return issues;
  }

  /**
   * 獲取當前語言設定
   */
  private String getCurrentLanguage() {
    SharedPreferences prefs = context.getSharedPreferences("FCUPrefs", Context.MODE_PRIVATE);
    return prefs.getString("app_language", "zh"); // 預設中文
  }

  /**
   * 建立專案列表工作表
   */
  private void createProjectSheet(Workbook workbook, List<Project> projects) {
    String currentLang = getCurrentLanguage();
    String sheetName = "zh".equals(currentLang) ? "專案列表" : "Project List";
    Sheet sheet = workbook.createSheet(sheetName);

    // 建立標題樣式
    CellStyle headerStyle = workbook.createCellStyle();
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerFont.setFontHeightInPoints((short) 16);
    headerStyle.setFont(headerFont);
    headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    // 建立標題行
    Row headerRow = sheet.createRow(0);
    String[] headers;
    if ("zh".equals(currentLang)) {
      headers = new String[]{"專案ID", "專案名稱", "專案概述", "專案成員"};
    } else {
      headers = new String[]{"Project ID", "Project Name", "Project Summary", "Project Members"};
    }

    for (int i = 0; i < headers.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(headers[i]);
      cell.setCellStyle(headerStyle);
    }

    // 填入專案資料
    for (int i = 0; i < projects.size(); i++) {
      Row row = sheet.createRow(i + 1);
      Project project = projects.get(i);

      // 專案基本資訊
      row.createCell(0).setCellValue(project.getId());
      row.createCell(1).setCellValue(project.getName());
      row.createCell(2).setCellValue(project.getSummary());

      // 組合所有成員名稱
      String membersText = "";
      if (project.getMemberNames() != null && !project.getMemberNames().isEmpty()) {
        membersText = String.join(", ", project.getMemberNames());
      } else {
        membersText = "zh".equals(currentLang) ? "無成員" : "No Members";
      }
      row.createCell(3).setCellValue(membersText);
    }

    // 手動調整欄寬
    for (int i = 0; i < headers.length; i++) {
      sheet.setColumnWidth(i, 25 * 256); // 增加欄寬以容納多個成員名稱
    }
  }

  /**
   * 建立專案議題工作表
   */
  private void createIssueSheet(Workbook workbook, Project project, List<Issue> issues) {
    String currentLang = getCurrentLanguage();
    String issueSuffix = "zh".equals(currentLang) ? "專案的議題" : " Issues";
    String sheetName = project.getName() + issueSuffix;

    if (sheetName.length() > 31) {
      sheetName = sheetName.substring(0, 28) + "...";
    }

    Sheet sheet = workbook.createSheet(sheetName);

    // 標題樣式
    CellStyle headerStyle = workbook.createCellStyle();
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerFont.setFontHeightInPoints((short) 16);
    headerStyle.setFont(headerFont);
    headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    // 專案資訊行
    Row projectInfoRow = sheet.createRow(0);
    Cell projectInfoCell = projectInfoRow.createCell(0);
    String projectInfo = "zh".equals(currentLang) ?
        "專案: " + project.getName() + " (ID: " + project.getId() + ")" :
        "Project: " + project.getName() + " (ID: " + project.getId() + ")";
    projectInfoCell.setCellValue(projectInfo);

    CellStyle projectInfoStyle = workbook.createCellStyle();
    Font projectInfoFont = workbook.createFont();
    projectInfoFont.setBold(true);
    projectInfoFont.setFontHeightInPoints((short) 14);
    projectInfoStyle.setFont(projectInfoFont);
    projectInfoCell.setCellStyle(projectInfoStyle);

    // 專案成員資訊行
    Row membersInfoRow = sheet.createRow(1);
    Cell membersInfoCell = membersInfoRow.createCell(0);
    String membersPrefix = "zh".equals(currentLang) ? "專案成員: " : "Project Members: ";
    String membersText = membersPrefix;
    if (project.getMemberNames() != null && !project.getMemberNames().isEmpty()) {
      membersText += String.join(", ", project.getMemberNames());
    } else {
      membersText += "zh".equals(currentLang) ? "無成員" : "No Members";
    }
    membersInfoCell.setCellValue(membersText);
    membersInfoCell.setCellStyle(projectInfoStyle);

    // 空白行
    sheet.createRow(2);

    // 標題行
    Row headerRow = sheet.createRow(3);
    String[] headers;
    if ("zh".equals(currentLang)) {
      headers = new String[]{"議題ID", "主旨", "概述", "開始時間", "結束時間", "狀態", "被指派者"};
    } else {
      headers = new String[]{"Issue ID", "Subject", "Summary", "Start Time", "End Time", "Status",
          "Assignee"};
    }

    for (int i = 0; i < headers.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(headers[i]);
      cell.setCellStyle(headerStyle);
    }

    // 內容樣式
    CellStyle contentStyle = workbook.createCellStyle();
    Font contentFont = workbook.createFont();
    contentFont.setFontHeightInPoints((short) 14);
    contentStyle.setFont(contentFont);

    // 填入議題資料
    for (int i = 0; i < issues.size(); i++) {
      Row row = sheet.createRow(i + 4);
      Issue issue = issues.get(i);

      Cell cell0 = row.createCell(0);
      cell0.setCellValue(i + 1);
      cell0.setCellStyle(contentStyle);

      Cell cell1 = row.createCell(1);
      cell1.setCellValue(issue.getName());
      cell1.setCellStyle(contentStyle);

      Cell cell2 = row.createCell(2);
      cell2.setCellValue(issue.getSummary());
      cell2.setCellStyle(contentStyle);

      Cell cell3 = row.createCell(3);
      cell3.setCellValue(issue.getStart_time());
      cell3.setCellStyle(contentStyle);

      Cell cell4 = row.createCell(4);
      cell4.setCellValue(issue.getEnd_time());
      cell4.setCellStyle(contentStyle);

      Cell cell5 = row.createCell(5);
      cell5.setCellValue(issue.getStatus());
      cell5.setCellStyle(contentStyle);

      Cell cell6 = row.createCell(6);
      cell6.setCellValue(issue.getDesignee());
      cell6.setCellStyle(contentStyle);
    }

    // 手動調整欄寬
    for (int i = 0; i < headers.length; i++) {
      sheet.setColumnWidth(i, 20 * 256);
    }
  }

  /**
   * 匯出用戶參與的專案
   */
  public void exportUserProjectsToExcel(String fileName) {
    try {
      // 獲取用戶參與的專案
      List<Project> projects = getAllUserProjects();
      if (projects.isEmpty()) {
        showToast(context.getString(R.string.excel_no_projects_to_export));
        return;
      }

      exportProjectsToExcel(projects, fileName, context.getString(R.string.excel_user_projects));

    } catch (Exception e) {
      showToast(context.getString(R.string.excel_export_unexpected_error, e.getMessage()));
    }
  }

  /**
   * 通用的專案匯出方法
   */
  private void exportProjectsToExcel(List<Project> projects, String fileName, String description) {
    try {
      // 建立Excel工作簿
      Workbook workbook = new XSSFWorkbook();

      // 建立專案列表工作表
      createProjectSheet(workbook, projects);

      // 為每個專案建立議題工作表
      for (Project project : projects) {
        List<Issue> issues = getIssuesByProjectId(project.getId());
        createIssueSheet(workbook, project, issues);
      }

      // 儲存檔案
      File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
      FileOutputStream outputStream = new FileOutputStream(file);
      workbook.write(outputStream);

      // 關閉資源
      outputStream.close();
      workbook.close();

      showToast(
          context.getString(R.string.excel_export_success, description, file.getAbsolutePath()));

    } catch (IOException e) {
      showToast(context.getString(R.string.excel_export_io_error, e.getMessage()));
    } catch (Exception e) {
      showToast(context.getString(R.string.excel_export_unexpected_error, e.getMessage()));
    }
  }

  /**
   * 主要匯出方法（匯出用戶參與的專案）
   */
  public void exportToExcel(String fileName) {
    exportUserProjectsToExcel(fileName);
  }

  /**
   * 顯示 Toast 訊息
   */
  private void showToast(String message) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
  }

  /**
   * 清理資源
   */
  public void cleanup() {
    if (db != null && db.isOpen()) {
      db.close();
    }
  }
}