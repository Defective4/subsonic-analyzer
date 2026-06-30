package io.github.defective4.audioanalyzer.app.proxy.cron;

public class CronTask {
    private final CronExpression at;
    private final Runnable task;

    public CronTask(CronExpression at, Runnable task) {
        this.at = at;
        this.task = task;
    }

    public CronExpression getAt() {
        return at;
    }

    public Runnable getTask() {
        return task;
    }

    @Override
    public String toString() {
        return "CronTask [at=" + at + "]";
    }

}
