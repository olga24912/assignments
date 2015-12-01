package ru.spbau.mit;

import java.util.*;

public class SumTwoNumbersGame implements Game {
    private int number1, number2;
    private final Random rnd = new Random();
    private final int seed = 179;
    private final GameServer server;

    public SumTwoNumbersGame(GameServer server) {
        rnd.setSeed(seed);
        number1 = Math.abs(rnd.nextInt()) + 1;
        number2 = Math.abs(rnd.nextInt()) + 1;
        this.server = server;
    }

    @Override
    public synchronized void onPlayerConnected(String id) {
        server.sendTo(id, "" + number1 + " " + number2);
    }

    @Override
    public synchronized void onPlayerSentMsg(String id, String msg) {
        int sum = Integer.parseInt(msg);
        if (sum == number1 + number2) {
            server.sendTo(id, "Right");
            server.broadcast(id + " won");
            number1 = Math.abs(rnd.nextInt()) + 1;
            number2 = Math.abs(rnd.nextInt()) + 1;
            server.broadcast("" + number1 + " " + number2);
        } else {
            server.sendTo(id, "Wrong");
        }
    }
}
