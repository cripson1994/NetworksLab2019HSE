package itmo2018.se;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.Callable;

public class Executor implements Callable<Void> {
    private DataInputStream content;
    private ClientDataHolder client;
    private FileManager fileManager;
    private Writer writer;

    public Executor(byte[] bytes, ClientDataHolder client, FileManager fileManager, Writer writer) {
        this.content = new DataInputStream(new ByteArrayInputStream(bytes));
        this.client = client;
        this.fileManager = fileManager;
        this.writer = writer;
    }

    @Override
    public Void call() throws Exception {
        byte cmd = content.readByte();
        switch (cmd) {
            case 1:
                executeList();
                break;
            case 2:
                executeUpload();
                break;
            case 3:
                executeSources();
                break;
            case 4:
                executeUpdate();
                break;
        }
        writer.registerClient(client);
        return null;
    }

    private void executeList() throws IOException {
        System.out.println("list");

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteStream);
        out.writeInt(fileManager.filesNumber());
        for (FileInfo file : fileManager) {
            out.writeInt(file.getId());
            out.writeUTF(file.getName());
            out.writeLong(file.getSize());
        }

        ByteBuffer response = ByteBuffer.allocate(out.size());
        response.put(byteStream.toByteArray());
        response.flip();
        client.addResponse(response);
    }

    private void executeUpload() throws IOException {
        System.out.println("upload");

        String name = content.readUTF();
        long size = content.readLong();

        System.out.println(name + " " + size);
        int id = fileManager.registerFile(name, size, client.getClientInfo());

        ByteBuffer response = ByteBuffer.allocate(4);
        response.putInt(id);
        response.flip();
        client.addResponse(response);
    }

    private void executeSources() throws IOException {
        System.out.println("sources");

        int id = content.readInt();
        ByteBuffer response;
        if (id < 0 || id >= fileManager.filesNumber()) {
            response = ByteBuffer.allocate(4);
            response.putInt(0);
        } else {
            FileInfo file = fileManager.getFile(id);
            response = ByteBuffer.allocate(4 + file.ownersNumber() * (4 + 2));
            response.putInt(file.ownersNumber());
            for (Iterator<ClientInfo> it = file.owners(); it.hasNext(); ) {
                ClientInfo clientInfo = it.next();
                response.put(clientInfo.getIp());
                response.putShort((short) clientInfo.getSharingPort());
            }
        }
        response.flip();
        client.addResponse(response);
    }

    private void executeUpdate() throws IOException {
        System.out.println("update");

        ClientInfo clientInfo = client.getClientInfo();
        clientInfo.updateCloseTask();
        int port = shortToInt(content.readShort());
        client.getClientInfo().updateSharingPort(port);
        int count = content.readInt();
        for (int i = 0; i < count; i++) {
            int fileId = content.readInt();
            fileManager.getFile(fileId).addOwner(clientInfo);
        }

        ByteBuffer response = ByteBuffer.allocate(1);
        response.put((byte) 1);
        response.flip();
        client.addResponse(response);
    }

    private int shortToInt(short s) {
        if (s >= 0) {
            return s;
        }
        return 32768 + 32768 + s;
    }
}
