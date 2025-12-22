package ind.finance.aaroharth

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.Animatable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ind.finance.aaroharth.R
import ind.finance.aaroharth.databinding.ActivitySplashBinding
import java.lang.invoke.MethodHandles
import java.util.logging.Handler

class SplashActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val prefs=getSharedPreferences("app_prefs",MODE_PRIVATE)
        val firstopen=prefs.getBoolean("firstopen",true)
        if(firstopen){
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, SignUp::class.java))
                finish()
            },2500)
        }
        else{
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            },2500)
        }
        val scaleUp: Animation= AnimationUtils.loadAnimation(this,R.anim.scale_up)
        binding.appLogo.startAnimation(scaleUp)
    }
}