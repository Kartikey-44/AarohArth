package ind.finance.aaroharth

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class AuthFlowTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(SignUp::class.java)

    private lateinit var auth: FirebaseAuth

    @Before
    fun setup() {
        auth = FirebaseAuth.getInstance()
        auth.signOut()

        // 🔴 CONNECT Espresso to production Idling
        Idling.bridge = EspressoIdlingBridge

        // 🔴 Register the actual idling resource Espresso must watch
        IdlingRegistry.getInstance()
            .register(EspressoIdlingBridge.resource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance()
            .unregister(EspressoIdlingBridge.resource)

        // 🔴 Reset to production-safe no-op
        Idling.bridge = NoOpIdlingBridge
    }

    @Test
    fun testSignUpAndSignIn() {

        val email = "testuser_${UUID.randomUUID()}@example.com"
        val password = "password123"

        // ---------- SIGN UP ----------
        onView(withId(R.id.email_entry_field))
            .perform(typeText(email), closeSoftKeyboard())

        onView(withId(R.id.password_entry_field))
            .perform(typeText(password), closeSoftKeyboard())

        onView(withId(R.id.btn_sign_up))
            .perform(click())

        // ---------- VERIFY HOME ----------
        onView(withId(R.id.main))
            .check(matches(isDisplayed()))

        // ---------- SIGN OUT ----------
        auth.signOut()

        // ---------- GO TO SIGN IN ----------
        activityRule.scenario.onActivity {
            it.startActivity(Intent(it, SignIn::class.java))
        }

        // ---------- SIGN IN ----------
        onView(withId(R.id.email_entry_field))
            .perform(typeText(email), closeSoftKeyboard())

        onView(withId(R.id.password_entry_field))
            .perform(typeText(password), closeSoftKeyboard())

        onView(withId(R.id.btn_sign_in))
            .perform(click())

        // ---------- VERIFY HOME AGAIN ----------
        onView(withId(R.id.main))
            .check(matches(isDisplayed()))
    }
}
