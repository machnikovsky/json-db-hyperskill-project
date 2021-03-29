package server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// this class handles each client connection
class ClientHandler extends Thread {// Runnable {

    private final Socket socket;
    private final ServerSocket serverSocket;
    volatile Map<String, JsonElement> JSONdb;
    volatile boolean exit;

    public ClientHandler(Socket clientSocket, Map<String, JsonElement> JSONdb, ServerSocket serverSocket) {
        this.socket = clientSocket;
        this.JSONdb = JSONdb;
        this.exit = true;
        this.serverSocket = serverSocket;
    }


    public void run() {

        Gson gson = new Gson();

        ReadWriteLock lock = new ReentrantReadWriteLock();
        Lock readLock = lock.readLock();
        Lock writeLock = lock.writeLock();

        String recievedJSON = null;

        String pathToDb = "C:\\Users\\klien\\IdeaProjects\\JSON Database\\JSON Database\\task\\src\\server\\data\\db.json";
        File file = new File(pathToDb);
        FileWriter fw = null;
        try {
            fw = new FileWriter(file, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw, true);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        try(
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());

                )
        {
         //here

            recievedJSON = input.readUTF();

            JsonObject requestEntity = gson.fromJson(recievedJSON, JsonObject.class);
            ResponseEntity responseEntity = new ResponseEntity();

            JsonElement key = null;
            JsonElement value = null;
            String type = requestEntity.get("type").getAsString();
            if (!type.equals("exit")) {
                key = requestEntity.get("key");
                if (key.isJsonArray()) {
                    key = key.getAsJsonArray();
                }
                if (requestEntity.get("value") != null) {
                    value = requestEntity.get("value");
                    if (value.isJsonObject()) {
                        value = value.getAsJsonObject();
                    }
                }
            }


            switch (type) {
                case "get":
                    readLock.lock();
                    if (!JSONdb.containsKey(key.isJsonArray() ? key.getAsJsonArray().get(0).getAsString() : key.getAsString())) {
                        responseEntity.response = "ERROR";
                        responseEntity.reason = "No such key";
                    } else {
                        responseEntity.response = "OK";
                        if (key.isJsonArray()) {
                            JsonElement jsonElement = null;
                            if (key.getAsJsonArray().size() == 1) {
                                jsonElement = JSONdb.get(key.getAsJsonArray().get(0).getAsString());
                            } else {
                                JsonObject currentObject = JSONdb.get(key.getAsJsonArray().get(0).getAsString()).getAsJsonObject();
                                for (int i = 1; i < key.getAsJsonArray().size() - 1; i++) {
                                    currentObject = currentObject
                                            .get(key.getAsJsonArray().get(i).getAsString()).getAsJsonObject();
                                }

                                jsonElement = currentObject
                                        .get(key.getAsJsonArray().get(key.getAsJsonArray().size() - 1).getAsString());

                            }
                            responseEntity.value = jsonElement;
                        } else {
                            responseEntity.value = JSONdb.get(key.getAsString());
                        }
                    }
                    readLock.unlock();
                    break;
                case "set":
                    writeLock.lock();

                    if (!key.isJsonArray()) {
                        JSONdb.put(key.getAsString(), value);

                    } else {

                        JsonArray actualKey = key.getAsJsonArray();
                        int keySize = actualKey.size();
                        JsonElement currentJsonElement = JSONdb.get(actualKey.get(0).getAsString());

                        for (int i = 1; i < keySize; i++) {
                            JsonObject previousJson = currentJsonElement.getAsJsonObject();

                            if (currentJsonElement.isJsonPrimitive() && i != keySize - 1) {
                                previousJson.add(currentJsonElement.getAsString(), new JsonObject());
                            } else if (currentJsonElement == null && i != keySize - 1) {
                                previousJson.add(actualKey.get(i).getAsString(), new JsonObject());
                            } else if (currentJsonElement.isJsonObject() && i != keySize - 1) {
                                currentJsonElement = currentJsonElement.getAsJsonObject();
                            }
                            if (i == keySize - 1) {
                                previousJson.add(actualKey.get(i).getAsString(), value);
                            }
                            currentJsonElement = currentJsonElement.getAsJsonObject().get(actualKey.get(i).getAsString());

                        }
                    }

                    try (FileWriter writerTemp = new FileWriter(file)) {
                            writerTemp.write(new Gson().toJson(JSONdb, Map.class));


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    responseEntity.response = "OK";
                    writeLock.unlock();
                    break;
                case "delete":
                    writeLock.lock();

                    if (key.isJsonPrimitive() || key.getAsJsonArray().size() == 1) {
                        if (!JSONdb.containsKey(key.getAsString())) {
                            responseEntity.response = "ERROR";
                            responseEntity.reason = "No such key";
                            break;
                        } else {
                            JSONdb.remove(key.getAsString());
                            responseEntity.response = "OK";
                        }
                    } else {
                        JsonArray keysToDelete = key.getAsJsonArray();
                        int keySize = keysToDelete.size();
                        JsonElement elem = JSONdb.get(keysToDelete.get(0).getAsString());
                        for (int i = 1; i < keySize; i++) {
                            JsonObject previous = elem.getAsJsonObject();
                            String currentKey = keysToDelete.get(i).getAsString();
                            if (elem.isJsonPrimitive() && i != keySize - 1 || elem == null) {
                                responseEntity.response = "OK";
                                break;
                            } else if (elem.isJsonObject() && i != keySize - 1) {
                                elem = elem.getAsJsonObject();
                            }
                            if (i == keySize - 1) {
                                if (previous.has(currentKey)) {
                                    previous.remove(currentKey);
                                    responseEntity.response = "OK";
                                    break;
                                } else {
                                    responseEntity.response = "ERROR";
                                    break;
                                }
                            }
                            elem = elem.getAsJsonObject().get(keysToDelete.get(i).getAsString());
                        }



                        File tempFile = new File("myTempFile.txt");

                        BufferedWriter tempWriter = null;
                        try {
                            tempWriter = new BufferedWriter(new FileWriter(tempFile));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String actualKey = key.isJsonArray() ? key.getAsJsonArray().get(0).getAsString() : key.getAsString();
                        String lineToRemove = gson.toJson(new DbEntity(actualKey, value));
                        String currentLine = null;

                        while (true) {
                            try {
                                if ((currentLine = br.readLine()) == null) break;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String trimmedLine = currentLine.trim();
                            if (trimmedLine.equals(lineToRemove)) continue;
                            try {
                                tempWriter.write(currentLine + System.getProperty("line.separator"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            tempWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                            br.close();
                        boolean successful = tempFile.renameTo(file);

                        responseEntity.response = "OK";
                    }
                    writeLock.unlock();
                    break;
                case "exit":
                    responseEntity.response = "OK";
                    exit = true;
                    serverSocket.close();
                    break;
                default:
                    responseEntity.response = "";
                    break;
            }

                output.writeUTF(gson.toJson(responseEntity));
                socket.close();
                fw.close();
                pw.close();
                br.close();



        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
