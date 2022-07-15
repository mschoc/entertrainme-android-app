package ch.paixon.entertrainme;


import ch.paixon.entertrainme.dtos.ConnectionContainerDto;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ConnectionService {

    @GET("connections")
    Call<ConnectionContainerDto> searchConnections(@Query("from") String origin, @Query("to") String destination, @Query("transportations") String transportations, @Query("date") String date , @Query("time") String time, @Query("isArrivalTime") String isArrivalTime, @Query("limit") String limitNumberOfConnections);

}
