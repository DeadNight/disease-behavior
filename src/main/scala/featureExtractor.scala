import java.io.{FileWriter, PrintWriter}
import java.time.ZonedDateTime
import java.time.temporal.{ChronoUnit, WeekFields}
import java.util.Locale

import org.opensextant.geodesy.Geodetic2DArc

/**
  * Created by nirle on 5/7/2017.
  */
object featureExtractor extends App {
  import scala.util.parsing.json._

  var lastDataPoint: DataPoint = null
  val dataPoints: Seq[DataPoint] = DAO.loadDataPoints()
  val data =
    JSONObject(dataPoints.groupBy(dp => dp.deviceId).map{ case (deviceId, byDevice) => deviceId.toString -> {
      JSONObject(Map(
        "email" -> byDevice.head.email,
        "weeks" -> {
          JSONObject(byDevice.groupBy(dp => getWeek(dp.date)).toSeq.sortBy(_._1).map { case (week, byWeek) => week.toString -> {
            JSONObject(Map(
              "minDay" -> byWeek.minBy(dp => dp.date.getDayOfYear).date.truncatedTo(ChronoUnit.DAYS).toOffsetDateTime.toString,
              "maxDay" -> byWeek.maxBy(dp => dp.date.getDayOfYear).date.truncatedTo(ChronoUnit.DAYS).toOffsetDateTime.toString,
              "days" -> {
                JSONObject(byWeek.groupBy(dp => dp.date.getDayOfYear).toSeq.sortBy(_._1).map { case (day, byDay) => day.toString -> {
                  JSONObject(Map(
                    "date" -> byDay.head.date.truncatedTo(ChronoUnit.DAYS).toOffsetDateTime.toString,
                    "totalDistance" -> (byDay.maxBy(dp => dp.totalDistance).totalDistance - byDay.minBy(p => p.totalDistance).totalDistance),
                    "data" -> JSONArray(byDay.sortBy(_.date.toEpochSecond).map(dp => {
                      if (lastDataPoint == null || lastDataPoint.deviceId != deviceId)
                        lastDataPoint = dp
                      val gap = lastDataPoint.date.until(dp.date, ChronoUnit.MINUTES)

                      val obj = JSONObject(Map(
                        "timestamp" -> dp.date.toOffsetDateTime.toString,
                        "speed" -> dp.speed,
                        "distance" -> dp.distance,
                        "gap" -> gap,
                        "latitude" -> dp.geoPosition.getLatitude.inDegrees,
                        "longitude" -> dp.geoPosition.getLongitude.inDegrees
                      ))
                      lastDataPoint = dp
                      obj
                    }).toList)
                  ))
                }}.toMap)
              }
            ))
          }}.toMap)
        }
      ))
    }})

  val writer: PrintWriter = new PrintWriter(new FileWriter("features.json"))
  writer.println(data.toString())
  writer.close()

  def getWeek(date: ZonedDateTime): Int = {
    date.get(WeekFields.of(Locale.getDefault).weekOfWeekBasedYear)
  }
}
