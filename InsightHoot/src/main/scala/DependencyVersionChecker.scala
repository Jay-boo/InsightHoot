import java.net.URLClassLoader
import scala.util.matching.Regex

object DependencyVersionChecker extends App {
  def getJarVersion(jarUrl: String): String = {
    // Regex to match version numbers in JAR file names
    val versionPattern: Regex = ".*-([0-9]+\\.[0-9]+\\.[0-9]+)\\.jar".r
    jarUrl match {
      case versionPattern(version) => version
      case _ => "unknown"
    }
  }

  def run() {
    val classLoader = this.getClass.getClassLoader
    classLoader match {
      case urlClassLoader: URLClassLoader =>
        val urls = urlClassLoader.getURLs
        urls.foreach { url =>
          val urlPath = url.getPath
          if (urlPath.endsWith(".jar")) {
            val version = getJarVersion(urlPath)
            println(s"JAR: $urlPath, Version: $version")
          }
        }
      case _ => println("Not a URLClassLoader")
    }
  }
}