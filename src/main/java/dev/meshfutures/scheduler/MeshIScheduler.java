package dev.meshfutures.scheduler;

public interface MeshIScheduler {

    MeshTaskBuilder buildTask(String name, Runnable runnable);
}
