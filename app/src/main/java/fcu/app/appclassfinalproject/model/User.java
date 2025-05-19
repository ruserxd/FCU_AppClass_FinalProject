package fcu.app.appclassfinalproject.model;

public class User {

  private int ID;
  private String account;
  private String email;

  public User(int ID, String account, String email) {
    this.ID = ID;
    this.account = account;
    this.email = email;
  }

  public int getID() {
    return ID;
  }

  public String getAccount() {
    return account;
  }

  public String getEmail() {
    return email;
  }
}
