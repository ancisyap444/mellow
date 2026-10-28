package util;

import interfaces.Saveable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileHandler {
    private final String filePath;

    public FileHandler(String filePath) {
        this.filePath = (filePath == null || filePath.trim().isEmpty()) ? GameConfig.SAVE_FILE_PATH : filePath.trim();
    }

    public FileHandler() {
        this(GameConfig.SAVE_FILE_PATH);
    }

    public boolean save(Saveable saveable) {
        if (saveable == null) {
            return false;
        }
        File file = new File(filePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write(saveable.serialize());
            writer.flush();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public boolean load(Saveable saveable) {
        if (saveable == null) {
            return false;
        }
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            saveable.deserialize(sb.toString());
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
