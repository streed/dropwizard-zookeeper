package com.reed.dropwizard.zookeeper;

import com.google.common.base.Throwables;

import io.dropwizard.Application;
import io.dropwizard.Configuration;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceDiscovery;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.type.TypeReference;

public class AdvertiseApplication {
  private String basePath;
  private final CuratorFramework curatorClient;
  private final InstanceSerializer<ApplicationMetadata> jacksonInstanceSerializer;

  public final class ApplicationMetadata {
    @JsonProperty( "address" )
    private final String address;

    @JsonProperty( "port" )
    private final int port;

    public ApplicationMetadata( @JsonProperty( "address" ) String address,
                               @JsonProperty( "port" ) int port ) {
      this.address = address;
      this.port = port;
    }

    public String getAddress() {
      return address;
    }

    public int getPort() {
      return port;
    }
  }

  public AdvertiseApplication( String connectionString, String basePath ) {
    try {
      curatorClient = CuratorFrameworkFactory.builder()
        .connectionTimeoutMs( 1000 )
        .retryPolicy( new RetryNTimes( 10, 500 ) )
        .connectString( connectionString )
        .build();

    } catch( Exception e ) {
      throw Throwables.propagate( e );
    }
    
    this.basePath = basePath;
    jacksonInstanceSerializer = new JsonInstanceSerializer( ApplicationMetadata.class );
  }

  public AdvertiseApplication  register( Application application, String address, Integer port ) {
    try {
      ServiceDiscovery<ApplicationMetadata> discovery = getDiscovery();
      discovery.start();
      discovery.registerService( getInstance( application, address, port ) );
      discovery.close();
    } catch( Exception e ) {
      throw Throwables.propagate( e );
    }

    return this;
  }

  public AdvertiseApplication unregister( Application application, String address, Integer port ) {
    try {
      ServiceDiscovery<ApplicationMetadata> discovery = getDiscovery();
      discovery.start();
      discovery.unregisterService( getInstance( application, address, port ) );
      discovery.close();
    } catch( Exception e ) {
      throw Throwables.propagate( e );
    }
    
    return this;
  }

  private ServiceDiscovery<ApplicationMetadata> getDiscovery() { 
    return ServiceDiscoveryBuilder.builder( ApplicationMetadata.class )
      .basePath( basePath )
      .client( curatorClient )
      .serializer( jacksonInstanceSerializer )
      .build();
  }

  private ServiceInstance<ApplicationMetadata> getInstance( Application application, String address, Integer port ) {
    ApplicationMetadata metadata = new ApplicationMetadata( address, port );
    try {
      return ServiceInstance.<ApplicationMetadata>builder()
        .name( application.getName() )
        .address( address )
        .port( port )
        .payload( metadata )
        .build();
    } catch( Exception e ) {
      throw Throwables.propagate( e );
    }
  }
}
