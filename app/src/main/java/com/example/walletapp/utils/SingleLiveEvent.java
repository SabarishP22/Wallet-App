package com.example.walletapp.utils;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicBoolean;

public class SingleLiveEvent<T> extends MutableLiveData<T> {
    private final AtomicBoolean hasBeenHandled = new AtomicBoolean(false);

    @Override
    public void observe(LifecycleOwner owner, Observer<? super T> observer) {
        super.observe(owner, t -> {
            if (hasBeenHandled.compareAndSet(false, true)) {
                observer.onChanged(t);
            }
        });
    }

    @Override
    public void setValue(T value) {
        hasBeenHandled.set(false);
        super.setValue(value);
    }
}
