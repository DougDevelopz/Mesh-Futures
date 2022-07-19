package dev.meshfutures;

import dev.meshfutures.scheduler.MeshScheduler;

import java.util.concurrent.TimeUnit;

public class MeshSchedulerMain {

    private static int i = 10;

    public static void main(String[] args){
        MeshScheduler meshScheduler = new MeshScheduler(1);
        meshScheduler.buildTask("example-1", () -> {
                    i--;
                    System.out.println("Countdown: " + i);
                    if (i <= 0) {
                        //Cancel the task if i <= 0
                        meshScheduler.cancelScheduler("example-1");
                        System.out.println("Countdown scheduler has been cancelled");
                        System.exit(1);
                    }
                    //Repeating every second
                }).repeat(1, TimeUnit.SECONDS)
                //Schedule by passing the .schedule() method
                .schedule();

    }

}
