import server.Server;

import java.io.File;
import java.util.Date;

public class FileManager {
    private final String file_path;

    FileManager(String file_path) {
        this.file_path = file_path;
    }

    public void  printFileInformation(File file) {
        System.out.println((file.isDirectory() ? "[DIR] " : "[FILE] ")
                + file.getName() + "  " + file.getPath() + "  " + new Date(file.lastModified()));
    }

    void print_all_files() {
        File dir = new File(file_path);
        File[] entries = dir.listFiles();
        if (entries != null) {
            System.out.println("dir / file    name   path    last modified");
            for (File entry : entries) {
                printFileInformation(entry);
            }
        }
    }

    public static void main(String[] args) {
        FileManager fm = new FileManager("server");
        fm.print_all_files();

    }

}
