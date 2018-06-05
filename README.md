# RH-SSO Listener log details

Generate full audit information in logs.

## Versioning

Refer to [Semantic Versioning 2.0.0](http://semver.org/).

## Deployment on Maven repository

 ```bash
 mvn clean deploy -Pnexus
 ```

**Nexus** maven profile defines:

    <nexus.url.release>${nexus.url}/content/repositories/releases</nexus.url.release>
    <nexus.url.snapshot>${nexus.url}/content/repositories/snapshots</nexus.url.snapshot>

## Install and configure

* Create Module

- Create folder structure

```bash
mkdir -p ./modules/system/layers/com/lyra/idm/keycloak/listener-log/0.0.1/
cp listener-log-0.0.1.jar ./modules/system/layers/com/lyra/idm/keycloak/listener-log/0.0.1/
```

- Create module.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.3" name="com.lyra.idm.keycloak.listener-log" slot="0.0.1">
    <resources>
        <resource-root path="listener-log-0.0.1.jar"/>
    </resources>
    <dependencies>
        <module name="org.keycloak.keycloak-core"/>
        <module name="org.keycloak.keycloak-server-spi"/>
        <module name="org.keycloak.keycloak-server-spi-private"/>
        <module name="org.jboss.logging"/>
    </dependencies>
</module>
```

* Configure this plugin:

Using CLI commands:

- for loading:

```
/system-property=LOG_DETAIL_VERSION:add(value="0.0.1")
/subsystem=keycloak-server/:write-attribute(name=providers,value=["classpath:${jboss.home.dir}/providers/*","module:com.lyra.idm.keycloak.listener-log:${LOG_DETAIL_VERSION}"])
```

- for enabling:

```
/subsystem=keycloak-server/spi=eventsListener/:add
/subsystem=keycloak-server/spi=eventsListener/provider=logDetail/:add(enabled=true)
/subsystem=keycloak-server/spi=eventsListener/provider=logDetail/:write-attribute(name=properties,value={"enable-admin-operations" => "true","enable-operations" => "false","level" => "INFO","level-admin" => "WARN","exclude-events" => "[\"REFRESH_TOKEN\"]","excludes-admin-events" => "[]"})
```

Options:

    - enable-operations (true): Manage login events
    - level (INFO): Define logging level for "login events"
    - exclude-events (blank): list of excluded "login events"
    - enable-admin-operations (true): Manage admin events
    - level-admin (WARN): Define logging level for "admin events"
    - exclude-admin-events (blank): list of excluded "admin events"


- On Keycloak, enable audit (see [official documentation](https://www.keycloak.org/docs/3.2/server_admin/topics/events.html)) and add "logDetail" **Event listener**.

## Release Notes

## Author

Sylvain M. for [Lyra](https://lyra.com).





