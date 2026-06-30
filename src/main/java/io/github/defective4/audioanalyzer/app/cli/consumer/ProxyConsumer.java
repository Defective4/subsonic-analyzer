package io.github.defective4.audioanalyzer.app.cli.consumer;

import static io.github.defective4.audioanalyzer.app.option.ProgramOptions.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.yaml.snakeyaml.Yaml;

import io.github.defective4.audioanalyzer.app.App;
import io.github.defective4.audioanalyzer.config.ProxyConfiguration;
import io.github.defective4.audioanalyzer.config.yaml.YamlMapper;

public class ProxyConsumer implements CLIConsumer {

    @Override
    public boolean consume(CommandLine cli, App prog) throws Exception {
        String target = cli.getOptionValue(SUBSONIC_URL_OPTION);
        while (target.endsWith("/")) target = target.substring(0, target.length() - 1);
        String host = cli.getOptionValue(PROXY_HOST);
        int port = cli.getParsedOptionValue(PROXY_PORT);
        File cfgFile = new File(cli.getOptionValue(PROXY_CONFIG, DEFAULT_PROXY_CONFIG));

        if (!cfgFile.exists()) {
            ProxyConfiguration def = new ProxyConfiguration();
            try (Writer writer = new FileWriter(cfgFile)) {
                writer.write(new Yaml().dumpAsMap(YamlMapper.dump(def)));
            }
        }

        ProxyConfiguration config;
        try (Reader reader = new FileReader(cfgFile)){
            config = YamlMapper.load(new Yaml().load(reader), ProxyConfiguration.class);
        }


        return true;
    }

    @Override
    public String desc() {
        return "Start a proxy service between a Subsonic server and client(s)";
    }

    @Override
    public Options ops() {
        return PROXY_OPTIONS;
    }

}
