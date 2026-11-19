package it.attendance100.mybicocca.data.mapper.mensa

import it.attendance100.mybicocca.data.remote.compass.dto.CompassAllergen
import it.attendance100.mybicocca.data.remote.compass.dto.CompassCfpCalculatedData
import it.attendance100.mybicocca.data.remote.compass.dto.CompassCustomLetter
import it.attendance100.mybicocca.data.remote.compass.dto.CompassDayDishType
import it.attendance100.mybicocca.data.remote.compass.dto.CompassDayMealType
import it.attendance100.mybicocca.data.remote.compass.dto.CompassDayMenu
import it.attendance100.mybicocca.data.remote.compass.dto.CompassDishDetail
import it.attendance100.mybicocca.data.remote.compass.dto.CompassIngredient
import it.attendance100.mybicocca.data.remote.compass.dto.CompassMealTypeSummary
import it.attendance100.mybicocca.data.remote.compass.dto.CompassMenuDish
import it.attendance100.mybicocca.data.remote.compass.dto.CompassMenuSummary
import it.attendance100.mybicocca.data.remote.compass.dto.CompassNutritionalDeclarationItem
import it.attendance100.mybicocca.data.remote.compass.dto.CompassNutritionalValueRow
import it.attendance100.mybicocca.data.remote.compass.dto.CompassTakeawayPriceData
import it.attendance100.mybicocca.data.remote.compass.dto.CompassTakeawayReservation
import it.attendance100.mybicocca.data.remote.compass.dto.CompassTakeawayService
import it.attendance100.mybicocca.domain.model.mensa.MensaAllergen
import it.attendance100.mybicocca.domain.model.mensa.MensaCartPricing
import it.attendance100.mybicocca.domain.model.mensa.MensaCourse
import it.attendance100.mybicocca.domain.model.mensa.MensaDayMealType
import it.attendance100.mybicocca.domain.model.mensa.MensaDaySchedule
import it.attendance100.mybicocca.domain.model.mensa.MensaDietaryBadge
import it.attendance100.mybicocca.domain.model.mensa.MensaDish
import it.attendance100.mybicocca.domain.model.mensa.MensaDishDetail
import it.attendance100.mybicocca.domain.model.mensa.MensaGdaItem
import it.attendance100.mybicocca.domain.model.mensa.MensaIngredient
import it.attendance100.mybicocca.domain.model.mensa.MensaMealType
import it.attendance100.mybicocca.domain.model.mensa.MensaMenuSummary
import it.attendance100.mybicocca.domain.model.mensa.MensaNutritionRow
import it.attendance100.mybicocca.domain.model.mensa.MensaPickupLocation
import it.attendance100.mybicocca.domain.model.mensa.MensaReservation
import it.attendance100.mybicocca.domain.model.mensa.MensaTakeawayDayMealType
import it.attendance100.mybicocca.domain.model.mensa.MensaTakeawayService
import it.attendance100.mybicocca.domain.model.mensa.MensaTimeSlot
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val COMPASS_BASE_URL = "https://unibicocca.compasscloud.it"
private val ITALIAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy")

internal fun parseCompassDate(raw: String?): LocalDate? {
    val clean = raw?.trim()?.take(10)?.takeIf { it.isNotEmpty() } ?: return null
    return runCatching { LocalDate.parse(clean) }.getOrNull()
        ?: runCatching { LocalDate.parse(clean, ITALIAN_DATE_FORMAT) }.getOrNull()
}

private fun normalizeCompassUrl(raw: String?): String? {
    val trimmed = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        trimmed
    } else {
        "$COMPASS_BASE_URL/${trimmed.trimStart('/')}"
    }
}

internal fun CompassMenuSummary.toDomain(): MensaMenuSummary = MensaMenuSummary(
    id = id,
    name = name.trim(),
    shortName = shortName?.trim().takeUnless { it.isNullOrEmpty() } ?: name.trim(),
    description = description?.trim().orEmpty(),
    cateringTypeDescription = cateringTypeDescription?.trim().orEmpty(),
    activeDays = activeDays.mapNotNull(::parseCompassDate).distinct().sorted(),
    mealTypes = extendedMealTypes.map { it.toDomain() }.sortedBy { it.order },
)

internal fun CompassMealTypeSummary.toDomain(): MensaMealType = MensaMealType(
    id = id,
    name = name.trim(),
    shortName = shortName?.trim().takeUnless { it.isNullOrEmpty() } ?: name.trim(),
    order = order ?: Int.MAX_VALUE,
)

internal fun CompassDayMenu.toDomain(fallbackDateKey: String): MensaDaySchedule? {
    val parsedDate = parseCompassDate(date) ?: parseCompassDate(fallbackDateKey) ?: return null
    return MensaDaySchedule(
        menuId = menuId,
        date = parsedDate,
        name = name.trim(),
        active = active ?: true,
        mealTypes = mealTypes.map { it.toDomain() }.sortedBy { it.order },
    )
}

internal fun CompassDayMealType.toDomain(): MensaDayMealType = MensaDayMealType(
    id = id,
    name = name.trim(),
    order = order ?: Int.MAX_VALUE,
    courses = dishTypes.map { it.toDomain() }.sortedBy { it.order },
)

internal fun CompassDayDishType.toDomain(): MensaCourse = MensaCourse(
    id = id,
    name = name.trim(),
    code = code?.trim().orEmpty(),
    order = order ?: Int.MAX_VALUE,
    dishes = dishes.map { it.toDomain() },
)

internal fun CompassMenuDish.toDomain(): MensaDish = MensaDish(
    id = id,
    name = name.trim(),
    shortName = shortName?.trim().takeUnless { it.isNullOrEmpty() } ?: name.trim(),
    courseId = dishType?.id,
    courseName = dishType?.name?.trim(),
    mealTypeId = mealType?.id,
    mealTypeName = mealType?.name?.trim(),
    imageUrl = normalizeCompassUrl(image?.url),
    thumbUrl = normalizeCompassUrl(image?.thumb ?: image?.url),
    majorAllergens = majorAllergens.map { it.toDomain() },
    traceAllergens = traceAllergens.map { it.toDomain() },
    caloricLabel = caloricIcon?.description?.trim(),
    caloricColorHex = caloricIcon?.color?.trim() ?: color?.trim(),
    dietaryBadges = customLetters.map { it.toDomain() },
)

internal fun CompassAllergen.toDomain(): MensaAllergen = MensaAllergen(
    id = id,
    name = name.trim(),
    shortName = shortName?.trim().takeUnless { it.isNullOrEmpty() } ?: name.trim(),
    iconUrl = normalizeCompassUrl(image?.url),
)

internal fun CompassCustomLetter.toDomain(): MensaDietaryBadge = MensaDietaryBadge(
    key = key.trim(),
    label = label.trim(),
    iconUrl = normalizeCompassUrl(image?.light ?: image?.dark),
)

internal fun CompassDishDetail.toDomain(dishId: Int): MensaDishDetail = MensaDishDetail(
    id = dishId,
    name = name.trim(),
    description = description?.trim()?.takeIf { it.isNotEmpty() },
    imageUrl = normalizeCompassUrl(image?.url),
    netQuantityGrams = netQuantityRaw,
    ingredients = ingredients.map { it.toDomain() },
    majorAllergens = majorAllergens.map { it.toDomain() },
    traceAllergens = traceAllergens.map { it.toDomain() },
    displayNutrition = displayNutritionalValues ?: false,
    nutritionSource = nutritionalValues?.source?.trim(),
    nutritionRows = nutritionalValues?.data.orEmpty().map { it.toDomain() },
    gdaItems = nutritionalDeclaration?.data.orEmpty().map { it.toDomain() },
    dietaryBadges = customLetters.map { it.toDomain() },
)

internal fun CompassIngredient.toDomain(): MensaIngredient = MensaIngredient(
    id = id,
    name = (cleanName ?: name).trim(),
    quantity = (cleanQuantity ?: quantity)?.trim()?.takeIf { it.isNotEmpty() },
)

internal fun CompassNutritionalValueRow.toDomain(): MensaNutritionRow = MensaNutritionRow(
    name = name.trim(),
    unit = unit?.trim().orEmpty(),
    portionValue = rawPortionValue,
    portionKjValue = rawPortionKjValue,
    per100gValue = raw100gValue,
    per100gKjValue = raw100gKjValue,
    field = field?.trim(),
)

internal fun CompassNutritionalDeclarationItem.toDomain(): MensaGdaItem = MensaGdaItem(
    name = name.trim(),
    formattedValue = value?.trim().orEmpty(),
    percentage = percentage ?: 0.0,
    field = field?.trim(),
)

internal fun CompassTakeawayService.toDomain(): MensaTakeawayService {
    val locations = locationDelivery?.groups.orEmpty().flatMap { group ->
        group.locations.map { loc ->
            MensaPickupLocation(
                id = loc.id,
                name = loc.name.trim(),
                groupName = group.name.trim(),
            )
        }
    }
    val schedules = buildMap {
        mealTypes.forEach { (dateKey, dayMealTypes) ->
            val date = parseCompassDate(dateKey) ?: return@forEach
            put(
                date,
                dayMealTypes.map { mt ->
                    MensaTakeawayDayMealType(
                        id = mt.id,
                        name = mt.name.trim(),
                        timeSlots = mt.turns.flatMap { group ->
                            group.data.map { slot ->
                                MensaTimeSlot(
                                    id = slot.id,
                                    name = slot.name?.trim().orEmpty(),
                                    bookable = slot.bookable ?: false,
                                    reservationsCount = slot.stats?.reservations,
                                    affluencyCapacity = slot.stats?.affluency,
                                    percentage = slot.stats?.percentage,
                                )
                            }
                        },
                    )
                },
            )
        }
    }
    return MensaTakeawayService(
        id = id,
        name = name.trim(),
        menuId = menu?.id ?: 0,
        menuName = menu?.name?.trim().orEmpty(),
        ageRangeId = ageRange?.id,
        viewPrices = payments?.viewPrices ?: true,
        walletPayment = payments?.walletPayment ?: false,
        userRequestsEnabled = userRequests?.enabled ?: userRequests?.enabledPascal ?: false,
        infoText = texts?.infoBox?.trim()?.takeIf { it.isNotEmpty() },
        descriptionText = texts?.description?.trim()?.takeIf { it.isNotEmpty() },
        maxDaysAhead = maximumDaysAfter ?: 7,
        isFavorite = isFavorite ?: false,
        pickupLocations = locations,
        scheduleByDate = schedules,
    )
}

internal fun CompassTakeawayPriceData.toDomain(cfp: CompassCfpCalculatedData?): MensaCartPricing =
    MensaCartPricing(
        currencySymbol = currency?.symbol?.takeIf { it.isNotBlank() } ?: "€",
        total = total?.value ?: 0.0,
        fixedCost = fixedCost?.value ?: 0.0,
        extraCost = extraCost?.value ?: 0.0,
        combinationCost = combinationCost?.value ?: 0.0,
        reservationCost = reservationCost?.value ?: 0.0,
        cfpSum = cfp?.sum,
        cfpUnit = cfp?.um,
        cfpEquivalentPhrase = cfp?.equivalentPhrase,
    )

internal fun CompassTakeawayReservation.toDomain(): MensaReservation = MensaReservation(
    id = id,
    code = code?.trim().orEmpty(),
    date = parseCompassDate(date),
    rawDate = date?.trim().orEmpty(),
    createdAt = createdAt?.trim(),
    deliveryAddress = deliveryAddress?.trim()?.takeIf { it.isNotEmpty() },
    menuName = menu?.name?.trim(),
    mealTypeName = mealType?.name?.trim(),
    canteenName = service?.canteen?.trim(),
    shiftName = service?.shift?.trim(),
    confirmed = status?.confirmed ?: false,
    deletable = status?.deletable ?: false,
    active = status?.active ?: false,
    provided = status?.provided ?: false,
    statusDescription = status?.statusDescription?.trim().orEmpty(),
    statusColorHex = status?.statusColor?.trim(),
    dishes = dishes.map { it.toDomain() },
    barcodeValue = "P-$id",
    cfpFinalValue = cfp?.values?.finalValue,
    cfpUnit = cfp?.values?.um,
)
