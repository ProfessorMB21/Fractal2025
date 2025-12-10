package ru.gr05307.math

import kotlin.math.sqrt
import kotlin.math.absoluteValue

class Complex(var re: Double = 0.0, var im: Double = 0.0) {

    inline operator fun plus(other: Complex) = Complex(re + other.re, im + other.im)
    inline operator fun minus(other: Complex) = Complex(re - other.re, im - other.im)
    inline operator fun times(other: Complex) = Complex(re * other.re - im * other.im, re * other.im + im * other.re)
    inline operator fun div(other: Complex): Complex {
        val denom = other.re * other.re + other.im * other.im
        return Complex(
            (re * other.re + im * other.im) / denom,
            (im * other.re - re * other.im) / denom
        )
    }

    inline operator fun plusAssign(other: Complex) {
        re += other.re
        im += other.im
    }

    inline operator fun minusAssign(other: Complex) {
        re -= other.re
        im -= other.im
    }

    inline operator fun timesAssign(other: Complex) {
        val r = re * other.re - im * other.im
        im = re * other.im + im * other.re
        re = r
    }

    inline operator fun divAssign(other: Complex) {
        val denom = other.re * other.re + other.im * other.im
        val r = (re * other.re + im * other.im) / denom
        im = (im * other.re - re * other.im) / denom
        re = r
    }

    val abs: Double get() = sqrt(re * re + im * im)
    val abs2: Double get() = re * re + im * im

    fun conjugate() = Complex(re, -im)

    override fun equals(other: Any?) = other is Complex && re == other.re && im == other.im
    override fun hashCode() = 31 * re.hashCode() + im.hashCode()

    override fun toString(): String {
        return when {
            im == 0.0 -> "$re"
            re == 0.0 -> "${im}i"
            im > 0 -> "$re+${im}i"
            else -> "$re${im}i"
        }
    }
}