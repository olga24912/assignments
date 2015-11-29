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
            Integer val = parseInteger(value);

            Method setter = pluginClass.getMethod(setName, val != null ? Integer.TYPE : String.class);
            setter.invoke(plugin, val != null ? val : value);
        }
        if (!(plugin instanceof Game)) {
            throw new IllegalArgumentException();
        }
        game = (Game) plugin;
    }

    private static Integer parseInteger(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ignored) {
            return null;
        }
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
                    String message = connection.receive(100);
                    if (message != null) {
                        game.onPlayerSentMsg(id, message);
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

        new Thread(new GameServerRunnable(connection, id)).start();
    }

    @Override
    public void broadcast(String message) {
        synchronized (listOfConnection) {
            for (Connection connection : listOfConnection.values()) {
                connection.send(message);
            }
        }
    }

    @Override
    public void sendTo(String id, String message) {
        synchronized (listOfConnection) {
            listOfConnection.get(id).send(message);
        }
    }
}
