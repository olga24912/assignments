package ru.spbau.mit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


public class GameServerImpl implements GameServer {
    private Game game;

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
            synchronized (listOfConnection) {
                listOfConnection.remove(id);
            }
        }
    }

    @Override
    public void accept(final Connection connection) {
        String id = "" + countOfConnection++;
        synchronized (listOfConnection) {
            listOfConnection.put(id, connection);
        }
        connection.send(id);

        Thread gameServerTread = new Thread(new GameServerRunnable(connection, id));

        gameServerTread.start();
    }

    @Override
    public void broadcast(String message) {
        for (Connection connection : listOfConnection.values()) {
                connection.send(message);
        }
    }

    @Override
    public void sendTo(String id, String message) {
        synchronized (listOfConnection) {
            listOfConnection.get(id).send(message);
        }
    }
}
