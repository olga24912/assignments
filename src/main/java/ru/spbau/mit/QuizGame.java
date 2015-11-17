package ru.spbau.mit;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;


public class QuizGame implements Game {
    private GameServer server;
    private int maxLettersToOpen;
    private long delayUntilNextLetter;
    private String dictionaryFilename;
    private Boolean gameContinue = false;
    private Boolean wasStarted = false;

    private ArrayList<String> questionAndAnswer;
    private int currentQuestionNumber = 0;
    Thread playThread;

    public QuizGame(GameServer server) {
        this.server = server;
        playThread = new Thread(new PlayGame());
    }

    public void setDelayUntilNextLetter (int delayUntilNextLetter) {
        this.delayUntilNextLetter = delayUntilNextLetter;
    }

    public void setMaxLettersToOpen (int maxLettersToOpen) {
        this.maxLettersToOpen = maxLettersToOpen;
    }

    public void setDictionaryFilename (String dictionaryFilename) {
        this.dictionaryFilename = dictionaryFilename;
    }

    private String currentQ, currentA;

    private void nextQuestion() throws FileNotFoundException {
        if (questionAndAnswer == null) {
            questionAndAnswer = new ArrayList<>();
            currentQuestionNumber = 0;
            Scanner scanner = new Scanner(new File(dictionaryFilename));
            while(scanner.hasNext()) {
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

    class PlayGame implements Runnable {
        @Override
        public void run() {
            mainLoop: while(true) {
                if (gameContinue) {
                    try {
                        nextQuestion();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    server.broadcast("New round started: " + currentQ + " (" + currentA.length() + " letters)");
                    for (int i = 0; i < maxLettersToOpen; i++) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(delayUntilNextLetter);
                        } catch (InterruptedException e) {
                            Thread.interrupted();
                            continue mainLoop;
                        }
                        if (Thread.interrupted()) {
                            continue mainLoop;
                        } else {
                            server.broadcast("Current prefix is " + currentA.substring(0, i + 1));
                        }
                    }
                    if (Thread.interrupted()) {
                        continue;
                    } else {
                        try {
                            TimeUnit.MILLISECONDS.sleep(delayUntilNextLetter);
                        } catch (InterruptedException e) {
                            Thread.interrupted();
                            continue;
                        }
                    }
                    if (!Thread.interrupted()) {
                        server.broadcast("Nobody guessed, the word was " + currentA);
                    }
                }
            }
        }
    }

    @Override
    public void onPlayerConnected(String id) {
    }

    @Override
    public void onPlayerSentMsg(String id, String msg) {
        synchronized (this) {
            if (Objects.equals(msg, "!start")) {
                gameContinue = true;
                if (!wasStarted) {
                    wasStarted = true;
                    playThread.start();
                }
            } else if (Objects.equals(msg, "!stop")) {
                gameContinue = false;
                playThread.interrupt();
                server.broadcast("Game has been stopped by " + id);
            } else if (Objects.equals(msg, currentA)) {
                server.broadcast("The winner is " + id);
                playThread.interrupt();
            } else {
                server.sendTo(id, "Wrong try");
            }
        }
    }
}
