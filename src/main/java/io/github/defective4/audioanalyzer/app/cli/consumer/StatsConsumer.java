package io.github.defective4.audioanalyzer.app.cli.consumer;

import static io.github.defective4.audioanalyzer.app.option.ProgramOptions.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import io.github.defective4.audioanalyzer.app.App;
import io.github.defective4.audioanalyzer.format.PrintFormat;

public final class StatsConsumer implements CLIConsumer {
    @Override
    public boolean consume(CommandLine cli, App prog) throws Exception {
        prog.printSongs(getParsedOptionValue(cli, ST_PRINT_FORMAT_OPTION, PrintFormat.JSON),
                getOptionValue(cli, ST_SONG_OPTION), getOptionValue(cli, ST_OUTPUT_OPTION, "-"));
        return true;
    }

    @Override
    public String desc() {
        return "Print statistics about songs in the database";
    }

    @Override
    public Options ops() {
        return STATS_OPTIONS;
    }
}