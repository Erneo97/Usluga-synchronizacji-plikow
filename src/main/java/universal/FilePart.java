package universal;

import java.io.Serializable;

/**
 * Klasa reprezentująca fragment pliku przesyłany podczas synchronizacji.
 * Implementuje {@link Serializable}, aby umożliwić przesyłanie obiektów przez sieć.
 */
public class FilePart implements Serializable {
    /**
     * Maksymalny rozmiar fragmentu pliku w bajtach (16 KB).
     */
    public static final int maxSizePart = 16 * 1024; // 16 KB

    /**
     * Rozmiar danych w tym fragmencie (w bajtach).
     */
    public int partSize;

    /**
     * Całkowity rozmiar pliku, którego fragment ten reprezentuje.
     */
    public int fullSizeOfFile;

    /**
     * Numer fragmentu (indeks fragmentu w sekwencji).
     */
    public int partNumber;

    /**
     * Ścieżka pliku, do którego należy ten fragment.
     */
    public String pathFile;

    /**
     * Tablica bajtów zawierająca dane fragmentu pliku.
     */
    public byte[] data;

    /**
     * Tworzy nowy fragment pliku na podstawie podanego bufora danych.
     *
     * @param buffer       Bufor z danymi pliku.
     * @param read         Ilość danych do odczytania z bufora.
     * @param fullFileSize Całkowity rozmiar pliku.
     * @param partNumber   Numer fragmentu w sekwencji.
     * @param pathFile     Ścieżka pliku, którego fragment dotyczy.
     */
    public FilePart(byte[] buffer, int read, int fullFileSize, int partNumber, String pathFile) {
        this.partSize = Math.min(read, maxSizePart);
        this.data = new byte[this.partSize];
        this.pathFile = pathFile;

        System.arraycopy(buffer, 0, this.data, 0, this.partSize);

        this.fullSizeOfFile = fullFileSize;
        this.partNumber = partNumber;
    }
}
