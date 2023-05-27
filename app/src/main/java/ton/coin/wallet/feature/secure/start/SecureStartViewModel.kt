package ton.coin.wallet.feature.secure.start

import org.koin.core.component.inject
import ton.coin.wallet.common.lifecycle.ConductorViewModel
import ton.coin.wallet.repository.TonRepository

class SecureStartViewModel : ConductorViewModel() {

    private val tonRepository: TonRepository by inject()
}