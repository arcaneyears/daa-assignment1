package com.taubay.daa.bench;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Minimal CSV sink — no dependencies, deterministic formatting, US locale for decimals. */
public final class CsvWriter implements AutoCloseable {

    private final BufferedWriter writer;

    public CsvWriter(Path path, List<String> header) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        this.writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
        writeRow(header);
    }

    public void writeRow(List<String> cells) throws IOException {
        writer.write(String.join(",", cells));
        writer.newLine();
    }

    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
