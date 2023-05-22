package ton.coin.wallet

import org.koin.dsl.module
import ton.coin.wallet.common.async.AppDispatchers
import ton.coin.wallet.repository.TonLocalDataSource
import ton.coin.wallet.repository.TonRemoteDataSource
import ton.coin.wallet.repository.TonRepository

val appModule = module {
    single { AppDispatchers() }

    single { TonLocalDataSource(get(), get()) }

    single { TonRemoteDataSource(get()) }

    single { TonRepository(dispatchers = get(), localDs = get(), remoteDs = get()) }
}