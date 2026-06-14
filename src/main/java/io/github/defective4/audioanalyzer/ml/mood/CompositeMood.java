package io.github.defective4.audioanalyzer.ml.mood;

import static io.github.defective4.audioanalyzer.expr.NumericExpressionType.*;

import java.util.Map;
import java.util.Map.Entry;

import io.github.defective4.audioanalyzer.expr.NumericExpression;
import io.github.defective4.audioanalyzer.ml.model.Track;

public record CompositeMood(Map<String, NumericExpression> scoreFilters, NumericExpression bpmFilter) {

    public CompositeMood(String model, NumericExpression expr, NumericExpression bpmExpr) {
        this(Map.of(model, expr), bpmExpr);
    }

    public CompositeMood(String model, NumericExpression expr) {
        this(Map.of(model, expr), new NumericExpression(MORE_THAN, 0));
    }

    public CompositeMood(Map<String, NumericExpression> scoreFilters) {
        this(scoreFilters, new NumericExpression(MORE_THAN, 0));
    }

    public boolean matches(Track track) {
        if (!bpmFilter.matches(track.bpm())) return false;
        for (Entry<String, NumericExpression> entry : scoreFilters.entrySet()) {
            if (track.scores().get(entry.getKey()) == null || !entry.getValue().matches(track.scores().get(entry.getKey()))) return false;
        }
        return true;
    }
}
