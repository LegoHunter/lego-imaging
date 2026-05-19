package io.legohunter.imaging.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimplePhotoServiceRequest<T> implements PhotoServiceRequest<T> {
    private final T value;

    @Override
    public T get() {
        return value;
    }
}
