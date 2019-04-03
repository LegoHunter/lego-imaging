package com.vattima.lego.imaging.service;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface PhotoServiceResponse<T> extends Supplier<T>, Consumer<T> {
    boolean isError();
    Integer responseCode();
    String responseMessage();
}
