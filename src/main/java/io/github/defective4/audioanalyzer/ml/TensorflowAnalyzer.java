package io.github.defective4.audioanalyzer.ml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TensorflowAnalyzer implements AutoCloseable {

    private static final String ANALYZER_PY = "/analyzer.py";
    private static final String BASH = "/bin/bash";
    private final Path pyFile;
    private final String venv;

    public TensorflowAnalyzer(String venv) throws IOException {
        this.venv = venv;
        pyFile = Files.createTempFile("aa", ".py");
        prepare();
    }

    public JsonObject analyze(File file) throws IOException, InterruptedException {
        Process proc = createProcess(file.getPath());
        try (Reader reader = proc.errorReader()) {
            while (true) {
                if (reader.read() < 0) break;
            }
        }
        if (proc.waitFor() != 0) throw new IOException("Process exited with failure");
        try (Reader reader = proc.inputReader()) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } finally {
            proc.destroy();
        }
    }

    @Override
    public void close() {
        pyFile.toFile().delete();
    }

    private Process createProcess(String file) throws IOException {
        String[] args = { BASH, "-c",
                String.format("source \"%s/bin/activate\"; python3 \"%s\" \"%s\"", venv, pyFile, file) };
        Process proc = new ProcessBuilder(args).start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> proc.destroyForcibly()));
        return proc;
    }

    private void prepare() throws IOException {
        try (InputStream in = getClass().getResourceAsStream(ANALYZER_PY)) {
            Files.copy(in, pyFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
