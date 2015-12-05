package evinas.talk.couchbase.shoppinglist.ws;


import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface PixabayService {
   
    @GET("?key=1735445-31c003fa54cf135125056d1e9&per_page=3&image_type=illustration")
    Observable<PixabayResponse> searchImage(@Query("q") String subject);
}
