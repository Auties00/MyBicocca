package it.attendance100.mybicocca.data.api.esse3

import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class Esse3StructureApiTest : Esse3ApiTestBase() {

    private val logger = Logger.getLogger(this::class.java.name)

    @Test
    suspend fun testGetDisciplinaryAreas() {
        val areas = api.structure.getDisciplinaryAreas()
        assertNotNull(areas)
        assertTrue(areas.isNotEmpty(), "Disciplinary areas should not be empty")
        logger.info("getDisciplinaryAreas: found ${areas.size} areas")

        for (area in areas) {
            assertNotNull(area, "Each area should not be null")
            val code = area.disciplinaryAreaCode
            logger.info("  area: code=$code, des=${area.disciplinaryAreaDescription}")

            val single = api.structure.getDisciplinaryArea(code)
            assertNotNull(single)
            assertTrue(single.disciplinaryAreaCode == code, "getDisciplinaryArea($code) code should match")
            logger.info("  getDisciplinaryArea($code): ok")
        }
    }

    @Test
    suspend fun testGetDisciplinaryAreasWithFilter() {
        val areas = api.structure.getDisciplinaryAreas(disciplinaryAreaDescription = "")
        assertNotNull(areas)
        logger.info("getDisciplinaryAreas (empty filter): found ${areas.size} areas")
    }

    @Test
    suspend fun testGetCoursesOfStudy() {
        val courses = api.structure.getCoursesOfStudy()
        assertNotNull(courses)
        assertTrue(courses.isNotEmpty(), "Courses of study should not be empty")
        logger.info("getCoursesOfStudy: found ${courses.size} courses")

        for (course in courses.take(3)) {
            assertNotNull(course)
            assertNotNull(course.courseOfStudyId, "courseOfStudyId should not be null")
            logger.info("  course: id=${course.courseOfStudyId}, code=${course.courseOfStudyCode}, des=${course.courseOfStudyDescription}")
        }
    }

    @Test
    suspend fun testGetCoursesOfStudyWithFilters() {
        // Filter by orderActiveFlag
        val activeCourses = api.structure.getCoursesOfStudy(orderActiveFlag = 1, limit = 5)
        assertNotNull(activeCourses)
        logger.info("getCoursesOfStudy (orderActiveFlag=1, limit=5): found ${activeCourses.size} courses")

        // Filter by orderEnableEnrollmentFlag
        val enrollmentCourses = api.structure.getCoursesOfStudy(orderEnableEnrollmentFlag = 1, limit = 5)
        assertNotNull(enrollmentCourses)
        logger.info("getCoursesOfStudy (orderEnableEnrollmentFlag=1, limit=5): found ${enrollmentCourses.size} courses")
    }

    @Test
    suspend fun testGetCourseOfStudyForStudent() {
        val courseOfStudyId = studentProfile.degreeCourseId
        val course = api.structure.getCourseOfStudy(courseOfStudyId)
        assertNotNull(course)
        assertTrue(
            course.courseOfStudyId == courseOfStudyId,
            "getCourseOfStudy($courseOfStudyId): returned id should match"
        )
        logger.info(
            "getCourseOfStudy($courseOfStudyId): code=${course.courseOfStudyCode}, des=${course.courseOfStudyDescription}"
        )
    }

    @Test
    suspend fun testGetStudyOrdersAndPaths() {
        val courseOfStudyId = studentProfile.degreeCourseId

        val orders = api.structure.getStudyOrders(courseOfStudyId)
        assertNotNull(orders)
        assertTrue(orders.isNotEmpty(), "Study orders should not be empty for student's course")
        logger.info("getStudyOrders($courseOfStudyId): found ${orders.size} orders")

        for (order in orders) {
            assertNotNull(order)
            val aaOrdId = order.academicYearOrderId
            logger.info("  order: aaOrdId=$aaOrdId, des=${order.courseOfStudyOrderDescription}")

            val single = api.structure.getStudyOrder(courseOfStudyId, aaOrdId)
            assertNotNull(single)
            assertTrue(single.academicYearOrderId == aaOrdId, "getStudyOrder aaOrdId should match")
            logger.info("  getStudyOrder($courseOfStudyId, $aaOrdId): ok")

            val paths = api.structure.getPaths(courseOfStudyId, aaOrdId)
            assertNotNull(paths)
            logger.info("  getPaths($courseOfStudyId, $aaOrdId): found ${paths.size} paths")

            for (path in paths) {
                assertNotNull(path)
                val studyPlanId = path.studyPlanId
                logger.info("    path: studyPlanId=$studyPlanId, des=${path.studyPlanDescription}")

                val singlePath = api.structure.getPath(courseOfStudyId, aaOrdId, studyPlanId)
                assertNotNull(singlePath)
                assertTrue(singlePath.studyPlanId == studyPlanId, "getPath studyPlanId should match")
                logger.info("    getPath($courseOfStudyId, $aaOrdId, $studyPlanId): ok")

                // Test access titles for study plan
                try {
                    val accessTitles = api.structure.getAccessTitlesStudyPlanOrder(
                        courseOfStudyId, aaOrdId, studyPlanId, "A"
                    )
                    assertNotNull(accessTitles)
                    logger.info("    getAccessTitlesStudyPlanOrder($courseOfStudyId,$aaOrdId,$studyPlanId,A): ${accessTitles.size} titles")
                } catch (e: Esse3NotAuthorizedException) {
                    logger.warning("    Skipping getAccessTitlesStudyPlanOrder: insufficient permissions - ${e.message}")
                } catch (e: Esse3ValidationException) {
                    logger.warning("    Skipping getAccessTitlesStudyPlanOrder: validation error - ${e.message}")
                }
            }

            // Test access titles per order
            try {
                val accessTitlesOrder = api.structure.getAccessTitlesOrder(courseOfStudyId, aaOrdId, "A")
                assertNotNull(accessTitlesOrder)
                logger.info("  getAccessTitlesOrder($courseOfStudyId,$aaOrdId,A): ${accessTitlesOrder.size} titles")
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("  Skipping getAccessTitlesOrder: insufficient permissions - ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("  Skipping getAccessTitlesOrder: validation error - ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetAccessTitles() {
        val courseOfStudyId = studentProfile.degreeCourseId
        try {
            val accessTitles = api.structure.getAccessTitles(courseOfStudyId, "A")
            assertNotNull(accessTitles)
            logger.info("getAccessTitles($courseOfStudyId, A): ${accessTitles.size} titles")
            for (title in accessTitles) {
                assertNotNull(title)
                logger.info("  title: typologyCode=${title.typologyCode}")
            }
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping getAccessTitles: insufficient permissions - ${e.message}")
        } catch (e: Esse3ValidationException) {
            logger.warning("Skipping getAccessTitles: validation error - ${e.message}")
        }
    }

    @Test
    suspend fun testGetCourseAccessTitles() {
        val courses = api.structure.getCoursesOfStudy(limit = 3)
        for (course in courses) {
            val code = course.courseOfStudyCode ?: continue
            try {
                val titles = api.structure.getCourseAccessTitles(code, "A")
                assertNotNull(titles)
                logger.info("getCourseAccessTitles($code, A): ${titles.size} titles")
                for (title in titles) {
                    assertNotNull(title)
                    assertTrue(title.courseOfStudyCode.isNotBlank(), "courseOfStudyCode should not be blank")
                }
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("Skipping getCourseAccessTitles($code): insufficient permissions - ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("Skipping getCourseAccessTitles($code): validation error - ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetCourseCharacteristics() {
        val courses = api.structure.getCoursesOfStudy(orderActiveFlag = 1, limit = 3)
        val currentYear = 2024
        for (course in courses) {
            val code = course.courseOfStudyCode ?: continue
            try {
                val characteristics = api.structure.getCourseCharacteristics(code, currentYear - 1, currentYear)
                assertNotNull(characteristics)
                logger.info("getCourseCharacteristics($code): ${characteristics.size} entries")
                for (ch in characteristics) {
                    assertNotNull(ch)
                    logger.info("  characteristic: cdsId=${ch.courseOfStudyId}, academicYear=${ch.academicYear}")
                }
                break
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("Skipping getCourseCharacteristics($code): insufficient permissions - ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("Skipping getCourseCharacteristics($code): validation error - ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetCoursePositions() {
        val courses = api.structure.getCoursesOfStudy(orderActiveFlag = 1, limit = 3)
        val currentYear = 2024
        for (course in courses) {
            val code = course.courseOfStudyCode ?: continue
            try {
                val positions = api.structure.getCoursePositions(code, currentYear - 1, currentYear)
                assertNotNull(positions)
                logger.info("getCoursePositions($code): ${positions.size} entries")
                for (pos in positions) {
                    assertNotNull(pos)
                    logger.info("  position: cdsId=${pos.courseOfStudyId}")
                }
                break
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("Skipping getCoursePositions($code): insufficient permissions - ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("Skipping getCoursePositions($code): validation error - ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetCourseDeadlines() {
        val courses = api.structure.getCoursesOfStudy(orderActiveFlag = 1, limit = 3)
        for (course in courses) {
            val code = course.courseOfStudyCode ?: continue
            val aaId = course.academicYearActivityId ?: continue
            try {
                val deadlines = api.structure.getCourseDeadlines(code, aaId)
                assertNotNull(deadlines)
                logger.info("getCourseDeadlines($code, $aaId): ${deadlines.size} entries")
                for (d in deadlines) {
                    assertNotNull(d)
                    logger.info("  deadline: cdsId=${d.courseOfStudyId}, academicYear=${d.academicYear}")
                }
                break
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("Skipping getCourseDeadlines($code): insufficient permissions - ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("Skipping getCourseDeadlines($code): validation error - ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetCourseTaxes() {
        val courses = api.structure.getCoursesOfStudy(orderActiveFlag = 1, limit = 3)
        for (course in courses) {
            val code = course.courseOfStudyCode ?: continue
            val aaId = course.academicYearActivityId ?: continue
            try {
                val taxes = api.structure.getCourseTaxes(code, aaId)
                assertNotNull(taxes)
                logger.info("getCourseTaxes($code, $aaId): ${taxes.size} entries")
                for (tax in taxes) {
                    assertNotNull(tax)
                    logger.info("  tax: cdsId=${tax.courseOfStudyId}, academicYearId=${tax.academicYear_Id}")
                }
                break
            } catch (e: Esse3NotAuthorizedException) {
                logger.warning("Skipping getCourseTaxes($code): insufficient permissions - ${e.message}")
            } catch (e: Esse3ValidationException) {
                logger.warning("Skipping getCourseTaxes($code): validation error - ${e.message}")
            }
        }
    }

    @Test
    suspend fun testGetFullCourseOfStudy() {
        val courseOfStudyId = studentProfile.degreeCourseId
        try {
            val full = api.structure.getFullCourseOfStudy(courseOfStudyId)
            assertNotNull(full)
            logger.info("getFullCourseOfStudy($courseOfStudyId): code=${full.courseOfStudyCode}, des=${full.courseOfStudyDescription}")
        } catch (e: Esse3NotAuthorizedException) {
            logger.warning("Skipping getFullCourseOfStudy: insufficient permissions - ${e.message}")
        }
    }

    @Test
    suspend fun testGetDeletedCoursesOfStudy() {
        val deleted = api.structure.getDeletedCoursesOfStudy()
        assertNotNull(deleted)
        logger.info("getDeletedCoursesOfStudy: found ${deleted.size} deleted courses")
        for (d in deleted.take(5)) {
            assertNotNull(d)
            assertNotNull(d.courseOfStudyId, "courseOfStudyId should not be null")
            logger.info("  deleted: cdsId=${d.courseOfStudyId}, dataMod=${d.courseOfStudyModificationDate}")
        }
    }

    @Test
    suspend fun testGetDeletedCoursesOfStudyWithLimit() {
        val deleted = api.structure.getDeletedCoursesOfStudy(limit = 10, start = 0)
        assertNotNull(deleted)
        assertTrue(deleted.size <= 10, "Should return at most 10 results")
        logger.info("getDeletedCoursesOfStudy (limit=10): found ${deleted.size} deleted courses")
    }

    @Test
    suspend fun testGetExternalEntities() {
        val entities = api.structure.getExternalEntities()
        assertNotNull(entities)
        logger.info("getExternalEntities: found ${entities.size} entities")
        for (entity in entities.take(5)) {
            assertNotNull(entity)
            logger.info("  entity: id=${entity.entityId}, code=${entity.entityCode}, type=${entity.entityTypeCode}")
        }
    }

    @Test
    suspend fun testGetExternalEntitiesWithFilters() {
        val entities = api.structure.getExternalEntities(limit = 5, start = 0)
        assertNotNull(entities)
        assertTrue(entities.size <= 5, "Should return at most 5 results")
        logger.info("getExternalEntities (limit=5): found ${entities.size} entities")
    }

    @Test
    suspend fun testGetSitesAndSingleSite() {
        val sites = api.structure.getSites()
        assertNotNull(sites)
        assertTrue(sites.isNotEmpty(), "Sites should not be empty")
        logger.info("getSites: found ${sites.size} sites")

        for (site in sites) {
            assertNotNull(site)
            val siteId = site.siteId
            logger.info("  site: id=$siteId, des=${site.siteDescription}")

            val single = api.structure.getSite(siteId)
            assertNotNull(single)
            assertTrue(single.siteId == siteId, "getSite($siteId) id should match")
            logger.info("  getSite($siteId): ok")
        }
    }

    @Test
    suspend fun testGetSitesWithFilters() {
        // Filter by courseOfStudyId
        val courseOfStudyId = studentProfile.degreeCourseId
        val sites = api.structure.getSites(courseOfStudyId = courseOfStudyId)
        assertNotNull(sites)
        logger.info("getSites (courseOfStudyId=$courseOfStudyId): found ${sites.size} sites")
    }

    @Test
    suspend fun testGetDidacticStructures() {
        val structures = api.structure.getDidacticStructures()
        assertNotNull(structures)
        assertTrue(structures.isNotEmpty(), "Didactic structures should not be empty")
        logger.info("getDidacticStructures: found ${structures.size} structures")

        for (structure in structures) {
            assertNotNull(structure)
            val facultyId = structure.facultyId
            logger.info("  structure: facultyId=$facultyId, code=${structure.facultyCode}, des=${structure.facultyDescription}")

            val single = api.structure.getDidacticStructures(facultyId)
            assertNotNull(single)
            logger.info("  getDidacticStructures($facultyId): ok")
        }
    }

    @Test
    suspend fun testGetDidacticStructuresWithFilters() {
        val courses = api.structure.getCoursesOfStudy(orderActiveFlag = 1, limit = 1)
        val courseCode = courses.firstOrNull()?.courseOfStudyCode ?: return
        val structures = api.structure.getDidacticStructures(courseOfStudyCode = courseCode)
        assertNotNull(structures)
        logger.info("getDidacticStructures (cdsCod=$courseCode): found ${structures.size} structures")
    }

    @Test
    suspend fun testGetCourseTypes() {
        val types = api.structure.getCourseTypes()
        assertNotNull(types)
        assertTrue(types.isNotEmpty(), "Course types should not be empty")
        logger.info("getCourseTypes: found ${types.size} types")

        for (type in types) {
            assertNotNull(type)
            logger.info("  type: code=${type.courseTypeCode}, des=${type.courseTypeDescription}")
            assertNotNull(type.courseTypeCode, "courseTypeCode should not be null")
        }
    }

    @Test
    suspend fun testGetCourseTypesWithFilters() {
        // Filter with phdFlag
        val phdTypes = api.structure.getCourseTypes(phdFlag = 1)
        assertNotNull(phdTypes)
        logger.info("getCourseTypes (phdFlag=1): found ${phdTypes.size} types")

        // Filter with specializationSchoolFlag
        val specTypes = api.structure.getCourseTypes(specializationSchoolFlag = 1)
        assertNotNull(specTypes)
        logger.info("getCourseTypes (scuolaSpecFlg=1): found ${specTypes.size} types")
    }
}
