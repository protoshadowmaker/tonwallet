package ton.coin.wallet.controller

import retrofit2.http.GET
import retrofit2.http.Url

interface TonConfigService {
    @GET
    suspend fun loadConfig(@Url configUrl: String): String
}