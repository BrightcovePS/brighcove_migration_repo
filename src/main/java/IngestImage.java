import java.io.Serializable;

public class IngestImage implements Serializable{



    public String image_request;
    public String video_id;
    public String reference_id;

    public String getReference_id() {
        return reference_id;
    }

    public void setReference_id(String reference_id) {
        this.reference_id = reference_id;
    }

    public String getVideo_id() {
        return video_id;
    }

    public void setVideo_id(String video_id) {
        this.video_id = video_id;
    }



    public String getImage_request() {
        return image_request;
    }

    public void setImage_request(String image_request) {
        this.image_request = image_request;
    }





}
