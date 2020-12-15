package server;


import com.google.gson.Gson;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in);

        Gson gson = new Gson();

        String pathToDb = "C:\\Users\\klien\\IdeaProjects\\JSON Database\\JSON Database\\task\\src\\server\\data\\db.json";
        File file = new File(pathToDb);
        FileWriter fw = new FileWriter(file, true);
        PrintWriter pw = new PrintWriter(fw, true);
        BufferedReader br = new BufferedReader(new FileReader(file));



        Map<String, String> JSONdb = new HashMap<>();
        String readLine;
        while ((readLine = br.readLine()) != null) {
            DbEntity dbEntity = gson.fromJson(readLine, DbEntity.class);
            JSONdb.put(dbEntity.key, dbEntity.value);
        }

        String address = "127.0.0.1";
        int port = 23456;

        try (ServerSocket server = new ServerSocket(port, 50, InetAddress.getByName(address))) {
            System.out.println("Server started!");
            AtomicBoolean stop = new AtomicBoolean(false);

            int poolSize = Runtime.getRuntime().availableProcessors();
            ExecutorService executor = Executors.newFixedThreadPool(poolSize);

            ReadWriteLock lock = new ReentrantReadWriteLock();
            Lock readLock = lock.readLock();
            Lock writeLock = lock.writeLock();

            while (!stop.get()) {
                Runnable task = () -> {
                    Socket socket = null;
                    try {
                        socket = server.accept();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    DataInputStream input = null;
                    try {
                        input = new DataInputStream(socket.getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    DataOutputStream output = null;
                    try {
                        output = new DataOutputStream(socket.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String recievedJSON = null;
                    try {
                        recievedJSON = input.readUTF();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    RequestEntity requestEntity = gson.fromJson(recievedJSON, RequestEntity.class);

                    ResponseEntity responseEntity = new ResponseEntity();
                    String type = requestEntity.type;
                    String key = requestEntity.key;
                    String value = requestEntity.value;


                    switch (type) {
                        case "get":
                            if (!JSONdb.containsKey(key)) {
                                responseEntity.response = "ERROR";
                                responseEntity.reason = "No such key";
                            } else {
                                responseEntity.response = "OK";
                                responseEntity.value = JSONdb.get(key);
                            }
                            break;
                        case "set":
                            if (JSONdb.containsKey(key)) {




                                String lineToRemove = gson.toJson(new DbEntity(key, JSONdb.get(key)));
                                String lineToSet = gson.toJson(new DbEntity(key, value));

                                int lineNumber = 0;
                                while (true) {
                                    try {
                                        if (br.readLine().equals(lineToRemove)) break;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    lineNumber++;
                                }

                                List<String> lines = null;
                                try {
                                    lines = Files.readAllLines(Path.of(pathToDb), StandardCharsets.UTF_8);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                lines.set(lineNumber, lineToSet);
                                try {
                                    Files.write(Path.of(pathToDb), lines, StandardCharsets.UTF_8);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                JSONdb.put(key, value);
//
//
//                                File tempFile = new File("myTempFile.txt");
//
//                                BufferedWriter tempWriter = null;
//                                try {
//                                    tempWriter = new BufferedWriter(new FileWriter(tempFile));
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//
//                                String lineToRemove = gson.toJson(new DbEntity(key, JSONdb.get(key)));
//                                String currentLine = null;
//
//                                while (true) {
//                                    try {
//                                        if ((currentLine = br.readLine()) == null) break;
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                    String trimmedLine = currentLine.trim();
//                                    if (trimmedLine.equals(lineToRemove)) {
//                                        try {
//                                            tempWriter.write(gson.toJson(new DbEntity(key, value)) + System.getProperty("line.separator"));
//                                        } catch (IOException e) {
//                                            e.printStackTrace();
//                                        }
//                                    } else {
//                                        try {
//                                            tempWriter.write(currentLine + System.getProperty("line.separator"));
//                                        } catch (IOException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                }
//                                try {
//                                    tempWriter.close();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                                try {
//                                    br.close();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                                boolean successful = tempFile.renameTo(file);
//
//                                //responseEntity.response = "OK";
//
//
//
//
////                                String currentLine = null;
////                                while (true) {
////                                    try {
////                                        if ((currentLine = br.readLine()) == null) break;
////                                    } catch (IOException e) {
////                                        e.printStackTrace();
////                                    }
////                                    if (currentLine.equals(gson.toJson(new DbEntity(key, JSONdb.get(key))))) {
////                                        currentLine.replace(currentLine, gson.toJson(new DbEntity(key, value)));
////                                    }
////                                }
                            } else {
                                JSONdb.put(key, value);
                                pw.println(gson.toJson(new DbEntity(key, value)));
                            }
                            responseEntity.response = "OK";
                            break;
                        case "delete":
                            if (!JSONdb.containsKey(key)) {
                                responseEntity.response = "ERROR";
                                responseEntity.reason = "No such key";
                            } else {
                                JSONdb.remove(key);

                                File tempFile = new File("myTempFile.txt");

                                BufferedWriter tempWriter = null;
                                try {
                                    tempWriter = new BufferedWriter(new FileWriter(tempFile));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                String lineToRemove = gson.toJson(new DbEntity(key, value));
                                String currentLine = null;

                                while (true) {
                                    try {
                                        if (!((currentLine = br.readLine()) != null)) break;
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
                                try {
                                    br.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                boolean successful = tempFile.renameTo(file);

                                responseEntity.response = "OK";
                            }
                            break;
                        case "exit":
                            stop.set(true);
                            responseEntity.response = "OK";
                            break;
                        default:
                            responseEntity.response = "";
                            break;
                    }
                    try {
                        output.writeUTF(gson.toJson(responseEntity));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                };
                executor.submit(task);

            }
            executor.shutdown();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

