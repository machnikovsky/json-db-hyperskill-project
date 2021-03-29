package client;

import com.beust.jcommander.JCommander;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

//CLIENT
public class Main {

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        Gson gson = new Gson();


        String address = "127.0.0.1";
        int port = 23456;

        try (
                Socket socket = new Socket(InetAddress.getByName(address), port);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output  = new DataOutputStream(socket.getOutputStream())
        ) {
            Args arguments = new Args();

            JCommander.newBuilder()
                    .addObject(arguments)
                    .build()
                    .parse(args);

            System.out.println("Client started!");


            JsonObject request = new JsonObject();

            //RequestEntity request = new RequestEntity();
            //request.makeRequestFromArgs(arguments);
            //request.type = arguments.type;
            //request.key = arguments.key;
            //request.value = arguments.value;
            //request.file = arguments.file;


            if (arguments.file != null) {
                String fullPath = "C:\\Users\\klien\\IdeaProjects\\JSON Database\\JSON Database\\task\\src\\client\\data\\" + arguments.file;
                File fileArg = new File(fullPath);
                BufferedReader brFileArg = null;
                try {
                    brFileArg = new BufferedReader(new FileReader(fileArg));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                try {
                    request = gson.fromJson(brFileArg.readLine(), JsonObject.class);
                    //request = gson.fromJson(brFileArg.readLine(), RequestEntity.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {

                if (arguments.type != null) {
                    request.addProperty("type", arguments.type);
                }
                if (arguments.key != null) {
                    request.addProperty("key", arguments.key);
                }
                if (arguments.value != null) {
                    request.addProperty("value", arguments.value);
                }

            }

            String requestJSON = gson.toJson(request);

            output.writeUTF(requestJSON);
            System.out.println("Sent: " + requestJSON);
            String receivedMsg = input.readUTF();
            System.out.println("Received: " + receivedMsg);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
