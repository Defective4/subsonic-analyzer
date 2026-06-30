package io.github.defective4.audioanalyzer.app.cli.consumer;

import static io.github.defective4.audioanalyzer.app.option.ProgramOptions.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import io.github.defective4.audioanalyzer.app.App;

public final class ModelsConsumer implements CLIConsumer {
    @Override
    public boolean consume(CommandLine cli, App prog) throws Exception {
        boolean update = hasOption(cli, MODELS_UPDATE);
        String baseURL = getParsedOptionValue(cli, MODELS_BASE_URL, DEFAULT_MODELS_BASE_URL).toString();
        prog.checkModels(update, baseURL);
        return true;
    }

    @Override
    public String desc() {
        return "Check and download ml models required for program operation";
    }

    @Override
    public Options ops() {
        return MODELS_OPTIONS;
    }
}