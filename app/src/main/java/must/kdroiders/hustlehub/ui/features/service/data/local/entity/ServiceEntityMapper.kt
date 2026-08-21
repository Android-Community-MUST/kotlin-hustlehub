package must.kdroiders.hustlehub.ui.features.service.data.local.entity

import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory

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
        isFeatured = isFeatured,
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
        isFeatured = isFeatured,
        portfolioJson = toJsonArray(portfolio),
        tagsJson = toJsonArray(tags),
        iconUrl = iconUrl,
        lastUpdated = now,
    )

private fun parseCategory(name: String): ServiceCategory = runCatching { ServiceCategory.valueOf(name) }.getOrDefault(ServiceCategory.OTHER)

private fun parseAvailability(name: String): ServiceAvailability = runCatching { ServiceAvailability.valueOf(name) }.getOrDefault(ServiceAvailability.AVAILABLE)

private fun parseJsonArray(json: String): List<String> =
    runCatching {
        json
            .removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotEmpty() }
    }.getOrDefault(emptyList())

private fun toJsonArray(items: List<String>): String = items.joinToString(separator = ",", prefix = "[", postfix = "]") { "\"$it\"" }
