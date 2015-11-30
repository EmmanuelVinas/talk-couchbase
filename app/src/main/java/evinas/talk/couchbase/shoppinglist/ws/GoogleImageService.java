package evinas.talk.couchbase.shoppinglist.ws;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface GoogleImageService {

    @GET("search/images?v=1.0&imgsz=small&rsz=1&as_filetype=png")
    Observable<GoogleImageResponse> searchImage(@Query("q") String subject);
}
