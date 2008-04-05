package com.sun.ejb.containers;

import com.sun.enterprise.container.common.spi.JavaEETransaction;
import com.sun.enterprise.container.common.spi.JavaEETransactionManager;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.container.common.spi.util.CallFlowAgent;
import com.sun.enterprise.config.serverbeans.EjbContainer;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.v3.server.Globals;
import com.sun.enterprise.v3.server.ServerEnvironment;
import com.sun.enterprise.admin.monitor.callflow.Agent;
import com.sun.logging.LogDomains;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import java.util.Map;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Mahesh Kannan
 *         Date: Feb 10, 2008
 */
@Service
public class EjbContainerUtilImpl
    implements PostConstruct, EjbContainerUtil {

    private Logger _logger = LogDomains.getLogger(LogDomains.EJB_LOGGER);

    @Inject
    private ServerContext serverContext;

    //@Inject
    private  EJBTimerService _ejbTimerService;

    private  Map<Long, BaseContainer> id2Container
            = new ConcurrentHashMap<Long, BaseContainer>();

    private  Timer _timer = new Timer(true);

    private  boolean _insideContainer = true;

    @Inject
    private  InvocationManager _invManager;

    @Inject
    private  InjectionManager _injectionManager;

    @Inject
    private  GlassfishNamingManager _gfNamingManager;

    @Inject
    private  ComponentEnvManager _compEnvManager;

    @Inject
    private JavaEETransactionManager txMgr;

    @Inject
    private EjbContainer ejbContainer;

    @Inject
    private ApplicationHelper applicationHelper;

    @Inject
    private ServerEnvironment env;

    @Inject(optional=true)
    private Agent callFlowAgent;

    private  static EjbContainerUtil _me;

    public void postConstruct() {
        if (callFlowAgent == null) {
            callFlowAgent = (Agent) Proxy.newProxyInstance(EjbContainerUtilImpl.class.getClassLoader(),
                    new Class[] {Agent.class},
                    new InvocationHandler() {
                        public Object invoke(Object proxy, Method m, Object[] args) {
                            return null;
                        }
                    });
        }
        _me = this;
    }

    public static EjbContainerUtil getInstance() {
        if (_me == null) {
            // This situation shouldn't happen. Print the error message 
            // and the stack trace to know how did we get here.

            // Create the instance first to access the logger.
            _me = Globals.getDefaultHabitat().getComponent(
                    EjbContainerUtilImpl.class);
            _me.getLogger().log(Level.SEVERE, 
                    "Internal error: EJBContainerUtilImpl was null",
                    new Throwable());
        }
        return _me;
    }

    public  Logger getLogger() {
        return _logger;
    }

    public  void setEJBTimerService(EJBTimerService es) {
        _ejbTimerService = es;
    }

    public  EJBTimerService getEJBTimerService() {
        return _ejbTimerService;
    }

    public  void registerContainer(BaseContainer container) {
        id2Container.put(container.getContainerId(), container);
    }

    public  void unregisterContainer(BaseContainer container) {
        id2Container.remove(container.getContainerId());
    }

    public  BaseContainer getContainer(long id) {
        return id2Container.get(id);
    }

    public  EjbDescriptor getDescriptor(long id) {
        BaseContainer container = id2Container.get(id);
        return (container != null) ? container.getEjbDescriptor() : null;
    }

    public  ClassLoader getClassLoader(long id) {
        BaseContainer container = id2Container.get(id);
        return (container != null) ? container.getClassLoader() : null;
    }

    public  Timer getTimer() {
        return _timer;
    }

    public  void setInsideContainer(boolean bool) {
        _insideContainer = bool;
    }

    public  boolean isInsideContainer() {
        return _insideContainer;
    }

    public  InvocationManager getInvocationManager() {
        return _invManager;
    }

    public  InjectionManager getInjectionManager() {
        return _injectionManager;
    }

    public  GlassfishNamingManager getGlassfishNamingManager() {
        return _gfNamingManager;
    }

    public  ComponentEnvManager getComponentEnvManager() {
        return _compEnvManager;
    }

    public  ComponentInvocation getCurrentInvocation() {
        return _invManager.getCurrentInvocation();
    }

    public JavaEETransactionManager getTransactionManager() {
        return txMgr;
    }

    public ServerContext getServerContext() {
        return serverContext;
    }

    public  ContainerSynchronization getContainerSync(Transaction jtx)
        throws RollbackException, SystemException
    {
        JavaEETransaction tx = (JavaEETransaction) jtx;
        TxData txData = tx.getContainerData();

        if ( txData == null ) {
            txData = new TxData();
            tx.setContainerData(txData);
        }

        if( txData.sync == null ) {
            txData.sync = new ContainerSynchronization(tx, this);
            tx.registerSynchronization(txData.sync);
        }

        return txData.sync;
    }

    public void removeContainerSync(Transaction tx) {
        //No op
    }

    public EjbContainer getEjbContainer() {
        return ejbContainer;
    }

    public Application findApplicationByName(String appName) {
        return applicationHelper.findApplicationByName(appName);
    }

    public ServerEnvironment getServerEnvironment() {
        return env;
    }

    public  Vector getBeans(Transaction jtx) {
        JavaEETransaction tx = (JavaEETransaction) jtx;
        TxData txData = tx.getContainerData();

        if ( txData == null ) {
            txData = new TxData();
            tx.setContainerData(txData);
        }

        if( txData.beans == null ) {
            txData.beans = new Vector();
        }

        return txData.beans;

    }

    public Agent getCallFlowAgent() {
        return callFlowAgent;
    }

    public void addWork(Runnable task) {
        task.run();
    }
        // Various pieces of data associated with a tx.  Store directly
    // in J2EETransaction to avoid repeated Map<tx, data> lookups.
    private  class TxData {
        ContainerSynchronization sync;
        Vector beans;
    }
    
}
