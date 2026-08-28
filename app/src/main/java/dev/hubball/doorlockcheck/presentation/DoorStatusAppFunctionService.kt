package dev.hubball.doorlockcheck.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Lets you track and record whether the front door has been checked.
 */
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@AppFunctionServiceEntryPoint(
    serviceName = "DoorStatusAppFunctionService",
    appFunctionXmlFileName = "door_status_app_function_service"
)
abstract class BaseDoorStatusAppFunctionService: AppFunctionService() {

    /**
     * Checks whether the front door has been marked as checked.
     *
     * @return True if the front door has been marked as checked, false otherwise.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun isFrontDoorChecked(): Boolean = withContext(Dispatchers.IO) {
        val preferences = applicationContext.dataStore.data.first()
        return@withContext preferences[isFrontDoorCheckedKey] ?: false
    }

    /**
     * Records whether the front door has been checked.
     *
     * @param isChecked True to mark the front door as checked, false to mark it as not checked.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun setFrontDoorChecked(isChecked: Boolean) {
        withContext(Dispatchers.IO) {
            applicationContext.dataStore.edit { preferences ->
                preferences[isFrontDoorCheckedKey] = isChecked
            }
        }
    }
}
