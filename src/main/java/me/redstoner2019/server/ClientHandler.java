package me.redstoner2019.server;

import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public ClientHandler(Socket socket) {
        System.out.println("Client connected");
        this.socket = socket;
        try {
            this.ois = new ObjectInputStream(socket.getInputStream());
            this.oos = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (oos != null) {
                    try {
                        JSONObject packet = new JSONObject((String) ois.readObject());

                        JSONObject response = getErrorJSON("Not a valid request.");

                        if(packet.has("header")){
                            response = getErrorJSON("'" + packet.getString("header") + "' not implemented.");

                            switch (packet.getString("header")) {
                                case "request-data" -> {
                                    response = CacheServer.loadCache();
                                }
                                case "refresh" -> {
                                    CacheServer.refresh();
                                    response = getSuccessJSON();
                                }
                                case "add-user" -> {
                                    if(packet.has("user")){
                                        CacheServer.addUser(packet.getString("user"));
                                        response = getSuccessJSON();
                                    } else {
                                        response = getErrorJSON("Field 'user' is missing.");
                                    }
                                }
                                case "add-repo" -> {
                                    if(packet.has("user")){
                                        if(packet.has("repo")){
                                            CacheServer.addRepo(packet.getString("user"),packet.getString("repo"));
                                            response = getSuccessJSON();
                                        } else {
                                            response = getErrorJSON("Field 'repo' is missing.");
                                        }
                                    } else {
                                        response = getErrorJSON("Field 'user' is missing.");
                                    }
                                }
                                case "add-version" -> {
                                    if(packet.has("user")){
                                        if(packet.has("repo")){
                                            if(packet.has("version")){
                                                CacheServer.addVersion(packet.getString("user"),packet.getString("repo"),packet.getString("version"));
                                                response = getSuccessJSON();
                                            } else {
                                                response = getErrorJSON("Field 'version' is missing.");
                                            }
                                        } else {
                                            response = getErrorJSON("Field 'repo' is missing.");
                                        }
                                    } else {
                                        response = getErrorJSON("Field 'user' is missing.");
                                    }
                                }
                            }
                        }
                        oos.writeObject(response.toString());
                    } catch (IOException | ClassNotFoundException e) {
                        try {
                            socket.close();
                            oos.close();
                            ois.close();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        System.out.println("Disconnected");
                        break;
                    }
                }
            }
        });
        t.start();
    }

    public JSONObject getErrorJSON(String error){
        return new JSONObject("{header : \"error\", message : \"" + error +"\"}");
    }
    public JSONObject getSuccessJSON(){
        return new JSONObject("{header : \"success\"}");
    }
}
