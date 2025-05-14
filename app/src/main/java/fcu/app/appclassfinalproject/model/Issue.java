package fcu.app.appclassfinalproject.model;

public class Issue {
    private String name;
    private String summary;
    private String start_time;
    private String end_time;
    private String status;
    private String designee;

    public Issue(String name, String summary, String start_time, String end_time, String status, String designee) {
        this.name = name;
        this.summary = summary;
        this.start_time = start_time;
        this.end_time = end_time;
        this.status = status;
        this.designee = designee;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public String getStatus() {
        return status;
    }

    public String getDesignee() {
        return designee;
    }
}
