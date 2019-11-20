import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.*;

import static org.apache.poi.ss.usermodel.CellType.NUMERIC;

    public class ReadInput1
    {
        public static Map<Patch_update_accountid,Patch_update> getData()
        {
            Map<Patch_update_accountid,Patch_update> updateMap = new HashMap<Patch_update_accountid,Patch_update>();
            try
            {
                FileInputStream file = new FileInputStream(new File("/Users/hanna_joseph/migration_scripts/brighcove_migration_repo/patchtest.xlsx"));

                //Create Workbook instance holding reference to .xlsx file
                XSSFWorkbook workbook = new XSSFWorkbook(file);

                //Get first/desired sheet from the workbook
                XSSFSheet sheet = workbook.getSheetAt(0);


                //Iterate through each rows one by one
                Iterator<Row> rowIterator = sheet.iterator();
                while (rowIterator.hasNext())
                {
                    Row row = rowIterator.next();
                    //For each row, iterate through all the columns
                    Iterator<Cell> cellIterator = row.cellIterator();
                    String account_id="";
                    String video_id ="";
                    String patch_request= "";
                    Patch_update patch_update = null;
                    Patch_update_accountid  patch_update_accountid= null;
                    patch_update =new Patch_update();
                    patch_update_accountid = new Patch_update_accountid();
                    while (cellIterator.hasNext())
                    {
                        Cell cell = cellIterator.next();
                        //Check the cell type and format accordingly

                            if(cell.getColumnIndex()==0){
                                account_id = new BigDecimal((cell.getNumericCellValue())).toString();
                                patch_update_accountid.setAccount_id(account_id);
                            }
                        if(cell.getColumnIndex()==1){
                            //video_id = new BigDecimal((cell.getNumericCellValue())).toString();
                            video_id = String.valueOf(cell.getStringCellValue());
                            patch_update.setVideo_id(video_id);
                        }
                        if(cell.getColumnIndex()==2){
                            patch_request = String.valueOf(cell.getStringCellValue());
                            patch_update.setPatch_request(patch_request);
                        }

                    }
                    updateMap.put(patch_update_accountid,patch_update);
                    System.out.println("");

                }
                file.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return updateMap;
        }

        public static List<Patch_update_accountid> getAccountData()
        {
            List<Patch_update_accountid> accountidList = new ArrayList<Patch_update_accountid>();
            try
            {
                FileInputStream file = new FileInputStream(new File("/Users/hanna_joseph/migration_scripts/brighcove_migration_repo/get_data_input.xlsx"));

                //Create Workbook instance holding reference to .xlsx file
                XSSFWorkbook workbook = new XSSFWorkbook(file);

                //Get first/desired sheet from the workbook
                XSSFSheet sheet = workbook.getSheetAt(0);


                //Iterate through each rows one by one
                Iterator<Row> rowIterator = sheet.iterator();
                while (rowIterator.hasNext())
                {
                    Row row = rowIterator.next();
                    //For each row, iterate through all the columns
                    Iterator<Cell> cellIterator = row.cellIterator();
                    String account_id="";

                    Patch_update_accountid  patch_update_accountid= null;
                    patch_update_accountid = new Patch_update_accountid();
                    while (cellIterator.hasNext())
                    {
                        Cell cell = cellIterator.next();
                        //Check the cell type and format accordingly

                        if(cell.getColumnIndex()==0){
                            account_id = new BigDecimal((cell.getNumericCellValue())).toString();
                            //account_id = String.valueOf(cell.getStringCellValue());
                            patch_update_accountid.setAccount_id(account_id);
                        }


                    }
                    accountidList.add(patch_update_accountid);

                }
                file.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return accountidList;
        }
        
        public static String filterPostRequest (String postRequest) {
        	
        	int nameStartPos = postRequest.indexOf("\"name\": \"") + 9;
        	int nameEndPos = postRequest.indexOf("\",\"reference_id\"");        			
        	String name = postRequest.substring(nameStartPos, nameEndPos);
        	postRequest = postRequest.replace(name, name.replaceAll("[\",]", ""));
        	
        	int descStartPos = postRequest.indexOf("\"description\": \"") + 16;
        	int descEndPos = postRequest.indexOf("\",\"long_description\"");        			
        	String description = postRequest.substring(descStartPos, descEndPos);
        	String trimmedDesc = description.replaceAll("\",", "").substring(0, Math.min(description.replaceAll("[\",]", "").length(), 248));
        	postRequest = postRequest.replace(description, trimmedDesc);

        	int longDescStartPos = postRequest.indexOf("\"long_description\": \"") + 21;
        	int longDescEndPos = postRequest.indexOf("\",\"state\"");        			
        	String longDescription = postRequest.substring(longDescStartPos, longDescEndPos);
        	postRequest = postRequest.replace(longDescription, longDescription.replaceAll("[\",]", ""));
        	        	
        	return postRequest;
        }

        public static Map<Post_update_accountid,CreateRemoteVideo> getPostData()
        {
            Map<Post_update_accountid,CreateRemoteVideo> updateMap = new HashMap<Post_update_accountid,CreateRemoteVideo>();
            try
            {
                FileInputStream file = new FileInputStream(new File("/Users/hanna_joseph/migration_scripts/brighcove_migration_repo/video_cloud_createtest.xlsx"));


                //Create Workbook instance holding reference to .xlsx file
                XSSFWorkbook workbook = new XSSFWorkbook(file);

                //Get first/desired sheet from the workbook
                XSSFSheet sheet = workbook.getSheetAt(0);


                //Iterate through each rows one by one
                Iterator<Row> rowIterator = sheet.iterator();
                while (rowIterator.hasNext())
                {
                    Row row = rowIterator.next();
                    //For each row, iterate through all the columns
                    Iterator<Cell> cellIterator = row.cellIterator();
                    String account_id="";
                    String post_request= "";
                    String hls_request= "";
                    String add_mp4_request= "";
                    String folder_id= "";
                    String reference_id="";
                    CreateRemoteVideo createRemoteVideo = null;
                    Post_update_accountid  post_update_accountid= null;
                    createRemoteVideo =new CreateRemoteVideo();
                    post_update_accountid = new Post_update_accountid();
                    while (cellIterator.hasNext())
                    {
                        Cell cell = cellIterator.next();
                        //Check the cell type and format accordingly

                        if(cell.getColumnIndex()==0){
                            account_id = new BigDecimal((cell.getNumericCellValue())).toString();
                            post_update_accountid.setAccount_id(account_id);
                        }
                        if(cell.getColumnIndex()==1){
                            post_request = String.valueOf(cell.getStringCellValue());
                            
                            post_request = filterPostRequest(post_request);
                            System.out.println(post_request);
                            Map<String, String> map = new ObjectMapper().readValue(post_request, Map.class);
                            System.out.println(map.get("referece_id"));
//                            JSONObject val = new JSONObject(post_request);
//                            System.out.println(val.get("referece_id"));
//                            val.get("long_description");
//                            VideoData data = new ObjectMapper().readValue(post_request, VideoData.class);
//                            System.out.println(data.getName());
                            createRemoteVideo.setPost_request(post_request);
                        }
                        if(cell.getColumnIndex()==2){
                            hls_request = String.valueOf(cell.getStringCellValue());
                            createRemoteVideo.setHls_request(hls_request);
                        }
                        if(cell.getColumnIndex()==3){
                            folder_id = String.valueOf(cell.getStringCellValue());
                            createRemoteVideo.setFolder_id(folder_id);
                        }
                        if(cell.getColumnIndex()==4){
                            add_mp4_request = String.valueOf(cell.getStringCellValue());
                            createRemoteVideo.setAdd_mp4_request(add_mp4_request);
                        }
                        if(cell.getColumnIndex()==5){
                            reference_id = String.valueOf(cell.getStringCellValue());
                            createRemoteVideo.setReference_id(reference_id);
                        }

                    }
                    updateMap.put(post_update_accountid,createRemoteVideo);

                }
                file.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return updateMap;
        }

    }
