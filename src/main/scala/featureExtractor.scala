import java.io.{FileWriter, PrintWriter}
import java.time.LocalDateTime
import java.time.temporal.WeekFields
import java.util.Locale

/**
  * Created by nirle on 5/7/2017.
  */
object featureExtractor extends App {
  import scala.util.parsing.json._

  val positions: Seq[Position] = DAO.loadPositions()
  val data =
    JSONObject(positions.groupBy(p => p.email).map{ case (email, byDevice) => email -> {
      JSONObject(byDevice.groupBy(p => getWeek(p.date)).map { case (week, byWeek) => week.toString ->
        JSONArray(byWeek.map(p => JSONObject(Map("timestamp" -> f"${p.date}", "speed" -> p.speed, "attributes" -> p.attributes))).toList)
      }) } })

  val writer: PrintWriter = new PrintWriter(new FileWriter("features.json"))
  writer.println(data.toString())
  writer.close()

  def getWeek(date: LocalDateTime): Int = {
    date.get(WeekFields.of(Locale.getDefault).weekOfWeekBasedYear())
  }
}
