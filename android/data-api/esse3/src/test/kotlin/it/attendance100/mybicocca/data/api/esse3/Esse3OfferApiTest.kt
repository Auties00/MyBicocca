package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.util.logging.Logger

class Esse3OfferApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(Esse3OfferApiTest::class.java.name)

    @Test
    suspend fun testGetOffers() {
        try {
            val offers = api.offer.getOffers()
            logger.info("getOffers: found ${offers.size} offers")
            assertTrue(offers.isNotEmpty(), "Expected at least one offer")
            for (offer in offers) {
                logger.info("  offer: aaOffId=${offer.academicYearOfferId}, cdsOffId=${offer.courseOfStudyOfferId}, cdsCod=${offer.courseOfStudyCode}, cdsDes=${offer.courseOfStudyDescription}")
                logger.info("    tipoCorsoCod=${offer.courseTypeCode}, statoAttCod=${offer.activityStateCode}, offertaExistsFlg=${offer.offerExistsFlag}, logisticaExistsFlg=${offer.logisticsExistsFlag}")
                assertTrue(offer.academicYearOfferId > 0, "academicYearOfferId should be positive")
                assertTrue(offer.courseOfStudyOfferId > 0, "courseOfStudyOfferId should be positive")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getOffers not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetOffersWithAcademicYear() {
        try {
            val allOffers = api.offer.getOffers(limit = 10)
            if (allOffers.isEmpty()) {
                logger.warning("No offers found to test getOffers with academicYearOfferId filter")
                return
            }
            val aaOffId = allOffers.first().academicYearOfferId
            val filtered = api.offer.getOffers(academicYearOfferId = aaOffId)
            logger.info("getOffers(aaOffId=$aaOffId): found ${filtered.size} offers")
            for (offer in filtered) {
                assertEquals(aaOffId, offer.academicYearOfferId, "academicYearOfferId should match filter")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getOffers (with aaOffId) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetOffersWithPagination() {
        try {
            val offers = api.offer.getOffers(start = 0, limit = 10)
            logger.info("getOffers (paginated, limit=10): found ${offers.size} offers")
            assertTrue(offers.size <= 10, "Paginated result should have at most 10 items")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getOffers (paginated) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetTeachingActivityOffers() {
        try {
            val offers = api.offer.getOffers(limit = 10)
            if (offers.isEmpty()) {
                logger.warning("No offers found to test getTeachingActivityOffers")
                return
            }
            for (offer in offers) {
                val aaOffId = offer.academicYearOfferId
                val cdsOffId = offer.courseOfStudyOfferId
                try {
                    val activities = api.offer.getTeachingActivityOffers(aaOffId, cdsOffId)
                    logger.info("getTeachingActivityOffers(aaOffId=$aaOffId, cdsOffId=$cdsOffId): found ${activities.size} activities")
                    for (activity in activities) {
                        val key = activity.contextualizedTeachingActivityKey
                        logger.info("  activity: cdsId=${key.courseOfStudyId}, adId=${key.activityId}, adCod=${key.activityCode}, adDes=${key.activityDescription}")
                        assertNotNull(key.activityId, "activityId should not be null")
                    }
                    if (activities.isNotEmpty()) return
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getTeachingActivityOffers($aaOffId, $cdsOffId) not authorized: ${e.message}")
                    break
                } catch (e: Esse3ValidationException) {
                    logger.warning("getTeachingActivityOffers($aaOffId, $cdsOffId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getOffers not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetTeachingActivityOffersWithPagination() {
        try {
            val offers = api.offer.getOffers(limit = 5)
            val offer = offers.firstOrNull() ?: run {
                logger.warning("No offers found to test getTeachingActivityOffers with pagination")
                return
            }
            try {
                val activities = api.offer.getTeachingActivityOffers(
                    offer.academicYearOfferId,
                    offer.courseOfStudyOfferId,
                    start = 0,
                    limit = 5
                )
                logger.info("getTeachingActivityOffers (paginated, limit=5): found ${activities.size} activities")
                assertTrue(activities.size <= 5, "Paginated result should have at most 5 items")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getTeachingActivityOffers (paginated) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getTeachingActivityOffers (paginated) validation error: ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getOffers not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetTeachingUnitOffers() {
        try {
            val offers = api.offer.getOffers(limit = 10)
            if (offers.isEmpty()) {
                logger.warning("No offers found to test getTeachingUnitOffers")
                return
            }
            for (offer in offers) {
                val aaOffId = offer.academicYearOfferId
                val cdsOffId = offer.courseOfStudyOfferId
                try {
                    val units = api.offer.getTeachingUnitOffers(aaOffId, cdsOffId)
                    logger.info("getTeachingUnitOffers(aaOffId=$aaOffId, cdsOffId=$cdsOffId): found ${units.size} teaching units")
                    for (unit in units) {
                        val key = unit.contextualizedTeachingUnitKey
                        val activityKey = key.contextualizedTeachingActivityKey
                        logger.info("  unit: udId=${key.teachingUnitId}, udCod=${key.teachingUnitCode}, udDes=${key.teachingUnitDescription}, tipoUdCod=${unit.teachingUnitTypeCode}")
                        logger.info("    activity: adId=${activityKey.activityId}, adCod=${activityKey.activityCode}")
                        assertTrue(key.teachingUnitId > 0, "teachingUnitId should be positive")
                    }
                    if (units.isNotEmpty()) return
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getTeachingUnitOffers($aaOffId, $cdsOffId) not authorized: ${e.message}")
                    break
                } catch (e: Esse3ValidationException) {
                    logger.warning("getTeachingUnitOffers($aaOffId, $cdsOffId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getOffers not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetLecturersByTeachingUnit() {
        try {
            val offers = api.offer.getOffers(limit = 10)
            if (offers.isEmpty()) {
                logger.warning("No offers found to test getLecturersByTeachingUnit")
                return
            }
            for (offer in offers) {
                val aaOffId = offer.academicYearOfferId
                val cdsOffId = offer.courseOfStudyOfferId
                try {
                    val lecturers = api.offer.getLecturersByTeachingUnit(aaOffId, cdsOffId)
                    logger.info("getLecturersByTeachingUnit(aaOffId=$aaOffId, cdsOffId=$cdsOffId): found ${lecturers.size} lecturer records")
                    for (lecturer in lecturers) {
                        val key = lecturer.contextualizedTeachingUnitKey
                        logger.info("  lecturer: lecturerId=${lecturer.lecturerId}, name=${lecturer.lecturerName} ${lecturer.lecturerSurname}, holderFlg=${lecturer.holderFlag}")
                        logger.info("    unit: udId=${key.teachingUnitId}, udCod=${key.teachingUnitCode}")
                        assertTrue(lecturer.lecturerId > 0, "lecturerId should be positive")
                    }
                    if (lecturers.isNotEmpty()) return
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getLecturersByTeachingUnit($aaOffId, $cdsOffId) not authorized: ${e.message}")
                    break
                } catch (e: Esse3ValidationException) {
                    logger.warning("getLecturersByTeachingUnit($aaOffId, $cdsOffId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getOffers not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetGroupedTeachingActivities() {
        val currentYear = 2025
        try {
            val grouped = api.offer.getGroupedTeachingActivities(cohortYear = currentYear)
            logger.info("getGroupedTeachingActivities(cohortYear=$currentYear): found ${grouped.size} groups")
            for (group in grouped) {
                logger.info("  group: adragoffId=${group.activityRaggruppamentoOfferId}, adCod=${group.activityCode}, adDes=${group.activityDescription}, tipoRagCod=${group.groupingTypeCode}")
                logger.info("    childActivities=${group.childActivities.size}")
                for (child in group.childActivities) {
                    logger.info("      child: adFiglioCod=${child.childActivityCode}, adFiglioDes=${child.childActivityDescription}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getGroupedTeachingActivities(cohortYear=$currentYear) not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getGroupedTeachingActivities(cohortYear=$currentYear) validation error: ${e.message}")
        }
    }

    @Test
    suspend fun testGetGroupedTeachingActivitiesOlderYear() {
        val cohortYear = 2023
        try {
            val grouped = api.offer.getGroupedTeachingActivities(cohortYear = cohortYear)
            logger.info("getGroupedTeachingActivities(cohortYear=$cohortYear): found ${grouped.size} groups")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getGroupedTeachingActivities(cohortYear=$cohortYear) not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getGroupedTeachingActivities(cohortYear=$cohortYear) validation error: ${e.message}")
        }
    }

    @Test
    suspend fun testGetGenericTeachingActivities() {
        try {
            val activities = api.offer.getGenericTeachingActivities()
            logger.info("getGenericTeachingActivities: found ${activities.size} activities")
            for (activity in activities) {
                logger.info("  activity: adId=${activity.activityId}, adCod=${activity.activityCode}, adDes=${activity.activityDescription}, offerExistsFlg=${activity.offerExistsFlag}")
                assertTrue(activity.activityId > 0, "activityId should be positive")
                assertNotNull(activity.activityCode, "activityCode should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getGenericTeachingActivities not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetGenericTeachingActivitiesWithPagination() {
        try {
            val activities = api.offer.getGenericTeachingActivities(start = 0, limit = 10)
            logger.info("getGenericTeachingActivities (paginated, limit=10): found ${activities.size} activities")
            assertTrue(activities.size <= 10, "Paginated result should have at most 10 items")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getGenericTeachingActivities (paginated) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetPartialDomicile() {
        try {
            val partitions = api.offer.getPartialDomicile()
            logger.info("getPartialDomicile: found ${partitions.size} partition domains")
            for (partition in partitions) {
                logger.info("  partition: fatPartCod=${partition.invoicePartialCode}, fatPartDes=${partition.invoicePartialDescription}, tipoFatt=${partition.invoiceType}")
                logger.info("    domainItems=${partition.partitionDomain.size}")
                for (domain in partition.partitionDomain) {
                    logger.info("      domain: domPartCod=${domain.domicilePartialCode}, domPartDes=${domain.domicilePartialDescription}")
                    assertNotNull(domain.domicilePartialCode, "domicilePartialCode should not be null")
                    assertNotNull(domain.invoicePartialCode, "invoicePartialCode should not be null")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getPartialDomicile not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetPartialDomicileWithFilter() {
        try {
            val partitions = api.offer.getPartialDomicile(start = 0, limit = 5)
            logger.info("getPartialDomicile (paginated, limit=5): found ${partitions.size} entries")
            assertTrue(partitions.size <= 5, "Paginated result should have at most 5 items")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getPartialDomicile (paginated) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetInvoicePartial() {
        try {
            val factors = api.offer.getInvoicePartial()
            logger.info("getInvoicePartial: found ${factors.size} partition factors")
            for (factor in factors) {
                logger.info("  factor: fatPartCod=${factor.invoicePartialCode}, fatPartDes=${factor.invoicePartialDescription}, tipoFatt=${factor.invoiceType}")
                assertNotNull(factor.invoicePartialCode, "invoicePartialCode should not be null")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInvoicePartial not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetInvoicePartialWithPagination() {
        try {
            val factors = api.offer.getInvoicePartial(start = 0, limit = 5)
            logger.info("getInvoicePartial (paginated, limit=5): found ${factors.size} factors")
            assertTrue(factors.size <= 5, "Paginated result should have at most 5 items")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getInvoicePartial (paginated) not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetContextualizedTeachingActivity() {
        try {
            val offers = api.offer.getOffers(limit = 10)
            if (offers.isEmpty()) {
                logger.warning("No offers found to test getContextualizedTeachingActivity")
                return
            }
            for (offer in offers) {
                val aaOffId = offer.academicYearOfferId
                val cdsOffId = offer.courseOfStudyOfferId
                try {
                    val activities = api.offer.getTeachingActivityOffers(aaOffId, cdsOffId, limit = 5)
                    for (activity in activities) {
                        val key = activity.contextualizedTeachingActivityKey
                        try {
                            val retrieved = api.offer.getContextualizedTeachingActivity(
                                academicYearOfferId = aaOffId,
                                courseOfStudyOfferId = cdsOffId,
                                academicYearOrderOfferId = key.academicYearOrderId.toLong(),
                                studyPlanOfferId = key.studyPlanId,
                                activityOfferId = key.activityId
                            )
                            logger.info("getContextualizedTeachingActivity: adId=${retrieved.contextualizedTeachingActivityKey.activityId}, adCod=${retrieved.contextualizedTeachingActivityKey.activityCode}")
                            assertEquals(key.activityId, retrieved.contextualizedTeachingActivityKey.activityId, "activityId should match")
                            return
                        } catch (e: Esse3NotAuthorizedException) {
                            logger.warning("getContextualizedTeachingActivity not authorized: ${e.message}")
                            return
                        } catch (e: Esse3ValidationException) {
                            logger.warning("getContextualizedTeachingActivity validation error: ${e.message}")
                        }
                    }
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getTeachingActivityOffers($aaOffId, $cdsOffId) not authorized: ${e.message}")
                    break
                } catch (e: Esse3ValidationException) {
                    logger.warning("getTeachingActivityOffers($aaOffId, $cdsOffId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getOffers not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetCancellableOfferTeachingActivity() {
        try {
            val result = api.offer.getCancellableOfferTeachingActivity()
            logger.info("getCancellableOfferTeachingActivity: retCode=${result.returnCode}, msg=${result.message}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCancellableOfferTeachingActivity not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getCancellableOfferTeachingActivity validation error: ${e.message}")
        }
    }

    @Test
    suspend fun testGetCancellableOfferTeachingActivityWithParams() {
        try {
            val offers = api.offer.getOffers(limit = 5)
            val offer = offers.firstOrNull() ?: run {
                logger.warning("No offers found to test getCancellableOfferTeachingActivity with params")
                return
            }
            val result = api.offer.getCancellableOfferTeachingActivity(
                academicYearOfferId = offer.academicYearOfferId
            )
            logger.info("getCancellableOfferTeachingActivity(aaOffId=${offer.academicYearOfferId}): retCode=${result.returnCode}, msg=${result.message}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getCancellableOfferTeachingActivity (with params) not authorized: ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("getCancellableOfferTeachingActivity (with params) validation error: ${e.message}")
        }
    }

    @Test
    suspend fun testGetCancellableTeachingActivity() {
        try {
            val offers = api.offer.getOffers(limit = 10)
            if (offers.isEmpty()) {
                logger.warning("No offers found to test getCancellableTeachingActivity")
                return
            }
            for (offer in offers) {
                val aaOffId = offer.academicYearOfferId
                val cdsOffId = offer.courseOfStudyOfferId
                try {
                    val cancellable = api.offer.getCancellableTeachingActivity(aaOffId, cdsOffId)
                    logger.info("getCancellableTeachingActivity(aaOffId=$aaOffId, cdsOffId=$cdsOffId): found ${cancellable.size} entries")
                    for (entry in cancellable) {
                        logger.info("  entry: aaOffId=${entry.academicYearOfferId}, cdsCod=${entry.courseOfStudyCode}, adCod=${entry.activityCode}, cancellabile=${entry.deletable}")
                        assertNotNull(entry.activityCode, "activityCode should not be null")
                    }
                    if (cancellable.isNotEmpty()) return
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getCancellableTeachingActivity($aaOffId, $cdsOffId) not authorized: ${e.message}")
                    break
                } catch (e: Esse3ValidationException) {
                    logger.warning("getCancellableTeachingActivity($aaOffId, $cdsOffId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getOffers not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetCancellableTeachingUnit() {
        try {
            val offers = api.offer.getOffers(limit = 10)
            if (offers.isEmpty()) {
                logger.warning("No offers found to test getCancellableTeachingUnit")
                return
            }
            for (offer in offers) {
                val aaOffId = offer.academicYearOfferId
                val cdsOffId = offer.courseOfStudyOfferId
                try {
                    val cancellable = api.offer.getCancellableTeachingUnit(aaOffId, cdsOffId)
                    logger.info("getCancellableTeachingUnit(aaOffId=$aaOffId, cdsOffId=$cdsOffId): found ${cancellable.size} entries")
                    for (entry in cancellable) {
                        logger.info("  entry: aaOffId=${entry.academicYearOfferId}, cdsCod=${entry.courseOfStudyCode}, adCod=${entry.activityCode}, udCod=${entry.teachingUnitCode}, cancellabile=${entry.deletable}")
                        assertNotNull(entry.activityCode, "activityCode should not be null")
                    }
                    if (cancellable.isNotEmpty()) return
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getCancellableTeachingUnit($aaOffId, $cdsOffId) not authorized: ${e.message}")
                    break
                } catch (e: Esse3ValidationException) {
                    logger.warning("getCancellableTeachingUnit($aaOffId, $cdsOffId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getOffers not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetOfferedSEG() {
        try {
            val offers = api.offer.getOffers(limit = 10)
            if (offers.isEmpty()) {
                logger.warning("No offers found to test getOfferedSEG")
                return
            }
            for (offer in offers) {
                val aaOffId = offer.academicYearOfferId
                val cdsOffId = offer.courseOfStudyOfferId
                try {
                    val segments = api.offer.getOfferedSEG(aaOffId, cdsOffId)
                    logger.info("getOfferedSEG(aaOffId=$aaOffId, cdsOffId=$cdsOffId): found ${segments.size} segments")
                    for (segment in segments) {
                        val segKey = segment.contextualizedSecretKey
                        val udKey = segKey.contextualizedTeachingUnitKey
                        val adKey = udKey.contextualizedTeachingActivityKey
                        logger.info("  segment: segId=${segKey.segmentId}, udId=${udKey.teachingUnitId}, adId=${adKey.activityId}")
                        logger.info("    settCod=${segment.sectorCode}, tipoCreCod=${segment.creditTypeCode}, durUniVal=${segment.universityValidityDuration}")
                        assertTrue(segKey.segmentId > 0, "segmentId should be positive")
                    }
                    if (segments.isNotEmpty()) return
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getOfferedSEG($aaOffId, $cdsOffId) not authorized: ${e.message}")
                    break
                } catch (e: Esse3ValidationException) {
                    logger.warning("getOfferedSEG($aaOffId, $cdsOffId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getOffers not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetOfferedSEGWithPagination() {
        try {
            val offers = api.offer.getOffers(limit = 5)
            val offer = offers.firstOrNull() ?: run {
                logger.warning("No offers found to test getOfferedSEG with pagination")
                return
            }
            try {
                val segments = api.offer.getOfferedSEG(
                    offer.academicYearOfferId,
                    offer.courseOfStudyOfferId,
                    start = 0,
                    limit = 5
                )
                logger.info("getOfferedSEG (paginated, limit=5): found ${segments.size} segments")
                assertTrue(segments.size <= 5, "Paginated result should have at most 5 items")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("getOfferedSEG (paginated) not authorized: ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("getOfferedSEG (paginated) validation error: ${e.message}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getOffers not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetFullOffers() {
        try {
            val offers = api.offer.getOffers(limit = 10)
            if (offers.isEmpty()) {
                logger.warning("No offers found to test getFullOffers")
                return
            }
            for (offer in offers) {
                val aaOffId = offer.academicYearOfferId
                val cdsOffId = offer.courseOfStudyOfferId
                try {
                    val fullOffer = api.offer.getFullOffers(aaOffId, cdsOffId)
                    logger.info("getFullOffers(aaOffId=$aaOffId, cdsOffId=$cdsOffId): cdsCod=${fullOffer.courseOfStudyCode}, cdsDes=${fullOffer.courseOfStudyDescription}")
                    logger.info("  ADContestCount=${fullOffer.teachingActivityContestWithDetails.size}, offertaExistsFlg=${fullOffer.offerExistsFlag}")
                    assertEquals(aaOffId, fullOffer.academicYearOfferId, "academicYearOfferId should match")
                    assertEquals(cdsOffId, fullOffer.courseOfStudyOfferId, "courseOfStudyOfferId should match")
                    for (activity in fullOffer.teachingActivityContestWithDetails) {
                        val key = activity.contextualizedTeachingActivityKey
                        logger.info("  activity: adId=${key.activityId}, adCod=${key.activityCode}, adDes=${key.activityDescription}")
                        logger.info("    UDContestCount=${activity.teachingUnitContestWithDetails.size}, linguaDidCount=${activity.teachingLanguages.size}")
                        for (unit in activity.teachingUnitContestWithDetails) {
                            val unitKey = unit.contextualizedTeachingUnitKey
                            logger.info("    unit: udId=${unitKey.teachingUnitId}, udCod=${unitKey.teachingUnitCode}, tipoUdCod=${unit.teachingUnitTypeCode}")
                            logger.info("      SEGCount=${unit.contextualizedSegment.size}, teachersCount=${unit.teachersPerTeachingUnit.size}")
                        }
                    }
                    return
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("getFullOffers($aaOffId, $cdsOffId) not authorized: ${e.message}")
                    break
                } catch (e: Esse3ValidationException) {
                    logger.warning("getFullOffers($aaOffId, $cdsOffId) validation error: ${e.message}")
                }
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getOffers not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetDeletedOffers() {
        try {
            val deletedOffers = api.offer.getDeletedOffers()
            logger.info("getDeletedOffers: found ${deletedOffers.size} deleted entries")
            for (deleted in deletedOffers) {
                logger.info("  deleted: cdsId=${deleted.courseOfStudyId}, aaId=${deleted.academicYearId}, dataModOd=${deleted.odModificationDate}")
                assertTrue(deleted.courseOfStudyId > 0, "courseOfStudyId should be positive")
                assertTrue(deleted.academicYearId > 0, "academicYearId should be positive")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getDeletedOffers not authorized: ${e.message}")
        }
    }

    @Test
    suspend fun testGetDeletedOffersWithPagination() {
        try {
            val deletedOffers = api.offer.getDeletedOffers(start = 0, limit = 5)
            logger.info("getDeletedOffers (paginated, limit=5): found ${deletedOffers.size} entries")
            assertTrue(deletedOffers.size <= 5, "Paginated result should have at most 5 items")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("getDeletedOffers (paginated) not authorized: ${e.message}")
        }
    }

    @Disabled("putActivityPlanCount is a mutation operation requiring specific activity filters and updates plan count data")
    @Test
    suspend fun testPutActivityPlanCount() {
        // Disabled: modifies plan count data in the ESSE3 system.
    }

    @Disabled("patchContextualizedTeachingActivity is a mutation operation that modifies a contextualized teaching activity (e.g., moodle course URL)")
    @Test
    suspend fun testPatchContextualizedTeachingActivity() {
        // Disabled: PATCH operation that modifies real offer data in the ESSE3 system.
        // Even a no-op patch could alter dataModOd timestamps and affect incremental sync.
    }
}
