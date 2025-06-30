package universal;

import announcements.FileInformation;
import announcements.ListClientsFiles;
import announcements.TypeOfFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileManager {
    private final String file_path;

    public FileManager(String file_path) {
        this.file_path = file_path;
    }

    public void  printFileInformation(File file) {
        System.out.println((file.isDirectory() ? "[DIR] " : "[FILE] ")
                + file.getName() + "  " + file.getPath() + "  " + new Date(file.lastModified()) + "  " + file.length());
    }

    @Deprecated
    private void print_all_files() {
        File[] entries = getListOfFiles();
        if (entries != null) {
            System.out.println("dir / file    name   path    last modified");
            for (File entry : entries) {
                printFileInformation(entry);
            }
        }
    }


    public List<FileInformation> getListOfFilesInformation() {
        File[] entries = getListOfFiles();
        if (entries == null) {
            return null;
        }

        List<FileInformation> list = new ArrayList<>();
        for (File entry : entries) {
            addFileAndSubfiles(entry, list);
        }

        return list;
    }

    private void addFileAndSubfiles(File file, List<FileInformation> list) {
        FileInformation information = getInformationFromFile(file);
        list.add(information);

        if (file.isDirectory()) {
            File[] subEntries = file.listFiles();
            if (subEntries != null) {
                for (File subFile : subEntries) {
                    addFileAndSubfiles(subFile, list); // rekurencja
                }
            }
        }
    }

    private FileInformation getInformationFromFile(File file) {
        FileInformation information = new FileInformation();

        information.fileName = file.getName();
        information.fileSize = String.format("%d", file.length());
        information.modfiferTime = new Date(file.lastModified()).toString();
        information.fileType = file.isDirectory() ? TypeOfFile.DIR.name() : TypeOfFile.FILE.name();
        information.filePath = file.getPath();

        return information;
    }


    private File[] getListOfFiles() {
        File managedDirectory = new File(file_path);
        File[] entries = managedDirectory.listFiles();
        return entries;
    }

    public static void main(String[] args) {
        FileManager fm = new FileManager("server");
        List<FileInformation> info = fm.getListOfFilesInformation();
        System.out.println(info);

        ListClientsFiles listClientsFiles = new ListClientsFiles();
        listClientsFiles.IP = "127.0.0.1";
        listClientsFiles.filesInformation = info;


    }

}
