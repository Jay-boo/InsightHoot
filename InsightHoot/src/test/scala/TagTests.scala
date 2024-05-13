import org.apache.spark.sql.DataFrame

import scala.io.Source

class TagTests extends munit.FunSuite with SparkMachine {
  test("sum of two integers") {
    val obtained = 2 + 2
    val expected = 4
    assertEquals(obtained, expected)
  }

  test("getPayload() test"){
    val df: DataFrame=spark.read.parquet("src/test/resources/raw_data/topic_name=4sysops/4sysops_02052024.parquet")
    df.show()
    val payloadDF:DataFrame=Main.getPayload(df)
  }


  test("tagDF() test"){

  }

}
