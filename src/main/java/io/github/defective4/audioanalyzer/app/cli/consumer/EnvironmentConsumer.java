package io.github.defective4.audioanalyzer.app.cli.consumer;

import static io.github.defective4.audioanalyzer.app.option.ProgramOptions.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import io.github.defective4.audioanalyzer.app.App;

public final class EnvironmentConsumer implements CLIConsumer {
    @Override
    public boolean consume(CommandLine cli, App prog) throws Exception {
        prog.printEnvironment(hasOption(cli, ENV_UNCENSOR_OPTION));
        return true;
    }

    @Override
    public String desc() {
        return "Print information about available environment variables";
    }

    @Override
    public Options ops() {
        return ENV_OPTIONS;
    }
}