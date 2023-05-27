package ton.coin.wallet.feature.settings

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.coroutines.launch
import ton.coin.wallet.R
import ton.coin.wallet.common.lifecycle.ViewModelController
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.Theme
import ton.coin.wallet.common.ui.conductor.HorizontalFadeChangeFromHandler
import ton.coin.wallet.common.ui.conductor.TRANSITION_DURATION
import ton.coin.wallet.common.ui.conductor.VerticalFadeChangeFromHandler
import ton.coin.wallet.common.ui.custom.KeyValueButton
import ton.coin.wallet.common.ui.darkToolbar
import ton.coin.wallet.common.ui.dialog
import ton.coin.wallet.common.ui.divider
import ton.coin.wallet.common.ui.dp
import ton.coin.wallet.common.ui.settingsButton
import ton.coin.wallet.common.ui.settingsDangerousButton
import ton.coin.wallet.common.ui.settingsTitle
import ton.coin.wallet.common.ui.textButton
import ton.coin.wallet.data.WalletVersion
import ton.coin.wallet.feature.settings.mnemonic.ViewMnemonicController
import ton.coin.wallet.feature.welcome.WelcomeController

class SettingsController : ViewModelController() {

    private val viewModel: SettingsViewModel by lazy {
        SettingsViewModel().apply {
            attachViewModel(this)
        }
    }

    private var root: LinearLayout? = null
    private var scroll: ScrollView? = null
    private var alertDialog: AlertDialog? = null
    private var activeAddressButton: KeyValueButton? = null
    private var popup: PopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?
    ): View {
        val context = container.context
        val contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(settingsTitle(context).apply {
                setText(R.string.settings_section_general)
            })
            addView(KeyValueButton(context).apply {
                activeAddressButton = this
                titleTextView.setText(R.string.settings_active_address)
                valueTextView.setText(R.string.settings_address_v4r2)
                setOnClickListener { showWalletVersionPopup() }
            })
            addView(settingsTitle(context).apply {
                setText(R.string.settings_section_security)
            })
            addView(settingsButton(context).apply {
                setText(R.string.settings_action_show_recovery)
                setOnClickListener { onRecoveryClicked() }
            })
            addView(divider(context), LinearLayoutLpBuilder().wMatch().hDp(0.5).build())
            addView(settingsDangerousButton(context).apply {
                setText(R.string.settings_action_delete)
                setOnClickListener { onDeleteClicked() }
            })
        }
        val root = LinearLayout(inflater.context).apply {
            setBackgroundResource(R.color.black)
            orientation = LinearLayout.VERTICAL
            addView(darkToolbar(context).apply {
                setTitle(R.string.settings_title)
                navigationIcon = ContextCompat.getDrawable(context, R.drawable.ic_back_24).apply {
                    this?.setTint(ContextCompat.getColor(context, R.color.white))
                }
                setNavigationOnClickListener { onNavigationPressed() }
                elevation = 0f
            }, LinearLayoutLpBuilder().wMatch().build())
            addView(
                ScrollView(context).apply {
                    scroll = this
                    clipToPadding = false
                    setBackgroundResource(R.drawable.bg_rect_round_top_12dp)
                    addView(contentLayout)
                }, LinearLayoutLpBuilder().wMatch().hMatch().build()
            )
        }
        this.root = root
        return root
    }

    private fun onDeleteClicked() {
        val context = activity ?: return
        alertDialog?.dismiss()
        val actions = listOf(
            context.getString(R.string.settings_delete_cancel),
            context.getString(R.string.settings_delete_confirm)
        )
        val callbacks = mutableListOf<() -> Unit>().apply {
            add { alertDialog?.dismiss() }
            add {
                alertDialog?.dismiss()
                viewModel.logOut()
            }
        }
        alertDialog = AlertDialog.Builder(context, R.style.TonAlertDialog).setView(
            dialog(
                context,
                context.getString(R.string.settings_delete_title),
                context.getString(R.string.settings_delete_description),
                actions,
                callbacks,
                mapOf(context.getString(R.string.settings_delete_confirm) to Theme.DEFAULT.lightColors.textButtonDangerText)
            )
        ).create()
        alertDialog?.show()
    }

    private fun onRecoveryClicked() {
        router.pushController(
            RouterTransaction.with(ViewMnemonicController())
                .pushChangeHandler(VerticalFadeChangeFromHandler(TRANSITION_DURATION))
                .popChangeHandler(VerticalFadeChangeFromHandler(TRANSITION_DURATION))
        )
    }

    private fun onNavigationPressed() {
        router.popCurrentController()
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        lifecycleScope.launch {
            viewModel.stateFlow.collect {
                onStateChanged(it)
            }
        }
    }

    private fun onStateChanged(state: ScreenData) {
        when (state.status) {
            is ScreenState.Redirection -> {
                if (state.status.target == "welcome") {
                    router.popToRoot(HorizontalFadeChangeFromHandler(TRANSITION_DURATION))
                    router.replaceTopController(
                        RouterTransaction.with(WelcomeController()).pushChangeHandler(
                            VerticalFadeChangeFromHandler(TRANSITION_DURATION)
                        ).popChangeHandler(VerticalFadeChangeFromHandler(TRANSITION_DURATION))
                    )
                }
            }

            else -> {}
        }
        activeAddressButton?.valueTextView?.setText(
            when (state.walletVersion) {
                WalletVersion.V4R2 -> R.string.settings_address_v4r2
                WalletVersion.V3R2 -> R.string.settings_address_v3r2
                WalletVersion.V3R1 -> R.string.settings_address_v3r1
            }
        )
    }

    private fun showWalletVersionPopup() {
        val context = activity ?: return
        val popupElevation = 2.dp()
        val popupView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(textButton(context).apply {
                minWidth = 100.dp()
                setText(R.string.settings_address_v4r2)
                setOnClickListener { onWalletVersionSelected(WalletVersion.V4R2) }
            })
            addView(textButton(context).apply {
                minWidth = 100.dp()
                setText(R.string.settings_address_v3r2)
                setOnClickListener { onWalletVersionSelected(WalletVersion.V3R2) }
            })
            addView(textButton(context).apply {
                minWidth = 100.dp()
                setText(R.string.settings_address_v3r1)
                setOnClickListener { onWalletVersionSelected(WalletVersion.V3R1) }
            })
        }
        val location = intArrayOf(0, 0)
        val target = activeAddressButton?.valueTextView ?: return
        target.getLocationInWindow(location)
        val showAction = { popupToDisplay: PopupWindow ->
            popupToDisplay.showAtLocation(
                target,
                Gravity.START or Gravity.TOP,
                location[0] - 60.dp(),
                location[1] + 36.dp()
            )
        }
        popup = PopupWindow(popupView, 100.dp(), ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
            animationStyle = R.style.PopupAnimationGravityTop
            elevation = popupElevation.toFloat()
            setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_rect_round_6dp))
        }.apply {
            showAction(this)
        }
    }

    private fun onWalletVersionSelected(walletVersion: WalletVersion) {
        viewModel.onWalletVersionSelected(walletVersion)
        popup?.dismiss()
    }

    override fun onDetach(view: View) {
        super.onDetach(view)
        alertDialog?.dismiss()
        popup?.dismiss()
    }

    override fun onInsetsChanged() {
        super.onInsetsChanged()
        root?.updatePadding(
            systemBarInsets.left, systemBarInsets.top, systemBarInsets.right, 0
        )
        scroll?.updatePadding(0, 0, 0, systemBarInsets.bottom)
    }
}