package de.haukesomm.vertretungsplan.helper

import com.configcat.ConfigCatClient
import com.configcat.PollingModes


class ConfigCatHelper {

    fun isEndOfLifeReached(): Boolean {
        val client = ConfigCatClient.newBuilder()
                .mode(PollingModes.LazyLoad(120))
                // TODO: Eigenen API-Key hier hinterlegen
                .build("") // <-- This is the actual SDK Key for your Test environment
        client.forceRefresh()
        return client.getValue(Boolean::class.java, "de_haukesomm_vertretungsplan_endoflife", false)
    }
}