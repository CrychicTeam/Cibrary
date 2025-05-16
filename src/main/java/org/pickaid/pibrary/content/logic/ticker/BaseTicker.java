package org.pickaid.pibrary.content.logic.ticker;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

public final class BaseTicker {
    private static final Map<BooleanSupplier, TickerInfo> TASKS = new ConcurrentHashMap<>();

    public static void schedule(Runnable runnable, int interval, boolean isClient) {
        scheduleTask(() -> {
            runnable.run();
            return true;
        }, interval, isClient);
    }

    public static void scheduleTask(BooleanSupplier task, int interval, boolean isClient) {
        TASKS.put(task, new TickerInfo(interval, 0, isClient));
    }

    public static void cancelTask(BooleanSupplier task) {
        TASKS.remove(task);
    }

    public static void processTasks(boolean isClient) {
        if (TASKS.isEmpty()) return;
        Iterator<Map.Entry<BooleanSupplier, TickerInfo>> it = TASKS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BooleanSupplier, TickerInfo> entry = it.next();
            TickerInfo info = entry.getValue();

            if (info.isClient() == isClient) {
                info.incrementCounter();
                if (info.isReady()) {
                    info.resetCounter();
                    try {
                        boolean finished = entry.getKey().getAsBoolean();
                        if (finished) {
                            it.remove();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        it.remove();
                    }
                }
            }
        }
    }

    public static class TickerInfo {
        private final int interval;
        private int counter;
        private final boolean isClient;

        public TickerInfo(int interval, int counter, boolean isClient) {
            this.interval = interval;
            this.counter = counter;
            this.isClient = isClient;
        }

        public boolean isClient() {
            return isClient;
        }

        public void incrementCounter() {
            counter++;
        }

        public boolean isReady() {
            return counter >= interval;
        }

        public void resetCounter() {
            counter = 0;
        }
    }
}