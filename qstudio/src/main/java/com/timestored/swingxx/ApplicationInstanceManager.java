package com.timestored.swingxx;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Provides ability to restrict java program to one instance.
 * When user goes to start another instance, relevant 
 * {@link ApplicationInstanceListener}'s will be notified.
 */
public class ApplicationInstanceManager {

    private static ApplicationInstanceListener subListener;

	private static final Logger LOG = Logger.getLogger(ApplicationInstanceManager.class.getName());

    /** Randomly chosen, but static, high socket number */
    private static final int SINGLE_INSTANCE_NETWORK_SOCKET = 44331;

    /** Must end with newline */
    private static final String SINGLE_INSTANCE_SHARED_KEY = "$$NewInstance$$\n";

    /**
     * Registers this instance of the application.
     * @param args arguments that would passed via socket to the existing instance
     * @return true if first instance, false if not.
     */
    public static boolean registerInstance(String[] args) {
        // returnValueOnError should be true if lenient (allows app to run on network error) or false if strict.
        boolean returnValueOnError = true;
        // try to open network socket
        // if success, listen to socket for new instance message, return true
        // if unable to open, connect to existing and send new instance message, return false
        try {
            final ServerSocket socket = new ServerSocket(SINGLE_INSTANCE_NETWORK_SOCKET, 10, InetAddress
                    .getLocalHost());
            LOG.fine("Listening for application instances on socket " + SINGLE_INSTANCE_NETWORK_SOCKET);
            Thread instanceListenerThread = new Thread(new Runnable() {
                @Override
				public void run() {
                    boolean socketClosed = false;
                    while (!socketClosed) {
                        if (socket.isClosed()) {
                            socketClosed = true;
                        } else {
                            try {
                                Socket client = socket.accept();
                                ObjectInputStream oin = new ObjectInputStream((client.getInputStream()));
                                String message = (String) oin.readObject();
                                if (SINGLE_INSTANCE_SHARED_KEY.trim().equals(message.trim())) {
                                    LOG.fine("Shared key matched - new application instance found");
                                    String[] myArgs = (String[]) oin.readObject();
                                    fireNewInstance(Arrays.asList(myArgs));
                                }
                                oin.close();
                                client.close();
                            } catch (ClassNotFoundException e) {
                                LOG.severe("Shared key matched - new application instance found");
                                socketClosed = true;
                            } catch (IOException e) {
                                LOG.severe("Shared key matched - new application instance found");
                                socketClosed = true;
							}
                        }
                    }
                }
            });
            instanceListenerThread.start();
            // listen
        } catch (UnknownHostException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            return returnValueOnError;
        } catch (IOException e) {
            LOG.fine("Port is already taken.  Notifying first instance.");
            try {
                Socket clientSocket = new Socket(InetAddress.getLocalHost(), SINGLE_INSTANCE_NETWORK_SOCKET);
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.writeObject(SINGLE_INSTANCE_SHARED_KEY);
                out.writeObject(args);
                out.close();
                clientSocket.close();
                LOG.fine("Successfully notified first instance.");
                return false;
            } catch (UnknownHostException e1) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
                return returnValueOnError;
            } catch (IOException e1) {
                LOG.log(Level.SEVERE, "Error connecting to local port for single instance notification");
                LOG.log(Level.SEVERE, e1.getMessage(), e1);
                return returnValueOnError;
            }

        }
        return true;
    }

    public static void setApplicationInstanceListener(ApplicationInstanceListener listener) {
        subListener = listener;
    }

    private static void fireNewInstance(List<String> args) {
      if (subListener != null) {
        subListener.newInstanceCreated(args);
      }
  }
}

