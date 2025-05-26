package fcu.app.appclassfinalproject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fcu.app.appclassfinalproject.dataBase.SqlDataBaseHelper;
import fcu.app.appclassfinalproject.model.Project;
import fcu.app.appclassfinalproject.model.Issue;
public class ExportExcel {
    private static final String TAG = "ExcelExporter";
    private Context context;
    private SQLiteDatabase db;
    private SqlDataBaseHelper dbHelper;
    public ExportExcel(Context context, SQLiteDatabase database) {
        this.context = context;
        this.db = database;
        dbHelper = new SqlDataBaseHelper(context);
        db = dbHelper.getReadableDatabase();
    }

    // 從資料庫獲取所有專案
    private List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM Projects ORDER BY id", null);

        if (cursor.moveToFirst()) {
            do {
                Project project = new Project(
                        cursor.getInt(0),           // id
                        cursor.getString(1),        // name
                        cursor.getString(2),        // summary
                        cursor.getInt(3)        // manager
                );
                projects.add(project);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return projects;
    }

    // 根據專案ID獲取議題
    private List<Issue> getIssuesByProjectId(int projectId) {
        List<Issue> issues = new ArrayList<>();
        String query = "SELECT * FROM Issues WHERE project_id = ? ";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(projectId)});
        if (cursor.moveToFirst()) {
            do {
                Issue issue = new Issue(
                        cursor.getString(1),        // name
                        cursor.getString(2),        // summary
                        cursor.getString(3),        // start time
                        cursor.getString(4),        // end time
                        cursor.getString(5),        // status
                        cursor.getString(6)         //designee
                );
                issues.add(issue);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return issues;
    }

    private Map<Integer, String> getManagerIdAccountMap() {
        Map<Integer, String> map = new HashMap<>();
        Cursor cursor = db.rawQuery("SELECT id, account FROM Users", null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String account = cursor.getString(1);
                map.put(id, account);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return map;
    }


    // 建立專案列表工作表
    private void createProjectSheet(Workbook workbook, List<Project> projects) {
        Sheet sheet = workbook.createSheet("專案列表");

        // 建立標題樣式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 建立標題行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"專案ID", "專案名稱", "專案概述","管理者"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 取得 manager 對照表
        Map<Integer, String> managerMap = getManagerIdAccountMap();
        // 填入專案資料
        for (int i = 0; i < projects.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Project project = projects.get(i);
            String managerAccount = managerMap.getOrDefault(project.getManagerId(), "未知");
            row.createCell(3).setCellValue(managerAccount);

            row.createCell(0).setCellValue(project.getId());
            row.createCell(1).setCellValue(project.getName());
            row.createCell(2).setCellValue(project.getSummary());
            //row.createCell(3).setCellValue(managerName);

        }

        // 手動調整欄寬
        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 20 * 256); // 這樣設定欄寬為 20 個字符

        }
    }

    // 建立專案議題工作表
    private void createIssueSheet(Workbook workbook, Project project, List<Issue> issues) {
        String sheetName = project.getName() + "_議題";
        // Excel工作表名稱不能超過31個字元
        if (sheetName.length() > 31) {
            sheetName = sheetName.substring(0, 28) + "...";
        }

        Sheet sheet = workbook.createSheet(sheetName);

        // 建立標題樣式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 專案資訊行
        Row projectInfoRow = sheet.createRow(0);
        Cell projectInfoCell = projectInfoRow.createCell(0);
        projectInfoCell.setCellValue("專案: " + project.getName() + " (ID: " + project.getId() + ")");
        CellStyle projectInfoStyle = workbook.createCellStyle();
        Font projectInfoFont = workbook.createFont();
        projectInfoFont.setBold(true);
        projectInfoFont.setFontHeightInPoints((short) 14);
        projectInfoStyle.setFont(projectInfoFont);
        projectInfoCell.setCellStyle(projectInfoStyle);

        // 空白行
        sheet.createRow(1);

        // 建立標題行
        Row headerRow = sheet.createRow(2);
        String[] headers = {"議題ID", "主旨", "概述", "開始時間", "結束時間", "狀態", "被指派者"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 填入議題資料
        for (int i = 0; i < issues.size(); i++) {
            Row row = sheet.createRow(i + 3);
            Issue issue = issues.get(i);

            row.createCell(0).setCellValue(i+1);
            row.createCell(1).setCellValue(issue.getName());
            row.createCell(2).setCellValue(issue.getSummary());
            row.createCell(3).setCellValue(issue.getStart_time());
            row.createCell(4).setCellValue(issue.getEnd_time());
            row.createCell(5).setCellValue(issue.getStatus());
            row.createCell(6).setCellValue(issue.getDesignee());
        }

        // 手動調整欄寬
        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 20 * 256); // 這樣設定欄寬為 20 個字符

        }
    }

    // 主要匯出方法
    public void exportToExcel(String fileName) {
        try {
            // 獲取資料
            List<Project> projects = getAllProjects();
            if (projects.isEmpty()) {
                Toast.makeText(this.context,"沒有專案資料可以匯出",Toast.LENGTH_LONG).show();
            }

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
            Toast.makeText(this.context,"Excel檔案匯出成功: "+ file.getAbsolutePath(),Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Toast.makeText(this.context,"匯出Excel時發生錯誤"+e,Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this.context,"匯出過程中發生未預期的錯誤"+e,Toast.LENGTH_SHORT).show();
        }
    }

}