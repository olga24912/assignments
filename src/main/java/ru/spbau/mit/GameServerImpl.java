package ru.spbau.mit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class GameServerImpl implements GameServer {
    Game game;
    ReadWriteLock listOfConnectionLock = new ReentrantReadWriteLock();

    public GameServerImpl(String gameClassName, Properties properties) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> pluginClass = Class.forName(gameClassName);

        Object plugin = pluginClass.getConstructor(GameServer.class).newInstance(this);
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            String setName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
            try {
                int val = Integer.parseInt(value);
                Method setter = pluginClass.getMethod(setName, Integer.TYPE);
                setter.invoke(plugin, val);
            } catch (NumberFormatException e) {
                Method setter = pluginClass.getMethod(setName, String.class);
                setter.invoke(plugin, value);
            }
        }
        if (!(plugin instanceof Game)) {
            throw new IllegalArgumentException();
        }
        game = (Game) plugin;
    }

    private int countOfConnection = 0;

    private final Map<String, Connection> listOfConnection = new HashMap<>();

    private class GameServerRunnable implements Runnable {
        final Connection connection;
        final String id;

        private GameServerRunnable(Connection connection, String id) {
            this.connection = connection;
            this.id = id;
        }

        @Override
        public void run() {
            game.onPlayerConnected(id);
            while (!connection.isClosed()) {
                try {
                    synchronized (connection) {
                        if (!connection.isClosed()) {
                            String message = connection.receive(0);

                            if (message != null) {
                                game.onPlayerSentMsg(id, message);
                            }
                        }
                    }
                } catch (InterruptedException ignored) {
                    break;
                }
            }
            listOfConnectionLock.writeLock().lock();
            listOfConnection.remove(id);
            listOfConnectionLock.writeLock().unlock();
        }
    }

    @Override
    public void accept(final Connection connection) {
        String id = "" + countOfConnection++;

        listOfConnectionLock.writeLock().lock();
        listOfConnection.put(id, connection);
        listOfConnectionLock.writeLock().unlock();

        connection.send(id);

        new Thread(new GameServerRunnable(connection, id)).start();
    }

    @Override
    public void broadcast(String message) {
        listOfConnectionLock.readLock().lock();
        for (Connection connection : listOfConnection.values()) {
            connection.send(message);
        }
        listOfConnectionLock.readLock().unlock();
    }

    @Override
    public void sendTo(String id, String message) {
        synchronized (listOfConnection) {
            listOfConnection.get(id).send(message);
        }
    }
}
