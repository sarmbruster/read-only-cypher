package org.neo4j.rocypher;

import org.neo4j.server.rest.security.SecurityRule;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Stefan Armbruster
 */
public class ReadOnlySecurityRule implements SecurityRule {

    @Override
    public boolean isAuthorized(HttpServletRequest request) {
        String readOnlyString = request.getHeader("X-ReadOnly");
        boolean readOnly = Boolean.parseBoolean(readOnlyString);
        ReadOnlyTransactionEventHandler.setReadOnlyThreadLocal(readOnly);
        return true;
    }

    @Override
    public String forUriPath() {
        return "/*";
    }

    @Override
    public String wwwAuthenticateHeader() {
        return null;
    }
}
