package xyz.edward_p.hamlib

import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream


/**
 * Singleton class to create a pair of
 * pseudo terminal master and slave
 */
class Pty private constructor(
    val fd: Int,
    val devname: String
) {
    companion object {
        init {
            System.loadLibrary("hamlibjni")
        }

        @Volatile
        private var instance: Pty? = null

        private external fun getInstanceNative(): Pty;

        fun getInstance(): Pty {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = getInstanceNative()
                    }
                }
            }
            return instance!!
        }
    }

    private fun wrapFileDescriptor(fd: Int): FileDescriptor {
        val fileDescriptor = FileDescriptor()
        val fileDescriptorClass = FileDescriptor::class.java
        val field = fileDescriptorClass.getDeclaredField("descriptor")
        field.isAccessible = true
        field.set(fileDescriptor, fd)
        return fileDescriptor
    }

    fun getInputStream(): FileInputStream {
        return FileInputStream(wrapFileDescriptor(fd));
    }

    fun getOutputStream(): FileOutputStream {
        return FileOutputStream(wrapFileDescriptor(fd));
    }

}