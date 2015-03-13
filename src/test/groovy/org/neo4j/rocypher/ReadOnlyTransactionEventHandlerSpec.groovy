package org.neo4j.rocypher

import org.junit.Rule
import org.neo4j.extension.spock.Neo4jResource
import org.neo4j.graphdb.TransactionFailureException
import org.neo4j.helpers.collection.IteratorUtil
import spock.lang.Specification

/**
 * @author Stefan Armbruster
 */
class ReadOnlyTransactionEventHandlerSpec extends Specification {

    @Rule
    @Delegate
    Neo4jResource neo4j = new Neo4jResource()

   /* def setup() {
        graphDatabaseService.registerTransactionEventHandler(new ReadOnlyTransactionEventHandler())
    }*/

    def "by default cypher writes work"() {
        when:
        "create ()".cypher()

        then:
        noExceptionThrown()
    }

    def "setting readonly prevents cypher writes"() {
        setup:
        ReadOnlyTransactionEventHandler.setReadOnlyThreadLocal(true)

        when:
        "create ()".cypher()

        then:
        thrown(TransactionFailureException)
    }

    def "run concurrently two tx with different setting"() {
        when:
        def t1 = Thread.start {
            def tx = graphDatabaseService.beginTx()
            try {
                "create (:Label1{msg:'From 1st thread'})".cypher()
                sleep 100
                tx.success()
            } finally {
                tx.close()
            }
        }
        def t2 = Thread.start {
            def tx = graphDatabaseService.beginTx()
            ReadOnlyTransactionEventHandler.setReadOnlyThreadLocal(true)
            try {
                "create (:Label2{msg:'From 2nd thread'})".cypher()
                sleep 100
                tx.success()
            } finally {
                tx.close()
                ReadOnlyTransactionEventHandler.setReadOnlyThreadLocal(false)
            }
        }
        t1.join()
        t2.join()
        def result = IteratorUtil.asCollection( "match (n) return n.msg as msg".cypher())

        then: "we don't see anything from 2nd thread"
        result.size() == 1
        result[0].msg == "From 1st thread"


    }
}
