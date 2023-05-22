package ton.coin.wallet.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.ton.mnemonic.Mnemonic
import ton.coin.wallet.common.async.AppDispatchers
import ton.coin.wallet.data.DraftTransaction
import ton.coin.wallet.data.TonCoins
import ton.coin.wallet.data.WalletMnemonic
import ton.coin.wallet.data.WalletVersion
import java.math.BigInteger

class TonLocalDataSource(private val context: Context, private val dispatchers: AppDispatchers) {

    private var draftTransaction: DraftTransaction? = null
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wallet.cache")
    private val KEY_MNEMONIC = stringPreferencesKey("mnemonic")
    private val KEY_WALLET_VERSION = stringPreferencesKey("walletVersion")
    private val KEY_WALLET_ADDRESS = stringPreferencesKey("walletAddress")
    private val KEY_WALLET_BALANCE = stringPreferencesKey("walletBalance")

    suspend fun createWallet(): Result<WalletMnemonic> {
        return try {
            Result.success(WalletMnemonic(Mnemonic.generate(wordCount = 24)))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun cleanData() = withContext(dispatchers.io) {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun saveWalletMnemonic(mnemonic: WalletMnemonic) = withContext(dispatchers.io) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MNEMONIC] = mnemonic.mnemonic.joinToString(separator = ",")
        }
    }

    suspend fun getWalletMnemonic(): Result<WalletMnemonic?> = withContext(dispatchers.io) {
        val mnemonic =
            context.dataStore.data.map { prefs -> prefs[KEY_MNEMONIC]?.split(",") }.first()
        Result.success(if (mnemonic == null) null else WalletMnemonic(mnemonic))
    }

    suspend fun getWalletAddress(): Result<String?> = withContext(dispatchers.io) {
        Result.success(
            context.dataStore.data.map { prefs -> prefs[KEY_WALLET_ADDRESS] }.firstOrNull()
        )
    }

    suspend fun saveWalletAddress(address: String?) = withContext(dispatchers.io) {
        context.dataStore.edit { prefs ->
            if (address == null) {
                prefs.remove(KEY_WALLET_ADDRESS)
            } else {
                prefs[KEY_WALLET_ADDRESS] = address
            }
        }
    }

    suspend fun getWalletBalance(): Result<TonCoins> = withContext(dispatchers.io) {
        val balanceValue =
            context.dataStore.data.map { prefs -> prefs[KEY_WALLET_BALANCE] }.firstOrNull() ?: "0"
        Result.success(TonCoins(BigInteger(balanceValue)))
    }

    suspend fun saveWalletBalance(balance: TonCoins) = withContext(dispatchers.io) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WALLET_BALANCE] = balance.value.toString()
        }
    }

    suspend fun getWalletVersion(): Result<WalletVersion> = withContext(dispatchers.io) {
        val walletVersion =
            context.dataStore.data.map { prefs -> prefs[KEY_WALLET_VERSION] }.firstOrNull()
        Result.success(WalletVersion.valueOfOrDefault(walletVersion))
    }

    suspend fun saveWalletVersion(version: WalletVersion) = withContext(dispatchers.io) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WALLET_VERSION] = version.name
        }
    }

    suspend fun getDraftTransaction(): Result<DraftTransaction?> = withContext(dispatchers.io) {
        Result.success(draftTransaction)
    }

    suspend fun saveDraftTransaction(draft: DraftTransaction?) = withContext(dispatchers.io) {
        draftTransaction = draft
    }
}
