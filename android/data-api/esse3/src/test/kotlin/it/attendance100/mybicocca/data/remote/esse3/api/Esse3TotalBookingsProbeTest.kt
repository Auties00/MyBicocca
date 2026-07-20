package it.attendance100.mybicocca.data.remote.esse3.api

import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BookableExamFilter
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.minutes

/**
 * Live probe for issue #7 (show the total number of bookings, Esse3's `numIscritti`).
 *
 * Establishes empirically, against the real Esse3 instance, which endpoint can provide the
 * total for a *booked* call. Per the OpenAPI specs:
 * - `/prenotazioni` (IscrizioneAppello) declares no `numIscritti` at all;
 * - `/libretti/{matId}/appelli` (AppelloLibretto) inherits it from Appello, but both `q`
 *   filters ("APPELLI_PRENOTABILI*") exclude calls the student already booked;
 * - `/appelli/{cds}/{ad}/{app}` (detail) declares it, at a 1.7–4 s cold cost.
 *
 * The Ktor client logs full bodies (LogLevel.ALL), so the raw JSON of every probe is in the
 * test output — useful to spot fields the DTOs do not declare.
 */
class Esse3TotalBookingsProbeTest : Esse3ApiTestBase() {

    companion object {
        private val logger = Logger.getLogger(Esse3TotalBookingsProbeTest::class.java.name)
    }

    @Test
    fun probeTotalBookingsSources() = runTest(timeout = 5.minutes) {
        val segments = api.transcript.getCareerSegments()
        val matId = segments.firstOrNull()?.matId
            ?: error("No career segment with a matId; cannot probe")
        logger.info("PROBE: matId=$matId")

        // ── 1) Bookable list exactly as the app requests it ─────────────────────────────
        // Expectation: booked calls ABSENT (the q filter excludes them); numIscritti may or
        // may not be honoured for the calls that do appear.
        val bookable = api.transcript.getRecordBookExamCalls(
            matId = matId,
            q = Esse3BookableExamFilter.AppelliPrenotabiliEFuturi,
            optionalFields = "dataInizioApp,oraEsa,dataInizioIscr,dataFineIscr,tipoEsaCod," +
                    "note,presidenteNome,presidenteCognome,presidenteId,tipoGestPrenDes,numIscritti",
        )
        logger.info("PROBE 1 (q=PRENOTABILI_E_FUTURI): ${bookable.size} calls")
        bookable.forEach {
            logger.info(
                "PROBE 1: cds=${it.courseOfStudyId} ad=${it.activityId} app=${it.callId} " +
                        "numIscritti=${it.enrolledNumber} — ${it.activityDescription}",
            )
        }

        // ── 3) Bookings with optionalFields=ALL ─────────────────────────────────────────
        // The DTO declares no total, but ALL forces the server to emit every field it has:
        // check the raw JSON in the Ktor log for any count-like field the spec hides.
        val bookings = api.transcript.getBookingsByMatId(
            matId = matId,
            actorCode = "STU",
            optionalFields = "ALL",
        )
        logger.info("PROBE 3 (prenotazioni, optionalFields=ALL): ${bookings.size} rows — inspect raw JSON above")
        bookings.forEach {
            logger.info(
                "PROBE 3: cds=${it.courseOfStudyId} ad=${it.activityId} app=${it.callId} " +
                        "posiz=${it.position} posizApp=${it.applicationPosition} — ${it.studentActivityDescription}",
            )
        }

        // ── 4) Per-appello detail for each booked call (plan C, cost included) ──────────
        // The 1.7–4 s endpoint the repository bans for lists; measured here to judge whether
        // a single lazy call on the booked-exam detail page is acceptable.
        bookings.take(3).forEach { booking ->
            val cds = booking.courseOfStudyId ?: return@forEach
            val ad = booking.activityId?.toLong() ?: return@forEach
            val app = booking.callId?.toLong() ?: return@forEach
            runCatching {
                var enrolled: Int?
                val elapsed = measureTimeMillis {
                    val detail = api.examsCalendar.getExamCall(
                        courseOfStudyId = cds,
                        activityId = ad,
                        callId = app,
                        optionalFields = "numIscritti",
                    )
                    enrolled = detail.enrolledNumber
                }
                logger.info("PROBE 4: detail cds=$cds ad=$ad app=$app numIscritti=$enrolled in ${elapsed}ms")
            }.onFailure {
                logger.info("PROBE 4: detail cds=$cds ad=$ad app=$app FAILED: ${it.message}")
            }
        }

        // ── 2, LAST and non-fatal) Libretto appelli WITHOUT the q filter ────────────────
        // First run: timed out at 30 s — the unfiltered list is the expensive computation
        // the repo KDoc warned about, so it is NOT viable as a batch source. Kept here only
        // to record the finding; failure no longer kills the other probes.
        runCatching {
            val unfiltered = api.transcript.getRecordBookExamCalls(
                matId = matId,
                optionalFields = "numIscritti",
            )
            logger.info("PROBE 2 (no q): ${unfiltered.size} calls")
            unfiltered.forEach {
                logger.info(
                    "PROBE 2: cds=${it.courseOfStudyId} ad=${it.activityId} app=${it.callId} " +
                            "numIscritti=${it.enrolledNumber} — ${it.activityDescription}",
                )
            }
        }.onFailure {
            logger.info("PROBE 2 (no q): FAILED (${it.message}) — unfiltered list not viable")
        }
    }
}
