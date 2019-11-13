import java.io.Serializable;

public class Patch_update implements Serializable{

    public String getVideo_id() {
        return video_id;
    }

    public void setVideo_id(String video_id) {
        this.video_id = video_id;
    }

    public String video_id;
    public String patch_request;

    public String getPatch_request() {
        return patch_request;
    }

    public void setPatch_request(String patch_request) {
        this.patch_request = patch_request;
    }


}
