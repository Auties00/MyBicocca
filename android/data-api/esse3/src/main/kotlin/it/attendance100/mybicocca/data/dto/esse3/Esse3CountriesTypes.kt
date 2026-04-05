package it.attendance100.mybicocca.data.dto.esse3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Esse3Country(
    /** ID numerico univoco della nazione */
    @SerialName("nazioneId")
    val nationId: Long? = null,

    /** Codice di 4 cifre Lettera + 3 numeri: Znnn - la laettera è sempre una Z. Nel caso dell´Italia questo attributo non è istanziato. */
    @SerialName("codFisc")
    val fiscalCode: String? = null,

    /** Codice di tre cifre - 3 numeri, utilizzato per descrivere la nazione. Italia 200 Albania 201 ... */
    @SerialName("cod")
    val code: String? = null,

    /** descrizione */
    @SerialName("des")
    val description: String? = null,

    /** Se anno inizio validità è non nullo deve essere istanziato anche l attributo Codifica_Precedente, che contiene l indicazione dello stato da cui ha avuto origine la nazione codificata. Ad esempio dalla iugoslavia hanno avuto origine   la SLOVENIA, la CROAZIA, la SERBIA-MONTENEGRO. Le n uove codifiche presenteranno indicazione della codifica della nazione da cui hanno avuto origine. Se la nazione di origine non ha più validità viene indicato anche l anno di fine validità. */
    @SerialName("dataInizioVal")
    val evaluationStartDate: String? = null,

    /** Data fine validità nazione. */
    @SerialName("dataFineVal")
    val evaluationEndDate: String? = null,

    /** Codifica nazione precedente, nel caso di nazione che sostituisce una nazione non più valida. */
    @SerialName("codificaPrec")
    val previousEncoding: Long? = null,

    /** Indica se la nazione viene gestita a livello di comuni e province codifcate. */
    @SerialName("comuCodifFlg")
    val municipalityCodedFlag: Int? = null,

    /** ID codice MIUR */
    @SerialName("nazioneCod")
    val nationCode: String? = null,

    /** Codice CSA, utilizzato per mappare le nazioni durante l´allineamento docenti dal sistema CSA. */
    @SerialName("csaCod")
    val csaCode: String? = null,

    /** Indica che è un territorio ceduto. */
    @SerialName("territorioCedutoFlg")
    val cededTerritoryFlag: Int? = null,

    /** Struttura codice IBAN. */
    @SerialName("strIban")
    val ibanString: String? = null,

    /** Prefisso internazionale. */
    @SerialName("prefixInternazionale")
    val internationalPrefix: String? = null,

    /** La gestione delle coordinate bancarie è equiparata a quella italiana. */
    @SerialName("equipCorsoBancaItaFlg")
    val italianBankCourseEquivalentFlag: Int? = null,

    /** Codifica ISO 3166 numerica a tre cifre */
    @SerialName("codIso31661")
    val iso31661Code: String? = null
)

@Serializable
data class Esse3PostalCode(
    /** ID numerico univoco della nazione */
    @SerialName("nazioneId")
    val nationId: Long? = null,

    /** Codice di 4 cifre Lettera + 3 numeri: Znnn - la laettera è sempre una Z. Nel caso dell´Italia questo attributo non è istanziato. */
    @SerialName("nazioneCodFisc")
    val nationFiscalCode: String? = null,

    /** ID numerico della regione */
    @SerialName("regioneId")
    val regionId: Long? = null,

    /** ID numerico univoco della nazione */
    @SerialName("regioneCod")
    val regionCode: String? = null,

    /** Descrizione regione */
    @SerialName("regioneDes")
    val regionDescription: String? = null,

    /** Sigla automobilistica della provincia */
    @SerialName("sigla")
    val abbreviation: String? = null,

    /** Codice ISTAT di 3 cifre utilizzato per indicare la provincia. Esempio 084 Agrigento */
    @SerialName("provinciaCod")
    val provinceCode: String? = null,

    /** descrizione provincia */
    @SerialName("provinciaDes")
    val provinceDescription: String? = null,

    /** ID numerico unico del comune */
    @SerialName("comuneId")
    val municipalityId: Long? = null,

    /** Codice di 4 cifre (Lettera + 3 numeri) che è utilizzato nel codice fiscale per indicare il comune di nascita. */
    @SerialName("comuneCod")
    val municipalityCode: String? = null,

    /** descrizione comune */
    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    /** Codice di 5 cifre - OBSOLETO. */
    @SerialName("comuCap")
    val municipalityPostalCode: String? = null,

    /** Identificativo CAP */
    @SerialName("capId")
    val postalCodeId: Long? = null,

    /** Codice Avviamento postale del comune/località */
    @SerialName("cap")
    val postalCode: String? = null,

    /** Descizione della zona del cap */
    @SerialName("capDes")
    val postalCodeDescription: String? = null
)

@Serializable
data class Esse3Province(
    /** ID numerico univoco della nazione */
    @SerialName("nazioneId")
    val nationId: Long? = null,

    /** Codice di 4 cifre Lettera + 3 numeri: Znnn - la laettera è sempre una Z. Nel caso dell´Italia questo attributo non è istanziato. */
    @SerialName("nazioneCodFisc")
    val nationFiscalCode: String? = null,

    /** ID numerico della regione */
    @SerialName("regioneId")
    val regionId: Long? = null,

    /** Codice di due cifre utilizzato dall´Istat per indicare la regione: 19 Sicilia 01 Piemonte */
    @SerialName("regioneCod")
    val regionCode: String? = null,

    /** Descrizione */
    @SerialName("regioneDes")
    val regionDescription: String? = null,

    /** ID numerico univoco della nazione */
    @SerialName("codRegioVulc")
    val vulcanoRegionCode: String? = null,

    /** Sigla automobilistica della provincia */
    @SerialName("sigla")
    val abbreviation: String? = null,

    /** Codice ISTAT di 3 cifre utilizzato per indicare la provincia. Esempio 084 Agrigento */
    @SerialName("provinciaCod")
    val provinceCode: String? = null,

    /** Descrizione provincia */
    @SerialName("provinciaDes")
    val provinceDescription: String? = null,

    /** Anno di definizione della provincia. */
    @SerialName("annoDefinizione")
    val definitionYear: Long? = null,

    /** Valorizzato nel caso di separazioni di province, ad esempio: Prato e Firenze Rimini e Forlì ... L attributo è valorizzato nel caso IN cui sia introdotto un anno di definizione (Anno_Definizione) - Esempio di Rimini: - RN Emilia-Romagna Rimini 099 1993 FO */
    @SerialName("provinciaPrecedente")
    val previousProvince: String? = null,

    /** Anno di fine validità della provincia. */
    @SerialName("annoFineValidita")
    val validityEndYear: Long? = null,

    /** Indica l'attivazione o meno della provincia 0) è attiva; 1) è obsoleta; 2) non esiste più perché è stata accorpata da un altra  provincia. */
    @SerialName("attivoId")
    val activeId: Int? = null
)

@Serializable
data class Esse3Municipality(
    /** ID numerico univoco della nazione */
    @SerialName("nazioneId")
    val nationId: Long? = null,

    /** Codice di 4 cifre Lettera + 3 numeri: Znnn - la laettera è sempre una Z. Nel caso dell´Italia questo attributo non è istanziato. */
    @SerialName("nazioneCodFisc")
    val nationFiscalCode: String? = null,

    /** ID numerico della regione */
    @SerialName("regioneId")
    val regionId: Long? = null,

    /** ID numerico univoco della nazione */
    @SerialName("regioneCod")
    val regionCode: String? = null,

    /** Descrizione regione */
    @SerialName("regioneDes")
    val regionDescription: String? = null,

    /** Sigla automobilistica della provincia */
    @SerialName("sigla")
    val abbreviation: String? = null,

    /** Codice ISTAT di 3 cifre utilizzato per indicare la provincia. Esempio 084 Agrigento */
    @SerialName("provinciaCod")
    val provinceCode: String? = null,

    /** descrizione provincia */
    @SerialName("provinciaDes")
    val provinceDescription: String? = null,

    /** ID numerico unico del comune */
    @SerialName("comuneId")
    val municipalityId: Long? = null,

    /** Id comune per tabelle MIUR */
    @SerialName("idComune")
    val municipalityRefId: Long? = null,

    /** Codice di 4 cifre (Lettera + 3 numeri) che è utilizzato nel codice fiscale per indicare il comune di nascita. */
    @SerialName("comuneCod")
    val municipalityCode: String? = null,

    /** Codice di 6 cifre utilizzato dall´ISTAT per indicare il comune */
    @SerialName("codIstat")
    val istatCode: String? = null,

    /** Codice di 5 cifre - OBSOLETO. */
    @SerialName("cap")
    val postalCode: String? = null,

    /** descrizione comune */
    @SerialName("comuneDes")
    val municipalityDescription: String? = null,

    /** Mappatura verso la tabella ministeriale dei Comuni */
    @SerialName("codIstatMiur")
    val miurIstatCode: String? = null,

    /** Distanza in Km del comune dall´ateneo. Serve per il calcolo delle tasse Dal bando del 2000/01 Studenti IN sede: l importo massimo della borsa di studio per gli studenti residenti nel comune di Trento o nei comuni limitrofi, è di lire 4.500.000. Per gli studenti residenti IN luoghi che permettono di raggiungere quotidianamente la sede del Corso di studi prescelto, l entità della singola borsa sarà determinata anche IN base alla distanza fino ad un limite di 5.100.000 di lire; */
    @SerialName("distanzaKm")
    val distanceKm: Long? = null,

    /** Variazioni collegate al comune (ORA: Cambio denominazione, AGG: aggregato, AGP: aggregato in parte, AGT: aggregato temporaneamente, VED: Soggetti ad ulteriori cambiamenti) */
    @SerialName("var")
    val variable: String? = null,

    /** Variazione di codice. */
    @SerialName("varCod")
    val variableCode: String? = null,

    /** Variazione di sigla. */
    @SerialName("varSigla")
    val variableAbbreviation: String? = null,

    /** Variazione di descrizione. */
    @SerialName("varDes")
    val variableDescription: String? = null,

    /** Data costituzione comune */
    @SerialName("dataCostit")
    val constitutionDate: String? = null,

    /** ID numerico unico del comune */
    @SerialName("newComuneId")
    val newMunicipalityId: Long? = null,

    /** Indica l'attivazione o meno del comune. 0) il comune è attivo; 1) il comune è obsoleto, ad esempio per la nascita di una nuova provincia; 2) Il comune non esiste più perché è stato accorpato da un altro comune; si tenga presente che il suo codice fiscale è quello valido nella composizione del C.F. di un cittadino nato nel comune quand'era attivo. */
    @SerialName("attivoId")
    val activeId: Int? = null
)
