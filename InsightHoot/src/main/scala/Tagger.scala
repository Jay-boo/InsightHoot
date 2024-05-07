import com.github.vickumar1981.stringdistance.StringDistance.Levenshtein
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import scala.collection.mutable.LinkedHashMap
import scala.collection.Map


object Tagger {
  case class DocumentTag(tag:String,theme:String)

  val jsonTags=ujson.read(os.read(os.pwd/"src"/"main"/"resources"/"tags.json"))
  var tags:Seq[DocumentTag]=Seq.empty[DocumentTag]
  jsonTags.obj.foreach({
    case (theme:String,tagsList)=>
      tagsList.arr.foreach(
        tag=> tags:+= DocumentTag(tag.str,theme)
      )
  })
//  println("Tags:",tags,tags.length)


  //  val mapTags= jsonTags.obj.mapValues({
  //    case ujson.Arr(arr) => arr.map(_.str.toLowerCase)
  //    case _ => Seq.empty[String]
  //  })
  //  val mapTags:LinkedHashMap[String,Seq[String]] =jsonTags.arr
//  val tags:Map[String,Seq[String]]=jsonTags.obj.mapValues(value=>value.arr.map(_.str).toSeq)
//  val tags: Seq[String]=Seq("hello")


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
    val dfWithTags: DataFrame = df.map { case Row(title: String, relevant_tokens: Seq[String]) =>
      val tags = getDocumentTags(relevant_tokens)
      (title, relevant_tokens, tags)
    }.toDF("title", "relevant_tokens", "tags")
    dfWithTags
  }


  def main(args: Array[String]): Unit = {
    println(getDocumentTags(Seq("Javac","java","lover")))
  }

}
