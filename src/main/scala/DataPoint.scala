import java.time.ZonedDateTime

import org.opensextant.geodesy.Geodetic2DPoint

/**
  * Created by nirle on 3/30/2017.
  */
case class DataPoint(
                      deviceId: Long,
                      email: String,
                      date: ZonedDateTime,
                      speed: Double,
                      distance: Double,
                      totalDistance: Double,
                      geoPosition: Geodetic2DPoint,
                      rawLatitude: Double,
                      rawLongitude: Double) {
  def dayPart: DayPart =  if(isDaytime) Day else  Night

  def isDaytime = !(isNightBefore || isNightAfter)

  def isNightBefore = date.getHour < 6

  def isNightAfter = date.getHour >= 22
}
