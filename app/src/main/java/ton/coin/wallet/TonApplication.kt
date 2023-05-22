package ton.coin.wallet

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.res.Configuration
import androidx.multidex.MultiDexApplication
import com.soywiz.klogger.DefaultLogOutput
import com.soywiz.klogger.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import ton.coin.wallet.common.ui.Density
import java.io.File

val logger = Logger(name = "TonWallet")

class TonApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        globalInit(this)
        localInit()
    }

    private fun localInit() {
        handleConfigurationChanged(resources.configuration)
        startKoin {
            androidContext(this@TonApplication)
            modules(appModule)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleConfigurationChanged(newConfig)
    }

    private fun handleConfigurationChanged(newConfig: Configuration) {
        Density.updateDisplaySize(this, newConfig)
    }

    companion object {
        private var appInstance: TonApplication? = null
        val instance: TonApplication get() = requireNotNull(appInstance)
        private fun globalInit(instance: TonApplication) {
            this.appInstance = instance
            logger.output = DefaultLogOutput
            logger.level = if (BuildConfig.DEBUG) {
                Logger.Level.DEBUG
            } else {
                Logger.Level.NONE
            }

        }

        fun getFilesDirFixed(): File {
            for (a in 0..9) {
                val path: File? = instance.applicationContext.filesDir
                if (path != null) {
                    return path
                }
            }
            try {
                val info: ApplicationInfo = instance.applicationContext.applicationInfo
                val path = File(info.dataDir, "files")
                path.mkdirs()
                return path
            } catch (e: Exception) {
                //FileLog.e(e)
            }
            return File("/data/data/org.telegram.messenger/files")
        }
    }
}