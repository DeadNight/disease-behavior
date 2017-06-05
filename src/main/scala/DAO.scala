import java.time.{LocalDateTime, ZoneId, ZoneOffset, ZonedDateTime}
import java.util.TimeZone

import jdk.nashorn.internal.parser.JSONParser

import scala.collection.mutable
import scala.util.parsing.json.{JSON, JSONObject}

/**
  * Created by nirle on 3/30/2017.
  */
object DAO {
  val connectionUrl: String = "jdbc:sqlserver://localhost;databaseName=traccar;integratedSecurity=true"

  def loadPositions(): Seq[Position] = {
    import java.sql.DriverManager

    val positions = mutable.ListBuffer[Position]()

    try {
      // Load SQL Server JDBC driver and establish connection.
      val connection = DriverManager.getConnection(connectionUrl)
      try {
        val statement = connection.prepareStatement(
          "select d.id, d.name, p.servertime, p.speed, p.attributes" +
          " from devices d" +
          " inner join positions p" +
          " on d.id = p.deviceid" +
          " where d.name not in('Lior', 'Tal', 'Ron', 'maayan')" +
          " order by d.id, p.servertime")
        val result = statement.executeQuery()
        while(result.next()) {
          val attributes: JSONObject = JSON.parseRaw(result.getString("attributes")).getOrElse(JSONObject(Map.empty)).asInstanceOf[JSONObject]
          positions += Position(
            result.getLong("id"),
            result.getString("name"),
            ZonedDateTime.ofInstant(result.getTimestamp("servertime").toLocalDateTime, ZoneOffset.ofHours(2), TimeZone.getDefault.toZoneId),
            result.getDouble("speed"),
            attributes.obj("distance").asInstanceOf[Double],
            attributes.obj("totalDistance").asInstanceOf[Double])
        }
      } finally {
        if (connection != null)
          connection.close()
      }
    } catch {
      case e: Exception =>
        System.out.println()
        e.printStackTrace()
    }

    positions
  }
}
