package ton.coin.wallet.feature.send.scanqr

import org.koin.core.component.inject
import ton.coin.wallet.common.lifecycle.ConductorViewModel
import ton.coin.wallet.repository.TonRepository

class ScanQrViewModel : ConductorViewModel() {
    private val tonRepository: TonRepository by inject()
}