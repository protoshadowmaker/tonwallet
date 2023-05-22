package ton.coin.wallet.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.ton.lite.client.LiteClient
import ton.coin.wallet.common.async.AppDispatchers

class TonController(private val dispatchers: AppDispatchers) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(dispatchers.main + job)

    private var _client: LiteClient? = null
    private val client: LiteClient get() = requireNotNull(_client)
}