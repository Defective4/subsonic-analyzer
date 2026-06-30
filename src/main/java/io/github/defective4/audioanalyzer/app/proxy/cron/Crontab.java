package io.github.defective4.audioanalyzer.app.proxy.cron;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class Crontab {

    private final List<CronTask> tasks = new CopyOnWriteArrayList<>();
    private final Timer timer = new Timer(true);

    public Crontab() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.MINUTE, 1);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                LocalDateTime time = LocalDateTime.now();
                tasks.stream().filter(task -> task.getAt().matches(time)).forEach(task -> task.getTask().run());
            }
        }, calendar.getTime(), TimeUnit.MINUTES.toMillis(1));
    }

    public void addTask(CronTask task) {
        tasks.add(task);
    }
}
