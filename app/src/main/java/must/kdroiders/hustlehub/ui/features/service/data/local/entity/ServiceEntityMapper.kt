package must.kdroiders.hustlehub.ui.features.service.data.local.entity

import must.kdroiders.hustlehub.data.model.Service
import must.kdroiders.hustlehub.data.model.ServiceAvailability
import must.kdroiders.hustlehub.data.model.ServiceCategory
import org.json.JSONArray

/** Converts a cached [ServiceEntity] back to the domain [Service] model. */
fun ServiceEntity.toDomain(): Service =
    Service(
        id = id,
        providerId = providerId,
        title = title,
        category = parseCategory(category),
        description = description,
        priceRange = priceRange,
        portfolio = parseJsonArray(portfolioJson),
        availability = parseAvailability(availability),
        averageRating = averageRating,
        reviewCount = reviewCount,
        openToBarter = openToBarter,
        tags = parseJsonArray(tagsJson),
        createdAt = lastUpdated,
        updatedAt = lastUpdated,
        iconUrl = iconUrl,
    )

/** Converts a domain [Service] to a [ServiceEntity] for local storage. */
fun Service.toEntity(now: Long = System.currentTimeMillis()): ServiceEntity =
    ServiceEntity(
        id = id,
        providerId = providerId,
        title = title,
        category = category.name,
        description = description,
        priceRange = priceRange,
        averageRating = averageRating,
        reviewCount = reviewCount,
        availability = availability.name,
        openToBarter = openToBarter,
        portfolioJson = toJsonArray(portfolio),
        tagsJson = toJsonArray(tags),
        iconUrl = iconUrl,
        lastUpdated = now,
    )

private fun parseCategory(name: String): ServiceCategory = runCatching { ServiceCategory.valueOf(name) }.getOrDefault(ServiceCategory.OTHER)

private fun parseAvailability(name: String): ServiceAvailability =
    runCatching { ServiceAvailability.valueOf(name) }.getOrDefault(ServiceAvailability.AVAILABLE)

private fun parseJsonArray(json: String): List<String> =
    runCatching {
        val arr = JSONArray(json)
        List(arr.length()) { arr.getString(it) }
    }.getOrDefault(emptyList())

private fun toJsonArray(items: List<String>): String = JSONArray(items).toString()
