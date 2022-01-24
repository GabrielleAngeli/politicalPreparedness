package com.example.android.politicalpreparedness.database

import androidx.room.*
import com.example.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {

    @Query("SELECT * FROM election_table")
    suspend fun getAllElections(): List<Election>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElection(election: Election)

    //Add select single election query
    @Query("SELECT * FROM election_table where id=:id")
    suspend fun getElectionById(id: String): Election

    //Add delete query
    @Query("DELETE FROM election_table where id=:id")
    suspend fun deleteElectionById(id: String)

    //Add clear query
    @Query("DELETE FROM election_table")
    suspend fun clear()

}