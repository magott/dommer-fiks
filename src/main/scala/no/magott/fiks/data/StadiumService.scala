package no.magott.fiks.data

import argonaut.{DecodeJson, Parse}
import no.magott.fiks.data

import scala.io.Source
import geo.{LatLong, CoordinateConversion}
import com.google.common.cache.{Cache, LoadingCache, CacheBuilder}
import scala.util.Properties
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.query.Imports._
import MongoSetting._
import java.util.concurrent.TimeUnit.DAYS
import dispatch.{Http, as, url}
import scala.concurrent.Future
import unfiltered.request.{Host, HttpRequest}
import no.magott.fiks.HerokuRedirect.XForwardProto
import scalaz.Scalaz._


import scalaz.\/

class StadiumService {

  private val MongoSetting(db) = Properties.envOrNone("MONGOLAB_URI")

  private val stadiumCache:Cache[String, Stadium] = CacheBuilder.newBuilder.maximumSize(500).expireAfterWrite(30, DAYS).expireAfterAccess(30, DAYS).build()

  def findStadium(name:String) = {
    Option(stadiumCache.getIfPresent(name))
    .orElse{
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

  def insertStadium(s:MongoStadium) = {
    db("stadium").save(s.toMongo)
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

  def lookupStadiumViaGjermshus(matchId:String) : String \/ data.MongoStadium = {
    import dispatch._, Defaults._
    import scalaz.Scalaz._
    val gjermhusService = url(s"http://fiksservice.littinnsikt.no/Farena/Farena/$matchId")
    val http = Http(gjermhusService OK as.String).either //.map(_.disjunction)
    http() match {
      case Right(s) => parseGjermshusResponse(s)
      case Left(e) => {
        e.printStackTrace()
        e.getMessage.left
      }
    }
  }

  def parseGjermshusResponse(http: String) : String \/ MongoStadium = {
      val either = Parse.decodeEither[MongoStadium](http)
      either
  }
}

sealed trait Stadium{
  import fix.UriString._
  def name:String
  def latLong:LatLong
  def googleMapsLink = uri"http://maps.google.com/?daddr=${latLong.lat.toString},${latLong.long.toString}"
}

case class FileStadium(name:String, status:String, owner:String, utmNorth:Int, utmEast:Int, utmZone:Int) extends Stadium{
  def latLong : LatLong = {
    val ll = (new CoordinateConversion).utm2LatLon("%s V %s %s".format(utmZone, utmEast, utmNorth))
    LatLong(ll(0), ll(1))
  }
}

case class MongoStadium(name:String,latLong: LatLong) extends Stadium {
  def toMongo = {
    MongoDBObject("name" -> name, "latLong" -> latLong.toMongo)
  }
  import fix.UriString._
  def insertLink(r:HttpRequest[_]) = {
    uri"${XForwardProto.unapply(r).getOrElse("http")}://${Host.unapply(r).get}/stadium/new?stadiumName=${name}&lat=${latLong.lat.toString}&long=${latLong.long.toString}"
  }
}

object MongoStadium{
  def fromMongo(m:MongoDBObject) = MongoStadium(m.as[String]("name"), m.getAs[DBObject]("latLong").map(LatLong.fromMongo(_)).get)
  implicit val clickEventDecoder: DecodeJson[MongoStadium] = DecodeJson(
    c => for {
      name <- (c --\ "Name").as[String]
      latLong <- (c --\ "LatLong").as[LatLong]
    } yield {
      MongoStadium(name, latLong)
    }
  )
}
