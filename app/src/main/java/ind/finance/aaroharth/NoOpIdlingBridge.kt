package ind.finance.aaroharth

object NoOpIdlingBridge : IdlingBridge {
    override fun increment() {}
    override fun decrement() {}
}
