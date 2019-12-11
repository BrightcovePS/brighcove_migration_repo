import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class BrightCoveIngestImage {
    final static Logger logger = Logger.getLogger(BrightCoveIngestImage.class);
    public static void main(String args[]){

        HttpResponse<String> tokenresponse = null;
        Map<Post_update_accountid,IngestImage> updateMap = ReadInput1.getImageData();
        Set<Post_update_accountid> keySet = updateMap.keySet();
        //Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();
        String reference_id= null;
        int i=1;
        //Create a blank sheet
        XSSFSheet sheet = workbook.createSheet("Video Cloud Accounts");
        Map<String, Object[]> data = new TreeMap<String, Object[]>();

        data.put(String.valueOf(i), new Object[] {"Account_id", "reference_id", "image_request","Response"});
        for (Post_update_accountid post_update_accountid: keySet ) {
            i++;

            final String image_request = updateMap.get(post_update_accountid).getImage_request();
            final String video_id = updateMap.get(post_update_accountid).getVideo_id();
            reference_id = updateMap.get(post_update_accountid).getReference_id();
            logger.debug("post_request :: "+ image_request
                    +" and video_id ::"+ video_id +" and reference_id ::"+reference_id);
            //JSONObject requestJsonObject = new JSONObject(post_request);



            try {
                tokenresponse = Unirest.post("https://oauth.brightcove.com/v4/access_token?grant_type=client_credentials")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("Authorization", "Basic YTcwNmI0OWMtYmUzZC00MmE2LTk4NmEtYjIwYTdmOWU1MTUzOkVIMUNBd04tMjJhRFVka3JQcWJCUV9fSzdkbEpTdko4UTNUNjZCMFV2TnVJUF9EX2U2S0RmOXRoTTdlOWJfbWxreUx3a2ZHaGxla015czFEYkZybGlB")
                        .header("Cache-Control", "no-cache")
                        .header("Postman-Token", "12ec436a-a9ee-146c-1189-559f4b9f6ea4")
                        .asString();
            } catch (UnirestException e) {
                e.printStackTrace();
                logger.error("Could not retrieve Token"+ e.getMessage());
            }
            JSONObject jsonObject = new JSONObject(tokenresponse.getBody());
            String token = jsonObject.getString("access_token");
            logger.debug("Access_token "+token);
            HttpResponse<String> response = null;

            String error_code = "";
            try {
                logger.debug("Post Request " + image_request);
                response = Unirest.post("https://ingest.api.brightcove.com/v1/accounts/"+post_update_accountid.getAccount_id()+"/videos/"+video_id+"/ingest-requests")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .header("Cache-Control", "no-cache")
                        .header("Postman-Token", "cd87b9a9-4ca3-5cf8-f10a-8f3fdbea574b")
                        .body(image_request)
                        .asString();
                if (response != null) {
                    logger.debug("Post response ::" + response.getBody());
//                    JSONObject responseObject = new JSONObject(response.getBody());
//                    error_code = responseObject.getString("error_code");
                }


                data.put(String.valueOf(i), new Object[]{post_update_accountid.getAccount_id(), reference_id, image_request, (response !=null ? response.getBody():"") });

                //data.put(String.valueOf(i), new Object[] {post_update_accountid.getAccount_id(), reference_id,"","","","","","",add_mp4_request,mp4response});


                //This data needs to be written (Object[])



            } catch (UnirestException e) {
                logger.debug("Failed Performing patch update for accountid :: "+ post_update_accountid.getAccount_id()
                        +" and video_id ::"+ reference_id);
                data.put(String.valueOf(i), new Object[]{post_update_accountid.getAccount_id(), reference_id, image_request, (response !=null ? response.getBody():"") });

               // data.put(String.valueOf(i), new Object[] {post_update_accountid.getAccount_id(), reference_id,"","","","","","",add_mp4_request,mp4response});
                writeLogs(sheet, data);
                e.printStackTrace();

            }

        }
        writeLogs(sheet, data);
        try
        {
            //Write the workbook in file system
            FileOutputStream out = new FileOutputStream(new File("D:\\brightcovescript\\1AFL_image_data_logs.xlsx"));
            workbook.write(out);
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally{
            try {
            FileOutputStream out = new FileOutputStream(new File("D:\\brightcovescript\\2AFL_image_data_logs.xlsx"));

                workbook.write(out);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private static void writeLogs(XSSFSheet sheet, Map<String, Object[]> data) {
        //Iterate over data and write to sheet
        Set<String> keyset = data.keySet();
        int rownum = 0;
        for (String key : keyset)
        {
            Row row = sheet.createRow(rownum++);
            Object [] objArr = data.get(key);
            int cellnum = 0;
            for (Object obj : objArr)
            {
                Cell cell = row.createCell(cellnum++);
                if(obj instanceof String)
                    cell.setCellValue((String)obj);
                else if(obj instanceof Integer)
                    cell.setCellValue((Integer)obj);
            }
        }
    }
}
