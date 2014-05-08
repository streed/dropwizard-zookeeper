package com.reed.dropwizard.zookeeper.metadata;

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
