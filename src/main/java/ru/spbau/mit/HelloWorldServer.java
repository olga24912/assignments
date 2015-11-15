package ru.spbau.mit;


public class HelloWorldServer implements Server {

    private class SendHelloWorld implements Runnable {
        public SendHelloWorld(Connection connection) {
            this.connection = connection;
        }

        final Connection connection;

        @Override
        public void run() {
            connection.send("Hello world");
            connection.close();
        }
    }

    @Override
    public void accept(final Connection connection) {
        Thread sendHelloWorldTread = new Thread(new SendHelloWorld(connection));
        sendHelloWorldTread.start();
    }
}
