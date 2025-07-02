package universal.managers;

import universal.FilePart;
import universal.announcements.FileInformation;
import universal.announcements.ListClientsFiles;
import universal.models.TypeOfFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    public File getFile(String fileName) {
        String projectRootPath = System.getProperty("user.dir");
        return new File(projectRootPath + File.separator + this.mainDirPath + File.separator + fileName);
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
        information.fileSize = (int) file.length();
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
        File file = getFile(filePath);

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

    public boolean saveFileFromParts(Map<Integer, FilePart> parts) {
        if (parts==null || parts.isEmpty()) return false;

        List<Integer> sortedKeys = new ArrayList<>(parts.keySet());
        Collections.sort(sortedKeys);

        String fileName = parts.get(sortedKeys.get(0)).pathFile;

        String fullPath = this.mainDirPath + File.separator + fileName;
        createDir(fullPath);
        try (FileOutputStream fos = new FileOutputStream(fullPath)) {
            for (int key : sortedKeys) {
                FilePart part = parts.get(key);
                fos.write(part.data, 0, part.partSize);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createDir( String fullPath) {
        File file = new File(fullPath);


        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            boolean dirsCreated = parentDir.mkdirs();
            if (!dirsCreated) {
                System.out.println("Nie udało się utworzyć katalogów: " + parentDir.getAbsolutePath());
                return false;
            }
        }
        return true;
    }

    public void updateModificationDates(List<FileInformation> list) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);

        for (FileInformation fi : list) {
            String pathFile = this.mainDirPath + File.separator + fi.filePath;
            File file = new File(pathFile);
            if (file.exists()) {
                try {
                    Date parsedDate = sdf.parse(fi.modfiferTime);
                    long newModTime = parsedDate.getTime();

                    boolean success = file.setLastModified(newModTime);
                    if (success) {
                        fi.modfiferTime = new Date(newModTime).toString();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Plik nie istnieje: " + pathFile);
            }
        }
    }

    public  boolean moveFile(String sourcePath, String targetPath) {
        Path source = Paths.get(this.mainDirPath + File.separator + sourcePath);
        Path target = Paths.get(this.mainDirPath + File.separator + targetPath);

        try {
            Files.createDirectories(target.getParent());

            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Nie udało się przenieść pliku: " + e.getMessage());
            return false;
        }
    }

}
