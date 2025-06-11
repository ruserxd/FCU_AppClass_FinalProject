package fcu.app.appclassfinalproject.model;

import java.util.List;

public class Project {

  private int ID;
  private String name;
  private String summary;
  private List<Integer> memberIds; // 專案成員ID
  private List<String> memberNames; // 成員名稱列表


  public Project(int ID, String name, String summary, List<Integer> memberIds,
      List<String> memberNames) {
    this.ID = ID;
    this.name = name;
    this.summary = summary;
    this.memberIds = memberIds;
    this.memberNames = memberNames;
  }

  public int getId() {
    return ID;
  }

  public String getName() {
    return name;
  }

  public String getSummary() {
    return summary;
  }

  public List<String> getMemberNames() {
    return memberNames;
  }
}