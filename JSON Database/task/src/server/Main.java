package server;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//SERVER
public class Main {

    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in);

        Gson gson = new Gson();

        String pathToDb = "C:\\Users\\klien\\IdeaProjects\\JSON Database\\JSON Database\\task\\src\\server\\data\\db.json";
        File file = new File(pathToDb);
        FileWriter fw = new FileWriter(file, true);
        PrintWriter pw = new PrintWriter(fw, true);
        BufferedReader br = new BufferedReader(new FileReader(file));



        Map<String, JsonElement> JSONdb = new HashMap<>();
        String readLine;
        while ((readLine = br.readLine()) != null) {
            DbEntity dbEntity = gson.fromJson(readLine, DbEntity.class);
            JSONdb.put(dbEntity.key, dbEntity.value);
        }

        String address = "127.0.0.1";
        int port = 23456;

        try (ServerSocket server = new ServerSocket(port, 50, InetAddress.getByName(address))) {
            System.out.println("Server started!");

            int poolSize = Runtime.getRuntime().availableProcessors();
            ExecutorService executor = Executors.newFixedThreadPool(poolSize);


            while (!server.isClosed()) {
                Socket clientSocket = server.accept();
                ClientHandler handler = new ClientHandler(clientSocket, JSONdb, server);
                executor.submit(handler);
            }
            //executor.shutdown();

        } catch (IOException e) {
            e.printStackTrace();
        }
        }
    }

