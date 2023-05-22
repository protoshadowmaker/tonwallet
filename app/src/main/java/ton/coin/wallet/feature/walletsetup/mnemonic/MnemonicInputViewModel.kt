package ton.coin.wallet.feature.walletsetup.mnemonic

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.ton.mnemonic.Mnemonic
import ton.coin.wallet.common.lifecycle.ConductorViewModel

abstract class MnemonicInputViewModel : ConductorViewModel() {
    fun searchMnemonic(query: String) = flow {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) {
            emit(emptyList())
        } else {
            emit(Mnemonic.mnemonicWords().filter { it.startsWith(normalizedQuery) })
        }
    }.flowOn(dispatchers.default)
}