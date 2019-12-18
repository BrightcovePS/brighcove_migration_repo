import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
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

public class BrightcoveAssetNestedMetadataBulkExport {

	static final Logger logger = Logger.getLogger(BrightcoveAssetNestedMetadataBulkExport.class);
	static int offset = 0;
	static int expiry = 0;
	static long tokenExpiry = 0;
	static int limit = 100;
	static String token = "";
	static int rownum = 0;
	static String accountId = "";//Update your accountId here
	static String authString = "Basic ";//Update the authorization String here

	static final String AUTHORIZATION = "Authorization";

	public static void main(String[] args) {

		XSSFWorkbook workbook = new XSSFWorkbook();
		int index = 1;
		// Create a blank sheet
		XSSFSheet sheet = workbook.createSheet("BulkExport");
		Map<String, String[]> data = new TreeMap<>();

		try {
			token = getTokenWithExpiry();
						
			HttpResponse<String> totalCountResponse = Unirest
					.get("https://cms.api.brightcove.com/v1/accounts/"+accountId+"/counts/videos")
					.header(AUTHORIZATION, "Bearer " + token).asString();

			JSONObject totalCountResponseJson = new JSONObject(totalCountResponse.getBody());
			Integer count = totalCountResponseJson.getInt("count");
			logger.debug("Total no: of videos in account: " + count);
			
			while (offset < count) {
				if (Instant.now().toEpochMilli() < tokenExpiry) {

					HttpResponse<String> videosListResponse = Unirest
							.get("https://cms.api.brightcove.com/v1/accounts/"+accountId+"/videos?limit=" + limit + "&"
									+ "offset=" + offset)
							.header(AUTHORIZATION, "Bearer " + token).asString();
					
					logger.info("response: " + new JSONArray(videosListResponse.getBody()));

					JSONArray videos = new JSONArray(videosListResponse.getBody());
					
					
					for (int i = 0; i < videos.length(); i++) {

						JSONObject json = new JSONObject();
						
						json.put("videoId", videos.getJSONObject(i).get("id"));
						json.put("referenceId", videos.getJSONObject(i).get("reference_id"));
						json.put("title", videos.getJSONObject(i).get("name"));
						JSONObject json1 =(JSONObject) videos.getJSONObject(i).get("custom_fields");
						
						if(json1.has("supplier_id")) {
							json.put("supplier_id", json1.get("supplier_id"));
						}
						else {
							json.put("supplier_id","null");
						}
						if(json1.has("reserve_id")) {
							json.put("reserve_id", json1.get("reserve_id"));
						}
						else {
							json.put("reserve_id","null");
						}
						if(json1.has("part_number")) {
							json.put("part_number", json1.get("part_number"));
						}
						else {
							json.put("part_number","null");
						}
						

						String refId = videos.getJSONObject(i).get("reference_id").toString() != "null"
								? videos.getJSONObject(i).get("reference_id").toString()
								: "";
						String videoId = videos.getJSONObject(i).get("id").toString();
						
						String title = videos.getJSONObject(i).get("name").toString();
						
						String supplier_id= json.get("supplier_id").toString();
						
						String reserve_id= json.get("reserve_id").toString();
						
						String part_number= json.get("part_number").toString();
						
						

						data.put(String.valueOf(index), new String[] { title, videoId ,reserve_id ,part_number,supplier_id });

						index++;

					}

					offset += limit;
					logger.info("offset value now: " + offset);
				} else {
					token = getTokenWithExpiry();
				}

			}
			writeData(sheet, data);

			try {
				// Write the workbook in file system
				FileOutputStream out = new FileOutputStream(new File(
						"/home/ntedev/Downloads/brighcove_migration_repo-master/src/main/resources/metadataexport.xlsx"));
				workbook.write(out);
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (UnirestException e) {
			e.printStackTrace();
			logger.error("Could not retrieve Token" + e.getMessage());
		}

	}

	public static String getTokenWithExpiry() {
		HttpResponse<String> tokenresponse;
		String token = "";
		try {
			tokenresponse = Unirest.post("https://oauth.brightcove.com/v4/access_token?grant_type=client_credentials")
					.header("Content-Type", "application/x-www-form-urlencoded")
					.header(AUTHORIZATION,
							authString)
					.header("Cache-Control", "no-cache").asString();

			JSONObject jsonObject = new JSONObject(tokenresponse.getBody());
			token = jsonObject.getString("access_token");
			logger.debug("Access_token " + token);

			expiry = jsonObject.getInt("expires_in");
			tokenExpiry = Instant.now().toEpochMilli() + (expiry-60) * 1000;

		} catch (UnirestException e) {
			e.printStackTrace();
			logger.error(e.getMessage()); //1574672051846
		}
		return token;
	}

	private static void writeData(XSSFSheet sheet, Map<String, String[]> data) {
		// Iterate over data and write to sheet
		Set<String> keyset = data.keySet();

		for (String key : keyset) {
			Row row = sheet.createRow(rownum++);
			Object[] objArr = data.get(key);
			int cellnum = 0;
			for (Object obj : objArr) {
				Cell cell = row.createCell(cellnum++);
				if (obj instanceof String)
					cell.setCellValue((String) obj);
				else if (obj instanceof Integer)
					cell.setCellValue((Integer) obj);
			}
		}
	}
}
