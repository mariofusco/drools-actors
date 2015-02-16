package org.drools.actors.reteoo;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.ObjectSink;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.core.spi.ObjectType;

import static org.drools.actors.util.ActorFactory.actorOf;

public class ActorBasedObjectTypeNode extends ObjectTypeNode implements ActorBasedNode {

    private final ActorRef otnActor;

    public ActorBasedObjectTypeNode(int id,
                          EntryPointNode source,
                          ObjectType objectType,
                          BuildContext context) {
        super(id, source, objectType, context);
        this.otnActor = actorOf(props(this));
    }

    @Override
    public ActorRef getActor() {
        return otnActor;
    }

    public static class ObjectTypeNodeActor extends UntypedActor {

        private final ObjectTypeNode objectTypeNode;

        public ObjectTypeNodeActor(ObjectTypeNode objectTypeNode) {
            this.objectTypeNode = objectTypeNode;
        }

        @Override
        public void onReceive(Object message) throws Exception {
            ObjectSink[] sinks = objectTypeNode.getSinkPropagator().getSinks();
            for (ObjectSink sink : sinks) {
                ActorRef otnActor = ((ActorBasedNode) sink).getActor();
                otnActor.tell(message, self());
            }
        }
    }

    public static Props props(final ObjectTypeNode objectTypeNode) {
        return Props.create(new Creator<ObjectTypeNodeActor>() {
            @Override
            public ObjectTypeNodeActor create() throws Exception {
                return new ObjectTypeNodeActor(objectTypeNode);
            }
        });
    }
}
