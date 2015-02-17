package org.drools.actors.reteoo;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import akka.util.Timeout;
import org.drools.actors.messages.AckMessage;
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
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        PropagationMessage msg = new PropagationMessage(PropagationMessage.ActionType.INSERT, handle, context, objectTypeConf, workingMemory);
        entryPointActor.tell(msg, ActorRef.noSender());
    }

    public void waitPropagation() {
        Timeout timeout = new Timeout(Duration.create(5, "seconds"));
        Future<Object> future = Patterns.ask(entryPointActor, "ready", timeout);
        try {
            Await.ready(future, timeout.duration());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class EntryPointNodeActor extends UntypedActor {

        private final Map<Long, Integer> propagationMap = new HashMap<>();

        private final List<ActorRef> senders = new ArrayList<>();

        @Override
        public void onReceive(Object message) throws Exception {
            if ("ready".equals(message)) {
                if (propagationMap.isEmpty()) {
                    getSender().tell("ok", getSelf());
                } else {
                    senders.add(getSender());
                }
            } else if (message instanceof PropagationMessage) {
                PropagationMessage msg = (PropagationMessage) message;
                switch (msg.type) {
                    case INSERT:
                        doAssert(msg);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            } else if (message instanceof AckMessage) {
                ackPropagation(((AckMessage) message).messageId);
            }
        }

        private void doAssert(PropagationMessage msg) {
            ObjectTypeNode[] otns = msg.objectTypeConf.getObjectTypeNodes();
            startPropagation(msg.messageId, otns.length);
            for (ObjectTypeNode otn : otns) {
                ActorRef otnActor = ((ActorBasedNode) otn).getActor();
                otnActor.tell(msg, self());
            }
        }

        private void startPropagation(long id, int sinkNr) {
            propagationMap.put(id, sinkNr);
        }

        private void ackPropagation(long id) {
            int nr = propagationMap.remove(id) - 1;
            if (nr > 0) {
                propagationMap.put(id, nr);
            } else if (propagationMap.isEmpty()) {
                for (ActorRef sender : senders) {
                    sender.tell("ok", getSelf());
                }
                senders.clear();
            }
        }
    }
}
