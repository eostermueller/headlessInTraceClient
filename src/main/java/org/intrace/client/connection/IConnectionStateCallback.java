package org.intrace.client.connection;

import java.util.List;
import java.util.Map;

public interface IConnectionStateCallback
  {
    void setConnectionStatusMsg(String msg);
    void setConnectState(ConnectState state);
    ConnectState getConnectState();
    void setProgress(Map<String, String> progress);
    void setStatus(Map<String, String> progress);
    void setConfig(Map<String, String> progress);
  }