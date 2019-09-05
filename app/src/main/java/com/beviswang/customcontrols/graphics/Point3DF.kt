package com.beviswang.customcontrols.graphics

import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable

/**
 * Point2D in 3D holds three float coordinates
 * @author BevisWang
 */
class Point3DF : Parcelable {
    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f

    constructor()

    constructor(parcel: Parcel) : this() {
        x = parcel.readFloat()
        y = parcel.readFloat()
        z = parcel.readFloat()
    }

    constructor(x: Float = 0f, y: Float = 0f, z: Float = 0f) : this() {
        set(x, y, z)
    }

    constructor(point: PointF, z: Float) : this(point.x, point.y, z)

    constructor(point3D: Point3DF) : this(point3D.x, point3D.y, point3D.z)

    /**
     * Set the point's x, y and z coordinates
     */
    fun set(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    /**
     * Negate the point's coordinates
     */
    fun negate() {
        x = -x
        y = -y
        z = -z
    }

    /**
     * Offset the point's coordinates by dx, dy, dz
     */
    fun offset(dx: Float, dy: Float, dz: Float) {
        x += dx
        y += dy
        z += dz
    }

    /**
     * Returns true if the point's coordinates equal (x,y,z)
     */
    fun equals(x: Float, y: Float, z: Float): Boolean {
        return this.x == x && this.y == y && this.z == z
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val point = other as Point3DF

        if (x != point.x) return false
        if (y != point.y) return false
        if (z != point.z) return false
        return true
    }

    override fun toString(): String {
        return "Point2D($x, $y, $z)"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(x)
        parcel.writeFloat(y)
        parcel.writeFloat(z)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Point3DF> {
        override fun createFromParcel(parcel: Parcel): Point3DF {
            return Point3DF(parcel)
        }

        override fun newArray(size: Int): Array<Point3DF?> {
            return arrayOfNulls(size)
        }
    }
}