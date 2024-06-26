import com.github.vickumar1981.stringdistance.StringDistance.Levenshtein
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.functions._
import java.sql.Timestamp
import scala.io.Source


object Tagger {
  case class DocumentTag(tag:String,theme:String)

  val inputStream=getClass.getResourceAsStream("/tags.json")
  val jsonString=Source.fromInputStream(inputStream).mkString
  val jsonTags=ujson.read(jsonString)
  var tags:Seq[DocumentTag]=Seq.empty[DocumentTag]
  jsonTags.obj.foreach({
    case (theme:String,tagsList)=>
      tagsList.arr.foreach(
        tag=> tags:+= DocumentTag(tag.str,theme)
      )
  })

  def getTokenTag(token:String,min_score:Double=0.5): Option[DocumentTag]={
    val scores=tags.map(tag=>{Levenshtein.score(token.toLowerCase(),tag.tag.toLowerCase())})
    val maxIndex=scores.indexOf(scores.max)
    if (scores.max >= min_score){
      Some(tags(maxIndex))
    }else{
      None
    }
  }

  def getDocumentTags(tokens:Seq[String],min_score:Double=0.8):Seq[DocumentTag]={
    val documentTokens:Seq[Option[DocumentTag]]=tokens.map(getTokenTag(_,min_score=min_score))
    val result:Seq[DocumentTag]=documentTokens.flatMap(_.toList)
    result.distinct
  }

  def tagDF(df: DataFrame, spark: SparkSession): DataFrame = {
    import spark.implicits._
    val dfWithTags: DataFrame = df.map {
      case Row(title: String, feed_title:String,date: Timestamp, feed_url: String, link: String, content:String,relevant_tokens: Seq[String]) =>
        val tags = getDocumentTags(relevant_tokens)
        (title, feed_title,date, feed_url, link, content,relevant_tokens, tags)
      case Row(title: String,feed_title:String, date: Timestamp, feed_url: String, link: String, null,relevant_tokens: Seq[String]) =>
        val tags = getDocumentTags(relevant_tokens)
        (title, feed_title,date, feed_url, link, null,relevant_tokens, tags)
    }.toDF("title", "feed_title","date", "feed_url", "link", "content","relevant_tokens", "tags")
    dfWithTags
  }

  def main(args: Array[String]): Unit = {
    println(getDocumentTags(Seq("Javac","java","lover")))
  }

}
