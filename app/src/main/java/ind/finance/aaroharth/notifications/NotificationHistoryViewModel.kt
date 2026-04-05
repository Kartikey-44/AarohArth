package ind.finance.aaroharth.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ind.finance.aaroharth.data.model.Notification_History_Info
import ind.finance.aaroharth.repositories.NotificationRepository
import kotlinx.coroutines.launch

class NotificationHistoryViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _notifications = MutableLiveData<List<Notification_History_Info>>()
    val notifications: LiveData<List<Notification_History_Info>> = _notifications

    private val _notificationCount = MutableLiveData<Int>()
    val notificationCount: LiveData<Int> = _notificationCount

    fun loadNotifications() {
        viewModelScope.launch {
            _notifications.postValue(repository.getActiveNotifications())
            _notificationCount.postValue(repository.getActiveNotificationCount())
        }
    }

    fun refreshNotificationCount() {
        viewModelScope.launch {
            _notificationCount.postValue(repository.getActiveNotificationCount())
        }
    }
}