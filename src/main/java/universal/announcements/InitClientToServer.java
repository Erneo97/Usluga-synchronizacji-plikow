package universal.announcements;

import java.io.Serializable;

public class InitClientToServer implements Serializable {
    public String  IP, pathClientArchive;
    public long ID;

    @Override
    public String toString() {
        return this.ID + " - " + this.pathClientArchive;
    }
}
