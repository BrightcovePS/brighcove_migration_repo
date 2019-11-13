import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BrightCoveGetByReferenceId {
    final static Logger logger = Logger.getLogger(BrightCoveGetByReferenceId.class);
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
                        .header("Authorization", "Basic YTcwNmI0OWMtYmUzZC00MmE2LTk4NmEtYjIwYTdmOWU1MTUzOkVIMUNBd04tMjJhRFVka3JQcWJCUV9fSzdkbEpTdko4UTNUNjZCMFV2TnVJUF9EX2U2S0RmOXRoTTdlOWJfbWxreUx3a2ZHaGxla015czFEYkZybGlB")
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
                HttpResponse<String> response = Unirest.get("https://cms.api.brightcove.com/v1/accounts/6057984922001/videos/"+account_id+"/assets/")
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

        CSVWriter.writeToFile(csvStringToWrite, "D:\\brightcovescript\\video_cloud_reference_data.csv");
    }
}
