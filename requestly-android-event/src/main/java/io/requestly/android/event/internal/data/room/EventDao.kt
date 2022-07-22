package io.requestly.android.event.internal.data.room

import androidx.lifecycle.LiveData
import androidx.room.*
import io.requestly.android.event.internal.data.entity.Event


@Dao
internal interface EventDao {

    @Query("SELECT * FROM events ORDER BY timestamp DESC")
    fun getSorted(): LiveData<List<Event>>

    @Query("SELECT * from events")
    suspend fun getAll(): List<Event>

    @Insert
    fun insert(event: Event): Long?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(event: Event): Int

    @Query("DELETE FROM events")
    suspend fun deleteAll()

    @Query("SELECT * FROM events WHERE id= :id")
    fun getById(id: Long): LiveData<Event?>

    @Query("DELETE FROM events WHERE timestamp <= :threshold")
    suspend fun deleteBefore(threshold: Long)
}
