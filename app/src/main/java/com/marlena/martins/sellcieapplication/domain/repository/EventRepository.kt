package com.marlena.martins.sellcieapplication.domain.repository

import com.marlena.martins.sellcieapplication.domain.model.Event

interface EventRepository {
    fun getEvents(): List<Event>
}
