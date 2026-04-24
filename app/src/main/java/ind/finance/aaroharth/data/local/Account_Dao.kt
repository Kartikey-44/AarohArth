package ind.finance.aaroharth.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ind.finance.aaroharth.data.model.Account_Info
import kotlinx.coroutines.flow.Flow

@Dao
interface Account_Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account_Info): Long

    @Query("SELECT * FROM AccountsTable")
    fun getAllAccountsFlow(): Flow<List<Account_Info>>

    @Query("SELECT * FROM AccountsTable")
    suspend fun getAllAccounts(): List<Account_Info>

    @Query("SELECT DISTINCT AccountType FROM AccountsTable")
    suspend fun getAllAccountType(): List<String>

    @Query("SELECT DISTINCT AccountName FROM AccountsTable WHERE AccountType=:accountType")
    suspend fun getAllAccountName(accountType: String): List<String>

    @Query("SELECT balance FROM AccountsTable WHERE AccountName=:accountName")
    suspend fun getbalance(accountName: String): Long

    @Query("UPDATE AccountsTable SET balance= :balance, isSynced = 0 WHERE AccountName=:accountName")
    suspend fun updatebalance(balance: Long, accountName: String)

    @Query(
        """
    SELECT * FROM AccountsTable
    WHERE accountName LIKE '%' || :query || '%'
       OR accountType LIKE '%' || :query || '%'
       OR CAST(balance AS TEXT) LIKE '%' || :query || '%'
"""
    )
    suspend fun searchAccounts(query: String): List<Account_Info>

    @Query("SELECT * FROM AccountsTable WHERE accountType=:accountType")
    suspend fun filter(accountType: String): List<Account_Info>

    @Query("SELECT SUM(balance) FROM AccountsTable")
    suspend fun currentbalance(): Long

    @Query("SELECT COUNT(*) FROM AccountsTable")
    fun getNumberOfAccountsFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM AccountsTable")
    suspend fun numberOfAccounts(): Int

    @Query("SELECT balance FROM AccountsTable WHERE accountName=:name")
    suspend fun getamount(name: String): Long

    @Query("UPDATE AccountsTable SET balance= :balance WHERE AccountName=:name")
    suspend fun update(balance: Long, name: String)

    @Query("SELECT * FROM AccountsTable WHERE id = :id")
    suspend fun getAccountById(id: Long): Account_Info

    @Query("DELETE FROM AccountsTable WHERE id=:id")
    suspend fun deletebyid(id: Long)

    @Update
    suspend fun updateaccountinfo(account: Account_Info)

    @Query("SELECT SUM(balance) AS total FROM AccountsTable")
    suspend fun gettotalbalance(): Long

    @Query("SELECT accountType FROM AccountsTable")
    suspend fun getAllType(): List<String>

    @Query("SELECT accountName FROM AccountsTable")
    suspend fun getAllName(): List<String>

    @Query("SELECT * FROM AccountsTable WHERE isSynced = 0")
    suspend fun getUnsynced(): List<Account_Info>

    @Query("UPDATE AccountsTable SET isSynced = 1 WHERE id=:id")
    suspend fun setSynced(id: Long)
}
