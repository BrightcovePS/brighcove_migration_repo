import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BrightCoveGetVideoData {
    final static Logger logger = Logger.getLogger(BrightCoveGetVideoData.class);
    public static void main(String args[]) throws IOException {

        HttpResponse<String> tokenresponse = null;
        List<Patch_update_accountid> accountidList = ReadInput1.getAccountData();
        List<Map<String, String>> flatJson = null;
        List<List<Map<String, String>>> flatJsonList = new ArrayList<>();
        String csvStringToWrite = null;
        int headerCounter = 0;
        for (Patch_update_accountid patch_update_acc_id: accountidList ) {
            String account_id = patch_update_acc_id.getAccount_id();
            logger.debug("Performing patch update for accountid :: "+ account_id);

            try {
                tokenresponse = Unirest.post("https://oauth.brightcove.com/v4/access_token?grant_type=client_credentials")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("Authorization", "Basic ZWVlMWVjOGMtYThhNy00ODRhLThjYzMtMDE3YTRjMmRmZGY0OkpaZ3BZUTNkRzF3UDRScEhlbVVBczZWVHdSSWVoY2hDRk0yYXVzNjFDRE9GSW5xVGhWZUR6Rk9xdkFMRW9Mc1JXWGRsMGt3RFVIbEpKbFJOX1BLY2N3")
                        .header("Cache-Control", "no-cache")
                        .header("Postman-Token", "d79c9bee-f743-7fda-445f-8ae4d0267d1e")
                        .asString();
            } catch (UnirestException e) {
                e.printStackTrace();
                logger.error("Could not retrieve Token"+ e.getMessage());
            }
            JSONObject jsonObject = new JSONObject(tokenresponse.getBody());
            String token = jsonObject.getString("access_token");
            logger.debug("Access_token "+token);

            try {
                HttpResponse<String> response = Unirest.get("https://cms.api.brightcove.com/v1/accounts/"+account_id+"/videos?limit=5")
                        .header("Authorization", "Bearer "+token)
                        .header("Cache-Control", "no-cache")
                        .header("Postman-Token", "9d83f44b-6097-1f61-48a9-b6214bfe97fb")
                        .asString();
                logger.debug("Patch response ::"+ response.getBody());

                flatJson = JSONFlattener.parseJson(response.getBody());
                csvStringToWrite += CSVWriter.getCSVCustomised(flatJson,headerCounter);
                headerCounter++;
               // flatJsonList.add(flatJson);
                // Using the default separator ','
            } catch (UnirestException e) {
                e.printStackTrace();
                logger.error("Failed Getting patch update for accountid :: "+ patch_update_acc_id.getAccount_id() +" :: "+ e.getMessage());
            }

        }

        CSVWriter.writeToFile(csvStringToWrite, "D:\\brightcovescript\\video_cloud_data.csv");
    }
}
