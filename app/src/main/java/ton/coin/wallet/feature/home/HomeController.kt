package ton.coin.wallet.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bluelinelabs.conductor.RouterTransaction
import io.github.landarskiy.reuse.adapter.DiffAdapter
import kotlinx.coroutines.launch
import ton.coin.wallet.R
import ton.coin.wallet.common.lifecycle.ViewModelController
import ton.coin.wallet.common.ui.LinearLayoutLpBuilder
import ton.coin.wallet.common.ui.conductor.TRANSITION_DURATION
import ton.coin.wallet.common.ui.conductor.VerticalFadeChangeFromHandler
import ton.coin.wallet.common.ui.custom.reuse.types.ReuseDefaultContentScope
import ton.coin.wallet.common.ui.darkStatusBar
import ton.coin.wallet.common.ui.darkToolbar
import ton.coin.wallet.feature.home.adapter.DividerEntry
import ton.coin.wallet.feature.home.adapter.LoadingEntry
import ton.coin.wallet.feature.home.adapter.TransactionDateEntry
import ton.coin.wallet.feature.home.adapter.TransactionEntry
import ton.coin.wallet.feature.home.adapter.WalletStateData
import ton.coin.wallet.feature.home.adapter.WalletStateEntry
import ton.coin.wallet.feature.receive.ReceiveController
import ton.coin.wallet.feature.send.SendController
import ton.coin.wallet.feature.settings.SettingsController
import ton.coin.wallet.util.isSameDay
import java.time.LocalDateTime

class HomeController : ViewModelController() {

    private val viewModel: HomeViewModel by lazy {
        HomeViewModel().apply {
            attachViewModel(this)
        }
    }

    private var root: LinearLayout? = null
    private var recycler: RecyclerView? = null
    private var swipeToRefresh: SwipeRefreshLayout? = null
    private val defaultScope = ReuseDefaultContentScope()
    private val listAdapter = DiffAdapter(defaultScope.types)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        activity?.darkStatusBar()
        val context = container.context
        val recyclerView = RecyclerView(context).apply {
            recycler = this
            clipToPadding = false
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter
            setBackgroundResource(R.color.white)
            val animator = itemAnimator
            if (animator is SimpleItemAnimator) {
                animator.supportsChangeAnimations = false
            }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount
                    val isLastItemVisible = lastVisibleItemPosition == totalItemCount - 1
                    if (isLastItemVisible) {
                        viewModel.loadNextTransactions()
                    }
                }
            })
        }
        val swipeToRefresh = SwipeRefreshLayout(context).apply {
            swipeToRefresh = this
            addView(recyclerView)
            setOnRefreshListener {
                viewModel.reloadData()
            }
        }
        val root = LinearLayout(inflater.context).apply {
            setBackgroundResource(R.color.black)
            orientation = LinearLayout.VERTICAL
            addView(darkToolbar(context).apply {
                inflateMenu(R.menu.menu_main)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_scan -> {}
                        R.id.action_settings -> onSettingsClicked()
                    }
                    true
                }
                setNavigationOnClickListener { }
                elevation = 0f
            }, LinearLayoutLpBuilder().wMatch().build())
            addView(
                swipeToRefresh,
                LinearLayoutLpBuilder().wMatch().hMatch().build()
            )
        }
        this.root = root
        return root
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
        val dataBuilder = defaultScope.newDataBuilder().withWalletStateViewHolderFactory(
            WalletStateEntry(
                data = WalletStateData(address = state.walletAddress, balance = state.balance),
                onReceiveCallback = ::onReceiveClicked,
                onSendCallback = ::onSendClicked
            )
        )
        var lastDate: LocalDateTime? = null
        for (transaction in state.completedTransactions) {
            if (lastDate == null || !lastDate.isSameDay(transaction.date)) {
                dataBuilder.withTransactionDateViewHolderFactory(TransactionDateEntry(transaction.date))
            }
            dataBuilder.withTransactionViewHolderFactory(TransactionEntry(transaction))
            dataBuilder.withDividerViewHolderFactory(DividerEntry())
            lastDate = transaction.date
        }
        if (state.transactionsStatus == ScreenState.Loading && state.status != ScreenState.Loading) {
            dataBuilder.withLoadingViewHolderFactory(LoadingEntry())
        }
        swipeToRefresh?.isRefreshing =
            state.transactionsStatus != ScreenState.Loading && state.status == ScreenState.Loading
        listAdapter.setItems(dataBuilder.build())
    }

    private fun onSettingsClicked() {
        router.pushController(
            RouterTransaction.with(SettingsController())
                .pushChangeHandler(VerticalFadeChangeFromHandler(TRANSITION_DURATION))
                .popChangeHandler(VerticalFadeChangeFromHandler(TRANSITION_DURATION))
        )
    }

    private fun onReceiveClicked() {
        router.pushController(
            RouterTransaction.with(ReceiveController())
                .pushChangeHandler(VerticalFadeChangeFromHandler(TRANSITION_DURATION * 2, false))
                .popChangeHandler(VerticalFadeChangeFromHandler(TRANSITION_DURATION))
        )
    }

    private fun onSendClicked() {
        router.pushController(
            RouterTransaction.with(SendController())
                .pushChangeHandler(VerticalFadeChangeFromHandler(TRANSITION_DURATION))
                .popChangeHandler(VerticalFadeChangeFromHandler(TRANSITION_DURATION))
        )
    }

    fun printMnemonic(mnemonic: List<String>) {
        //WalletCache.save(Account(mnemonic))
        //TonControllerLegacy.instance.init()
    }

    fun printBalance() {
        //TonControllerLegacy.instance.printBalance()
    }

    fun send() {
        //TonControllerLegacy.instance.sendTon()
    }

    fun printHistory() {
        //TonControllerLegacy.instance.printHistory()
    }

    override fun onInsetsChanged() {
        super.onInsetsChanged()
        root?.updatePadding(
            systemBarInsets.left,
            systemBarInsets.top,
            systemBarInsets.right,
            0
        )
        recycler?.updatePadding(0, 0, 0, systemBarInsets.bottom)
    }
}