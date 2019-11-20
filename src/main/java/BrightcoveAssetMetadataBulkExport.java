import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

public class BrightcoveAssetMetadataBulkExport {

	static final Logger logger = Logger.getLogger(BrightcoveAssetMetadataBulkExport.class);
	static int offset = 0;
	static int expiry = 0;
	static long tokenExpiry = 0;
	static int limit = 3;
	static String token = "";
	static int rownum = 0;

	static final String AUTHORIZATION = "Authorization";

	public static void main(String[] args) {

		XSSFWorkbook workbook = new XSSFWorkbook();
        int index=1;
        //Create a blank sheet
        XSSFSheet sheet = workbook.createSheet("BulkExport");
        Map<String, String[]> data = new TreeMap<String, String[]>();

		
		try {
			token = getToken();

			HttpResponse<String> totalCountResponse = Unirest
					.get("https://cms.api.brightcove.com/v1/accounts/6098995842001/counts/videos")
					.header(AUTHORIZATION, "Bearer " + token).asString();

			JSONObject totalCountResponseJson = new JSONObject(totalCountResponse.getBody());
			Integer count = totalCountResponseJson.getInt("count");
			logger.info("Total no: of videos in account: " + count);

			while (offset < count) {
				if (Instant.now().toEpochMilli() < tokenExpiry) {
					HttpResponse<String> videosListResponse = Unirest
							.get("https://cms.api.brightcove.com/v1/accounts/6098995842001/videos?limit=" + limit + "&"
									+ "offset=" + offset)
							.header(AUTHORIZATION, "Bearer " + token).asString();

					logger.info("response: " + new JSONArray(videosListResponse.getBody()));

					JSONArray videos = new JSONArray(videosListResponse.getBody());

					for (int i = 0; i < videos.length(); i++) {
						
		            
						JSONObject json = new JSONObject();
						json.put("videoId", videos.getJSONObject(i).get("id"));
						json.put("referenceId", videos.getJSONObject(i).get("reference_id"));
						
						String refId = videos.getJSONObject(i).get("reference_id").toString() !="null" ? videos.getJSONObject(i).get("reference_id").toString():"";
						String videoId = videos.getJSONObject(i).get("id").toString();

						data.put(String.valueOf(index), new String[] {videoId, refId});

						index++;
						
					}

					offset += limit;
				} else {
					token = getToken();
				}

			}
			writeData(sheet, data);
			
			try
	        {
	            //Write the workbook in file system
	            FileOutputStream out = new FileOutputStream(new File("/home/ntedev/Downloads/bulk_output.xlsx"));
	            workbook.write(out);
	            out.close();
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	        }

		} catch (UnirestException e) {
			e.printStackTrace();
			logger.error("Could not retrieve Token" + e.getMessage());
		}

	}


	public static String getToken() {
		HttpResponse<String> tokenresponse;
		String token = "";
		try {
			tokenresponse = Unirest.post("https://oauth.brightcove.com/v4/access_token?grant_type=client_credentials")
					.header("Content-Type", "application/x-www-form-urlencoded")
					.header(AUTHORIZATION,
							"Basic ZWVlMWVjOGMtYThhNy00ODRhLThjYzMtMDE3YTRjMmRmZGY0OkpaZ3BZUTNkRzF3UDRScEhlbVVBczZWVHdSSWVoY2hDRk0yYXVzNjFDRE9GSW5xVGhWZUR6Rk9xdkFMRW9Mc1JXWGRsMGt3RFVIbEpKbFJOX1BLY2N3")
					.header("Cache-Control", "no-cache").asString();

			JSONObject jsonObject = new JSONObject(tokenresponse.getBody());
			token = jsonObject.getString("access_token");
			logger.debug("Access_token " + token);

			expiry = jsonObject.getInt("expires_in");
			tokenExpiry = Instant.now().toEpochMilli() + expiry * 1000;

		} catch (UnirestException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return token;
	}

	private static void writeData(XSSFSheet sheet, Map<String, String[]> data) {
        //Iterate over data and write to sheet
        Set<String> keyset = data.keySet();
        
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