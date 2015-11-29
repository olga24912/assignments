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

    private String currentQuestion, currentAnswer;

    public QuizGame(GameServer server) {
        this.server = server;
    }

    public void setDelayUntilNextLetter(int delayUntilNextLetter) {
        this.delayUntilNextLetter = delayUntilNextLetter;
    }

    public void setMaxLettersToOpen(int maxLettersToOpen) {
        this.maxLettersToOpen = maxLettersToOpen;
    }

    public void setDictionaryFilename(String dictionaryFilename) throws FileNotFoundException {
        this.dictionaryFilename = dictionaryFilename;
        questionAndAnswer = new ArrayList<>();
        currentQuestionNumber = 0;
        Scanner scanner = null;
        scanner = new Scanner(new File(dictionaryFilename));
        while (scanner.hasNext()) {
            String s = scanner.nextLine();
            questionAndAnswer.add(s);
        }
    }

    private void nextQuestion() {
        if (currentQuestionNumber == questionAndAnswer.size()) {
            currentQuestionNumber = 0;
        }
        currentQuestion = "";
        currentAnswer = "";
        String currentQuestionAndAnswer = questionAndAnswer.get(currentQuestionNumber);
        int i = 0;
        while (i < currentQuestionAndAnswer.length() && !currentQuestionAndAnswer.substring(i, i + 1).equals(";")) {
            ++i;
        }
        currentQuestion = currentQuestionAndAnswer.substring(0, i);
        currentAnswer = currentQuestionAndAnswer.substring(i + 1);
        currentQuestionNumber += 1;
    }

    private volatile Integer countOfTread = 0;

    private void nextTread() {
        synchronized (countOfTread) {
            lastIdOfWaiting = countOfTread++;
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
            } catch (InterruptedException ignored) {
            }
            lock.lock();
            try {
                if (idOfThisTread == lastIdOfWaiting) {
                    if (maxLettersToOpen == countOpenLatter) {
                        server.broadcast("Nobody guessed, the word was " + currentAnswer);
                        newRoundSendMsg();
                    } else {
                        server.broadcast("Current prefix is " + currentAnswer.substring(0, countOpenLatter + 1));
                        countOpenLatter++;
                        nextTread();
                    }
                }
            } finally {
                lock.unlock();
            }
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
                newRoundSendMsg();
            } else if ("!stop".equals(msg)) {
                lastIdOfWaiting = -1;
                server.broadcast("Game has been stopped by " + id);
            } else if (currentAnswer.equals(msg)) {
                server.broadcast("The winner is " + id);
                newRoundSendMsg();
            } else {
                server.sendTo(id, "Wrong try");
            }
        } finally {
            lock.unlock();
        }
    }

    private void newRoundSendMsg() {
        nextQuestion();
        server.broadcast("New round started: " + currentQuestion + " (" + currentAnswer.length() + " letters)");
        nextTread();
    }
}
