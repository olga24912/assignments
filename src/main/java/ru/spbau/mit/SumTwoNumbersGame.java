package ru.spbau.mit;

import java.util.*;

public class SumTwoNumbersGame implements Game {
    private int number1, number2;
    private Random rnd = new Random();
    private final int seed = 179;
    private GameServer server;
    public SumTwoNumbersGame(GameServer server) {
        rnd.setSeed(seed);
        number1 = Math.abs(rnd.nextInt()) + 1;
        number2 = Math.abs(rnd.nextInt()) + 1;
        this.server = server;
    }

    @Override
    public void onPlayerConnected(String id) {
        synchronized (this) {
            server.sendTo(id, "" + number1 + " " + number2);
        }
    }

    @Override
    public void onPlayerSentMsg(String id, String msg) {
        synchronized (this) {
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
}
