package evinas.talk.couchbase.shoppinglist.ws;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GoogleImageResponse {

    @JsonProperty("responseData")
    private ResponseData responseData;
    @JsonProperty("responseDetails")
    private Object responseDetails;
    @JsonProperty("responseStatus")
    private Integer responseStatus;

    public ResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(ResponseData responseData) {
        this.responseData = responseData;
    }

    public Object getResponseDetails() {
        return responseDetails;
    }

    public void setResponseDetails(Object responseDetails) {
        this.responseDetails = responseDetails;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }
}
