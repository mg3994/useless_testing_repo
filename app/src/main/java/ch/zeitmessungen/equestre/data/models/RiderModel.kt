package ch.zeitmessungen.equestre.data.models
// It is a List => [{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"","idx":1,"lastName":"Reiter","license":"Lizenz Nr","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Sagliocco","idx":2,"lastName":"Cloe","license":"338280","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Galli","idx":3,"lastName":"Martina","license":"331069","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Bernasconi","idx":4,"lastName":"Jacopo","license":"129857","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Martusciello","idx":5,"lastName":"Roberta","license":"332193","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Camponovo","idx":6,"lastName":"Martina","license":"301366","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Serrano","idx":7,"lastName":"Abilio Jorge","license":"222443","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Pagani","idx":8,"lastName":"Daisy","license":"327458","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Ineichen","idx":9,"lastName":"Estelle","license":"321360","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Luca","idx":10,"lastName":"Nikita","license":"267696","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Cavadini","idx":11,"lastName":"Cinzia","license":"293445","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Gottardi","idx":12,"lastName":"Lisa De","license":"337353","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Carelli","idx":13,"lastName":"Milena","license":"316882","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Palermi","idx":14,"lastName":"Mauro","license":"331068","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Calzascia","idx":15,"lastName":"Emma","license":"329333","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Romaneschi","idx":16,"lastName":"Katiuscia","license":"329919","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Galimberti","idx":17,"lastName":"Corrado","license":"311161","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Brunschwiler","idx":18,"lastName":"Beatrice","license":"126246","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Keller","idx":19,"lastName":"Jennifer","license":"267693","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Gianetta","idx":20,"lastName":"Silvia","license":"222786","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Crotta","idx":21,"lastName":"Sabrina","license":"213031","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Camozzi","idx":22,"lastName":"Linda","license":"256225","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Gottardi","idx":23,"lastName":"Nora De","license":"335381","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Teichmann","idx":24,"lastName":"Nicole","license":"312503","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Agustoni","idx":25,"lastName":"Emma","license":"310941","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Neumann","idx":26,"lastName":"Isabel","license":"325301","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Lüthi","idx":27,"lastName":"Regina","license":"295700","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Donati","idx":28,"lastName":"Pietro","license":"293086","nation":""},{"birthday":"1900-01-01T00:00:00Z","city":"","club":"","firstName":"Sanvito","idx":29,"lastName":"Riccardo","license":"341276","nation":""},
import org.json.JSONArray
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

data class RiderModel(
    val birthday: Date?,
    val city: String?,
    val club: String?,
    val firstName: String?,
    val idx: Int?,
    val lastName: String?,
    val license: String?,
    val nation: String?
) {
    companion object {
        private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

        fun fromJson(json: JSONObject): RiderModel {
            val birthdayString = json.optString("birthday", null)
            val birthdayDate = try {
                birthdayString?.let { isoDateFormat.parse(it) }
            } catch (e: ParseException) {
                null
            }

            return RiderModel(
                birthday = birthdayDate,
                city = json.optString("city", null),
                club = json.optString("club", null),
                firstName = json.optString("firstName", null),
                idx = if (json.has("idx")) json.optInt("idx") else null,
                lastName = json.optString("lastName", null),
                license = json.optString("license", null),
                nation = json.optString("nation", null)
            )
        }

        fun fromJsonArray(array: JSONArray): List<RiderModel> {
            val list = mutableListOf<RiderModel>()
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                list.add(fromJson(obj))
            }
            return list
        }
    }
}
