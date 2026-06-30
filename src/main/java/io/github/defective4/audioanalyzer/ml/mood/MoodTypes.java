package io.github.defective4.audioanalyzer.ml.mood;

import static io.github.defective4.audioanalyzer.expr.NumericExpressionType.*;

import java.util.Map;
import java.util.Set;

import io.github.defective4.audioanalyzer.app.cli.CLI;
import io.github.defective4.audioanalyzer.expr.NumericExpression;

public class MoodTypes {

    private static final Map<String, CompositeMood> MOODS = CLI.map("happy",
            new CompositeMood("mood_happy_discogs_effnet_1", new NumericExpression(MORE_THAN, 0.55f)),

            "aggressive",
            new CompositeMood("mood_aggressive_discogs_effnet_1", new NumericExpression(MORE_THAN, 0.55f)),

            "party", new CompositeMood("mood_party_discogs_effnet_1", new NumericExpression(MORE_THAN, 0.6f)),

            "relaxed", new CompositeMood("mood_relaxed_discogs_effnet_1", new NumericExpression(MORE_THAN, 0.45f)),

            "sad", new CompositeMood("mood_sad_discogs_effnet_1", new NumericExpression(MORE_THAN, 0.55f)),

            "dance", new CompositeMood("danceability_discogs_effnet_1", new NumericExpression(MORE_THAN, 0.45f)),

            "study",
            new CompositeMood(Map.of("mood_relaxed_discogs_effnet_1", new NumericExpression(MORE_THAN, 0.45),
                    "mood_aggressive_discogs_effnet_1", new NumericExpression(LESS_THAN, 0.2))),

            "workout",
            new CompositeMood(Map.of("danceability_discogs_effnet_1", new NumericExpression(MORE_THAN, 0.55)),
                    new NumericExpression(MORE_THAN, 120)),

            "sleep",
            new CompositeMood(Map.of("mood_relaxed_discogs_effnet_1", new NumericExpression(MORE_THAN, 0.5)),
                    new NumericExpression(LESS_THAN, 100)),

            "road_trip",
            new CompositeMood(Map.of("mood_happy_discogs_effnet_1", new NumericExpression(MORE_THAN, 0.4))),

            "cooking",
            new CompositeMood(Map.of("mood_happy_discogs_effnet_1", new NumericExpression(MORE_THAN, 0.35),
                    "mood_relaxed_discogs_effnet_1", new NumericExpression(MORE_THAN, 0.3),
                    "danceability_discogs_effnet_1", new NumericExpression(MORE_THAN, 0.3),
                    "mood_aggressive_discogs_effnet_1", new NumericExpression(LESS_THAN, 0.2))),

            "dining",
            new CompositeMood(Map.of("mood_relaxed_discogs_effnet_1", new NumericExpression(MORE_THAN, 0.4),
                    "mood_happy_discogs_effnet_1", new NumericExpression(MORE_THAN, 0.3),
                    "mood_aggressive_discogs_effnet_1", new NumericExpression(LESS_THAN, 0.15))),

            "background",
            new CompositeMood(Map.of("mood_relaxed_discogs_effnet_1", new NumericExpression(MORE_THAN, 0.35),
                    "mood_party_discogs_effnet_1", new NumericExpression(MORE_THAN, 0.3),
                    "mood_aggressive_discogs_effnet_1", new NumericExpression(LESS_THAN, 0.2))));

    private MoodTypes() {}

    public static CompositeMood getMood(String key) {
        return MOODS.get(key);
    }

    public static Set<String> getMoodNames() {
        return MOODS.keySet();
    }
}
