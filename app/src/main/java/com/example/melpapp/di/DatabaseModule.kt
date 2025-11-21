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
import java.security.SecureRandom
import javax.inject.Singleton
import android.util.Base64
import androidx.core.content.edit

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
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val dbPassword = prefs.getString("db_key", null) ?: run {

            val randomBytes = ByteArray(32)
            SecureRandom().nextBytes(randomBytes)

            val pass = Base64.encodeToString(randomBytes, Base64.NO_WRAP)

            prefs.edit() { putString("db_key", pass) }
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
