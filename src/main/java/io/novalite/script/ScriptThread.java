package io.novalite.script;

import io.novalite.commons.IBotScript;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class ScriptThread extends Thread {
    @Getter
    private volatile IBotScript script;

    public ScriptThread() {
        super("script");
    }

    @SneakyThrows
    @Override
    public void run() {
        while (!isInterrupted()) {
            synchronized (this) {
                if (script == null) {
                    log.debug("waiting for script");

                    wait();
                }
            }

            try {
                script.run();
            } catch (Exception e) {
                log.warn("exception in script run", e);
            } catch (VirtualMachineError vme) {//actual bad error
                throw vme;
            } catch (Error e) {//just catch all errors bc kotlin t0do error etc.
                log.warn("error in script run", e);
            }

            script = null;
        }
    }

    public boolean offer(IBotScript script) {
        synchronized (this) {
            if (!isAlive()) {
                throw new IllegalStateException("script thread dead");
            }

            if (this.script != null) {
                return false;
            }

            this.script = script;
            notify();
            return true;
        }
    }
}
