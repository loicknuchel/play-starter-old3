package common.models

// parse & transform HTML & CSS colors
case class Color(r: Int, g: Int, b: Int, a: Double, _valid: Boolean = false) {
  def withAlpha(a: Double): Color = this.copy(a = a)

  def darker(k: Int = 1) = {
    val f = Math.pow(0.8, k)
    this.copy(r = Math.round(r * f).toInt, g = Math.round(g * f).toInt, b = Math.round(b * f).toInt)
  }
  def lighter(k: Int = 1) = {
    val f = Math.pow(0.8, k)
    this.copy(r = Math.round(r / f).toInt, g = Math.round(g / f).toInt, b = Math.round(b / f).toInt)
  }

  /*private val lumDeviation: Double = 0.1
  private def changeLum(value: Int, lum: Double): Int = Math.round(Math.min(Math.max(0, value + (value * lum)), 255)).toInt

  def darker(k: Int = 1): Color = {
    val d = this.copy(r = changeLum(r, lumDeviation), g = changeLum(g, lumDeviation), b = changeLum(b, lumDeviation))
    if (k > 1) d.darker(k - 1) else d
  }
  def lighter(k: Int = 1): Color = {
    val d = this.copy(r = changeLum(r, -1 * lumDeviation), g = changeLum(g, -1 * lumDeviation), b = changeLum(b, -1 * lumDeviation))
    if (k > 1) d.darker(k - 1) else d
  }*/

  def toHexa: String = s"#${hex(r)}${hex(g)}${hex(b)}"
  def toRgb = s"rgb($r, $g, $b)"
  def toRgba = s"rgba($r, $g, $b, $a)"
  override def toString = toRgba

  private def hex(i: Int): String = {
    val value = if (i > 255) 255 else if (i < 0) 0 else i
    val str = Integer.toHexString(value)
    if (str.length() == 1) "0" + str else str
  }
}
object Color {
  val hexaPattern = "#([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})".r
  val smallHexaPattern = "#([0-9a-f])([0-9a-f])([0-9a-f])".r
  val rgbPattern = "rgb\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*\\)".r
  val rgbaPattern = "rgba\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*,\\s*([0-9]*\\.?[0-9]+)\\s*\\)".r
  val hslPattern = "hsl\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)%\\s*,\\s*([0-9]+)%\\s*\\)".r
  val hslaPattern = "hsla\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)%\\s*,\\s*([0-9]+)%\\s*,\\s*([0-9]*\\.?[0-9]+)\\s*\\)".r

  def apply(r: Int, g: Int, b: Int, a: Double) = {
    val _r = if (r > 255) 255 else if (r < 0) 0 else r
    val _g = if (g > 255) 255 else if (g < 0) 0 else g
    val _b = if (b > 255) 255 else if (b < 0) 0 else b
    val _a = if (a > 1) 1 else if (a < 0) 0 else a
    new Color(_r, _g, _b, _a, true)
  }

  def parse(color: String): Option[Color] = {
    parseHexa(color)
      .orElse(parseRgb(color))
      .orElse(parseRgba(color))
      .orElse(parseHsl(color))
      .orElse(parseHsla(color))
      .orElse(parseName(color))
  }
  def parseHexa(color: String): Option[Color] = {
    color.toLowerCase match {
      case hexaPattern(r, g, b) => Some(Color(Integer.parseInt(r, 16), Integer.parseInt(g, 16), Integer.parseInt(b, 16), 1))
      case smallHexaPattern(r, g, b) => Some(Color(Integer.parseInt(r + r, 16), Integer.parseInt(g + g, 16), Integer.parseInt(b + b, 16), 1))
      case _ => None
    }
  }
  def parseRgb(color: String): Option[Color] = {
    color.toLowerCase match {
      case rgbPattern(r, g, b) => Some(Color(r.toInt, g.toInt, b.toInt, 1))
      case _ => None
    }
  }
  def parseRgba(color: String): Option[Color] = {
    color.toLowerCase match {
      case rgbaPattern(r, g, b, a) => Some(Color(r.toInt, g.toInt, b.toInt, a.toDouble))
      case _ => None
    }
  }
  def parseHsl(color: String): Option[Color] = {
    color.toLowerCase match {
      case hslPattern(h, s, l) => {
        val (r, g, b) = hslToRgb(h.toDouble / 360, s.toDouble / 100, l.toDouble / 100)
        Some(Color(r, g, b, 1))
      }
      case _ => None
    }
  }
  def parseHsla(color: String): Option[Color] = {
    color.toLowerCase match {
      case hslaPattern(h, s, l, a) => {
        val (r, g, b) = hslToRgb(h.toDouble / 360, s.toDouble / 100, l.toDouble / 100)
        Some(Color(r, g, b, a.toDouble))
      }
      case _ => None
    }
  }
  def parseName(color: String): Option[Color] = {
    color.toLowerCase match {
      case "white" => parseHexa("#FFFFFF")
      case "silver" => parseHexa("#C0C0C0")
      case "gray" => parseHexa("#808080")
      case "black" => parseHexa("#000000")
      case "red" => parseHexa("#FF0000")
      case "maroon" => parseHexa("#800000")
      case "yellow" => parseHexa("#FFFF00")
      case "olive" => parseHexa("#808000")
      case "lime" => parseHexa("#00FF00")
      case "green" => parseHexa("#008000")
      case "aqua" => parseHexa("#00FFFF")
      case "teal" => parseHexa("#008080")
      case "blue" => parseHexa("#0000FF")
      case "navy" => parseHexa("#000080")
      case "fuchsia" => parseHexa("#FF00FF")
      case "purple" => parseHexa("#800080")
      case _ => None
    }
  }

  /**
   * Converts HSL color to RGB
   * from http://stackoverflow.com/questions/2353211/hsl-to-rgb-color-conversion
   * h, s, l are in [0, 1] interval
   * r, g, b are in [0, 255] interval
   *
   * @param   Double  h       The hue
   * @param   Double  s       The saturation
   * @param   Double  l       The lightness
   * @return  (Int, Int, Int) The RGB representation
   */
  def hslToRgb(h: Double, s: Double, l: Double): (Int, Int, Int) = {
    if (s == 0) (1, 1, 1)
    else {
      val q = if (l < 0.5) l * (1 + s) else l + s - l * s
      val p = 2 * l - q
      val r = hue2rgb(p, q, h + 1.0 / 3)
      val g = hue2rgb(p, q, h)
      val b = hue2rgb(p, q, h - 1.0 / 3)
      (Math.round(r * 255).toInt, Math.round(g * 255).toInt, Math.round(b * 255).toInt)
    }
  }

  /**
   * Converts RGB color to HSL
   * from http://stackoverflow.com/questions/2353211/hsl-to-rgb-color-conversion
   * r, g, b are in [0, 255] interval
   * h, s, l are in [0, 1] interval
   *
   * @param   Int  r       				The red color value
   * @param   Int  g       				The green color value
   * @param   Int  b       				The blue color value
   * @return  (Double, Double, Double)	The HSL representation
   */
  def rgbToHsl(r: Int, g: Int, b: Int): (Double, Double, Double) = {
    val _r = r.toDouble / 255
    val _g = g.toDouble / 255
    val _b = b.toDouble / 255
    val _max = max(_r, _g, _b)
    val _min = min(_r, _g, _b)
    val l = (_max + _min) / 2

    if (_max == _min) (0, 0, l)
    else {
      val d = _max - _min
      val s = if (l > 0.5) d / (2 - _max - _min) else d / (_max + _min)
      val _h =
        if (_max == _r) { (_g - _b) / d + (if (_g < _b) 6 else 0) }
        else if (_max == _g) { (_b - _r) / d + 2 }
        else { (_r - _g) / d + 4 }
      val h = _h / 6
      (h, s, l)
    }
  }

  private def hue2rgb(p: Double, q: Double, t: Double): Double = {
    val _t = if (t < 0) t + 1 else if (t > 1) t - 1 else t
    if (_t < 1.0 / 6) return p + (q - p) * 6 * _t
    if (_t < 1.0 / 2) return q
    if (_t < 2.0 / 3) return p + (q - p) * (2.0 / 3 - t) * 6
    return p
  }
  private def max(a: Double, b: Double, c: Double): Double = if (a >= b && a >= c) a else if (b >= a && b >= c) b else c
  private def min(a: Double, b: Double, c: Double): Double = if (a <= b && a <= c) a else if (b <= a && b <= c) b else c
}
