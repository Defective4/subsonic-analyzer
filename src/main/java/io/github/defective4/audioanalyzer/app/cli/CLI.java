package io.github.defective4.audioanalyzer.app.cli;

import static io.github.defective4.audioanalyzer.app.option.ProgramOptions.*;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import io.github.defective4.audioanalyzer.app.App;
import io.github.defective4.audioanalyzer.app.cli.consumer.AnalyzeConsumer;
import io.github.defective4.audioanalyzer.app.cli.consumer.CLIConsumer;
import io.github.defective4.audioanalyzer.app.cli.consumer.EnvironmentConsumer;
import io.github.defective4.audioanalyzer.app.cli.consumer.ModelsConsumer;
import io.github.defective4.audioanalyzer.app.cli.consumer.PlsGenerateConsumer;
import io.github.defective4.audioanalyzer.app.cli.consumer.PlsToolsConsumer;
import io.github.defective4.audioanalyzer.app.cli.consumer.StatsConsumer;

public class CLI {

    private static final Map<String, CLIConsumer> COMMANDS = map("analyze", new AnalyzeConsumer(), "generate-playlist",
            new PlsGenerateConsumer(), "stats", new StatsConsumer(), "models", new ModelsConsumer(), "playlist-tools",
            new PlsToolsConsumer(), "env", new EnvironmentConsumer());

    public static void main(String[] args) throws Exception {
        String envCmd = System.getenv(App.COMMAND_ENV);
        String cmd = envCmd != null || args.length > 0 ? envCmd != null ? envCmd : args[0] : null;
        if (cmd == null || !COMMANDS.containsKey(cmd)) {
            System.err.println(
                    cmd != null && !COMMANDS.containsKey(cmd) ? "Invalid command " + cmd : "Usage: <command> [args]");
            System.err.println("Valid commands are:");
            COMMANDS.forEach((k, v) -> { System.err.println(" - %s: %s".formatted(k, v.desc())); });
            System.exit(1);
            return;
        }
        Options options = COMMANDS.get(cmd).ops();
        CommandLine cli;

        try {
            cli = DefaultParser.builder().build().parse(options,
                    envCmd == null ? Arrays.copyOfRange(args, 1, args.length) : args);
            if (!cli.hasOption(HELP_OPTION)) {
                String db = getOptionValue(cli, DB_LOCATION_OPTION, DEFAULT_DB);
                String user = getOptionValue(cli, USER_OPTION);
                String password = getOptionValue(cli, PASSWORD_OPTION);
                String subsonicURL = getOptionValue(cli, SUBSONIC_URL_OPTION);
                if (subsonicURL != null && !subsonicURL.endsWith("/")) subsonicURL = subsonicURL + "/";
                String essentiaURL = getOptionValue(cli, AN_TENSORFLOW_OPTION, DEFAULT_ESSENTIA);
                if (!essentiaURL.endsWith("/")) essentiaURL = essentiaURL + "/";

                App prog = new App(db, user, password == null ? new char[0] : password.toCharArray(), subsonicURL,
                        essentiaURL);
                if (COMMANDS.get(cmd).consume(cli, prog)) return;
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
        }

        HelpFormatter hf = new HelpFormatter();
        hf.setWidth((int) (HelpFormatter.DEFAULT_WIDTH * 1.5));
        hf.printHelp("%s %s [options]".formatted(
                Path.of(CLI.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getFileName(),
                args[0]), options);
        System.exit(2);
    }

    public static <K, V> Map<K, V> map(Object... kv) {
        Map<K, V> map = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) map.put((K) kv[i], (V) kv[i + 1]);
        return Collections.unmodifiableMap(map);
    }
}
