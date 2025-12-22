package ind.finance.aaroharth
import androidx.test.espresso.idling.CountingIdlingResource

object FirebaseIdling {
    val resource = CountingIdlingResource("FirebaseAuth")
}