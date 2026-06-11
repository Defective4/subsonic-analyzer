package io.github.defective4.audioanalyzer.exception;

import java.util.List;

public class MissingModelsException extends Exception {
    private final List<String> missingModels;

    public MissingModelsException(String message, List<String> missingModels) {
        super(message);
        this.missingModels = List.copyOf(missingModels);
    }

    public List<String> getMissingModels() {
        return missingModels;
    }

}
