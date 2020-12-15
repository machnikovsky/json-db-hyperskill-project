package client;

import com.beust.jcommander.JCommander;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;


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

                RequestEntity request = new RequestEntity();
                //request.makeRequestFromArgs(arguments);
                request.type = arguments.type;
                request.key = arguments.key;
                request.value = arguments.value;
                request.file = arguments.file;
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
