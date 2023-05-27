package ton.coin.wallet.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ton.mnemonic.Mnemonic
import ton.coin.wallet.common.async.AppDispatchers
import ton.coin.wallet.data.CompletedTransaction
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
    private val KEY_PIN_CODE = stringPreferencesKey("pinCode")
    private val KEY_PIN_CODE_SIZE = intPreferencesKey("pinCodeSize")
    private val KEY_BIOMETRIC_AUTH = booleanPreferencesKey("biometricAuth")

    private fun walletAddressKey(version: WalletVersion): Preferences.Key<String> {
        return stringPreferencesKey("walletAddress${version.name}")
    }

    private fun walletBalanceKey(version: WalletVersion): Preferences.Key<String> {
        return stringPreferencesKey("walletBalance${version.name}")
    }

    private fun walletTransactionsKey(version: WalletVersion): Preferences.Key<String> {
        return stringPreferencesKey("walletTransactions${version.name}")
    }

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

    suspend fun getWalletAddress(version: WalletVersion): Result<String?> =
        withContext(dispatchers.io) {
            Result.success(
                context.dataStore.data.map { prefs ->
                    prefs[walletAddressKey(version)]
                }.firstOrNull()
            )
        }

    suspend fun saveWalletAddress(address: String, version: WalletVersion) =
        withContext(dispatchers.io) {
            context.dataStore.edit { prefs ->
                prefs[walletAddressKey(version)] = address
            }
        }

    suspend fun getWalletBalance(
        version: WalletVersion
    ): Result<TonCoins> = withContext(dispatchers.io) {
        val balanceValue = context.dataStore.data.map { prefs ->
            prefs[walletBalanceKey(version)]
        }.firstOrNull() ?: "0"
        Result.success(TonCoins(BigInteger(balanceValue)))
    }

    suspend fun saveWalletBalance(
        balance: TonCoins, version: WalletVersion
    ) = withContext(dispatchers.io) {
        context.dataStore.edit { prefs ->
            prefs[walletBalanceKey(version)] = balance.value.toString()
        }
    }

    suspend fun getWalletVersion(): Result<WalletVersion> = withContext(dispatchers.io) {
        val walletVersion = context.dataStore.data.map { prefs ->
            prefs[KEY_WALLET_VERSION]
        }.firstOrNull()
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

    suspend fun getPinCode(): Result<String> = withContext(dispatchers.io) {
        val pinCode = context.dataStore.data.map { prefs ->
            prefs[KEY_PIN_CODE]
        }.firstOrNull() ?: ""
        Result.success(pinCode)
    }

    suspend fun savePinCode(pinCode: String) = withContext(dispatchers.io) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PIN_CODE] = pinCode
        }
    }

    suspend fun getPinCodeSize(): Result<Int> = withContext(dispatchers.io) {
        val pinCodeSize = context.dataStore.data.map { prefs ->
            prefs[KEY_PIN_CODE_SIZE]
        }.firstOrNull() ?: 4
        Result.success(pinCodeSize)
    }

    suspend fun savePinCodeSize(pinCodeSize: Int) = withContext(dispatchers.io) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PIN_CODE_SIZE] = pinCodeSize
        }
    }

    suspend fun getBiometricAuth(): Result<Boolean> = withContext(dispatchers.io) {
        val biometricAuth = context.dataStore.data.map { prefs ->
            prefs[KEY_BIOMETRIC_AUTH]
        }.firstOrNull() ?: false
        Result.success(biometricAuth)
    }

    suspend fun saveBiometricAuth(biometricAuth: Boolean) = withContext(dispatchers.io) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BIOMETRIC_AUTH] = biometricAuth
        }
    }

    suspend fun saveTransactions(
        transactions: List<CompletedTransaction>, walletVersion: WalletVersion
    ) = withContext(dispatchers.io) {
        try {
            context.dataStore.edit { prefs ->
                prefs[walletTransactionsKey(walletVersion)] = Json.encodeToString(transactions)
            }
        } catch (ignored: Throwable) {
        }
    }

    suspend fun getTransactions(
        walletVersion: WalletVersion
    ): Result<List<CompletedTransaction>> = withContext(dispatchers.io) {
        try {
            val transactions = context.dataStore.data.map { prefs ->
                prefs[walletTransactionsKey(walletVersion)]
            }.firstOrNull() ?: "[]"
            Result.success(
                Json.decodeFromString<Array<CompletedTransaction>>(transactions).toList()
            )
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.success(emptyList())
        }
    }
}
