dropwizard-zookeeper
====================

Make dropwizard self register to zookeeper for future discovery.

```java
SomeApplication app = ...;

AdvertiseApplication advertise = new AdvertiseApplication( "127.0.0.1:7777", "/services" );

advertise.register( app, "10.1.1.1", 8080 );
```

This will register the application to a ZooKeeper cluster so that it is discoverable to other services.

Wishful Syntax:

```java
@Advertise
public class MyApplication extends Application<MyConfiguration> {
```

This class will be auto-discovered and exposed to ZooKeeper, using the above method. MyConfiguration
will need to extend _AddressableConfiguration_ in order for this to work. This is because the service requires these methods to be implemented _getAddress_ and _getPort_. These methods can be written to get this information from a configuration or progmatically.
