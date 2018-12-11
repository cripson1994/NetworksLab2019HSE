package itmo2018.se;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ListFileManager implements FileManager {
    private List<FileInfo> files = new ArrayList<>();

    @Override
    public FileInfo getFile(int id) {
        FileInfo file = files.get(id);
        for (Iterator<ClientInfo> it = file.owners(); it.hasNext(); ) {
            ClientInfo client = it.next();
            if (!client.isOnline()) {
                file.removeOwner(client);
            }
        }
        return file;
    }

    @Override
    public void registerFile(String name) {
        files.add(new FileInfo(files.size(), name));
    }

    @Override
    public void addOwner(int fileId, ClientInfo client, List<Integer> parts) {
        files.get(fileId).addOwner(client, parts);
    }

    @Override
    public Iterator<FileInfo> iterator() {
        return files.iterator();
    }
}
