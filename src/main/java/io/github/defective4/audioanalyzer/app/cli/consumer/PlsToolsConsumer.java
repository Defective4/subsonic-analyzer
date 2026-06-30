package io.github.defective4.audioanalyzer.app.cli.consumer;

import static io.github.defective4.audioanalyzer.app.option.ProgramOptions.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import io.github.defective4.audioanalyzer.app.App;

public final class PlsToolsConsumer implements CLIConsumer {
    @Override
    public boolean consume(CommandLine cli, App prog) throws Exception {
        String createNamed = getOptionValue(cli, CREATE_PLAYLIST_OPTION);
        String remove = getOptionValue(cli, DELETE_PLAYLIST_OPTION);
        if (createNamed == null && remove == null) {
            System.err.println("Either --%s or --%s is required".formatted(CREATE_PLAYLIST_OPTION.getLongOpt(),
                    DELETE_PLAYLIST_OPTION.getLongOpt()));
            return false;
        }
        prog.managePlaylist(createNamed, remove);
        return true;
    }

    @Override
    public String desc() {
        return "A set of tools to manage your playlist";
    }

    @Override
    public Options ops() {
        return PLAYLIST_OPTIONS;
    }
}