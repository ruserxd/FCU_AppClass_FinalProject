package fcu.app.appclassfinalproject.model;

public class Project {

  private int ID;
  private String name;
  private String summary;
  private int managerId;

  public Project(int ID, String name, String summary, int managerId) {
    this.ID = ID;
    this.name = name;
    this.summary = summary;
    this.managerId = managerId;
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

  public int getManagerId() {
    return managerId;
  }

}
