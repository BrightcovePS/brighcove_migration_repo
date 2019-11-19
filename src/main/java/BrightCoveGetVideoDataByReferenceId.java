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
import java.util.*;

public class BrightCoveGetVideoDataByReferenceId {
    final static Logger logger = Logger.getLogger(BrightCoveGetVideoDataByReferenceId.class);
    public static void main(String args[]) throws IOException {

        HttpResponse<String> tokenresponse = null;
        List<Patch_update_accountid> accountidList = ReadInput1.getAccountData();
        XSSFWorkbook workbook = new XSSFWorkbook();
        String reference_id= null;
        int i=1;
        //Create a blank sheet
        XSSFSheet sheet = workbook.createSheet("Video Cloud Accounts");
        Map<String, Object[]> data = new TreeMap<String, Object[]>();
        HttpResponse<String> response = null;
        for (Patch_update_accountid patch_update_acc_id: accountidList ) {

            reference_id = patch_update_acc_id.getAccount_id();
            logger.debug("Getting details for reference id :: "+ reference_id);

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
                response = Unirest.get("https://cms.api.brightcove.com/v1/accounts/6057984922001/videos/"+reference_id)
                        .header("Authorization", "Bearer "+token)
                        .header("Cache-Control", "no-cache")
                        .header("Postman-Token", "9d83f44b-6097-1f61-48a9-b6214bfe97fb")
                        .asString();
                //logger.debug("Patch response ::"+ response.getBody());
                data.put(String.valueOf(i), new Object[]{ reference_id, (response !=null ? response.getBody():"")});
                i++;
               /* flatJson = JSONFlattener.parseJson(response.getBody());
                csvStringToWrite += CSVWriter.getCSVCustomised(flatJson,headerCounter);
                headerCounter++;*/
               // flatJsonList.add(flatJson);
                // Using the default separator ','

            } catch (UnirestException e) {
                data.put(String.valueOf(i), new Object[]{ reference_id, (response !=null ? response.getBody():"")});
                e.printStackTrace();
                logger.error("Failed Getting patch update for accountid :: "+ patch_update_acc_id.getAccount_id() +" :: "+ e.getMessage());
            }

        }

        writeLogs(sheet, data);
        try
        {
            //Write the workbook in file system
            FileOutputStream out = new FileOutputStream(new File("D:\\brightcovescript\\outputlog1_getdetails_10001_15000_final.xlsx"));
            workbook.write(out);
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally{
            try {
                FileOutputStream out = new FileOutputStream(new File("D:\\brightcovescript\\outputlog2_getdetails_10001_15000_final.xlsx"));

                workbook.write(out);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

       // CSVWriter.writeToFile(csvStringToWrite, "D:\\brightcovescript\\video_cloud_reference_data.csv");
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
