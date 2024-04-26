class MyTests extends munit.FunSuite {
  test("sum of two integers") {
    val obtained = 2 + 2
    val expected = 4
    assertEquals(obtained, expected)
  }
}
