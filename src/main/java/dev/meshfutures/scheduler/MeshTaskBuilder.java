package dev.meshfutures.scheduler;

import java.util.concurrent.TimeUnit;

public interface MeshTaskBuilder {

    MeshTaskBuilder delay( long time, TimeUnit timeUnit);

    MeshTaskBuilder repeat(long time, TimeUnit timeUnit);

    MeshTaskBuilder clearDelay();

    MeshTaskBuilder clearRepeat();

    MeshScheduledTask schedule();
}
