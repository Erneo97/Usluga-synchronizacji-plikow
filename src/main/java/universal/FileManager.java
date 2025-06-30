package universal;

import announcements.FileInformation;
import announcements.ListClientsFiles;
import announcements.TypeOfFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
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
        listClientsFiles.ID = 1;
        listClientsFiles.filesInformation = info;


    }

    public boolean createDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return false;
    }

    public static void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }


    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
    }

    public static boolean overwriteFile(String filePath, byte[] content, long lastModifiedMillis) throws IOException {
        Files.write(Paths.get(filePath), content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        FileTime fileTime = FileTime.fromMillis(lastModifiedMillis);
        Files.setLastModifiedTime(Paths.get(filePath), fileTime);

        return true;
    }

    public static boolean addNewFile(String filePath, byte[] content) throws IOException {
        Files.write(Paths.get(filePath), content);
        return true;
    }

}
