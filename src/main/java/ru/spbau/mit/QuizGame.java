package ru.spbau.mit;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class QuizGame implements Game {
    private final GameServer server;
    private int maxLettersToOpen;
    private long delayUntilNextLetter;
    private String dictionaryFilename;

    private ArrayList<String> questionAndAnswer;
    private int currentQuestionNumber = 0;

    private volatile int lastIdOfWaiting = 0;
    private int countOpenLatter = 0;

    private Lock lock = new ReentrantLock();

    public QuizGame(GameServer server) {
        this.server = server;
    }

    public void setDelayUntilNextLetter(int delayUntilNextLetter) {
        this.delayUntilNextLetter = delayUntilNextLetter;
    }

    public void setMaxLettersToOpen(int maxLettersToOpen) {
        this.maxLettersToOpen = maxLettersToOpen;
    }

    public void setDictionaryFilename(String dictionaryFilename) {
        this.dictionaryFilename = dictionaryFilename;
    }

    private String currentQ, currentA;

    private void nextQuestion() throws FileNotFoundException {
        if (questionAndAnswer == null) {
            questionAndAnswer = new ArrayList<>();
            currentQuestionNumber = 0;
            Scanner scanner = new Scanner(new File(dictionaryFilename));
            while (scanner.hasNext()) {
                String s = scanner.nextLine();
                questionAndAnswer.add(s);
            }
        }
        if (currentQuestionNumber == questionAndAnswer.size()) {
            currentQuestionNumber = 0;
        }
        currentQ = "";
        currentA = "";
        String currentQuestionAndAnswer = questionAndAnswer.get(currentQuestionNumber);
        int i = 0;
        while (i < currentQuestionAndAnswer.length() && !Objects.equals(currentQuestionAndAnswer.substring(i, i + 1), ";")) {
            ++i;
        }
        currentQ = currentQuestionAndAnswer.substring(0, i);
        currentA = currentQuestionAndAnswer.substring(i + 1);
        currentQuestionNumber += 1;
    }

    Random rand = new Random();

    private void nextTread() {
        synchronized (rand) {
            lastIdOfWaiting = rand.nextInt();
            Thread tr = new Thread(new PlayGame(lastIdOfWaiting));
            tr.start();
        }
    }

    private class PlayGame implements Runnable {
        private int idOfThisTread;

        PlayGame(int idOfThisTread) {
            this.idOfThisTread = idOfThisTread;
        }

        @Override
        public void run() {
            try {
                TimeUnit.MILLISECONDS.sleep(delayUntilNextLetter);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.lock();
            if (idOfThisTread == lastIdOfWaiting) {
                if (maxLettersToOpen == countOpenLatter) {
                    server.broadcast("Nobody guessed, the word was " + currentA);
                    try {
                        nextQuestion();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    server.broadcast("New round started: " + currentQ + " (" + currentA.length() + " letters)");
                    nextTread();
                } else {
                    server.broadcast("Current prefix is " + currentA.substring(0, countOpenLatter + 1));
                    countOpenLatter++;
                    nextTread();
                }
            }
            lock.unlock();
        }
    }

    @Override
    public void onPlayerConnected(String id) {
    }

    @Override
    public void onPlayerSentMsg(String id, String msg) {
        lock.lock();
        try {
            if ("!start".equals(msg)) {
                try {
                    nextQuestion();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                server.broadcast("New round started: " + currentQ + " (" + currentA.length() + " letters)");
                nextTread();
            } else if ("!stop".equals(msg)) {
                lastIdOfWaiting = 0;
                server.broadcast("Game has been stopped by " + id);
            } else if (currentA.equals(msg)) {
                server.broadcast("The winner is " + id);
                try {
                    nextQuestion();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                server.broadcast("New round started: " + currentQ + " (" + currentA.length() + " letters)");
                nextTread();
            } else {
                server.sendTo(id, "Wrong try");
            }
        } finally {
            lock.unlock();
        }
    }
}
