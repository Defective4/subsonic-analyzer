package io.github.defective4.audioanalyzer.app.cli.consumer;

import static io.github.defective4.audioanalyzer.app.option.ProgramOptions.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import io.github.defective4.audioanalyzer.app.App;
import io.github.defective4.audioanalyzer.expr.NumericExpression;

public final class PlsGenerateConsumer implements CLIConsumer {
    @Override
    public boolean consume(CommandLine cli, App prog) throws Exception {
        String song = getOptionValue(cli, GEN_SIMILAR_SONG_OPTION, null);
        String mood = getOptionValue(cli, GEN_MOOD_FILTER_OPTION, null);
        String instrument = getOptionValue(cli, GEN_INSTRUMENT_FILTER_OPTION, null);
        String genre = getOptionValue(cli, GEN_GENRE_FILTER_OPTION, null);
        String playlistName = getOptionValue(cli, GEN_NAME_OPTION);
        String replacePlaylist = getOptionValue(cli, GEN_REPLACE_OPTION, null);
        int limit = getParsedOptionValue(cli, GEN_LIMIT_OPTION, DEFAULT_LIMIT);
        boolean newPublic = hasOption(cli, GEN_PUBLIC_OPTION);
        NumericExpression bpmExpr = getParsedOptionValue(cli, GEN_BPM_FILTER_OPTION, null);
        NumericExpression vocalExpr = getParsedOptionValue(cli, GEN_VOCALITY_FILTER_OPTION, null);

        if (replacePlaylist == null && playlistName == null) {
            System.err.println("Missing playlist name");
            return false;
        }
        boolean similarGenre = hasOption(cli, GEN_SAME_GENRE_OPTION);
        boolean similarMood = hasOption(cli, GEN_SAME_MOOD_OPTION);
        boolean similarInstrument = hasOption(cli, GEN_SAME_INSTRUMENT_OPTION);
        boolean tempo = hasOption(cli, GEN_SIMILAR_INCLUDE_BPM);
        boolean sameArtist = hasOption(cli, GEN_SAME_ARTIST_OPTION);
        String filterPlaylist = getOptionValue(cli, FILTER_ARTIST_OPTION);
        boolean shuffleSimilar = hasOption(cli, GEN_SHUFFLE_SIMILAR_OPTION);
        boolean printJSON = hasOption(cli, GEN_PRINT_JSON_OPTION);
        String compositeMoodName = getOptionValue(cli, GEN_COMPOSITE_MOOD);

        prog.groupTracks(song, mood, instrument, genre, playlistName, replacePlaylist, limit, newPublic, similarGenre,
                similarMood, similarInstrument, tempo, bpmExpr, vocalExpr, sameArtist, filterPlaylist, shuffleSimilar,
                printJSON, compositeMoodName);
        return true;
    }

    @Override
    public String desc() {
        return "Group tracks into playlists by mood, similarity, etc.";
    }

    @Override
    public Options ops() {
        return GENERATOR_OPTIONS;
    }
}