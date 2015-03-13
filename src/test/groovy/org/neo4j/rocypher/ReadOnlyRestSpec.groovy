package org.neo4j.rocypher

import org.junit.Rule
import org.neo4j.extension.spock.Neo4jServerResource
import org.neo4j.helpers.collection.IteratorUtil
import spock.lang.Specification

/**
 * @author Stefan Armbruster
 */
class ReadOnlyRestSpec extends Specification {

    @Rule
    @Delegate
    Neo4jServerResource neo4j = new Neo4jServerResource(
            config:['org.neo4j.server.rest.security_rules':'org.neo4j.rocypher.ReadOnlySecurityRule']
    )

    def "test query without X-ReadOnly"() {
        when:
        def result = http.withHeaders("X-ReadOnly", readOnly).POST("db/data/transaction/commit", createJsonForTransactionalEndpoint(["create (n) return n"], null))

        then:
        result.status() == responseCode
        result.content().errors*.code == errors
        IteratorUtil.single("optional match (n) return count(n) as count".cypher()).count == numberOfNodes

        where:
        readOnly | responseCode | numberOfNodes | errors
        "false"  | 200          | 1 | []
        "true"   | 200          | 0 | ["Neo.DatabaseError.Transaction.CouldNotCommit"]
    }
}
