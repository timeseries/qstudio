package com.timestored.swingxx;

import java.util.List;

/**
 * Interface for receiving notification when a new instance of 
 * the existing app is started.
 */
public interface ApplicationInstanceListener {
    public void newInstanceCreated(List<String> args);
}
