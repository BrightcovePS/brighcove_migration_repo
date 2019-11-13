import java.io.Serializable;

public class CreateRemoteVideo implements Serializable{



    public String post_request;
    public String folder_id;
    public String reference_id;

    public String getReference_id() {
        return reference_id;
    }

    public void setReference_id(String reference_id) {
        this.reference_id = reference_id;
    }

    public String add_mp4_request;

    public String getAdd_mp4_request() {
        return add_mp4_request;
    }

    public void setAdd_mp4_request(String add_mp4_request) {
        this.add_mp4_request = add_mp4_request;
    }

    public String getFolder_id() {
        return folder_id;
    }

    public void setFolder_id(String folder_id) {
        this.folder_id = folder_id;
    }

    public String getHls_request() {
        return hls_request;
    }

    public void setHls_request(String hls_request) {
        this.hls_request = hls_request;
    }

    public String hls_request;

    public String getPost_request() {
        return post_request;
    }

    public void setPost_request(String post_request) {
        this.post_request = post_request;
    }


}
