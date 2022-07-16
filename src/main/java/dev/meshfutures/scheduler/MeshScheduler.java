package dev.meshfutures.scheduler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class MeshScheduler {

    private final ExecutorService taskService;

    private final ScheduledExecutorService timerExecutionService;

    private final Map<String, Collection<MeshScheduledTask>> scheduledTasks = new ConcurrentHashMap<>();

    public MeshScheduler(int cores) {
        this.taskService = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setName("Jamun Task Service");
            thread.setDaemon(true);
            return thread;
        });
        this.timerExecutionService = Executors.newScheduledThreadPool(cores);
    }

    public MeshTaskBuilder buildTask(String name, Runnable runnable) {
        return new MeshTaskBuilderImpl(name, runnable);
    }

    public void cancelScheduler(String name) {
        for (MeshScheduledTask tasks : scheduledTasks.get(name)) {
            tasks.cancel();
        }
    }

    /**
     * Shuts down the Jamun scheduler.
     *
     * @return {@code true} if all tasks finished, {@code false} otherwise
     * @throws InterruptedException if the current thread was interrupted
     */
    public boolean shutdown() throws InterruptedException {
        Collection<MeshScheduledTask> terminating = null;
        synchronized (scheduledTasks) {
            for (Collection<MeshScheduledTask> tasks : scheduledTasks.values()) {
                terminating = List.copyOf(tasks);
            }
        }
        assert terminating != null;
        for (MeshScheduledTask task : terminating) {
            task.cancel();
        }
        timerExecutionService.shutdown();
        taskService.shutdown();
        return taskService.awaitTermination(1, TimeUnit.SECONDS);
    }

    private class MeshTaskBuilderImpl implements MeshTaskBuilder {

        private final String name;
        private final Runnable runnable;
        private long delay; // ms
        private long repeat; // ms

        private MeshTaskBuilderImpl(String name, Runnable runnable) {
            this.name = name;
            this.runnable = runnable;
        }

        @Override
        public MeshTaskBuilder delay(long time, TimeUnit unit) {
            this.delay = unit.toMillis(time);
            return this;
        }

        @Override
        public MeshTaskBuilder repeat(long time, TimeUnit unit) {
            this.repeat = unit.toMillis(time);
            return this;
        }

        @Override
        public MeshTaskBuilder clearDelay() {
            this.delay = 0;
            return this;
        }

        @Override
        public MeshTaskBuilder clearRepeat() {
            this.repeat = 0;
            return this;
        }

        @Override
        public MeshTask schedule() {
            MeshTask meshTask = new MeshTask(name, runnable, delay, repeat);
            scheduledTasks.put(name, Collections.singleton(meshTask));
            meshTask.schedule();
            return meshTask;
        }
    }

    private class MeshTask implements Runnable, MeshScheduledTask {

        private final String name;
        private final Runnable runnable;
        private final long delay;
        private final long repeat;
        private ScheduledFuture<?> future;
        private volatile Thread currentTaskThread;

        private MeshTask(String name, Runnable runnable, long delay, long repeat) {
            this.name = name;
            this.runnable = runnable;
            this.delay = delay;
            this.repeat = repeat;
        }

        void schedule() {
            if (repeat == 0) {
                this.future = timerExecutionService.schedule(runnable, delay, TimeUnit.MILLISECONDS);
            }
            this.future = timerExecutionService
                    .scheduleAtFixedRate(this, delay, repeat, TimeUnit.MILLISECONDS);
        }


        @Override
        public MeshTaskStatus status() {
            if (future == null) {
                System.out.println("null");
                return MeshTaskStatus.SCHEDULED;
            }

            if (future.isCancelled()) {
                return MeshTaskStatus.CANCELLED;
            }

            if (future.isDone()) {
                return MeshTaskStatus.FINISHED;
            }

            return MeshTaskStatus.SCHEDULED;
        }

        @Override
        public void cancel() {
            if (future != null) {
                future.cancel(false);

                Thread cur = currentTaskThread;
                if (cur != null) {
                    cur.interrupt();
                }

                onFinish(name);
            }
        }

        @Override
        public void run() {
            taskService.execute(() -> {
                currentTaskThread = Thread.currentThread();
                try {
                    runnable.run();
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    if (repeat == 0) {
                        onFinish(name);
                    }
                    currentTaskThread = null;
                }
            });
        }

        private void onFinish(String name) {
            scheduledTasks.remove(name);
        }
    }
}


