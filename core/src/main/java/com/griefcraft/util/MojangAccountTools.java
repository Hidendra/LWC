package com.griefcraft.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class MojangAccountTools {

    /**
     * The url to the server where names -> UUIDs are resolved at
     */
    private static final String UUID_RESOLVE_URL = "https://api.mojang.com/profiles/page/1";

    /**
     * The url to the server where UUIDs -> names are resolved at
     */
    private static final String NAME_RESOLVE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s";

    /**
     * Parser used to parse responses
     */
    private static final JSONParser jsonParser = new JSONParser();

    /**
     * Fetch the profile of the player with the given name. This call will block until it is fetched.
     *
     * @param name
     * @return the profile if found; null if it was either not found or lookup failed
     */
    public static MojangProfile fetchProfile(String name) {
        try {
            URL url = new URL(UUID_RESOLVE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Construct the post contents
            JSONObject data = new JSONObject();
            data.put("name", name);
            data.put("agent", "minecraft");

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(data.toJSONString().getBytes());
            outputStream.flush();
            outputStream.close();

            JSONObject response = (JSONObject) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
            JSONArray profiles = (JSONArray) response.get("profiles");

            if (profiles.size() == 0) {
                return null;
            }

            JSONObject profile = (JSONObject) profiles.get(0);
            UUID uuid = mojangToJavaUUID(profile.get("id").toString());
            String profileName = profile.get("name").toString();

            return new MojangProfile(uuid, profileName);
        } catch (IOException e) {
            return null;
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Fetch the profile of the player with the given UUID. This call will block until it is fetched.
     *
     * @param uuid
     * @return the profile if found; null if it was either not found or lookup failed
     */
    public static MojangProfile fetchProfile(UUID uuid) {
        try {
            URL url = new URL(String.format(NAME_RESOLVE_URL, uuid.toString().replace("-", "")));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(false);

            JSONObject profile = (JSONObject) jsonParser.parse(new InputStreamReader(connection.getInputStream()));

            String profileName = profile.get("name").toString();

            return new MojangProfile(uuid, profileName);
        } catch (IOException e) {
            return null;
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Convert a Mojang outputted UUID to a Java UUID
     *
     * @param id
     * @return
     */
    private static UUID mojangToJavaUUID(String id) {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
    }

}
