package ind.finance.aaroharth.appSecurity

object AppLockManager {
    var isAppLocked=true
    var lastBackgroundTime: Long=0L
    const val LOCK_TIMEOUT=120_000L
}