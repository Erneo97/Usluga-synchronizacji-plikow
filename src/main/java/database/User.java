package database;

public class User {
    private  String ip;
    private  String dir_user;
    private  int id;

    public User(String ip, String dir_user, int id) {
        this.ip = ip;
        this.dir_user = dir_user;
        this.id = id;
    }

    public User() {}

    public String getIp() {return ip;}
    public String getDir_user() {return dir_user;}
    public int getId() {return id;}

    @Override
    public String toString() {
        return this.id + "\t" + this.ip + "\t" + this.dir_user;
    }
}
