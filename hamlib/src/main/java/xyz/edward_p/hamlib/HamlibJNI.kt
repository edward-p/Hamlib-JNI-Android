package xyz.edward_p.hamlib

import xyz.edward_p.hamlib.data.Rig
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Hamlib JNI bindings
 */
class HamlibJNI private constructor() {

    init {
        this.rigLoadAllBackends();
    }

    private external fun rigLoadAllBackends(): Int

    external fun getAllRigs(): Array<Rig>

    external fun rigInit(rigModel: Int): Int

    external fun rigOpen(): Int

    external fun rigCleanUp(): Int

    external fun rigSetFreq(vfo: Int, freq: Double): Int


    companion object {

        val RIG_VFO_A = 1
        val RIG_VFO_B = 1 shl 1
        val RIG_VFO_C = 1 shl 2

        init {
            System.loadLibrary("hamlibjni")
        }

        val instance = HamlibJNI()

    }


    private external fun getPtm(): Int
    private fun wrapFileDescriptor(fd: Int): FileDescriptor {
        val fileDescriptor = FileDescriptor()
        val fileDescriptorClass = FileDescriptor::class.java
        val field = fileDescriptorClass.getDeclaredField("descriptor")
        field.isAccessible = true
        field.set(fileDescriptor, fd)
        return fileDescriptor
    }

    fun getInputStream(): FileInputStream {
        return FileInputStream(wrapFileDescriptor(getPtm()))
    }

    fun getOutputStream(): FileOutputStream {
        return FileOutputStream(wrapFileDescriptor(getPtm()))
    }


}
