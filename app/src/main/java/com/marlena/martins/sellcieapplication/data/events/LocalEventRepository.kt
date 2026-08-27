package com.marlena.martins.sellcieapplication.data.events

import android.content.Context
import com.marlena.martins.sellcieapplication.domain.model.Event
import com.marlena.martins.sellcieapplication.domain.repository.EventRepository
import org.json.JSONArray

class LocalEventRepository(
    private val context: Context
) : EventRepository {

    override fun getEvents(): List<Event> {
        val content = context.assets.open(EVENTS_FILE).bufferedReader().use { it.readText() }
        return JSONArray(content).let { events ->
            List(events.length()) { index ->
                events.getJSONObject(index).toEvent()
            }
        }
    }

    private fun org.json.JSONObject.toEvent() = Event(
        id = getString("id"),
        title = getString("title"),
        date = getString("date"),
        location = getString("location"),
        priceInCents = getLong("priceInCents"),
        availableTickets = getInt("availableTickets")
    )

    private companion object {
        const val EVENTS_FILE = "events.json"
    }
}
