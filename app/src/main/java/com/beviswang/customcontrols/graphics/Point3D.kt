package com.beviswang.customcontrols.graphics

import android.graphics.Point
import android.os.Parcel
import android.os.Parcelable

/**
 * Point2D in 3D holds three integer coordinates
 * @author BevisWang
 */
class Point3D : Parcelable {
    var x: Int = 0
    var y: Int = 0
    var z: Int = 0

    constructor()

    constructor(parcel: Parcel) : this() {
        x = parcel.readInt()
        y = parcel.readInt()
        z = parcel.readInt()
    }

    constructor(x: Int = 0, y: Int = 0, z: Int = 0) : this() {
        set(x, y, z)
    }

    constructor(point: Point, z: Int) : this(point.x, point.y, z)

    constructor(point3D: Point3D) : this(point3D.x, point3D.y, point3D.z)

    /**
     * Set the point's x, y and z coordinates
     */
    fun set(x: Int, y: Int, z: Int) {
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
    fun offset(dx: Int, dy: Int, dz: Int) {
        x += dx
        y += dy
        z += dz
    }

    /**
     * Returns true if the point's coordinates equal (x,y,z)
     */
    fun equals(x: Int, y: Int, z: Int): Boolean {
        return this.x == x && this.y == y && this.z == z
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val point = other as Point3D

        if (x != point.x) return false
        if (y != point.y) return false
        if (z != point.z) return false
        return true
    }

    override fun toString(): String {
        return "Point2D($x, $y, $z)"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(x)
        parcel.writeInt(y)
        parcel.writeInt(z)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Point3D> {
        override fun createFromParcel(parcel: Parcel): Point3D {
            return Point3D(parcel)
        }

        override fun newArray(size: Int): Array<Point3D?> {
            return arrayOfNulls(size)
        }
    }
}