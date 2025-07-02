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

/**
 * Klasa {@code FileManager} służy do zarządzania plikami i katalogami
 * w określonym katalogu głównym.
 * <p>
 * Umożliwia pobieranie informacji o plikach, tworzenie i usuwanie katalogów i plików,
 * aktualizację dat modyfikacji, przenoszenie plików oraz zapisywanie plików
 * przesłanych w częściach.
 */
public class FileManager {

    /** Ścieżka do katalogu głównego, którym zarządza FileManager. */
    private String mainDirPath;

    /**
     * Tworzy menedżera plików dla wskazanego katalogu głównego.
     *
     * @param mainDirPath ścieżka do katalogu głównego
     */
    public FileManager(String mainDirPath) {
        this.mainDirPath = mainDirPath;
    }

    /**
     * Wypisuje informacje o podanym pliku lub katalogu: typ (plik/katalog), nazwę,
     * ścieżkę, datę ostatniej modyfikacji i rozmiar.
     *
     * @param file plik lub katalog do wyświetlenia informacji
     */
    public void printFileInformation(File file) {
        System.out.println((file.isDirectory() ? "[DIR] " : "[FILE] ")
                + file.getName() + "  " + file.getPath() + "  " + new Date(file.lastModified()) + "  " + file.length());
    }

    /**
     * Pobiera obiekt {@link File} o podanej nazwie w katalogu głównym.
     *
     * @param fileName nazwa pliku
     * @return obiekt File wskazujący na plik/katalog w katalogu głównym
     */
    public File getFile(String fileName) {
        String projectRootPath = System.getProperty("user.dir");
        return new File(projectRootPath + File.separator + this.mainDirPath + File.separator + fileName);
    }

    /**
     * Zwraca listę informacji o wszystkich plikach i podkatalogach w katalogu głównym
     * oraz rekurencyjnie o ich zawartości.
     *
     * @return lista obiektów {@link FileInformation} z informacjami o plikach,
     *         lub null, jeśli katalog jest pusty lub nie istnieje
     */
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

    /**
     * Rekurencyjnie dodaje plik i jego podpliki/podkatalogi do listy informacji.
     *
     * @param file plik lub katalog do dodania
     * @param list lista, do której dodawane są informacje
     */
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

    /**
     * Tworzy obiekt {@link FileInformation} z informacji o pliku lub katalogu.
     *
     * @param file plik/katalog źródłowy
     * @return obiekt z informacjami o pliku
     */
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

    /**
     * Zwraca tablicę plików i katalogów z katalogu głównego.
     *
     * @return tablica plików lub null jeśli katalog nie istnieje lub jest pusty
     */
    private File[] getListOfFiles() {
        File managedDirectory = new File(mainDirPath);
        return managedDirectory.listFiles();
    }

    /**
     * Tworzy katalog o podanej ścieżce (wraz z rodzicami, jeśli nie istnieją).
     *
     * @param path ścieżka do nowego katalogu
     * @return true jeśli katalog został utworzony, false jeśli już istniał
     */
    public boolean createDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return false;
    }

    /**
     * Usuwa katalog wraz z całą zawartością.
     *
     * @param path ścieżka do katalogu
     * @throws IOException w przypadku problemów z usuwaniem plików
     */
    public static void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    /**
     * Usuwa plik lub katalog (rekurencyjnie) o podanej ścieżce względem katalogu głównego.
     *
     * @param filePath ścieżka względna pliku/katalogu
     * @return true jeśli usunięto, false jeśli plik/katalog nie istniał
     */
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

    /**
     * Zapisuje plik z części przesłanych w mapie (klucz - numer części, wartość - {@link FilePart}).
     *
     * @param parts mapa części pliku do zapisania
     * @return true jeśli zapis powiódł się, false w przypadku błędów lub pustej mapy
     */
    public boolean saveFileFromParts(Map<Integer, FilePart> parts) {
        if (parts == null || parts.isEmpty()) return false;

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

    /**
     * Tworzy katalogi rodziców dla podanej pełnej ścieżki pliku, jeśli nie istnieją.
     *
     * @param fullPath pełna ścieżka pliku, dla którego mają być utworzone katalogi
     * @return true jeśli katalogi są już utworzone lub zostały utworzone poprawnie, false w przeciwnym razie
     */
    public boolean createDir(String fullPath) {
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

    /**
     * Aktualizuje daty modyfikacji plików na podstawie listy informacji {@link FileInformation}.
     *
     * @param list lista obiektów z informacjami o plikach, zawierająca daty do ustawienia
     */
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

    /**
     * Przenosi plik z jednej ścieżki do drugiej w katalogu głównym, tworząc katalogi docelowe jeśli nie istnieją.
     *
     * @param sourcePath ścieżka źródłowa względem katalogu głównego
     * @param targetPath ścieżka docelowa względem katalogu głównego
     * @return true jeśli przeniesienie się powiodło, false w przypadku błędów
     */
    public boolean moveFile(String sourcePath, String targetPath) {
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
