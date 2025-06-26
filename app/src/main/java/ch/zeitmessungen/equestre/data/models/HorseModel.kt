package ch.zeitmessungen.equestre.data.models
//  It is a List => [{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":1,"mother":"","name":"Name Pferd","owner":"","passport":"Pass Nr Pf","signalementLabel":"Signalement"},{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":2,"mother":"","name":"TOP STAR DES DANNES","owner":"","passport":"112263","signalementLabel":"W,Br,16,SF"},{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":3,"mother":"","name":"MARTINE II","owner":"","passport":"137567","signalementLabel":"S,F,11,PONY"},{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":4,"mother":"","name":"Q' HERO DREAM M.M CH","owner":"","passport":"150121","signalementLabel":"W,F,8,CH"},{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":5,"mother":"","name":"FEAU","owner":"","passport":"129269","signalementLabel":"S,Br,13,NED"},{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":6,"mother":"","name":"ALMORETO","owner":"","passport":"149767","signalementLabel":"W,Br,5,SVK"},{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":7,"mother":"","name":"ROSA VIII","owner":"","passport":"140589","signalementLabel":"S,Br,7,SUI"},{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":8,"mother":"","name":"K ALTIMA DUDAIE","owner":"","passport":"147288","signalementLabel":"S,Sch,13,BEL"},{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":9,"mother":"","name":"I'M LEGEND CH","owner":"","passport":"143640","signalementLabel":"W,F,6,CH"},{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":10,"mother":"","name":"EDDY VON ESTEBAN","owner":"","passport":"130080","signalementLabel":"W,Br,14,HOLST"},{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":11,"mother":"","name":"VINSENT DI VILLA FRANCESCA","owner":"","passport":"128415","signalementLabel":"W,Br,15,ITA"},{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":12,"mother":"","name":"HERMIONE D'ALLOBROGIE","owner":"","passport":"143685","signalementLabel":"S,Br,6,PONY"},{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":13,"mother":"","name":"D COLIBRI Z","owner":"","passport":"146475","signalementLabel":"W,Br,6,ZH"},{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":14,"mother":"","name":"CHIRAC DU RUISSEAU Z","owner":"","passport":"126556","signalementLabel":"W,Br,17,ZH"},{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":15,"mother":"","name":"BELLINO DELLA LOGGIA","owner":"","passport":"149208","signalementLabel":"W,Br,5,ITA"},{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":16,"mother":"","name":"ZOLFATIC","owner":"","passport":"145797","signalementLabel":"S,Sch,16,ITA"},{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":17,"mother":"","name":"CRASH FLYING","owner":"","passport":"149444","signalementLabel":"W,Sch,4,SUI"},{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":18,"mother":"","name":"FLACHITO","owner":"","passport":"103364","signalementLabel":"W,Rappe,15,SUI"},{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":19,"mother":"","name":"HELODIE DE LA CENSE","owner":"","passport":"148509","signalementLabel":"S,Br,6,SF"},{"age":0,"birthday":"1900-01-01T00:00:00Z","father":"","fatherOfMother":"","gender":"Male","idx":20,"mother":"","name":"GIN TONIC SR","owner":"","passport":"146359","signalementLabel":"W,DBr,6,WEST"},
import org.json.JSONArray
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

data class HorseModel(
    val age: Int?,
    val birthday: Date?,
    val father: String?,
    val fatherOfMother: String?,
    val gender: String?,
    val idx: Int?,
    val mother: String?,
    val name: String?,
    val owner: String?,
    val passport: String?,
    val signalementLabel: String?
) {
    companion object {
        private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

        fun fromJson(json: JSONObject): HorseModel {
            val birthdayString = json.optString("birthday", null)
            val birthdayDate = try {
                birthdayString?.let { isoDateFormat.parse(it) }
            } catch (e: ParseException) {
                null
            }

            return HorseModel(
                age = if (json.has("age")) json.optInt("age") else null,
                birthday = birthdayDate,
                father = json.optString("father", null),
                fatherOfMother = json.optString("fatherOfMother", null),
                gender = json.optString("gender", null),
                idx = if (json.has("idx")) json.optInt("idx") else null,
                mother = json.optString("mother", null),
                name = json.optString("name", null),
                owner = json.optString("owner", null),
                passport = json.optString("passport", null),
                signalementLabel = json.optString("signalementLabel", null)
            )
        }

        fun fromJsonArray(array: JSONArray): List<HorseModel> {
            val list = mutableListOf<HorseModel>()
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                list.add(fromJson(obj))
            }
            return list
        }
    }
}
