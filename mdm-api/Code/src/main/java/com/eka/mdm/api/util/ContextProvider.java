package com.eka.mdm.api.util;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.stereotype.Component;
import com.eka.mdm.dataobject.RequestContext;


/**
 * The Class ContextProvider.
 */
@Component("contextProvider")
public class ContextProvider {
	
	/** The logger. */
	final static Logger logger = ESAPI.getLogger(ContextProvider.class);
    
    /** The current tenant. */
    private ThreadLocal<RequestContext> currentContext = new ThreadLocal<>();
    
    /**
     * Sets the current tenant.
     *
     * @param tenant the new current tenant
     */
    public  void setCurrentContext(RequestContext tenant) {
    	logger.debug(logger.EVENT_SUCCESS, ESAPI.encoder().encodeForHTML("Setting currentContext to " + currentContext));
        currentContext.set(tenant);
    }
    
    /**
     * Gets the current tenant.
     *
     * @return the current tenant
     */
    public  RequestContext getCurrentContext() {
        return currentContext.get();
    }
    
    /**
     * Clear.
     */
    public  void clear() {
    	currentContext.set(null);
    }

    /**
     * remove ThreadLocal.
     */
    public  void remove() {
    	currentContext.remove();
    }

}
