package ton.coin.wallet.util

fun String.removeLast(): String {
    return if (isEmpty()) {
        this
    } else {
        substring(0, length - 1)
    }
}