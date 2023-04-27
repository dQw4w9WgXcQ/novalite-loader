package io.novalite;

import lombok.SneakyThrows;
import net.runelite.client.callback.ClientThread;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class ClientThreadUtil {
    @SneakyThrows
    public static <T> T invoke(ClientThread clientThread, Callable<T> call) {
        FutureTask<T> future = new FutureTask<>(call);
        clientThread.invoke(future);
        return future.get(500, TimeUnit.MILLISECONDS);
    }

    public static void invoke(ClientThread clientThread, Runnable runnable) {
        invoke(
                clientThread,
                () -> {
                    runnable.run();
                    return null;
                }
        );
    }
}
