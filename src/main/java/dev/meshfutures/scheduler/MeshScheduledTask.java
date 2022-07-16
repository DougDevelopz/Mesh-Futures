package dev.meshfutures.scheduler;

public interface MeshScheduledTask {

    MeshTaskStatus status();

    void cancel();
}
