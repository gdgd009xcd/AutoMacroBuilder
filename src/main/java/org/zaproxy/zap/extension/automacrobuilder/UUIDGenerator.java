package org.zaproxy.zap.extension.automacrobuilder;

import java.util.UUID;

public class UUIDGenerator {
    synchronized public static UUID getUUID(){
        return UUID.randomUUID();
    }
}
