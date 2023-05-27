package ton.coin.wallet.cache

import android.app.Activity
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ton.coin.wallet.TonApplication
import ton.coin.wallet.data.Account

object WalletCache {

    private val prefs: SharedPreferences by lazy {
        TonApplication.instance.getSharedPreferences(
            "wallet.cache", Activity.MODE_PRIVATE
        )
    }

    suspend fun load(): Account? = withContext(Dispatchers.IO) {
        val mnemonic = prefs.getString("mnemonic", null)
        if (mnemonic == null) {
            null
        } else {
            Account(mnemonic.split(","))
        }
    }

    suspend fun save(account: Account) = withContext(Dispatchers.IO) {
        prefs.edit {
            putString("mnemonic", account.mnemonic.joinToString(separator = ","))
        }
    }
}