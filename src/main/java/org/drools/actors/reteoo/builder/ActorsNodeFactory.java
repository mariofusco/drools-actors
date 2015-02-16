package org.drools.actors.reteoo.builder;

import org.drools.actors.reteoo.ActorBasedAlphaNode;
import org.drools.actors.reteoo.ActorBasedEntryPointNode;
import org.drools.actors.reteoo.ActorBasedLIANode;
import org.drools.actors.reteoo.ActorBasedObjectTypeNode;
import org.drools.core.common.RuleBasePartitionId;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.LeftInputAdapterNode;
import org.drools.core.reteoo.ObjectSource;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.core.reteoo.builder.NodeFactory;
import org.drools.core.reteoo.builder.PhreakNodeFactory;
import org.drools.core.rule.EntryPointId;
import org.drools.core.spi.AlphaNodeFieldConstraint;
import org.drools.core.spi.ObjectType;

public class ActorsNodeFactory extends PhreakNodeFactory implements NodeFactory {

    @Override
    public EntryPointNode buildEntryPointNode(int id, ObjectSource objectSource, BuildContext context) {
        return new ActorBasedEntryPointNode(id, objectSource, context);
    }

    @Override
    public EntryPointNode buildEntryPointNode(int id, RuleBasePartitionId partitionId, boolean partitionsEnabled, ObjectSource objectSource, EntryPointId entryPoint) {
        return new ActorBasedEntryPointNode(id, partitionId, partitionsEnabled, objectSource, entryPoint);
    }

    @Override
    public ObjectTypeNode buildObjectTypeNode( int id, EntryPointNode objectSource, ObjectType objectType, BuildContext context ) {
        return new ActorBasedObjectTypeNode( id, objectSource, objectType, context );
    }

    @Override
    public AlphaNode buildAlphaNode( int id, AlphaNodeFieldConstraint constraint, ObjectSource objectSource, BuildContext context ) {
        return new ActorBasedAlphaNode( id, constraint, objectSource, context );
    }

    @Override
    public LeftInputAdapterNode buildLeftInputAdapterNode( int id, ObjectSource objectSource, BuildContext context ) {
        return new ActorBasedLIANode( id, objectSource, context );
    }
}
