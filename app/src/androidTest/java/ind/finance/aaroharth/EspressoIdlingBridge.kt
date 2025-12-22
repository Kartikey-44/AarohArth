package ind.finance.aaroharth

import androidx.test.espresso.idling.CountingIdlingResource

object EspressoIdlingBridge : IdlingBridge {
    val resource = CountingIdlingResource("FirebaseAuth")

    override fun increment() = resource.increment()
    override fun decrement() = resource.decrement()
}
