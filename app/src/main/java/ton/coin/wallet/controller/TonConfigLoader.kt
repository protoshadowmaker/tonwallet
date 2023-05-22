package ton.coin.wallet.controller

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory


object TonConfigLoader {
    suspend fun loadConfig(): String {
        //https://ton.org/testnet-global.config.json
        //"https://ton.org/global-config.json"
        //"https://ton.org/global-config-wallet.json"
        val retrofit = Retrofit.Builder().baseUrl("https://ton.org/")
            .addConverterFactory(ScalarsConverterFactory.create()).build()
        val service: TonConfigService = retrofit.create(TonConfigService::class.java)
        return service.loadConfig("https://ton.org/global-config.json")
    }
}