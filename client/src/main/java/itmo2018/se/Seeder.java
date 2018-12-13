package itmo2018.se;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Seeder implements Runnable, Closeable {
    private ServerSocket server;
    private String metaData;
    private ThreadPoolExecutor poolExecutor;
    private int limitLeech = 4;

    public Seeder(ServerSocket server, String workingDir) {
        this.server = server;
        this.metaData = workingDir + "/.metadata";
        poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(limitLeech);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = server.accept();
                if (poolExecutor.getTaskCount() >= limitLeech) {
                    socket.close();
                }
                poolExecutor.submit(new Executor(socket));
            } catch (IOException e) {
                break;
            }
        }
    }

    @Override
    public void close() throws IOException {
        server.close();
        poolExecutor.shutdownNow();
    }

    private class Executor implements Runnable {
        Socket socket;

        Executor(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                DataInputStream socketIn = new DataInputStream(socket.getInputStream());
                DataOutputStream socketOut = new DataOutputStream(socket.getOutputStream());
                while (true) {
                    byte cmd = socketIn.readByte();
                    if (cmd == 1) {
                        execStat(socketIn, socketOut);
                    } else {
                        execGet(socketIn, socketOut);
                    }
                }
            } catch (IOException e) {
                System.out.println("leech disconected");
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void execStat(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
            int id = socketIn.readInt();
            System.out.println("Stat");
        }

        private void execGet(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
            int id = socketIn.readInt();
            int part = socketIn.readInt();
            System.out.println("Get");
        }
    }
}
