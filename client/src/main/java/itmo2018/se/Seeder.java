package itmo2018.se;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Seeder implements Runnable, Closeable {
    private ServerSocket server;
    private MetaDataManager metaData;
    private ThreadPoolExecutor poolExecutor;
    private int limitLeech = 4;

    public Seeder(ServerSocket server, MetaDataManager metaData) {
        this.server = server;
        this.metaData = metaData;
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
            System.out.println("Stat");
            int id = socketIn.readInt();
            MetaDataNote note = metaData.getNote(id);
            if (note == null) {
                socketOut.writeInt(0);
            } else if (!note.existFile()) {
                metaData.deleteNote(id);
                socketOut.writeInt(0);
            } else {
                socketOut.writeInt(note.partsCount());
                for (int part : note) {
                    socketOut.writeInt(part);
                }
            }
            socketOut.flush();
        }

        private void execGet(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
            int id = socketIn.readInt();
            int part = socketIn.readInt();
            System.out.println("Get");
        }
    }
}
