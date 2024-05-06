import com.github.vickumar1981.stringdistance.StringDistance.Levenshtein
import org.apache.spark.mllib.linalg._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

object Tagger {
  val jsonTags=ujson.read(os.read(os.pwd/"src"/"main"/"resources"/"tags.json"))
  val tags: Seq[String] =jsonTags("tags").arr.map(_.str.toLowerCase)



  def getTokenTag(token:String,min_score:Double=0.5): Option[String]={
    val scores=tags.map(Levenshtein.score(token.toLowerCase(),_))
    val maxIndex=scores.indexOf(scores.max)
    println(token,tags,scores)
    if (scores.max >= min_score){
      Some(tags(maxIndex))
    }else{
      None
    }
  }

  def getDocumentTags(tokens:Seq[String],min_score:Double=0.8):Seq[String]={
    val documentTokens:Seq[Option[String]]=tokens.map(getTokenTag(_,min_score=min_score))
    val result:Seq[String]=documentTokens.flatMap(_.toList)
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
