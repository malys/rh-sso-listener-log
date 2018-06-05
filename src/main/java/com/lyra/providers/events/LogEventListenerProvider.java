package com.lyra.providers.events;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

@JBossLog
public class LogEventListenerProvider implements EventListenerProvider {

    private Set<EventType> excludedEvents;
    private Set<OperationType> excludedAdminOperations;
    private Boolean enableOperations;
    private Boolean enableAdminOperations;


    private Method levelMethod;
    private Method levelAdminMethod;

    public LogEventListenerProvider(Set<EventType> excludedEvents, Set<OperationType> excludedAdminOpearations, Boolean enableOperations, Boolean enableAdminOperations,
                                    Method logLevel, Method logLevelAdmin) {
        this.excludedEvents = excludedEvents;
        this.excludedAdminOperations = excludedAdminOpearations;
        this.enableOperations = enableOperations;
        this.enableAdminOperations = enableAdminOperations;
        this.levelMethod=logLevel;
        this.levelAdminMethod=logLevelAdmin;
    }

    public void onEvent(Event event) {
        if (enableOperations) {
            // Ignore excluded events
            if (excludedEvents != null && excludedEvents.contains(event.getType())) {
                return;
            } else {
                try {
                    levelMethod.invoke(log, new String[]{"EVENT: " + toString(event), null});
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.errorf("EVENT: " + toString(event));
                }

            }
        }
    }

    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        if (enableAdminOperations) {
            // Ignore excluded operations
            if (excludedAdminOperations != null && excludedAdminOperations.contains(event.getOperationType())) {
                return;
            } else {
                try {
                    levelAdminMethod.invoke(log, new String[]{"ADMIN_EVENT: " + toString(event, includeRepresentation), null});
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error("ADMIN_EVENT: " + toString(event, includeRepresentation));
                }
            }
        }
    }

    private String toString(Event event) {
        StringBuilder sb = new StringBuilder();

        sb.append("type=");
        sb.append(event.getType());
        sb.append(", realmId=");
        sb.append(event.getRealmId());
        sb.append(", clientId=");
        sb.append(event.getClientId());
        sb.append(", userId=");
        sb.append(event.getUserId());
        sb.append(", ipAddress=");
        sb.append(event.getIpAddress());

        if (event.getError() != null) {
            sb.append(", error=");
            sb.append(event.getError());
        }

        if (event.getDetails() != null) {
            for (Map.Entry<String, String> e : event.getDetails().entrySet()) {
                sb.append(", ");
                sb.append(e.getKey());
                if (e.getValue() == null || e.getValue().indexOf(' ') == -1) {
                    sb.append("=");
                    sb.append(e.getValue());
                } else {
                    sb.append("='");
                    sb.append(e.getValue());
                    sb.append("'");
                }
            }
        }

        return sb.toString();
    }

    private String toString(AdminEvent adminEvent, boolean includeRepresentation) {
        StringBuilder sb = new StringBuilder();

        sb.append("operationType=");
        sb.append(adminEvent.getOperationType());
        sb.append(", realmId=");
        sb.append(adminEvent.getAuthDetails().getRealmId());
        sb.append(", clientId=");
        sb.append(adminEvent.getAuthDetails().getClientId());
        sb.append(", userId=");
        sb.append(adminEvent.getAuthDetails().getUserId());
        sb.append(", ipAddress=");
        sb.append(adminEvent.getAuthDetails().getIpAddress());
        sb.append(", resourcePath=");
        sb.append(adminEvent.getResourcePath());

        if (adminEvent.getError() != null) {
            sb.append(", error=");
            sb.append(adminEvent.getError());
        }

        if (includeRepresentation) {
            sb.append(", representation=");
            sb.append(adminEvent.getRepresentation());
        }

        return sb.toString();
    }

    public void close() {
    }

}
