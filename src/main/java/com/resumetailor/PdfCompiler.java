package com.resumetailor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class PdfCompiler {

    public Path compile(Path texFile)
            throws IOException, InterruptedException {

        Path source = texFile.toAbsolutePath().normalize();

        if (!Files.isRegularFile(source)) {
            throw new IOException("LaTeX file not found: " + source);
        }

        Path folder = source.getParent();
        String filename = source.getFileName().toString();

        if (!filename.endsWith(".tex")) {
            throw new IOException("Expected a .tex file.");
        }

        String basename = filename.substring(0, filename.length() - 4);
        Path pdfFile = folder.resolve(basename + ".pdf");
        Path consoleLog = folder.resolve(basename + "-compiler.txt");

        // Prevent an old PDF from being mistaken for a new result.
        Files.deleteIfExists(pdfFile);

        ProcessBuilder builder = new ProcessBuilder(
                "pdflatex",
                "-interaction=nonstopmode",
                "-halt-on-error",
                "-no-shell-escape",
                source.getFileName().toString()
        );

        builder.directory(folder.toFile());
        builder.redirectErrorStream(true);
        builder.redirectOutput(consoleLog.toFile());

        Process process = builder.start();

        try {
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IOException(
                        "PDF compilation timed out. Check: " + consoleLog);
            }
        } catch (InterruptedException exception) {
            process.destroyForcibly();
            throw exception;
        }

        if (process.exitValue() != 0
                || !Files.isRegularFile(pdfFile)
                || Files.size(pdfFile) == 0) {

            throw new IOException(
                    "PDF compilation failed. Check: " + consoleLog);
        }

        return pdfFile;
    }
}