package database;

public class UserVerification {
    private  int id;
    public enum StatusVerification {CORRECT, UNVERIFIED};
    private StatusVerification status;


    public UserVerification( int id, StatusVerification status) {
        this.id = id;
        this.status = status;
    }

    public UserVerification() {}

    public int getId() {return id;}
    public StatusVerification getStatus() {return status;}


    @Override
    public String toString() {
        return this.id + "\t" + status.toString();
    }
}
