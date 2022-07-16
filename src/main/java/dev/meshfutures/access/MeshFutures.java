package dev.meshfutures.access;

import dev.meshfutures.AdherentFuture;
import dev.meshfutures.Promise;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MeshFutures {

    /**
     * Allows use to create a new Promise
     *
     * @param <T> is the resul type
     * @return a new Promise
     */
    public <T> Promise<T> createNew() {
        return new Promise<>();
    }

    /**
     * To submit a callable through the use of the Adherent future
     *
     * @param call            represents the callable
     * @param successful      if successful callable will be called
     * @param throwable       thrown if the future is a failure
     * @param executorService executor type to submit the callable
     * @param <T>             the result type
     * @return a callable to submit
     */
    public <T> AdherentFuture<T> submit(Callable<T> call, AdherentFuture.SuccessfulListener<T> successful, AdherentFuture.ThrowableListener throwable
            , ExecutorService executorService) {

        AdherentFuture<T> adherableFuture = AdherentFuture.create(successful, throwable, executorService);
        executorService.submit(() -> {
            try {
                successful.onSuccess(call.call());
            } catch (Exception e) {
                throwable.onFail(e);
            }
        });
        return adherableFuture;
    }

    /**
     * To submit a callable through the use of the Adherent future
     *
     * @param call       represents the callable
     * @param successful if successful callable will be called
     * @param throwable  thrown if the future is a failure
     * @param <T>        the result type
     * @return a callable to submit through cached threads
     */
    public <T> AdherentFuture<T> submit(Callable<T> call, AdherentFuture.SuccessfulListener<T> successful, AdherentFuture.ThrowableListener throwable) {
        return submit(call, successful, throwable, Executors.newCachedThreadPool());
    }
}
