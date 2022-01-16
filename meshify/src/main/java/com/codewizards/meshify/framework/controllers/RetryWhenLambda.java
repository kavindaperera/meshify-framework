package com.codewizards.meshify.framework.controllers;


import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class RetryWhenLambda implements Function<Flowable<? extends Throwable>, Flowable<?>> {

    private final int retries;

    private final int delay;

    private int retryCount;

    public RetryWhenLambda(int retries, int delay) {
        this.retries = retries;
        this.delay = delay;
        this.retryCount = 0;
    }

    @Override
    public Flowable<?> apply(@NonNull Flowable<? extends Throwable> flowable) throws Exception {
        return flowable.flatMap(throwable -> {
            if (++this.retryCount < this.retries) {
                return Flowable.timer((long)this.delay, (TimeUnit) TimeUnit.MILLISECONDS);
            }
            return Flowable.error((Throwable)throwable);
        });
    }

}
