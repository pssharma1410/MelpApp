package com.example.melpapp.di

import android.content.Context
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.melpapp.data.local.ChatDao
import com.example.melpapp.data.local.ChatDatabase
import com.example.melpapp.data.local.MessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): ChatDatabase {

        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()


        val prefs = EncryptedSharedPreferences.create(
            context,
            "secure_prefs",  // name of prefs (String) — FIXED
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val dbPassword = prefs.getString("db_key", null) ?: run {
            val pass = "Piyush@141001"
            prefs.edit().putString("db_key", pass).apply()
            pass
        }

        val passphrase = SQLiteDatabase.getBytes(dbPassword.toCharArray())
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context,
            ChatDatabase::class.java,
            "chat_db"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideChatDao(db: ChatDatabase): ChatDao = db.chatDao()

    @Provides
    fun provideMessageDao(db: ChatDatabase): MessageDao = db.messageDao()
}
