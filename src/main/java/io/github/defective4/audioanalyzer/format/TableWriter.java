package io.github.defective4.audioanalyzer.format;

import java.io.PrintWriter;
import java.io.Writer;

public abstract class TableWriter extends PrintWriter {
    protected final String[] columns;

    public TableWriter(Writer writer, String[] columns) {
        super(writer);
        this.columns = columns;
    }

    public abstract void writeLines(String[][] lines);
}
