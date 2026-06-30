package io.github.defective4.audioanalyzer.app.cli.consumer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import io.github.defective4.audioanalyzer.app.App;

public interface CLIConsumer {
    boolean consume(CommandLine cli, App prog) throws Exception;

    String desc();

    Options ops();
}