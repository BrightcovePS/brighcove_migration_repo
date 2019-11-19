import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class BrightcoveAssetMetadataBulkExport {

	static final Logger logger = Logger.getLogger(BrightcoveAssetMetadataBulkExport.class);
	static int offset = 0;
	static int expiry = 0;
	static long tokenExpiry = 0;
	static int limit = 3;
	static String token = "";

	static final String AUTHORIZATION = "Authorization";

	public static void main(String[] args) {

		List<JSONObject> jsonList = new ArrayList<>();
		
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
						
						jsonList.add(json);
					}

					offset += limit;
				} else {
					token = getToken();
				}

			}
			writeToCSV(jsonList);
			

		} catch (UnirestException e) {
			e.printStackTrace();
			logger.error("Could not retrieve Token" + e.getMessage());
		}

	}

	public static void writeToCSV(List<JSONObject> object) {
		JsonNode jsonTree;
		try {
			jsonTree = new ObjectMapper().readTree(object.toString());

			Builder csvSchemaBuilder = CsvSchema.builder();
			JsonNode firstObject = jsonTree.elements().next();

			firstObject.fieldNames().forEachRemaining(fieldName -> {
				csvSchemaBuilder.addColumn(fieldName);
			});
			CsvSchema csvSchema = csvSchemaBuilder.build().withHeader();

			CsvMapper csvMapper = new CsvMapper();
			csvMapper.writerFor(JsonNode.class).with(csvSchema)
					.writeValue(new File("src/main/resources/metadataexport.csv"), jsonTree);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	public static String getToken() {
		tokenExpiry = 0;
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

}
