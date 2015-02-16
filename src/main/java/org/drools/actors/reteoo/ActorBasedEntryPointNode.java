package org.drools.actors.reteoo;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import org.drools.actors.messages.PropagationMessage;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.common.RuleBasePartitionId;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.ObjectSource;
import org.drools.core.reteoo.ObjectTypeConf;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.core.rule.EntryPointId;
import org.drools.core.spi.PropagationContext;

import java.util.HashMap;
import java.util.Map;

import static org.drools.actors.util.ActorFactory.actorOf;

public class ActorBasedEntryPointNode extends EntryPointNode {

    private final ActorRef entryPointActor;

    public ActorBasedEntryPointNode(int id,
                                    ObjectSource objectSource,
                                    BuildContext context) {
        this(id,
             context.getPartitionId(),
             context.getKnowledgeBase().getConfiguration().isMultithreadEvaluation(),
             objectSource,
             context.getCurrentEntryPoint()); // irrelevant for this node, since it overrides sink management
    }

    public ActorBasedEntryPointNode(int id,
                                    RuleBasePartitionId partitionId,
                                    boolean partitionsEnabled,
                                    ObjectSource objectSource,
                                    EntryPointId entryPoint) {
        super(id, partitionId, partitionsEnabled, objectSource, entryPoint);
        this.entryPointActor = actorOf(EntryPointNodeActor.class);
    }

    @Override
    public void assertObject(InternalFactHandle handle,
                             PropagationContext context,
                             ObjectTypeConf objectTypeConf,
                             InternalWorkingMemory workingMemory) {
        //super.assertObject(handle, context, objectTypeConf, workingMemory);

        PropagationMessage msg = new PropagationMessage(PropagationMessage.ActionType.INSERT, handle, context, objectTypeConf, workingMemory);
        entryPointActor.tell(msg, ActorRef.noSender());
    }

    public static class EntryPointNodeActor extends UntypedActor {

        private Map<Integer, ActorRef[]> otnMap = new HashMap<Integer, ActorRef[]>();

        @Override
        public void onReceive(Object message) throws Exception {
            PropagationMessage msg = (PropagationMessage) message;

            switch (msg.type) {
                case INSERT:
                    doAssert(msg);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }

        private void doAssert(PropagationMessage msg) {
            ObjectTypeNode[] cachedNodes = msg.objectTypeConf.getObjectTypeNodes();
            for (int i = 0, length = cachedNodes.length; i < length; i++) {
                ActorRef otnActor = ((ActorBasedNode) cachedNodes[i]).getActor();
                otnActor.tell(msg, self());
            }
        }
    }
}
