package org.pickaid.pibrary.api.service;

import java.util.Objects;

public abstract class PiLivingEntityService {
    private final PiLivingServiceContext context;

    protected PiLivingEntityService(PiLivingServiceContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public final PiLivingServiceContext context() {
        return context;
    }
}
