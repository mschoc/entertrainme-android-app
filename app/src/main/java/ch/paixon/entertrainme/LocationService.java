package ch.paixon.entertrainme;


import ch.paixon.entertrainme.dtos.ConnectionContainerDto;
import ch.paixon.entertrainme.dtos.LocationContainerDto;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LocationService {

    @GET("locations")
    Call<LocationContainerDto> getLocations(@Query("query") String stationName,@Query("x") String x, @Query("y") String y, @Query("type") String type);

}
