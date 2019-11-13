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

public class BrightCoveMetadataPatch {
    final static Logger logger = Logger.getLogger(BrightCoveMetadataPatch.class);
    public static void main(String args[]){

        HttpResponse<String> tokenresponse = null;
        Map<Patch_update_accountid,Patch_update> updateMap = ReadInput1.getData();
        Set<Patch_update_accountid> keySet = updateMap.keySet();
        //Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();
        int i=1;
        //Create a blank sheet
        XSSFSheet sheet = workbook.createSheet("Video Cloud Accounts");
        Map<String, Object[]> data = new TreeMap<String, Object[]>();

        data.put(String.valueOf(i), new Object[] {"Account_id", "Video_id", "patch_request","Response"});
        for (Patch_update_accountid patch_update_acc_id: keySet ) {
            i++;
            final String video_id = updateMap.get(patch_update_acc_id).getVideo_id();
            final String patch_request = updateMap.get(patch_update_acc_id).getPatch_request();
            logger.debug("Performing patch update for accountid :: "+ patch_update_acc_id.getAccount_id()
                    +" and video_id ::"+ video_id);

            try {
                tokenresponse = Unirest.post("https://oauth.brightcove.com/v4/access_token?grant_type=client_credentials")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("Authorization", "Basic NTc5YmNjMDgtOGViMC00ZDcxLWJlZGMtMzc3NjMyNjMwN2MyOkdJbFVBODV4cUpUaUV2bUNpU1lsNWIzYWFmRm9odFY3THJLT2V1SGl3d3c4SUp2eGItSTlvdzVNUXo3Z0hncE1lVFFxak5VeHRoZGJKMzBPZnNKRWJ3")
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
            HttpResponse<String> response = null;
            try {
                logger.debug("Metadata Request "+patch_request);
                response = Unirest.patch("https://cms.api.brightcove.com/v1/accounts/"+patch_update_acc_id.getAccount_id()+"/videos/"+video_id)
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .header("Cache-Control", "no-cache")
                        .header("Postman-Token", "cd87b9a9-4ca3-5cf8-f10a-8f3fdbea574b")
                        .body(patch_request)
                        .asString();
                logger.debug("Patch response ::"+ response.getBody());
                data.put(String.valueOf(i), new Object[] {patch_update_acc_id.getAccount_id(), video_id, patch_request,response.getBody()});


                //This data needs to be written (Object[])



                //Iterate over data and write to sheet
                writeLogs(sheet, data);



            } catch (UnirestException e) {
                data.put(String.valueOf(i), new Object[] {patch_update_acc_id.getAccount_id(), video_id, patch_request,response.getBody()});
                writeLogs(sheet, data);
                e.printStackTrace();
                logger.debug("Failed Performing patch update for accountid :: "+ patch_update_acc_id.getAccount_id()
                        +" and video_id ::"+ video_id);
            }

        }

        try
        {
            //Write the workbook in file system
            FileOutputStream out = new FileOutputStream(new File("D:\\brightcovescript\\outputlog.xlsx"));
            workbook.write(out);
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void writeLogs(XSSFSheet sheet, Map<String, Object[]> data) {
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
