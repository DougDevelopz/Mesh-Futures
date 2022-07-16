package dev.meshfutures;

import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AdherentFuture<V> implements Future<V> {

    public static <T> AdherentFuture<T> create(SuccessfulListener<T> successful, ThrowableListener failure, ExecutorService service) {
        return new AdherentFuture<>(successful, failure, service);
    }

    private final SuccessfulListener<V> successfulListener;
    private final ThrowableListener throwableListener;
    private final ExecutorService listenerExecutor;
    private final Lock lock;
    private V result;
    private Throwable exception;


    private AdherentFuture(SuccessfulListener<V> successfulListener, ThrowableListener throwableListener, ExecutorService listenerExecutor) {
        this.successfulListener = successfulListener;
        this.throwableListener = throwableListener;
        this.listenerExecutor = listenerExecutor;
        this.lock = new ReentrantLock();
    }


    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        lock.lock();
        try {
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        lock.lock();
        try {
            return this.result != null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V get() throws InterruptedException {
        lock.lock();
        try {
            return result;
        } finally {
            lock.unlock();
        }
    }

    public void set(V result) {
        lock.lock();
        try {
            if (exception != null) {
                return;
            }
            this.result = result;
            System.out.println("BEING SET ON:" + Thread.currentThread().getId());
            if (this.listenerExecutor == null) {
                this.successfulListener.onSuccess(result);
            } else {
                this.listenerExecutor.submit(() -> {
                    System.out.println("BEING LISTENED ON:" + Thread.currentThread().getId());
                    successfulListener.onSuccess(result);
                });
            }
        } finally {
            lock.unlock();
        }
    }

    public void setError(Throwable throwable) {
        lock.lock();
        try {
            if (throwable != null) {
                return;
            }
            this.exception = throwable;
            this.throwableListener.onFail(exception);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
        return get();
    }

    public <T> CompletableFuture<T> toCompletable(AdherentFuture<T> future) {
        CompletableFuture<T> completableFuture = new CompletableFuture<T>();
        T result = null;
        try {
            result = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        completableFuture.complete(result);
        return completableFuture;
    }


    @FunctionalInterface
    public interface SuccessfulListener<T> {

        void onSuccess(T result);
    }

    @FunctionalInterface
    public interface ThrowableListener {

        void onFail(Throwable throwable);
    }
}
