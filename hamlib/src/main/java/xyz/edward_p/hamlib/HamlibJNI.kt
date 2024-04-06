package xyz.edward_p.hamlib

import xyz.edward_p.hamlib.data.Rig

/**
 * Hamlib JNI bindings
 */
class HamlibJNI private constructor() {

    init {
        this.rigLoadAllBackends();
    }

    private external fun rigLoadAllBackends(): Int

    external fun getAllRigs(): Array<Rig>

    external fun rigInit(rigModel:Int): Int

    external fun rigOpen(devName:String):Int

    external fun rigClose(): Int

    external fun rigCleanUp(): Int

    external fun rigSetFreq(vfo:Int, freq:Double): Int


    companion object {

        val RIG_VFO_A = 1
        val RIG_VFO_B = 1 shl 1
        val RIG_VFO_C = 1 shl 2

        init {
            System.loadLibrary("hamlibjni")
        }

        @Volatile
        private var instance: HamlibJNI?=null

        fun getInstance(): HamlibJNI {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = HamlibJNI()
                    }
                }
            }
            return instance!!
        }

    }


}
