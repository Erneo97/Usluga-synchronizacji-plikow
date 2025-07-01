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
    private String mainDirPath;

    public FileManager(String mainDirPath) {
        this.mainDirPath = mainDirPath;
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
        this.mainDirPath = this.mainDirPath.replace('/', '\\');
        FileInformation information = new FileInformation();

        information.fileName = file.getName();
        information.fileSize = String.format("%d", file.length());
        information.modfiferTime = new Date(file.lastModified()).toString();
        information.fileType = file.isDirectory() ? TypeOfFile.DIR.name() : TypeOfFile.FILE.name();
        information.filePath = file.getPath().replace(this.mainDirPath, "");

        return information;
    }


    private File[] getListOfFiles() {
        File managedDirectory = new File(mainDirPath);
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


    public boolean deleteFile(String filePath) {
        String projectRootPath = System.getProperty("user.dir");
        File file = new File(projectRootPath + File.separator + this.mainDirPath + File.separator + filePath);

        if (!file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    deleteFile(f.getPath());
                }
            }
        }
        return file.delete();
    }

    public static boolean overwriteFile(String filePath, byte[] content, long lastModifiedMillis) throws IOException {
        Files.write(Paths.get(filePath), content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        FileTime fileTime = FileTime.fromMillis(lastModifiedMillis);
        Files.setLastModifiedTime(Paths.get(filePath), fileTime);

        return true;
    }

    public  boolean addNewFile(String filePath, byte[] content)  {
        try {
            String projectRootPath = System.getProperty("user.dir");
            Path fullPath = Paths.get(projectRootPath, this.mainDirPath, filePath);
            Files.createDirectories(fullPath.getParent());
            Files.write(fullPath, content);

            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
