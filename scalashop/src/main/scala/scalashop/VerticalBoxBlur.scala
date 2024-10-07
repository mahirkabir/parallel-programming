package scalashop

import org.scalameter.*

object VerticalBoxBlurRunner:

  val standardConfig = config(
    Key.exec.minWarmupRuns := 5,
    Key.exec.maxWarmupRuns := 10,
    Key.exec.benchRuns := 10,
    Key.verbose := false
  ) withWarmer (Warmer.Default())

  def main(args: Array[String]): Unit =
    val radius = 3
    val width = 1920
    val height = 1080
    val src = Img(width, height)
    val dst = Img(width, height)
    val seqtime = standardConfig measure {
      VerticalBoxBlur.blur(src, dst, 0, width, radius)
    }
    println(s"sequential blur time: $seqtime")

    val numTasks = 32
    val partime = standardConfig measure {
      VerticalBoxBlur.parBlur(src, dst, numTasks, radius)
    }
    println(s"fork/join blur time: $partime")
    println(s"speedup: ${seqtime.value / partime.value}")

/** A simple, trivially parallelizable computation. */
object VerticalBoxBlur extends VerticalBoxBlurInterface:

  /** Blurs the columns of the source image `src` into the destination image
    * `dst`, starting with `from` and ending with `end` (non-inclusive).
    *
    * Within each column, `blur` traverses the pixels by going from top to
    * bottom.
    */
  def blur(src: Img, dst: Img, from: Int, end: Int, radius: Int): Unit =
    // Make sure that from and end are valid
    var fromClamped = clamp(from, 0, src.width)
    var endClamped = clamp(end, 0, src.width)
    var x = fromClamped
    while (x < endClamped) {
      var y = 0
      while (y < src.height) {
        dst.update(x, y, boxBlurKernel(src, x, y, radius))
        y += 1
      }
      x += 1
    }

  /** Blurs the columns of the source image in parallel using `numTasks` tasks.
    *
    * Parallelization is done by stripping the source image `src` into
    * `numTasks` separate strips, where each strip is composed of some number of
    * columns.
    */
  def parBlur(src: Img, dst: Img, numTasks: Int, radius: Int): Unit =
    // Make sure the numTasks is valid
    var numTasksClamped = clamp(numTasks, 1, src.width)
    var stripWidth = math.ceil(src.width.toDouble / numTasksClamped).toInt
    var points = 0 until src.width by stripWidth
    var pointsFinal = points :+ src.width
    // ChatGPT helped with the next line
    var fromEnds = pointsFinal.zip(pointsFinal.tail)

    var tasks = fromEnds.map { case (from, end) =>
      task { blur(src, dst, from, end, radius) }
    }

    tasks.foreach(task => task.join())
