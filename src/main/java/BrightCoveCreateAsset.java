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

public class BrightCoveCreateAsset {
    final static Logger logger = Logger.getLogger(BrightCoveCreateAsset.class);
    public static void main(String args[]){

        HttpResponse<String> tokenresponse = null;
        Map<Post_update_accountid,CreateRemoteVideo> updateMap = ReadInput1.getPostData();
        Set<Post_update_accountid> keySet = updateMap.keySet();
        //Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();
        String reference_id= null;
        int i=1;
        //Create a blank sheet
        XSSFSheet sheet = workbook.createSheet("Video Cloud Accounts");
        Map<String, Object[]> data = new TreeMap<String, Object[]>();

        data.put(String.valueOf(i), new Object[] {"Account_id", "reference_id", "post_request","Response","HLS_request","HLSResponse","folder_id","folder_reponse","mp4_request","mp4_response"});
        for (Post_update_accountid post_update_accountid: keySet ) {
            i++;

            final String post_request = updateMap.get(post_update_accountid).getPost_request();
            final String hls_request = updateMap.get(post_update_accountid).getHls_request();
            final String add_mp4_request = updateMap.get(post_update_accountid).getAdd_mp4_request();
            final String folder_id = updateMap.get(post_update_accountid).getFolder_id();
            reference_id = updateMap.get(post_update_accountid).getReference_id();
            logger.debug("post_request :: "+ post_request
                    +" and hls_request ::"+ hls_request +" and add_mp4_request ::"+" and folder_id ::"+folder_id);
            //JSONObject requestJsonObject = new JSONObject(post_request);

            logger.debug("Performing patch update for accountid :: "+ post_update_accountid.getAccount_id()
                    +" and video_id ::"+ reference_id);

            try {
                tokenresponse = Unirest.post("https://oauth.brightcove.com/v4/access_token?grant_type=client_credentials")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("Authorization", "Basic ZWVlMWVjOGMtYThhNy00ODRhLThjYzMtMDE3YTRjMmRmZGY0OkpaZ3BZUTNkRzF3UDRScEhlbVVBczZWVHdSSWVoY2hDRk0yYXVzNjFDRE9GSW5xVGhWZUR6Rk9xdkFMRW9Mc1JXWGRsMGt3RFVIbEpKbFJOX1BLY2N3")
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
            HttpResponse<String> hlsResponse = null;
            HttpResponse<String> mp4response = null;
            HttpResponse<String> folderResponse = null;
            try {
                logger.debug("Post Request "+post_request);
                response = Unirest.post("https://cms.api.brightcove.com/v1/accounts/"+post_update_accountid.getAccount_id()+"/videos")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .header("Cache-Control", "no-cache")
                        .header("Postman-Token", "cd87b9a9-4ca3-5cf8-f10a-8f3fdbea574b")
                        .body(post_request)
                        .asString();
                logger.debug("Post response ::"+ response.getBody());

                hlsResponse = Unirest.post("https://cms.api.brightcove.com/v1/accounts/"+post_update_accountid.getAccount_id()+"/videos/"+reference_id+"/assets/hls_manifest")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .header("Cache-Control", "no-cache")
                        .header("Postman-Token", "ef76e189-14a3-9769-3d46-6181f27d350a")
                        .body(hls_request)
                        .asString();

                mp4response = Unirest.post("https://cms.api.brightcove.com/v1/accounts/"+post_update_accountid.getAccount_id()+"/videos/"+reference_id+"/assets/renditions")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .header("Cache-Control", "no-cache")
                        .header("Postman-Token", "ccdf29a3-2fad-708e-87f8-094f61952b81")
                        .body(add_mp4_request)
                        .asString();

                folderResponse = Unirest.put("https://cms.api.brightcove.com/v1/accounts/"+post_update_accountid.getAccount_id()+"/folders/"+folder_id+"/videos/"+reference_id)
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .header("Cache-Control", "no-cache")
                        .header("Postman-Token", "78f37173-8933-7307-0e13-af5af7e85c5e")
                        .asString();

                data.put(String.valueOf(i), new Object[]{post_update_accountid.getAccount_id(), reference_id, post_request, (response !=null ? response.getBody():""), hls_request, (hlsResponse !=null ? hlsResponse.getBody():""), folder_id, (folderResponse !=null ? folderResponse.getBody():""), add_mp4_request,(mp4response !=null ? mp4response.getBody():"") });

                //data.put(String.valueOf(i), new Object[] {post_update_accountid.getAccount_id(), reference_id,"","","","","","",add_mp4_request,mp4response});


                //This data needs to be written (Object[])
                writeLogs(sheet, data);


            } catch (UnirestException e) {
                logger.debug("Failed Performing patch update for accountid :: "+ post_update_accountid.getAccount_id()
                        +" and video_id ::"+ reference_id);
                data.put(String.valueOf(i), new Object[]{post_update_accountid.getAccount_id(), reference_id, post_request, (response !=null ? response.getBody():""), hls_request, (hlsResponse !=null ? hlsResponse.getBody():""), folder_id, (folderResponse !=null ? folderResponse.getBody():""), add_mp4_request,(mp4response !=null ? mp4response.getBody():"") });

               // data.put(String.valueOf(i), new Object[] {post_update_accountid.getAccount_id(), reference_id,"","","","","","",add_mp4_request,mp4response});
                writeLogs(sheet, data);
                e.printStackTrace();

            }

        }

        try
        {
            //Write the workbook in file system
            FileOutputStream out = new FileOutputStream(new File("D:\\brightcovescript\\outputlog_createasset1Batch7_50001_52022.xlsx"));
            workbook.write(out);
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally{
            try {
            FileOutputStream out = new FileOutputStream(new File("D:\\brightcovescript\\outputlog_createasset2Batch7_50001_52022.xlsx"));

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
