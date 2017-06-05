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
    JSONObject(positions.groupBy(p => p.deviceId).map{ case (deviceId, byDevice) => deviceId.toString -> {
      JSONObject(Map(
        "email" -> byDevice.head.email,
        "weeks" -> {
          JSONObject(byDevice.groupBy(p => getWeek(p.date)).map { case (week, byWeek) => week.toString -> {
            JSONObject(Map(
              "minDay" -> byWeek.minBy(p => p.date.toLocalDate.toEpochDay).date.toLocalDate.toString,
              "maxDay" -> byWeek.maxBy(p => p.date.toLocalDate.toEpochDay).date.toLocalDate.toString,
              "days" -> {
                JSONObject(byWeek.groupBy(p => p.date.toLocalDate).map { case (day, byDay) => day.toString -> {
                  JSONObject(Map(
                    "date" -> byDay.head.date.toLocalDate.toString,
                    "totalDistance" -> (byDay.maxBy(p => p.totalDistance).totalDistance - byDay.minBy(p => p.totalDistance).totalDistance),
                    "data" -> JSONArray(byDay.map(p => JSONObject(Map("timestamp" -> p.date.toString, "speed" -> p.speed, "distance" -> p.distance))).toList)
                  ))
                }})
              }
            ))
          }})
        }
      ))
    }})

  val writer: PrintWriter = new PrintWriter(new FileWriter("features.json"))
  writer.println(data.toString())
  writer.close()

  def getWeek(date: LocalDateTime): Int = {
    date.get(WeekFields.of(Locale.getDefault).weekOfWeekBasedYear())
  }
}
