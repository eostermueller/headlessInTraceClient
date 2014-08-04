package org.headlessintrace.client.connection;

public enum ConnectState
  {
    DISCONNECTED_ERR("Disconnected"), 
    DISCONNECTED("Disconnected"), 
    CONNECTING("Connecting"), 
    CONNECTED("Connected");
    public final String str;
    private ConnectState(String xiStr)
    {
      str = xiStr;
    }
  }