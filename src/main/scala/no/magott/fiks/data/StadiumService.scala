package no.magott.fiks.data

import scala.io.Source
import geo.{LatLong, CoordinateConversion}
import com.google.common.cache.{Cache, LoadingCache, CacheBuilder}
import scala.util.Properties
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.query.Imports._
import MongoSetting._
import java.util.concurrent.TimeUnit.DAYS

class StadiumService {

  private val MongoSetting(db) = Properties.envOrNone("MONGOLAB_URI")

  private val stadiumCache:Cache[String, Stadium] = CacheBuilder.newBuilder.weakKeys.weakValues.expireAfterWrite(30, DAYS).build()

  def findStadium(name:String) = {
    Option(stadiumCache.getIfPresent(name)) orElse{
      val mongoStadium = stadiumFromMongo(name)
      mongoStadium.foreach(s => stadiumCache.put(s.name, s))
      mongoStadium
    } orElse {
      val fileStadium = stadiumsFromFile.find(_.name.contains(name))
      fileStadium.foreach(s => stadiumCache.put(s.name, s))
      fileStadium
    }
  }

  def stadiumFromMongo(name:String) = {
    db("stadium").findOne(MongoDBObject("name" -> name)).map(MongoStadium.fromMongo(_))
  }

  def stadiumsFromFile:List[Stadium] = {
    val file = fromFile
    file.map(parseCsvStadiumLine)
  }

  def fromFile = {
    val stream = getClass.getResourceAsStream("/stadium.csv")
    val content = Source.fromInputStream(stream,"UTF-8")
    val lines = content.getLines().toList.drop(1)
    lines
  }

  def parseCsvStadiumLine(line:String) = {
    val tokens = line.split(";")
    FileStadium(tokens(0), tokens(2), tokens(3), tokens(16).toInt, tokens(17).toInt, tokens(18).toInt)
  }
}

sealed trait Stadium{
  import fix.UriString._
  def name:String
  def latLongPosition:LatLong
  def googleMapsLink = uri"http://maps.google.com/?daddr=${latLongPosition.lat.toString},${latLongPosition.long.toString}"
}

case class FileStadium(name:String, status:String, owner:String, utmNorth:Int, utmEast:Int, utmZone:Int) extends Stadium{
  def latLongPosition : LatLong = {
    val latLong = (new CoordinateConversion).utm2LatLon("%s V %s %s".format(utmZone, utmEast, utmNorth))
    LatLong(latLong(0), latLong(1))
  }
}

case class MongoStadium(name:String,latLongPosition: LatLong) extends Stadium {
  def toMongo = {

  }
}

object MongoStadium{
  def fromMongo(m:MongoDBObject) = MongoStadium(m.as[String]("name"), m.getAs[DBObject]("latLong").map(LatLong.fromMongo(_)).get)
}
