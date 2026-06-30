package io.github.defective4.audioanalyzer.app.cli.consumer;

import static io.github.defective4.audioanalyzer.app.option.ProgramOptions.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import io.github.defective4.audioanalyzer.app.App;

public final class AnalyzeConsumer implements CLIConsumer {
    @Override
    public boolean consume(CommandLine cli, App prog) throws Exception {
        boolean notAnalyzeAll = hasOption(cli, AN_ALL_OPTION);
        String filterArtist = getOptionValue(cli, FILTER_ARTIST_OPTION);
        String filterAlbumArtist = getOptionValue(cli, AN_FILTER_ALBUM_ARTIST_OPTION);
        prog.analyze(!notAnalyzeAll, filterArtist, filterAlbumArtist);
        return true;
    }

    @Override
    public String desc() {
        return "(Re)analyze tracks from a remote Subsonic instance";
    }

    @Override
    public Options ops() {
        return ANALYSIS_OPTIONS;
    }
}