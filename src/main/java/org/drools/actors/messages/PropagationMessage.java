package org.drools.actors.messages;

import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.reteoo.ObjectTypeConf;
import org.drools.core.spi.PropagationContext;

import java.util.concurrent.atomic.AtomicLong;

public class PropagationMessage {

    public static final AtomicLong ID_GENERATOR = new AtomicLong();

    public enum ActionType { INSERT, UPDATE, DELETE }

    public final long messageId;
    public final ActionType type;
    public final InternalFactHandle handle;
    public final PropagationContext context;
    public final ObjectTypeConf objectTypeConf;
    public final InternalWorkingMemory workingMemory;

    public PropagationMessage(ActionType type, InternalFactHandle handle, PropagationContext context, ObjectTypeConf objectTypeConf, InternalWorkingMemory workingMemory) {
        this.messageId = ID_GENERATOR.incrementAndGet();
        this.type = type;
        this.handle = handle;
        this.context = context;
        this.objectTypeConf = objectTypeConf;
        this.workingMemory = workingMemory;
    }
}
