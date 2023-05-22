package ton.coin.wallet.feature.send

import kotlinx.coroutines.launch
import org.koin.core.component.inject
import ton.coin.wallet.common.lifecycle.ConductorViewModel
import ton.coin.wallet.repository.TonRepository

class SendViewModel : ConductorViewModel() {
    private val tonRepository: TonRepository by inject()

    fun cleanup() {
        cleanupScope.launch {
            tonRepository.cleanupDraftTransaction()
        }
    }
}