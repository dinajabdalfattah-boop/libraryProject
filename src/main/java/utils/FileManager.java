package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    /** يقرأ كل الأسطر من الملف كـ List<String> */
    public static List<String> readLines(String filePath) {
        try {
            File f = new File(filePath);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
                return new ArrayList<>();  // لو الملف جديد يرجّع list فاضية
            }
            return Files.readAllLines(Paths.get(filePath));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** يكتب list كامل في الملف (يمسح القديم ويكتب جديد) */
    public static void writeLines(String filePath, List<String> lines) {
        try {
            File f = new File(filePath);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }

            Files.write(Paths.get(filePath), lines);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** يضيف سطر واحد في نهاية الملف */
    public static void appendLine(String filePath, String line) {
        try {
            Files.write(
                    Paths.get(filePath),
                    (line + System.lineSeparator()).getBytes(),
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
