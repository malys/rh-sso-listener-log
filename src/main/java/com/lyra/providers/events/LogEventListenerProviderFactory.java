package com.lyra.providers.events;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ServerInfoAwareProviderFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@JBossLog
public class LogEventListenerProviderFactory implements EventListenerProviderFactory, ServerInfoAwareProviderFactory {

    public static final String EXCLUDES_OPS = "excludes-events";
    public static final String EXCLUDES_ADMIN_OPS = "excludes-admin-events";

    public static final String ENABLE_OPS = "enable-operations";
    public static final String ENABLE_ADMIN_OPS = "enable-admin-operations";

    public static final String LOG_LEVEL = "level";
    public static final String LOG_LEVEL_ADMIN = "level-admin";

    private Set<EventType> excludedEvents;
    private Set<OperationType> excludedAdminOperations;
    private Boolean enableOperations;
    private Boolean enableAdminOperations;

    private Method logLevel;
    private Method adminLogLevel;


    public EventListenerProvider create(KeycloakSession session) {
        log.debug("LogEventListenerProviderFactory creation");
        return new LogEventListenerProvider(excludedEvents, excludedAdminOperations,
                enableOperations, enableAdminOperations,
                logLevel, adminLogLevel);
    }

    public void init(Config.Scope config) {
        Method[] methodes = log.getClass().getMethods();
        log.debug("LogEventListenerProviderFactory init");
        String[] excludes = config.getArray(EXCLUDES_OPS);
        if (excludes != null) {
            excludedEvents = new HashSet<>();
            for (String e : excludes) {
                excludedEvents.add(EventType.valueOf(e));
            }
        }

        String[] excludesOperations = config.getArray(EXCLUDES_ADMIN_OPS);
        if (excludesOperations != null) {
            excludedAdminOperations = new HashSet<>();
            for (String e : excludesOperations) {
                excludedAdminOperations.add(OperationType.valueOf(e));
            }
        }
        enableOperations = config.getBoolean(ENABLE_OPS);
        if (enableOperations == null) {
            enableOperations = true;
        }

        enableAdminOperations = config.getBoolean(ENABLE_ADMIN_OPS);
        if (enableAdminOperations == null) {
            enableAdminOperations = true;
        }

        String logLevelLocal = config.get(LOG_LEVEL);
        if (logLevelLocal == null) {
            logLevelLocal = "info";
        }
        this.logLevel = findMethod(methodes, logLevelLocal.toLowerCase() );


        String logLevelAdmin = config.get(LOG_LEVEL_ADMIN);
        if (logLevelAdmin == null) {
            logLevelAdmin = "warn";
        }
        this.adminLogLevel = findMethod(methodes, logLevelAdmin.toLowerCase());
    }


    private Method findMethod(Method[] methodes, String level) {
        Optional<Method> result = Arrays.asList(methodes).stream()
                .filter(m -> (level + "f").equals(m.getName()) && m.getParameterTypes().length == 2)
                .findFirst();


        return result.get();
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "logDetail";
    }

    public Map<String, String> getOperationalInfo() {
        Map<String, String> ret = new LinkedHashMap<>();
        ret.put("enableOperations", enableOperations + "");
        ret.put("enableAdminOperations", enableAdminOperations + "");
        ret.put("logLevel", logLevel + "");
        ret.put("adminLogLevel", adminLogLevel + "");
        return ret;
    }

}
