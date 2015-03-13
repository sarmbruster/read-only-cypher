package org.neo4j.rocypher;

import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

/**
 * @author Stefan Armbruster
 */
public class ReadOnlyTransactionEventHandler extends TransactionEventHandler.Adapter {

    static ThreadLocal<Boolean> readOnlyThreadLocal = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    public static boolean getReadOnly() {
        return readOnlyThreadLocal.get();
    }

    public static void setReadOnlyThreadLocal(boolean status) {
        readOnlyThreadLocal.set(status);
    }

    @Override
    public Object beforeCommit(TransactionData data) throws Exception {
        boolean readOnly = readOnlyThreadLocal.get();
        if (readOnly) {
            if (notEmpty(data.assignedLabels()) ||
                    notEmpty(data.assignedNodeProperties()) ||
                    notEmpty(data.assignedRelationshipProperties()) ||
                    notEmpty(data.removedLabels()) ||
                    notEmpty(data.removedNodeProperties()) ||
                    notEmpty(data.removedRelationshipProperties()) ||
                    notEmpty(data.createdNodes()) ||
                    notEmpty(data.createdRelationships()) ||
                    notEmpty(data.deletedNodes()) ||
                    notEmpty(data.deletedRelationships())
                    ) {
                throw new IllegalArgumentException("this tx is read only, go away");
            }
        }
        return null;
    }

    private boolean notEmpty(Iterable iterable) {
        return iterable.iterator().hasNext();
    }
}
