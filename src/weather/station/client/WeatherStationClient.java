package weather.station.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import shared.utilities.ClientAuthenticationResponse;
import shared.utilities.StationDataMessage;

import shared.utilities.StationRegistrationRequest;

public class WeatherStationClient {
    
    private final String id;
    private final String position;
    private final Socket server;
    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;

    public WeatherStationClient(String IP, int port, String id, String position) throws IOException {
        this.id = id;
        this.position = position;
        server = new Socket(IP, port);
        OutputStream outputStream = server.getOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);
        InputStream inputStream = server.getInputStream();
        objectInputStream = new ObjectInputStream(inputStream);
        System.out.println("Connected to server at " + server.getInetAddress());
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        WeatherStationClient client = new WeatherStationClient("127.0.0.1", 8080, 
                "ID" + (int)(Math.random() * 1000 + 1), 
                (Math.random() * 10) + ":" + (Math.random() * 10));
        client.registerWithServer();
        client.beginSendingData();
    }

    private void sendObject(Object object) throws IOException {
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();
    }

    private Object readObject() throws IOException, ClassNotFoundException {
        Object object = objectInputStream.readObject();
        return object;
    }

    private void registerWithServer() throws IOException, ClassNotFoundException {
        sendObject(new StationRegistrationRequest(id));
        ClientAuthenticationResponse message = (ClientAuthenticationResponse)readObject();
        if(message.isAccepted()) {
            System.out.println("Successfully authenticated!");
        }
        else {
            System.out.println("Authentication request denied!");
        }
    }

    private void beginSendingData() throws IOException {
        while(true) {
            int humidity = (int)(Math.random() * 20 + 10);
            int temperature = (int)(Math.random() * 20 + 10);
            sendObject(new StationDataMessage(position, temperature, humidity));
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
}
