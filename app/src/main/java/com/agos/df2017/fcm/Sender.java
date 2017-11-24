package com.agos.df2017.fcm;

import com.agos.df2017.entities.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by arielortuno on 11/22/17.
 */

public class Sender {

    private static HttpURLConnection prepareConnection(HttpURLConnection connection) throws UnsupportedEncodingException {
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-type", "application/json");
        connection.setRequestProperty("Authorization", "key=AAAAGSfPcOQ:APA91bGDAhIdtwpXjtIUn_1QTQ-LnB33xX05NflQZzTtUpwpsKUGtZHMwSADKnnx03uZHKYCmZd5aG1x5GRvNn2GTbHJp51tYR3aZKylItLMsoslGh_x2V5WVlrHS6VrWIq9nxhhCxbp");
        return connection;
    }

    public static boolean post(String title, String message, String token, String uid) throws Exception {
        URL url = new URL("https://fcm.googleapis.com/fcm/send");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {

            JSONObject root = new JSONObject();
            JSONObject notification = new JSONObject();
            notification.put("body", message);
            notification.put("title", title);
            notification.put("user", uid);
            notification.put("sound", "default");

            JSONObject data = new JSONObject();
            data.put("message", message);
            data.put("user", uid);
            root.put("notification", notification);
            root.put("data", data);
            root.put("registration_ids", new JSONArray().put(token));


            prepareConnection(connection);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            DataOutputStream osw = new DataOutputStream(connection.getOutputStream());
            osw.write(root.toString().getBytes());
            osw.flush();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return true;
            }
            throw new Exception(String.format("Error->POST Metodo: %s, Code: %s", connection.getRequestMethod(), connection.getResponseCode()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            connection.disconnect();
        }
    }

    public static boolean post(String title, String message, String uid) throws Exception {
        URL url = new URL("https://fcm.googleapis.com/fcm/send");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {

            JSONObject root = new JSONObject();
            JSONObject notification = new JSONObject();
            notification.put("body", message);
            notification.put("title", title);
            notification.put("user", uid);
            notification.put("sound", "default");


            JSONObject data = new JSONObject();
            data.put("message", message);
            data.put("user", uid);
            root.put("notification", notification);
            root.put("data", data);
            root.put("to", "/topics/chats");


            prepareConnection(connection);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            DataOutputStream osw = new DataOutputStream(connection.getOutputStream());
            osw.write(root.toString().getBytes());
            osw.flush();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return true;
            }
            throw new Exception(String.format("Error->POST Metodo: %s, Code: %s", connection.getRequestMethod(), connection.getResponseCode()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            connection.disconnect();
        }
    }
}
