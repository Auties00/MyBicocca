package it.attendance100.mybicocca.data.remote.esse3.api

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.utils.io.ByteReadChannel
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BadgeData
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BadgeMatriculationStateFilter
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3BadgeStudentStateFilter
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import kotlinx.serialization.json.Json

class Esse3BadgeApi(
    client: HttpClient,
    json: Json
) : Esse3AbstractApi(client, json, "/badge-service-v1") {

    /**
     * recupera i dati del badge
     *
     * @param rfid rfid associato al badge dello studente
     * @param studentId identificativo della carriera
     * @param fiscalCode codice fiscale della persona
     * @param courseOfStudyCode corso di studio di iscrizione dello studente
     * @param academicYearAnnualEnrollment anno di iscrizione
     * @param studentStatusCode stato studente
     * @param matStatusCode stato matricola
     * @param order consente di specificare un ordine per il recupero dei record. La sintassi è la seguente * +/- : specifica l'ordinamento (+ = ASC, - = DESC); se omesso viene utilizzato + * field : nome del campo da ordinare E' possibile indicare più campi separandoli da virgola (Es: +annoCorso,+adCod)
     * @param fields specifica la lista dei campi opzionali (che non vengono recupeati di default); impostando il valore `ALL` vengono mostrati tutti i campi. Per gli oggetti è possibile utilizzare la notazione *Ant Glob Patterns* I dettagli sono presenti [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosuicampidelleclassidimodello) Esempi * per il campo childObj con la poprietà childProp utilizzare la notazione `childProp.prop1` * Per visualizzare tutte le proprietà di childObj utilizzare la notazione `childProp.*` * Per visualizzare tutte le proprietà di childObj e di tutti i discendenti utilizzare la notazione `childProp.**`
     * @param filter il parametro consente di applicare dei filtri alla classe di modello utilizzando il linguaggio  [RSQL](https://github.com/jirutka/rsql-parser). La lista degli operatori utilizzabili è disponibile [qui](https://wiki.u-gov.it/confluence/display/ESSE3/Servizi+REST+su+ESSE3#ServiziRESTsuESSE3-Filtrosullerigherecuperate) *NB* il filtro viene applicato DOPO aver recuperato i dati
     */
    suspend fun getNewBadges(
        rfid: String? = null,
        studentId: Long? = null,
        fiscalCode: String? = null,
        courseOfStudyCode: String? = null,
        academicYearAnnualEnrollment: Int? = null,
        studentStatusCode: Esse3BadgeStudentStateFilter? = null,
        matStatusCode: Esse3BadgeMatriculationStateFilter? = null,
        order: String? = null,
        fields: String? = null,
        filter: String? = null
    ): List<Esse3BadgeData> {
        return executeJsonGetList<Esse3BadgeData>("/badges", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT)) {
            rfid?.let { parameter("rfid", it) }
            studentId?.let { parameter("stuId", it) }
            fiscalCode?.let { parameter("codFis", it) }
            courseOfStudyCode?.let { parameter("cdsCod", it) }
            academicYearAnnualEnrollment?.let { parameter("aaIscrAnn", it) }
            studentStatusCode?.let { parameter("staStuCod", it.value) }
            matStatusCode?.let { parameter("staMatCod", it.value) }
            order?.let { parameter("order", it) }
            fields?.let { parameter("fields", it) }
            filter?.let { parameter("filter", it) }
        }
    }

    /**
     * recupera la pagina frontale di un badge
     *
     * @param badgeBlobId id del blob con le immagini del badge
     */
    suspend fun getBadgeBlobFrontPage(
        badgeBlobId: Long
    ): ByteReadChannel {
        return executeStreamGet("/badges/blobs/${badgeBlobId}/front", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT))
    }

    /**
     * recupera la pagina posteriore di un badge
     *
     * @param badgeBlobId id del blob con le immagini del badge
     */
    suspend fun getBadgeBlobRearPage(
        badgeBlobId: Long
    ): ByteReadChannel {
        return executeStreamGet("/badges/blobs/${badgeBlobId}/rear", setOf(Esse3PermissionLevel.TECHNICAL_USER, Esse3PermissionLevel.STUDENT))
    }
}
