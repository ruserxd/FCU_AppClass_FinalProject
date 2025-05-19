package fcu.app.appclassfinalproject.model;

public class issueMonth {

  private String start_time;
  private String end_time;
  private String status;


  public issueMonth(String start_time, String end_time, String status) {
    this.start_time = start_time;
    this.end_time = end_time;
    this.status = status;
  }

  public String getStatus() {
    return status;
  }

  public String getStart_time() {
    return start_time;
  }

  public String getEnd_time() {
    return end_time;
  }
}
